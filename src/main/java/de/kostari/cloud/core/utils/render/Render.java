package de.kostari.cloud.core.utils.render;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.system.MemoryUtil;

import de.kostari.cloud.core.scene.Camera;
import de.kostari.cloud.core.scene.SceneManager;
import de.kostari.cloud.core.utils.math.Vector2;
import de.kostari.cloud.core.utils.render.font.Font;
import de.kostari.cloud.core.utils.render.post.PostProcessing;
import de.kostari.cloud.core.utils.types.Color4f;

public class Render {

    private static final int MAX_BATCH_SIZE = 10_000;
    private static final int MAX_TEXTURE_SLOTS = 16;
    private static final int VERTEX_SIZE = 2 + 2 + 4 + 1;
    private static final int RECT_VERTICES = 4;
    private static final int INDICES_PER_RECT = 6;

    private static final Color4f WHITE = new Color4f(1, 1, 1, 1);

    private static int vaoId;
    private static int vboId;
    private static int eboId;
    private static int whiteTextureId;
    private static int quadCount;
    private static int textureSlotCount;
    private static int viewportWidth;
    private static int viewportHeight;

    private static final Matrix4f screenSpaceMatrix = new Matrix4f();
    private static FloatBuffer vertexBuffer;
    private static int[] textureSlots;
    private static boolean initialized = false;
    private static boolean screenSpace = false;

    private static Shader batchShader;
    private static final PostProcessing postProcessing = new PostProcessing();

    public static void init(int windowWidth, int windowHeight) {
        if (initialized) {
            return;
        }

        viewportWidth = Math.max(1, windowWidth);
        viewportHeight = Math.max(1, windowHeight);

        if (SceneManager.hasScene() && SceneManager.current().getCamera() == null) {
            SceneManager.current().initCamera();
        }

        vaoId = GL30.glGenVertexArrays();
        vboId = GL15.glGenBuffers();
        eboId = GL15.glGenBuffers();
        whiteTextureId = createWhiteTexture();

        vertexBuffer = MemoryUtil.memAllocFloat(MAX_BATCH_SIZE * RECT_VERTICES * VERTEX_SIZE);
        textureSlots = new int[MAX_TEXTURE_SLOTS];

        GL30.glBindVertexArray(vaoId);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER,
                (long) MAX_BATCH_SIZE * RECT_VERTICES * VERTEX_SIZE * Float.BYTES,
                GL15.GL_DYNAMIC_DRAW);

