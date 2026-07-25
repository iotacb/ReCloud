package de.kostari.cloud.core.utils.render.post;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import de.kostari.cloud.core.utils.render.Shader;
import de.kostari.cloud.core.utils.types.Color4f;
import de.kostari.cloud.core.window.Time;

public class PostProcessing {

    private final List<PostEffect> effects = new ArrayList<>();
    private final PostProcessingContext context = new PostProcessingContext(this);

    private Framebuffer sceneBuffer;
    private Framebuffer pingBuffer;
    private Framebuffer pongBuffer;

    private Shader copyShader;
    private int vaoId;
    private int vboId;
    private int width;
    private int height;
    private boolean initialized;
    private boolean enabled = true;
    private boolean capturing;

    public void init(int width, int height) {
        if (initialized) {
            return;
        }

        this.width = Math.max(1, width);
        this.height = Math.max(1, height);

        sceneBuffer = new Framebuffer(this.width, this.height);
        pingBuffer = new Framebuffer(this.width, this.height);
        pongBuffer = new Framebuffer(this.width, this.height);
        createFullscreenQuad();
        createCopyShader();

        for (PostEffect effect : effects) {
            effect.init();
        }

        initialized = true;
    }

    public void resize(int width, int height) {
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);

        if (!initialized) {
            return;
        }

        sceneBuffer.resize(this.width, this.height);
        pingBuffer.resize(this.width, this.height);
        pongBuffer.resize(this.width, this.height);
    }

    public void begin(Color4f clearColor) {
        if (!hasActiveEffects()) {
            return;
        }

        sceneBuffer.bind();
        clear(clearColor);
        capturing = true;
    }

    public void end() {
        if (!capturing) {
            return;
        }

        boolean wasBlendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_BLEND);

        int sourceTexture = sceneBuffer.getColorTextureId();
        Framebuffer[] targets = { pingBuffer, pongBuffer };
        int targetIndex = 0;

        for (PostEffect effect : effects) {
            if (!effect.isEnabled()) {
                continue;
            }

            Framebuffer target = targets[targetIndex];
            target.bind();
            clear(new Color4f(0, 0, 0, 1));
            effect.apply(context, sourceTexture);
            sourceTexture = target.getColorTextureId();
            targetIndex = 1 - targetIndex;
        }

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        GL11.glViewport(0, 0, width, height);
        clear(new Color4f(0, 0, 0, 1));

        copyShader.bind();
        copyShader.setUniform("inputTexture", 0);
        drawFullscreen(sourceTexture, copyShader);
        copyShader.unbind();

        if (wasBlendEnabled) {
            GL11.glEnable(GL11.GL_BLEND);
        }

        capturing = false;
    }

    public void cleanup() {
        for (PostEffect effect : effects) {
            effect.cleanup();
        }

        if (sceneBuffer != null) {
            sceneBuffer.cleanup();
        }
        if (pingBuffer != null) {
            pingBuffer.cleanup();
        }
        if (pongBuffer != null) {
            pongBuffer.cleanup();
        }
        if (copyShader != null) {
            copyShader.cleanup();
        }

        if (vboId != 0) {
            GL15.glDeleteBuffers(vboId);
            vboId = 0;
        }
        if (vaoId != 0) {
            GL30.glDeleteVertexArrays(vaoId);
            vaoId = 0;
        }

        initialized = false;
        capturing = false;
    }

    public <T extends PostEffect> T add(T effect) {
        if (effect == null) {
            return null;
        }

        effects.add(effect);
        if (initialized) {
            effect.init();
        }
        return effect;
    }

    public void remove(PostEffect effect) {
        if (effects.remove(effect) && effect != null) {
            effect.cleanup();
        }
    }

    public void clearEffects() {
        for (PostEffect effect : effects) {
            effect.cleanup();
        }
        effects.clear();
    }

    public BloomEffect enableBloom() {
        return add(PostEffects.bloom());
    }

    public BloomEffect enableBloom(float threshold, float intensity, float radius) {
        return add(PostEffects.bloom(threshold, intensity, radius));
    }

    public VignetteEffect enableVignette() {
        return add(PostEffects.vignette());
    }

    public VignetteEffect enableVignette(float intensity, float radius, float smoothness) {
        return add(PostEffects.vignette(intensity, radius, smoothness));
    }

    public ColorGradingEffect enableColorGrading() {
        return add(PostEffects.colorGrading());
    }

    public boolean hasActiveEffects() {
        if (!enabled || effects.isEmpty()) {
            return false;
        }

        for (PostEffect effect : effects) {
            if (effect.isEnabled()) {
                return true;
            }
        }
        return false;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public List<PostEffect> getEffects() {
        return Collections.unmodifiableList(effects);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public float getTime() {
        return Time.timePassed;
    }

    void drawFullscreen(int sourceTexture, Shader shader) {
        if (shader != null) {
            shader.bind();
        }
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, sourceTexture);
        GL30.glBindVertexArray(vaoId);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
        GL30.glBindVertexArray(0);
    }

    private void createFullscreenQuad() {
        float[] vertices = {
                -1f, -1f, 0f, 0f,
                1f, -1f, 1f, 0f,
                1f, 1f, 1f, 1f,
                -1f, -1f, 0f, 0f,
                1f, 1f, 1f, 1f,
                -1f, 1f, 0f, 1f
        };

        vaoId = GL30.glGenVertexArrays();
        vboId = GL15.glGenBuffers();

        GL30.glBindVertexArray(vaoId);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);

        FloatBuffer buffer = MemoryUtil.memAllocFloat(vertices.length);
        buffer.put(vertices).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        MemoryUtil.memFree(buffer);

        int stride = 4 * Float.BYTES;
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, stride, 0L);
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, stride, 2L * Float.BYTES);
        GL20.glEnableVertexAttribArray(1);

        GL30.glBindVertexArray(0);
    }

    private void createCopyShader() {
        copyShader = new Shader();
        copyShader.attachShaderFromFile(GL20.GL_VERTEX_SHADER, "../../shader/post_vertex.glsl");
        copyShader.attachShaderFromFile(GL20.GL_FRAGMENT_SHADER, "../../shader/post_copy_fragment.glsl");
        copyShader.link();
        copyShader.bind();
        copyShader.createUniform("inputTexture");
        copyShader.setUniform("inputTexture", 0);
        copyShader.unbind();
    }

    private void clear(Color4f clearColor) {
        Color4f color = clearColor == null ? new Color4f(0, 0, 0, 1) : clearColor;
        GL11.glClearColor(color.r, color.g, color.b, color.a);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
    }
}
