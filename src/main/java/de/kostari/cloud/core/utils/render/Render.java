package de.kostari.cloud.core.utils.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;

import de.kostari.cloud.core.scene.SceneManager;
import de.kostari.cloud.core.utils.types.Color4f;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class Render {

    private static final int MAX_BATCH_SIZE = 1000;
    private static final int VERTEX_SIZE = 2 + 2 + 4; // 2 for position, 2 for texture coordinates, 4 for color
    private static final int RECT_VERTICES = 4;
    private static final int INDICES_PER_RECT = 6;

    private static int vboId;
    private static int eboId;
    private static FloatBuffer vertexBuffer;
    private static IntBuffer indexBuffer;
    private static List<Shape> nonTexturedShapes;
    private static List<Shape> texturedShapes;
    private static boolean initialized = false;

    private static Shader nonTexturedShader;
    private static Shader texturedShader;

    private static class Shape {
        float[] vertices;
        int[] indices;
        int textureID = -1;
    }

    public static void init(int windowWidth, int windowHeight) {
        if (initialized)
            return;

        vboId = GL15.glGenBuffers();
        eboId = GL15.glGenBuffers();

        vertexBuffer = MemoryUtil.memAllocFloat(MAX_BATCH_SIZE * RECT_VERTICES * VERTEX_SIZE);
        indexBuffer = MemoryUtil.memAllocInt(MAX_BATCH_SIZE * INDICES_PER_RECT);

        nonTexturedShapes = new ArrayList<>();
        texturedShapes = new ArrayList<>();

        SceneManager.current().initCamera();

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, VERTEX_SIZE * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, VERTEX_SIZE * Float.BYTES, 2 * Float.BYTES);
        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, VERTEX_SIZE * Float.BYTES, 4 * Float.BYTES);
        GL20.glEnableVertexAttribArray(2);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboId);

        nonTexturedShader = new Shader();
        nonTexturedShader.attachShaderFromFile(GL20.GL_VERTEX_SHADER, "../../shader/vertex.glsl");
        nonTexturedShader.attachShaderFromFile(GL20.GL_FRAGMENT_SHADER, "../../shader/fragment.glsl");
        nonTexturedShader.link();

        texturedShader = new Shader();
        texturedShader.attachShaderFromFile(GL20.GL_VERTEX_SHADER, "../../shader/tex_vertex.glsl");
        texturedShader.attachShaderFromFile(GL20.GL_FRAGMENT_SHADER, "../../shader/tex_fragment.glsl");
        texturedShader.link();

        nonTexturedShader.bind();
        nonTexturedShader.createUniform("combinedMatrix");
        nonTexturedShader.setUniform("combinedMatrix", SceneManager.current().getCamera().getCombinedMatrix());
        nonTexturedShader.unbind();

        texturedShader.bind();
        texturedShader.createUniform("combinedMatrix");
        texturedShader.createUniform("textureSampler");
        texturedShader.setUniform("combinedMatrix", SceneManager.current().getCamera().getCombinedMatrix());
        texturedShader.setUniform("textureSampler", 0);
        texturedShader.unbind();

        initialized = true;
    }

    public static void cleanup() {
        if (!initialized)
            return;

        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL20.glDisableVertexAttribArray(2);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glDeleteBuffers(vboId);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL15.glDeleteBuffers(eboId);

        MemoryUtil.memFree(vertexBuffer);
        MemoryUtil.memFree(indexBuffer);

        nonTexturedShader.cleanup();
        texturedShader.cleanup();

        initialized = false;
    }

    private static void beginBatch() {
        nonTexturedShapes.clear();
        texturedShapes.clear();
    }

    private static void drawNonTexturedShapesBatch(List<Shape> shapes) {
        vertexBuffer.clear();
        indexBuffer.clear();

        int vertexIndex = 0;

        for (Shape shape : shapes) {
            vertexBuffer.put(shape.vertices);
            for (int i = 0; i < shape.indices.length; i++) {
                indexBuffer.put(shape.indices[i] + vertexIndex);
            }
            vertexIndex += shape.vertices.length / VERTEX_SIZE;
        }

        vertexBuffer.flip();
        indexBuffer.flip();

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_DYNAMIC_DRAW);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboId);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL15.GL_DYNAMIC_DRAW);

        nonTexturedShader.bind();
        GL11.glDrawElements(GL11.GL_TRIANGLES, indexBuffer.limit(), GL11.GL_UNSIGNED_INT, 0);
        nonTexturedShader.unbind();
    }

    private static void drawTexturedShapesBatch(List<Shape> shapes) {
        vertexBuffer.clear();
        indexBuffer.clear();

        int vertexIndex = 0;
        int lastTextureID = -1;

        for (Shape shape : shapes) {
            if (shape.textureID != lastTextureID) {
                if (lastTextureID != -1) {
                    vertexBuffer.flip();
                    indexBuffer.flip();

                    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
                    GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_DYNAMIC_DRAW);

                    GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboId);
                    GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL15.GL_DYNAMIC_DRAW);

                    texturedShader.bind();
                    GL11.glDrawElements(GL11.GL_TRIANGLES, indexBuffer.limit(), GL11.GL_UNSIGNED_INT, 0);
                    texturedShader.unbind();

                    vertexBuffer.clear();
                    indexBuffer.clear();
                    vertexIndex = 0;
                }
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, shape.textureID); // Bind the texture
                lastTextureID = shape.textureID;
            }

            vertexBuffer.put(shape.vertices);
            for (int i = 0; i < shape.indices.length; i++) {
                indexBuffer.put(shape.indices[i] + vertexIndex);
            }
            vertexIndex += shape.vertices.length / VERTEX_SIZE;
        }

        vertexBuffer.flip();
        indexBuffer.flip();

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_DYNAMIC_DRAW);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboId);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL15.GL_DYNAMIC_DRAW);

        texturedShader.bind();
        GL11.glDrawElements(GL11.GL_TRIANGLES, indexBuffer.limit(), GL11.GL_UNSIGNED_INT, 0);
        texturedShader.unbind();
    }

    private static void drawNonTexturedShapes() {
        int totalShapes = nonTexturedShapes.size();
        int batches = (totalShapes / MAX_BATCH_SIZE) + 1;

        for (int i = 0; i < batches; i++) {
            int start = i * MAX_BATCH_SIZE;
            int end = Math.min(start + MAX_BATCH_SIZE, totalShapes);
            drawNonTexturedShapesBatch(nonTexturedShapes.subList(start, end));
        }
    }

    private static void drawTexturedShapes() {
        int totalShapes = texturedShapes.size();
        int batches = (totalShapes / MAX_BATCH_SIZE) + 1;

        for (int i = 0; i < batches; i++) {
            int start = i * MAX_BATCH_SIZE;
            int end = Math.min(start + MAX_BATCH_SIZE, totalShapes);
            drawTexturedShapesBatch(texturedShapes.subList(start, end));
        }
    }

    public static void flush() {
        nonTexturedShader.bind();
        nonTexturedShader.setUniform("combinedMatrix", SceneManager.current().getCamera().getCombinedMatrix());
        nonTexturedShader.unbind();

        texturedShader.bind();
        texturedShader.setUniform("combinedMatrix", SceneManager.current().getCamera().getCombinedMatrix());
        texturedShader.unbind();

        drawNonTexturedShapes();
        drawTexturedShapes();
        beginBatch();
    }

    private static void addShape(Shape shape) {
        if (shape.textureID == -1) {
            nonTexturedShapes.add(shape);
        } else {
            texturedShapes.add(shape);
        }
    }

    public static void drawRect(int x, int y, int width, int height, boolean centered, Color4f color) {
        if (centered) {
            x -= width / 2;
            y -= height / 2;
        }
        if (color == null)
            color = new Color4f(1, 1, 1, 1);

        Shape shape = new Shape();
        shape.vertices = new float[] {
                x, y, 0, 0, color.r, color.g, color.b, color.a,
                x + width, y, 0, 0, color.r, color.g, color.b, color.a,
                x + width, y + height, 0, 0, color.r, color.g, color.b, color.a,
                x, y + height, 0, 0, color.r, color.g, color.b, color.a
        };
        shape.indices = new int[] { 0, 1, 2, 2, 3, 0 };
        addShape(shape);
    }

    public static void drawRect(float x, float y, float width, float height, boolean centered, Color4f color) {
        drawRect((int) x, (int) y, (int) width, (int) height, centered, color);
    }

    public static void drawTexture(int x, int y, int width, int height, boolean centered, int textureID) {
        if (centered) {
            x -= width / 2;
            y -= height / 2;
        }
        if (textureID == -1)
            return;

        Shape shape = new Shape();
        shape.textureID = textureID;
        shape.vertices = new float[] {
                x, y, 0, 0, 1, 1, 1, 1, // Bottom-left corner
                x + width, y, 1, 0, 1, 1, 1, 1, // Bottom-right corner
                x + width, y + height, 1, 1, 1, 1, 1, 1, // Top-right corner
                x, y + height, 0, 1, 1, 1, 1, 1 // Top-left corner
        };
        shape.indices = new int[] { 0, 1, 2, 2, 3, 0 };
        addShape(shape);
    }
}