        IntBuffer indices = MemoryUtil.memAllocInt(MAX_BATCH_SIZE * INDICES_PER_RECT);
        int offset = 0;
        for (int i = 0; i < MAX_BATCH_SIZE; i++) {
            indices.put(offset);
            indices.put(offset + 1);
            indices.put(offset + 2);
            indices.put(offset + 2);
            indices.put(offset + 3);
            indices.put(offset);
            offset += RECT_VERTICES;
        }
        indices.flip();

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboId);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indices, GL15.GL_STATIC_DRAW);
        MemoryUtil.memFree(indices);

        int stride = VERTEX_SIZE * Float.BYTES;
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, stride, 0L);
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, stride, 2L * Float.BYTES);
        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, stride, 4L * Float.BYTES);
        GL20.glEnableVertexAttribArray(2);
        GL20.glVertexAttribPointer(3, 1, GL11.GL_FLOAT, false, stride, 8L * Float.BYTES);
        GL20.glEnableVertexAttribArray(3);

        GL30.glBindVertexArray(0);

        batchShader = new Shader();
        batchShader.attachShaderFromFile(GL20.GL_VERTEX_SHADER, "../../shader/tex_vertex.glsl");
        batchShader.attachShaderFromFile(GL20.GL_FRAGMENT_SHADER, "../../shader/tex_fragment.glsl");
        batchShader.link();
        batchShader.bind();
        batchShader.createUniforms("combinedMatrix", "textureSamplers[0]");
        batchShader.setUniform("textureSamplers[0]", createTextureSamplerArray());
        batchShader.unbind();

        postProcessing.init(viewportWidth, viewportHeight);

        initialized = true;
        beginBatch();
    }

    public static void resize(int width, int height) {
        viewportWidth = Math.max(1, width);
        viewportHeight = Math.max(1, height);
        GL11.glViewport(0, 0, viewportWidth, viewportHeight);
        postProcessing.resize(viewportWidth, viewportHeight);
    }

    public static void beginFrame(Color4f clearColor) {
        if (postProcessing.hasActiveEffects()) {
            postProcessing.begin(clearColor);
            return;
        }

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        GL11.glViewport(0, 0, viewportWidth, viewportHeight);
        clear(clearColor);
    }

    public static void endFrame() {
        postProcessing.end();
    }

    public static void clear(Color4f clearColor) {
        Color4f color = clearColor == null ? new Color4f(0, 0, 0, 1) : clearColor;
        GL11.glClearColor(color.r, color.g, color.b, color.a);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }

    public static void cleanup() {
        if (!initialized) {
            return;
        }

        flush();
        postProcessing.cleanup();

        GL30.glBindVertexArray(vaoId);
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL20.glDisableVertexAttribArray(2);
        GL20.glDisableVertexAttribArray(3);
        GL30.glBindVertexArray(0);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glDeleteBuffers(vboId);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL15.glDeleteBuffers(eboId);

        GL30.glDeleteVertexArrays(vaoId);
        GL11.glDeleteTextures(whiteTextureId);

        MemoryUtil.memFree(vertexBuffer);
        batchShader.cleanup();

        initialized = false;
    }

    public static PostProcessing postProcessing() {
        return postProcessing;
    }

    private static void beginBatch() {
        quadCount = 0;
        textureSlotCount = 1;
        textureSlots[0] = whiteTextureId;
        vertexBuffer.clear();
    }

    public static void flush() {
        if (!initialized || quadCount == 0) {
            if (initialized) {
                beginBatch();
            }
            return;
        }

        vertexBuffer.flip();

        GL30.glBindVertexArray(vaoId);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, vertexBuffer);

        for (int i = 0; i < textureSlotCount; i++) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + i);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureSlots[i]);
        }

        batchShader.bind();
        batchShader.setUniform("combinedMatrix", activeMatrix());
        GL11.glDrawElements(GL11.GL_TRIANGLES, quadCount * INDICES_PER_RECT, GL11.GL_UNSIGNED_INT, 0L);
        batchShader.unbind();

        GL30.glBindVertexArray(0);
        beginBatch();
    }

    public static void drawRect(float x, float y, float width, float height, boolean centered, Color4f color) {
        drawRotatedRect(x, y, width, height, centered, color, 0);
    }

    public static void drawRect(float x, float y, float width, float height, Color4f color) {
        drawRect(x, y, width, height, false, color);
    }

    public static void drawRect(Vector2 position, Vector2 size, boolean centered, Color4f color) {
        drawRect(position.x, position.y, size.x, size.y, centered, color);
    }

    public static void drawRotatedRect(float x, float y, float width, float height, boolean centered, Color4f color,
            float angleDegrees) {
        queueRotatedQuad(x, y, width, height, centered, angleDegrees, color, whiteTextureId,
                0, 0, 1, 1);
    }

    public static void drawTexture(float x, float y, float width, float height, boolean centered, int textureID,
            Color4f color) {
        drawRotatedTexture(x, y, width, height, centered, textureID, 0, color);
    }

    public static void drawTexture(float x, float y, float width, float height, boolean centered, int textureID) {
        drawTexture(x, y, width, height, centered, textureID, null);
    }

    public static void drawTexture(Texture texture, float x, float y, float width, float height, boolean centered,
            Color4f color) {
        if (texture == null) {
            return;
        }
        queueRotatedQuad(x, y, width, height, centered, 0, color, texture.getTextureId(),
                texture.getU0(), texture.getV0(), texture.getU1(), texture.getV1());
    }

    public static void drawTexture(Texture texture, float x, float y, float width, float height, boolean centered) {
        drawTexture(texture, x, y, width, height, centered, null);
    }

    public static void drawTexture(Texture texture, float x, float y, boolean centered) {
        if (texture == null) {
            return;
        }
        drawTexture(texture, x, y, texture.getWidth(), texture.getHeight(), centered, null);
    }

    public static void drawTexture(Texture texture, Vector2 position, Vector2 size, boolean centered, Color4f color) {
        if (texture == null) {
            return;
        }
        drawTexture(texture, position.x, position.y, size.x, size.y, centered, color);
    }

    public static void drawRotatedTexture(float x, float y, float width, float height, boolean centered, int textureID,
            float angleDegrees, Color4f color) {
        if (textureID <= 0) {
            return;
        }
        queueRotatedQuad(x, y, width, height, centered, angleDegrees, color, textureID,
                0, 0, 1, 1);
    }

    public static void drawRotatedTexture(float x, float y, float width, float height, boolean centered, int textureID,
            float angleDegrees) {
        drawRotatedTexture(x, y, width, height, centered, textureID, angleDegrees, null);
    }

    public static void drawRotatedTexture(Texture texture, float x, float y, float width, float height,
            boolean centered, float angleDegrees, Color4f color) {
        if (texture == null) {
            return;
        }
        queueRotatedQuad(x, y, width, height, centered, angleDegrees, color, texture.getTextureId(),
                texture.getU0(), texture.getV0(), texture.getU1(), texture.getV1());
    }

    public static void drawRect(int x, int y, int width, int height, boolean centered, Color4f color) {
        drawRect((float) x, y, width, height, centered, color);
    }

    public static void screenSpace(Runnable drawCommands) {
        if (!initialized || drawCommands == null) {
            return;
        }

        flush();
        boolean previousScreenSpace = screenSpace;
        screenSpace = true;
        try {
            drawCommands.run();
        } finally {
            flush();
            screenSpace = previousScreenSpace;
        }
    }

    public static void drawRotatedRect(int x, int y, int width, int height, boolean centered, Color4f color,
            float angleDegrees) {
        drawRotatedRect((float) x, y, width, height, centered, color, angleDegrees);
    }

    public static void drawTexture(int x, int y, int width, int height, boolean centered, int textureID,
            Color4f color) {
        drawTexture((float) x, y, width, height, centered, textureID, color);
    }

    public static void drawRotatedTexture(int x, int y, int width, int height, boolean centered, int textureID,
            float angleDegrees, Color4f color) {
        drawRotatedTexture((float) x, y, width, height, centered, textureID, angleDegrees, color);
    }

    public static void drawText(Font font, String text, float x, float y, float scale, Color4f color) {
        if (font == null || text == null || text.isEmpty()) {
            return;
        }

        Color4f tint = color == null ? WHITE : color;
        float xCursor = x;
        float yCursor = y + getTextHeight(font) * scale;

        for (char c : text.toCharArray()) {
            if (c < 32 || c >= 128) {
                continue;
            }

            STBTTBakedChar charInfo = font.getCharData().get(c - 32);

            float x0 = xCursor + charInfo.xoff() * scale;
            float y0 = yCursor + charInfo.yoff() * scale;
            float x1 = xCursor + (charInfo.xoff() + (charInfo.x1() - charInfo.x0())) * scale;
            float y1 = yCursor + (charInfo.yoff() + (charInfo.y1() - charInfo.y0())) * scale;

            float s0 = charInfo.x0() / 512.0f;
            float t0 = charInfo.y0() / 512.0f;
            float s1 = charInfo.x1() / 512.0f;
            float t1 = charInfo.y1() / 512.0f;

            queueQuad(x0, y0, x1, y0, x1, y1, x0, y1, s0, t0, s1, t1, tint, font.getTextureId());
            xCursor += charInfo.xadvance() * scale;
        }
    }

    public static void drawText(Font font, String text, float x, float y, Color4f color) {
        drawText(font, text, x, y, 1f, color);
    }

    public static void drawTextShadow(Font font, String text, float x, float y, float scale, float depth,
            Color4f color) {
        Color4f shadowColor = color == null ? new Color4f(0, 0, 0, 1) : new Color4f(0, 0, 0, color.a);
        drawText(font, text, x + depth, y + depth, scale, shadowColor);
        drawText(font, text, x, y, scale, color);
    }

    public static void drawTextShadow(Font font, String text, float x, float y, float depth, Color4f color) {
        drawTextShadow(font, text, x, y, 1f, depth, color);
    }

    public static float getTextWidth(Font font, String text) {
        float width = 0.0f;

        if (font == null || text == null) {
            return width;
        }

        for (char c : text.toCharArray()) {
            if (c >= 32 && c < 128) {
                STBTTBakedChar charInfo = font.getCharData().get(c - 32);
                width += charInfo.xadvance();
            }
        }

        return width;
    }

    public static float getTextWidth(Font font, String text, float scale) {
        return getTextWidth(font, text) * scale;
    }

    public static float getTextHeight(Font font) {
        if (font == null) {
            return 0;
        }
        return font.getFontHeight() - 14;
    }

    private static void queueRotatedQuad(float x, float y, float width, float height, boolean centered,
            float angleDegrees, Color4f color, int textureID, float u0, float v0, float u1, float v1) {
        Color4f tint = color == null ? WHITE : color;

        float halfWidth = width * 0.5f;
        float halfHeight = height * 0.5f;
        float centerX = centered ? x : x + halfWidth;
        float centerY = centered ? y : y + halfHeight;

        float x0 = -halfWidth;
        float y0 = -halfHeight;
        float x1 = halfWidth;
        float y1 = halfHeight;

        if (Math.abs(angleDegrees) < 0.0001f) {
            queueQuad(centerX + x0, centerY + y0,
                    centerX + x1, centerY + y0,
                    centerX + x1, centerY + y1,
                    centerX + x0, centerY + y1,
                    u0, v0, u1, v1, tint, textureID);
            return;
        }

        float radians = (float) Math.toRadians(angleDegrees);
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);

        queueQuad(
                centerX + x0 * cos - y0 * sin, centerY + x0 * sin + y0 * cos,
                centerX + x1 * cos - y0 * sin, centerY + x1 * sin + y0 * cos,
                centerX + x1 * cos - y1 * sin, centerY + x1 * sin + y1 * cos,
                centerX + x0 * cos - y1 * sin, centerY + x0 * sin + y1 * cos,
                u0, v0, u1, v1, tint, textureID);
    }

    private static void queueQuad(float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3,
            float u0, float v0, float u1, float v1, Color4f color, int textureID) {
        if (!initialized) {
            return;
        }

        if (quadCount >= MAX_BATCH_SIZE) {
            flush();
        }

        int textureSlot = textureSlot(textureID);
        putVertex(x0, y0, u0, v0, color, textureSlot);
        putVertex(x1, y1, u1, v0, color, textureSlot);
        putVertex(x2, y2, u1, v1, color, textureSlot);
        putVertex(x3, y3, u0, v1, color, textureSlot);
        quadCount++;
    }

    private static int textureSlot(int textureID) {
        int id = textureID <= 0 ? whiteTextureId : textureID;
        for (int i = 0; i < textureSlotCount; i++) {
            if (textureSlots[i] == id) {
                return i;
            }
        }

        if (textureSlotCount >= MAX_TEXTURE_SLOTS) {
            flush();
        }

        textureSlots[textureSlotCount] = id;
        return textureSlotCount++;
    }

    private static void putVertex(float x, float y, float u, float v, Color4f color, int textureSlot) {
        vertexBuffer.put(x);
        vertexBuffer.put(y);
        vertexBuffer.put(u);
        vertexBuffer.put(v);
        vertexBuffer.put(color.r);
        vertexBuffer.put(color.g);
        vertexBuffer.put(color.b);
        vertexBuffer.put(color.a);
        vertexBuffer.put(textureSlot);
    }

    private static Matrix4f activeMatrix() {
        if (!screenSpace && SceneManager.hasScene()) {
            Camera camera = SceneManager.current().getCamera();
            if (camera != null) {
                return camera.getCombinedMatrix();
            }
        }

        return screenSpaceMatrix.identity().ortho2D(0, viewportWidth, viewportHeight, 0);
    }

    private static int[] createTextureSamplerArray() {
        int[] samplers = new int[MAX_TEXTURE_SLOTS];
        for (int i = 0; i < MAX_TEXTURE_SLOTS; i++) {
            samplers[i] = i;
        }
        return samplers;
    }

    private static int createWhiteTexture() {
        int textureId = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        ByteBuffer pixel = MemoryUtil.memAlloc(4);
        pixel.put((byte) 255).put((byte) 255).put((byte) 255).put((byte) 255).flip();
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, 1, 1, 0,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixel);
        MemoryUtil.memFree(pixel);
        return textureId;
    }
}
