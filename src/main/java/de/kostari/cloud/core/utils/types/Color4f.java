package de.kostari.cloud.core.utils.types;

public class Color4f {

    public float r;
    public float g;
    public float b;
    public float a;

    public Color4f(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
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
