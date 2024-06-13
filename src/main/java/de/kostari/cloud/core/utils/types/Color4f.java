package de.kostari.cloud.core.utils.types;

public class Color4f {

    public float r;
    public float g;
    public float b;
    public float a;

    public Color4f(float r, float g, float b, float a) {
        this.r = Math.clamp(r, 0, 1);
        this.g = Math.clamp(g, 0, 1);
        this.b = Math.clamp(b, 0, 1);
        this.a = Math.clamp(a, 0, 1);
    }

    public Color4f(Color4f color) {
        this.r = color.r;
        this.g = color.g;
        this.b = color.b;
        this.a = color.a;
    }

    @Override
    public String toString() {
        return "Color4f [r=" + r + ", g=" + g + ", b=" + b + ", a=" + a + "]";
    }
}
