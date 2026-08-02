package de.kostari.cloud.core.ui;

import de.kostari.cloud.core.utils.types.Color4f;

public record SliderSkin(Drawable track, Drawable fill, Drawable thumb,
        Drawable hoveredThumb, Drawable pressedThumb, Drawable disabledThumb) {

    public static SliderSkin defaultSkin() {
        return new SliderSkin(
                new UIMaterial().fill(new Color4f(0.06f, 0.09f, 0.14f, 0.95f))
                        .border(1, new Color4f(1, 1, 1, 0.13f)).radius(3),
                new UIMaterial().gradient(new Color4f(0.31f, 0.88f, 0.94f, 1),
                        new Color4f(0.08f, 0.58f, 0.72f, 1)).radius(3),
                new UIMaterial().gradient(new Color4f(0.9f, 0.98f, 1, 1),
                        new Color4f(0.44f, 0.82f, 0.92f, 1))
                        .border(1, new Color4f(0.75f, 0.97f, 1, 0.9f)).radius(9),
                new UIMaterial().fill(new Color4f(0.78f, 0.97f, 1, 1))
                        .border(1, new Color4f(1, 1, 1, 1)).radius(9)
                        .glow(new Color4f(0.25f, 0.85f, 1, 1), 8, 0.5f),
                new UIMaterial().fill(new Color4f(0.21f, 0.73f, 0.85f, 1))
                        .border(1, new Color4f(0.83f, 1, 1, 1)).radius(9),
                new UIMaterial().fill(new Color4f(0.38f, 0.42f, 0.47f, 1)).radius(9));
    }
}
