package de.kostari.cloud.core.utils.render.post;

public class ColorGradingEffect extends ShaderPostEffect {

    private float exposure = 0.0f;
    private float contrast = 1.0f;
    private float saturation = 1.0f;
    private float temperature = 0.0f;
    private float tint = 0.0f;
    private float gamma = 1.0f;

    @Override
    protected String fragmentShaderPath() {
        return "../../shader/post_color_grading_fragment.glsl";
    }

    @Override
    protected void createUniforms() {
        shader.createUniforms("exposure", "contrast", "saturation", "temperature", "tint", "gamma");
    }

    @Override
    protected void updateUniforms(PostProcessingContext context) {
        shader.setUniform("exposure", exposure);
        shader.setUniform("contrast", contrast);
        shader.setUniform("saturation", saturation);
        shader.setUniform("temperature", temperature);
        shader.setUniform("tint", tint);
        shader.setUniform("gamma", gamma);
    }

    public ColorGradingEffect exposure(float exposure) {
        this.exposure = exposure;
        return this;
    }

    public ColorGradingEffect contrast(float contrast) {
        this.contrast = contrast;
        return this;
    }

    public ColorGradingEffect saturation(float saturation) {
        this.saturation = saturation;
        return this;
    }

    public ColorGradingEffect temperature(float temperature) {
        this.temperature = temperature;
        return this;
    }

    public ColorGradingEffect tint(float tint) {
        this.tint = tint;
        return this;
    }

    public ColorGradingEffect gamma(float gamma) {
        this.gamma = gamma;
        return this;
    }

    public ColorGradingEffect reset() {
        exposure = 0.0f;
        contrast = 1.0f;
        saturation = 1.0f;
        temperature = 0.0f;
        tint = 0.0f;
        gamma = 1.0f;
        return this;
    }

    public float getExposure() {
        return exposure;
    }

    public float getContrast() {
        return contrast;
    }

    public float getSaturation() {
        return saturation;
    }

    public float getTemperature() {
        return temperature;
    }

    public float getTint() {
        return tint;
    }

    public float getGamma() {
        return gamma;
    }
}
