package de.kostari.cloud.core.ui;

import de.kostari.cloud.core.utils.types.Color4f;

public record TextBoxSkin(Drawable normal, Drawable hovered, Drawable focused, Drawable disabled,
        Color4f text, Color4f placeholder, Color4f caret) {

    public static TextBoxSkin defaultSkin() {
        Drawable normal = new UIMaterial()
                .gradient(new Color4f(0.045f, 0.065f, 0.1f, 0.98f), new Color4f(0.025f, 0.037f, 0.06f, 0.98f))
                .border(1, new Color4f(1, 1, 1, 0.16f)).radius(8);
        Drawable hovered = new UIMaterial()
                .gradient(new Color4f(0.06f, 0.085f, 0.13f, 0.98f), new Color4f(0.025f, 0.037f, 0.06f, 0.98f))
                .border(1, new Color4f(0.45f, 0.82f, 1, 0.55f)).radius(8);
        Drawable focused = new UIMaterial()
                .gradient(new Color4f(0.06f, 0.085f, 0.13f, 0.98f), new Color4f(0.025f, 0.037f, 0.06f, 0.98f))
                .border(1.5f, new Color4f(0.25f, 0.82f, 0.98f, 0.9f)).radius(8)
                .glow(new Color4f(0.2f, 0.78f, 1, 1), 9, 0.32f);
        Drawable disabled = new UIMaterial().fill(new Color4f(0.08f, 0.09f, 0.12f, 0.75f))
                .border(1, new Color4f(1, 1, 1, 0.08f)).radius(8);
        return new TextBoxSkin(normal, hovered, focused, disabled,
                new Color4f(0.92f, 0.97f, 1, 1),
                new Color4f(0.48f, 0.55f, 0.64f, 1),
                new Color4f(0.42f, 0.9f, 1, 1));
    }
}
