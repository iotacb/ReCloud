package de.kostari.cloud.core.ui;

public record UIConstraints(float minWidth, float maxWidth, float minHeight, float maxHeight) {

    public static final float INFINITY = Float.POSITIVE_INFINITY;

    public UIConstraints {
        minWidth = Math.max(0, minWidth);
        minHeight = Math.max(0, minHeight);
        maxWidth = Math.max(minWidth, maxWidth);
        maxHeight = Math.max(minHeight, maxHeight);
    }

    public static UIConstraints loose(float maxWidth, float maxHeight) {
        return new UIConstraints(0, Math.max(0, maxWidth), 0, Math.max(0, maxHeight));
    }

    public static UIConstraints unconstrained() {
        return loose(INFINITY, INFINITY);
    }

    public static UIConstraints tight(float width, float height) {
        return new UIConstraints(width, width, height, height);
    }

    public UIConstraints inset(Insets insets) {
        return new UIConstraints(
                subtractFinite(minWidth, insets.horizontal()),
                subtractFinite(maxWidth, insets.horizontal()),
                subtractFinite(minHeight, insets.vertical()),
                subtractFinite(maxHeight, insets.vertical()));
    }

    public float constrainWidth(float width) {
        return Math.max(minWidth, Math.min(maxWidth, Math.max(0, width)));
    }

    public float constrainHeight(float height) {
        return Math.max(minHeight, Math.min(maxHeight, Math.max(0, height)));
    }

    private static float subtractFinite(float value, float amount) {
        return Float.isFinite(value) ? Math.max(0, value - amount) : value;
    }
}
