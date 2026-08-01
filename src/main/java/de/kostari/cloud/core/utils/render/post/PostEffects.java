package de.kostari.cloud.core.utils.render.post;

import de.kostari.cloud.core.lighting.LightingEffect;

public final class PostEffects {

    private PostEffects() {
    }

    public static BloomEffect bloom() {
        return new BloomEffect();
    }

    public static BloomEffect bloom(float threshold, float intensity, float radius) {
        return new BloomEffect(threshold, intensity, radius);
    }

    public static VignetteEffect vignette() {
        return new VignetteEffect();
    }

    public static VignetteEffect vignette(float intensity, float radius, float smoothness) {
        return new VignetteEffect(intensity, radius, smoothness);
    }

    public static ColorGradingEffect colorGrading() {
        return new ColorGradingEffect();
    }

    public static LightingEffect lighting() {
        return new LightingEffect();
    }
}
