package de.kostari.cloud.core.utils.render.post;

import org.lwjgl.opengl.GL20;

import de.kostari.cloud.core.utils.render.Shader;

public abstract class ShaderPostEffect implements PostEffect {

    protected Shader shader;

    private boolean enabled = true;
    private boolean initialized;

    @Override
    public void init() {
        if (initialized) {
            return;
        }

        shader = new Shader();
        shader.attachShaderFromFile(GL20.GL_VERTEX_SHADER, "../../shader/post_vertex.glsl");
        shader.attachShaderFromFile(GL20.GL_FRAGMENT_SHADER, fragmentShaderPath());
        shader.link();
        shader.bind();
        shader.createUniform("inputTexture");
        createUniforms();
        shader.unbind();

        initialized = true;
    }

    @Override
    public void apply(PostProcessingContext context, int sourceTexture) {
        if (!enabled) {
            return;
        }

        shader.bind();
        shader.setUniform("inputTexture", 0);
        updateUniforms(context);
        context.drawFullscreen(sourceTexture, shader);
        shader.unbind();
    }

    @Override
    public void cleanup() {
        if (shader != null) {
            shader.cleanup();
        }
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

    public ShaderPostEffect enabled(boolean enabled) {
        setEnabled(enabled);
        return this;
    }

    protected abstract String fragmentShaderPath();

    protected void createUniforms() {
    }

    protected void updateUniforms(PostProcessingContext context) {
    }
}
