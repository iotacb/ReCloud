package de.kostari.cloud.core.ui;

import de.kostari.cloud.core.utils.types.Color4f;

public final class UIMaterial implements Drawable {

    private Color4f topColor = new Color4f(0.16f, 0.2f, 0.3f, 1);
    private Color4f bottomColor = new Color4f(0.06f, 0.08f, 0.14f, 1);
    private Color4f borderColor = new Color4f(1, 1, 1, 0.18f);
    private Color4f glowColor = new Color4f(0.2f, 0.8f, 1, 0);
    private Color4f sheenColor = new Color4f(1, 1, 1, 0);
    private float radius = 8;
    private float softness = 0.8f;
    private float borderWidth = 1;
    private float glowSize;
    private float glowIntensity;
    private float sheenWidth = 0.12f;
    private float sheenSpeed;
    private float sheenIntensity;
    private float pulseSpeed;
    private float pulseAmount;

    public UIMaterial fill(Color4f color) {
        topColor = color;
        bottomColor = color;
        return this;
    }

    public UIMaterial gradient(Color4f top, Color4f bottom) {
        topColor = top;
        bottomColor = bottom;
        return this;
    }

    public UIMaterial border(float width, Color4f color) {
        borderWidth = Math.max(0, width);
        borderColor = color;
        return this;
    }

    public UIMaterial radius(float value) {
        radius = Math.max(0, value);
        return this;
    }

    public UIMaterial softness(float value) {
        softness = Math.max(0.05f, value);
        return this;
    }

    public UIMaterial glow(Color4f color, float size, float intensity) {
        glowColor = color;
        glowSize = Math.max(0, size);
        glowIntensity = Math.max(0, intensity);
        return this;
    }

    public UIMaterial sheen(Color4f color, float width, float speed, float intensity) {
        sheenColor = color;
        sheenWidth = Math.clamp(width, 0.01f, 1);
        sheenSpeed = speed;
        sheenIntensity = Math.max(0, intensity);
        return this;
    }

    public UIMaterial pulse(float speed, float amount) {
        pulseSpeed = Math.max(0, speed);
        pulseAmount = Math.clamp(amount, 0, 1);
        return this;
    }

    @Override
    public void draw(UIRect bounds, float opacity) {
        UIShapeRenderer.draw(this, bounds, opacity);
    }

    Color4f topColor() {
        return topColor;
    }

    Color4f bottomColor() {
        return bottomColor;
    }

    Color4f borderColor() {
        return borderColor;
    }

    Color4f glowColor() {
        return glowColor;
    }

    Color4f sheenColor() {
        return sheenColor;
    }

    float radius() {
        return radius;
    }

    float softness() {
        return softness;
    }

    float borderWidth() {
        return borderWidth;
    }

    float glowSize() {
        return glowSize;
    }

    float glowIntensity() {
        return glowIntensity;
    }

    float sheenWidth() {
        return sheenWidth;
    }

    float sheenSpeed() {
        return sheenSpeed;
    }

    float sheenIntensity() {
        return sheenIntensity;
    }

    float pulseSpeed() {
        return pulseSpeed;
    }

    float pulseAmount() {
        return pulseAmount;
    }
}
