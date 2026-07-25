package de.kostari.cloud.core.utils.render.post;

import de.kostari.cloud.core.utils.types.Color3f;

public class VignetteEffect extends ShaderPostEffect {

    private float intensity;
    private float radius;
    private float smoothness;
    private Color3f color;

    public VignetteEffect() {
        this(0.45f, 0.45f, 0.35f);
    }

    public VignetteEffect(float intensity, float radius, float smoothness) {
        this.intensity = intensity;
        this.radius = radius;
        this.smoothness = smoothness;
        this.color = new Color3f(0, 0, 0);
    }

    @Override
    protected String fragmentShaderPath() {
        return "../../shader/post_vignette_fragment.glsl";
    }

    @Override
    protected void createUniforms() {
        shader.createUniforms("intensity", "radius", "smoothness", "vignetteColor");
    }

    @Override
    protected void updateUniforms(PostProcessingContext context) {
        shader.setUniform("intensity", intensity);
        shader.setUniform("radius", radius);
        shader.setUniform("smoothness", smoothness);
        shader.setUniform("vignetteColor", color.r, color.g, color.b);
    }

    public VignetteEffect intensity(float intensity) {
        this.intensity = intensity;
        return this;
    }

    public VignetteEffect radius(float radius) {
        this.radius = radius;
        return this;
    }

    public VignetteEffect smoothness(float smoothness) {
        this.smoothness = smoothness;
        return this;
    }

    public VignetteEffect color(float r, float g, float b) {
        this.color = new Color3f(r, g, b);
        return this;
    }

    public float getIntensity() {
        return intensity;
    }

    public float getRadius() {
        return radius;
    }

    public float getSmoothness() {
        return smoothness;
    }
}
