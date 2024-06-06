package de.kostari.cloud.core.utils.types;

public class Color3f extends Color4f {

    public Color3f(float r, float g, float b) {
        super(r, g, b, 1);
    }

    public Color3f(Color4f color) {
        super(color.r, color.g, color.b, 1);
    }

    public Color3f(Color3f color) {
        super(color.r, color.g, color.b, 1);
    }

}
