package de.kostari.cloud.core.ui;

public record UISize(float width, float height) {

    public static final UISize ZERO = new UISize(0, 0);

    public UISize {
        width = Math.max(0, width);
        height = Math.max(0, height);
    }
}
