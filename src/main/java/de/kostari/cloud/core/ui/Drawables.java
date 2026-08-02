package de.kostari.cloud.core.ui;

import java.util.Arrays;

import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.render.Texture;
import de.kostari.cloud.core.utils.types.Color4f;

public final class Drawables {

    private static final Drawable NONE = (bounds, opacity) -> {
    };

    private Drawables() {
    }

    public static Drawable none() {
        return NONE;
    }

    public static Drawable solid(Color4f color) {
        return (bounds, opacity) -> {
            Color4f tint = alpha(color, opacity);
            if (tint.a > 0) {
                Render.drawRect(bounds.x, bounds.y, bounds.width, bounds.height, false, tint);
            }
        };
    }

    public static Drawable verticalGradient(Color4f top, Color4f bottom) {
        return (bounds, opacity) -> Render.drawVerticalGradient(
                bounds.x, bounds.y, bounds.width, bounds.height,
                alpha(top, opacity), alpha(bottom, opacity));
    }

    public static Drawable gradient(Color4f topLeft, Color4f topRight,
            Color4f bottomRight, Color4f bottomLeft) {
        return (bounds, opacity) -> Render.drawGradientRect(
                bounds.x, bounds.y, bounds.width, bounds.height,
                alpha(topLeft, opacity), alpha(topRight, opacity),
                alpha(bottomRight, opacity), alpha(bottomLeft, opacity));
    }

    public static Drawable border(float width, Color4f color) {
        float borderWidth = Math.max(0, width);
        return (bounds, opacity) -> {
            if (borderWidth <= 0) {
                return;
            }
            Color4f tint = alpha(color, opacity);
            Render.drawRect(bounds.x, bounds.y, bounds.width, borderWidth, false, tint);
            Render.drawRect(bounds.x, bounds.bottom() - borderWidth, bounds.width, borderWidth, false, tint);
            Render.drawRect(bounds.x, bounds.y, borderWidth, bounds.height, false, tint);
            Render.drawRect(bounds.right() - borderWidth, bounds.y, borderWidth, bounds.height, false, tint);
        };
    }

    public static Drawable texture(Texture texture) {
        return (bounds, opacity) -> Render.drawTexture(texture,
                bounds.x, bounds.y, bounds.width, bounds.height, false,
                new Color4f(1, 1, 1, Math.clamp(opacity, 0, 1)));
    }

    public static Drawable nineSlice(Texture texture, Insets slices) {
        if (texture == null) {
            return none();
        }
        Insets safe = slices == null ? Insets.NONE : slices;
        return (bounds, opacity) -> drawNineSlice(texture, safe, bounds, opacity);
    }

    public static Drawable layered(Drawable... layers) {
        Drawable[] safe = layers == null ? new Drawable[0]
                : Arrays.stream(layers).filter(layer -> layer != null).toArray(Drawable[]::new);
        return (bounds, opacity) -> {
            for (Drawable layer : safe) {
                layer.draw(bounds, opacity);
            }
        };
    }

    private static void drawNineSlice(Texture texture, Insets slices, UIRect bounds, float opacity) {
        float left = Math.min(slices.left(), bounds.width * 0.5f);
        float right = Math.min(slices.right(), bounds.width * 0.5f);
        float top = Math.min(slices.top(), bounds.height * 0.5f);
        float bottom = Math.min(slices.bottom(), bounds.height * 0.5f);

        float[] xs = { bounds.x, bounds.x + left, bounds.right() - right, bounds.right() };
        float[] ys = { bounds.y, bounds.y + top, bounds.bottom() - bottom, bounds.bottom() };

        float uvWidth = texture.getU1() - texture.getU0();
        float uvHeight = texture.getV1() - texture.getV0();
        float sourceWidth = Math.max(1, texture.getWidth());
        float sourceHeight = Math.max(1, texture.getHeight());
        float[] us = {
                texture.getU0(),
                texture.getU0() + slices.left() / sourceWidth * uvWidth,
                texture.getU1() - slices.right() / sourceWidth * uvWidth,
                texture.getU1()
        };
        float[] vs = {
                texture.getV0(),
                texture.getV0() + slices.top() / sourceHeight * uvHeight,
                texture.getV1() - slices.bottom() / sourceHeight * uvHeight,
                texture.getV1()
        };
        Color4f tint = new Color4f(1, 1, 1, Math.clamp(opacity, 0, 1));

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                Render.drawTextureRegion(texture,
                        xs[column], ys[row], xs[column + 1] - xs[column], ys[row + 1] - ys[row],
                        us[column], vs[row], us[column + 1], vs[row + 1], tint);
            }
        }
    }

    static Color4f alpha(Color4f color, float opacity) {
        if (color == null) {
            return new Color4f(0, 0, 0, 0);
        }
        return new Color4f(color.r, color.g, color.b, color.a * Math.clamp(opacity, 0, 1));
    }
}
