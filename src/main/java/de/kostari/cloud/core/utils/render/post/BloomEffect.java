package de.kostari.cloud.core.utils.render.post;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import de.kostari.cloud.core.utils.render.Shader;

public class BloomEffect implements PostEffect {

    private static final int DOWNSAMPLE = 2;
    private static final int MAX_BLUR_PASSES = 10;

    private float threshold;
    private float intensity;
    private float radius;
    private boolean enabled = true;
    private boolean initialized;

    private Shader extractShader;
    private Shader blurShader;
    private Shader combineShader;
    private Framebuffer brightBuffer;
    private Framebuffer pingBuffer;
    private Framebuffer pongBuffer;
    private int bloomWidth;
    private int bloomHeight;

    public BloomEffect() {
        this(0.65f, 0.55f, 3.0f);
    }

    public BloomEffect(float threshold, float intensity, float radius) {
        this.threshold = threshold;
        this.intensity = intensity;
        this.radius = radius;
    }

    @Override
    public void init() {
        if (initialized) {
            return;
        }

        extractShader = createShader("../../shader/post_bloom_extract_fragment.glsl");
        extractShader.bind();
        extractShader.createUniforms("inputTexture", "threshold");
        extractShader.setUniform("inputTexture", 0);
        extractShader.unbind();

        blurShader = createShader("../../shader/post_bloom_blur_fragment.glsl");
        blurShader.bind();
        blurShader.createUniforms("inputTexture", "resolution", "direction", "radius");
        blurShader.setUniform("inputTexture", 0);
        blurShader.unbind();

        combineShader = createShader("../../shader/post_bloom_combine_fragment.glsl");
        combineShader.bind();
        combineShader.createUniforms("inputTexture", "bloomTexture", "intensity");
        combineShader.setUniform("inputTexture", 0);
        combineShader.setUniform("bloomTexture", 1);
        combineShader.unbind();

        initialized = true;
    }

    @Override
    public void apply(PostProcessingContext context, int sourceTexture) {
        if (!enabled) {
            return;
        }

        ensureBuffers(context);

        int outputFramebuffer = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
        extractBrightPixels(context, sourceTexture);

        int bloomTexture = brightBuffer.getColorTextureId();
        if (radius > 0.0f && intensity > 0.0f) {
            bloomTexture = blurBloom(context, bloomTexture);
        }

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, outputFramebuffer);
        GL11.glViewport(0, 0, context.getWidth(), context.getHeight());
        clear();

        combineShader.bind();
        combineShader.setUniform("intensity", Math.max(0.0f, intensity));
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, bloomTexture);
        context.drawFullscreen(sourceTexture, combineShader);
        combineShader.unbind();
    }

    @Override
    public void cleanup() {
        cleanupShader(extractShader);
        cleanupShader(blurShader);
        cleanupShader(combineShader);
        cleanupBuffer(brightBuffer);
        cleanupBuffer(pingBuffer);
        cleanupBuffer(pongBuffer);

        extractShader = null;
        blurShader = null;
        combineShader = null;
        brightBuffer = null;
        pingBuffer = null;
        pongBuffer = null;
        bloomWidth = 0;
        bloomHeight = 0;
        initialized = false;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public BloomEffect enabled(boolean enabled) {
        setEnabled(enabled);
        return this;
    }

    public BloomEffect threshold(float threshold) {
        this.threshold = threshold;
        return this;
    }

    public BloomEffect intensity(float intensity) {
        this.intensity = intensity;
        return this;
    }

    public BloomEffect radius(float radius) {
        this.radius = radius;
        return this;
    }

    public float getThreshold() {
        return threshold;
    }

    public float getIntensity() {
        return intensity;
    }

    public float getRadius() {
        return radius;
    }

    private int blurBloom(PostProcessingContext context, int sourceTexture) {
        int blurPasses = blurPasses();
        float passRadius = blurPassRadius(blurPasses);
        int currentTexture = sourceTexture;

        for (int i = 0; i < blurPasses; i++) {
            blurTo(context, currentTexture, pingBuffer, 1.0f, 0.0f, passRadius);
            blurTo(context, pingBuffer.getColorTextureId(), pongBuffer, 0.0f, 1.0f, passRadius);
            currentTexture = pongBuffer.getColorTextureId();
        }

        return currentTexture;
    }

    private void extractBrightPixels(PostProcessingContext context, int sourceTexture) {
        brightBuffer.bind();
        clear();

        extractShader.bind();
        extractShader.setUniform("threshold", threshold);
        context.drawFullscreen(sourceTexture, extractShader);
        extractShader.unbind();
    }

    private void blurTo(PostProcessingContext context, int sourceTexture, Framebuffer target,
            float directionX, float directionY, float passRadius) {
        target.bind();
        clear();

        blurShader.bind();
        blurShader.setUniform("resolution", (float) bloomWidth, (float) bloomHeight);
        blurShader.setUniform("direction", directionX, directionY);
        blurShader.setUniform("radius", passRadius);
        context.drawFullscreen(sourceTexture, blurShader);
        blurShader.unbind();
    }

    private void ensureBuffers(PostProcessingContext context) {
        int width = Math.max(1, (context.getWidth() + DOWNSAMPLE - 1) / DOWNSAMPLE);
        int height = Math.max(1, (context.getHeight() + DOWNSAMPLE - 1) / DOWNSAMPLE);

        if (brightBuffer != null && width == bloomWidth && height == bloomHeight) {
            return;
        }

        cleanupBuffer(brightBuffer);
        cleanupBuffer(pingBuffer);
        cleanupBuffer(pongBuffer);

        bloomWidth = width;
        bloomHeight = height;
        brightBuffer = new Framebuffer(bloomWidth, bloomHeight);
        pingBuffer = new Framebuffer(bloomWidth, bloomHeight);
        pongBuffer = new Framebuffer(bloomWidth, bloomHeight);
    }

    private int blurPasses() {
        return Math.max(1, Math.min(MAX_BLUR_PASSES, (int) Math.ceil(Math.max(0.0f, radius) / 3.0f)));
    }

    private float blurPassRadius(int blurPasses) {
        float spread = Math.max(0.5f, radius / Math.max(1, blurPasses));
        return Math.min(4.0f, spread);
    }

    private Shader createShader(String fragmentShaderPath) {
        Shader shader = new Shader();
        shader.attachShaderFromFile(GL20.GL_VERTEX_SHADER, "../../shader/post_vertex.glsl");
        shader.attachShaderFromFile(GL20.GL_FRAGMENT_SHADER, fragmentShaderPath);
        shader.link();
        return shader;
    }

    private void clear() {
        GL11.glClearColor(0, 0, 0, 1);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
    }

    private void cleanupShader(Shader shader) {
        if (shader != null) {
            shader.cleanup();
        }
    }

    private void cleanupBuffer(Framebuffer buffer) {
        if (buffer != null) {
            buffer.cleanup();
        }
    }
}
