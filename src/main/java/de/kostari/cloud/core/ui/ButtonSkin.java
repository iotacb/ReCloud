package de.kostari.cloud.core.ui;

import de.kostari.cloud.core.utils.types.Color4f;

public record ButtonSkin(
        Drawable normal,
        Drawable hovered,
        Drawable pressed,
        Drawable focused,
        Drawable disabled,
        Color4f normalText,
        Color4f hoveredText,
        Color4f pressedText,
        Color4f disabledText) {

    public static ButtonSkin defaultSkin() {
        Drawable normal = new UIMaterial()
                .gradient(new Color4f(0.15f, 0.19f, 0.27f, 0.98f), new Color4f(0.08f, 0.11f, 0.17f, 0.98f))
                .border(1, new Color4f(1, 1, 1, 0.16f)).radius(8);
        Drawable hovered = new UIMaterial()
                .gradient(new Color4f(0.24f, 0.31f, 0.43f, 1), new Color4f(0.12f, 0.17f, 0.25f, 1))
                .border(1, new Color4f(0.45f, 0.82f, 1, 0.62f)).radius(8)
                .glow(new Color4f(0.25f, 0.76f, 1, 1), 9, 0.28f);
        Drawable pressed = new UIMaterial()
                .gradient(new Color4f(0.06f, 0.09f, 0.14f, 1), new Color4f(0.12f, 0.16f, 0.23f, 1))
                .border(1, new Color4f(0.35f, 0.72f, 0.95f, 0.7f)).radius(8);
        Drawable disabled = new UIMaterial().fill(new Color4f(0.12f, 0.14f, 0.18f, 0.72f))
                .border(1, new Color4f(1, 1, 1, 0.08f)).radius(8);
        Color4f white = new Color4f(1, 1, 1, 1);
        return new ButtonSkin(normal, hovered, pressed, hovered, disabled,
                white, white, white, new Color4f(0.62f, 0.65f, 0.7f, 1));
    }

    public static ButtonSkin colors(Color4f normalColor, Color4f hoveredColor, Color4f pressedColor,
            Color4f borderColor, Color4f textColor) {
        Drawable normal = box(normalColor, borderColor);
        Drawable hovered = box(hoveredColor, borderColor);
        Drawable pressed = box(pressedColor == null ? normalColor : pressedColor, borderColor);
        Drawable disabled = box(new Color4f(0.12f, 0.13f, 0.16f, 0.75f),
                new Color4f(1, 1, 1, 0.08f));
        Color4f text = textColor == null ? new Color4f(1, 1, 1, 1) : textColor;
        return new ButtonSkin(normal, hovered, pressed, hovered, disabled,
                text, text, text, new Color4f(text.r, text.g, text.b, text.a * 0.55f));
    }

    private static Drawable box(Color4f fill, Color4f border) {
        return Drawables.layered(Drawables.solid(fill), Drawables.border(1, border));
    }

    Drawable drawable(UIState state) {
        return switch (state) {
            case HOVERED -> hovered;
            case PRESSED -> pressed;
            case FOCUSED -> focused;
            case DISABLED -> disabled;
            default -> normal;
        };
    }

    Color4f textColor(UIState state) {
        return switch (state) {
            case HOVERED -> hoveredText;
            case PRESSED -> pressedText;
            case DISABLED -> disabledText;
            default -> normalText;
        };
    }
}
