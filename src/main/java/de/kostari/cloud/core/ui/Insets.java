package de.kostari.cloud.core.ui;

public record Insets(float top, float right, float bottom, float left) {

    public static final Insets NONE = new Insets(0, 0, 0, 0);

    public Insets {
        top = Math.max(0, top);
        right = Math.max(0, right);
        bottom = Math.max(0, bottom);
        left = Math.max(0, left);
    }

    public static Insets all(float value) {
        return new Insets(value, value, value, value);
    }

    public static Insets symmetric(float vertical, float horizontal) {
        return new Insets(vertical, horizontal, vertical, horizontal);
    }

    public float horizontal() {
        return left + right;
    }

    public float vertical() {
        return top + bottom;
    }
}
