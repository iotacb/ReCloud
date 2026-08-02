package de.kostari.cloud.core.utils;

import de.kostari.cloud.core.utils.types.Color3f;
import de.kostari.cloud.core.utils.types.Color4f;

public class Colors {

    public static final Color3f WHITE = new Color3f(1, 1, 1);
    public static final Color3f BLACK = new Color3f(0, 0, 0);
    public static final Color3f RED = new Color3f(1, 0, 0);
    public static final Color3f GREEN = new Color3f(0, 1, 0);
    public static final Color3f BLUE = new Color3f(0, 0, 1);
    public static final Color4f TRANSPARENT = new Color4f(0, 0, 0, 0);

    public static final Color3f CYAN = new Color3f(0, 1, 1);
    public static final Color3f MAGENTA = new Color3f(1, 0, 1);
    public static final Color3f YELLOW = new Color3f(1, 1, 0);
    public static final Color3f ORANGE = new Color3f(1, 0.5f, 0);
    public static final Color3f PURPLE = new Color3f(0.5f, 0, 0.5f);
    public static final Color3f PINK = new Color3f(1, 0.75f, 0.8f);
    public static final Color3f BROWN = new Color3f(0.6f, 0.3f, 0.2f);
    public static final Color3f GRAY = new Color3f(0.5f, 0.5f, 0.5f);
    public static final Color3f LIGHT_GRAY = new Color3f(0.75f, 0.75f, 0.75f);
    public static final Color3f DARK_GRAY = new Color3f(0.25f, 0.25f, 0.25f);

    public static final Color3f BEIGE = new Color3f(0.96f, 0.96f, 0.86f);
    public static final Color3f IVORY = new Color3f(1, 1, 0.94f);
    public static final Color3f NAVY = new Color3f(0, 0, 0.5f);
    public static final Color3f TEAL = new Color3f(0, 0.5f, 0.5f);
    public static final Color3f LIME = new Color3f(0, 1, 0);
    public static final Color3f INDIGO = new Color3f(0.29f, 0, 0.51f);
    public static final Color3f VIOLET = new Color3f(0.93f, 0.51f, 0.93f);
    public static final Color3f TURQUOISE = new Color3f(0.25f, 0.88f, 0.82f);
    public static final Color3f SALMON = new Color3f(0.98f, 0.5f, 0.45f);
    public static final Color3f GOLD = new Color3f(1, 0.84f, 0);
    public static final Color3f SILVER = new Color3f(0.75f, 0.75f, 0.75f);
    public static final Color3f MAROON = new Color3f(0.5f, 0, 0);
    public static final Color3f OLIVE = new Color3f(0.5f, 0.5f, 0);
    public static final Color3f MINT = new Color3f(0.6f, 1, 0.6f);
    public static final Color3f CORAL = new Color3f(1, 0.5f, 0.31f);
    public static final Color3f PEACH = new Color3f(1, 0.9f, 0.71f);
    public static final Color3f LAVENDER = new Color3f(0.9f, 0.9f, 0.98f);
    public static final Color3f SAND = new Color3f(0.76f, 0.7f, 0.5f);
    public static final Color3f PLUM = new Color3f(0.87f, 0.63f, 0.87f);
    public static final Color3f CHARCOAL = new Color3f(0.21f, 0.27f, 0.31f);
    public static final Color3f CRIMSON = new Color3f(0.86f, 0.08f, 0.24f);
    public static final Color3f MINT_CREAM = new Color3f(0.96f, 1, 0.98f);
    public static final Color3f SLATE_BLUE = new Color3f(0.42f, 0.35f, 0.8f);
    public static final Color3f FOREST_GREEN = new Color3f(0.13f, 0.55f, 0.13f);

    /**
     * Creates a new color from a rgba color in the range of 0-255
     * 
     * @param r
     * @param g
     * @param b
     * @param a
     * @return
     */
    public static Color4f from255(int r, int g, int b, int a) {
        return new Color4f(r / 255.0f, g / 255.0f, b / 255.0f, a / 255.0f);
    }

    /**
     * Creates a color from RGB or RGBA hexadecimal notation.
     */
    public static Color4f hex(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Color value cannot be null");
        }
        String hex = value.strip();
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }
        if (hex.length() == 3 || hex.length() == 4) {
            StringBuilder expanded = new StringBuilder(hex.length() * 2);
            for (int i = 0; i < hex.length(); i++) {
                expanded.append(hex.charAt(i)).append(hex.charAt(i));
            }
            hex = expanded.toString();
        }
        if (hex.length() != 6 && hex.length() != 8) {
            throw new IllegalArgumentException("Expected an RGB or RGBA hex color: " + value);
        }
        int red = Integer.parseInt(hex.substring(0, 2), 16);
        int green = Integer.parseInt(hex.substring(2, 4), 16);
        int blue = Integer.parseInt(hex.substring(4, 6), 16);
        int alpha = hex.length() == 8 ? Integer.parseInt(hex.substring(6, 8), 16) : 255;
        return from255(red, green, blue, alpha);
    }

    /**
     * Creates a new color from a rgb color in the range of 0-255
     * 
     * @param r
     * @param g
     * @param b
     * @return
     */
    public static Color3f from255(int r, int g, int b) {
        return new Color3f(from255(r, g, b, 255));
    }

    /**
     * Adjusts the brightness of a color by a given factor
     * 
     * @param color  The color to adjust
     * @param factor The factor to adjust the brightness by
     * @return
     */
    public static Color4f brightness(Color4f color, float factor) {
        float r = Math.clamp((color.r * factor), 0, 1f);
        float g = Math.clamp((color.g * factor), 0, 1f);
        float b = Math.clamp((color.b * factor), 0, 1f);
        return new Color4f(r, g, b, color.a);
    }

    /**
     * Creates a new random color
     * 
     * @return
     */
    public static Color4f random() {
        return new Color4f((float) Math.random(), (float) Math.random(), (float) Math.random(), 1);
    }

    /**
     * Returns the provided color with the provided alpha value
     * 
     * @param color
     * @param alpha
     * @return
     */
    public static Color4f alpha(Color4f color, float alpha) {
        return new Color4f(color.r, color.g, color.b, alpha);
    }

    public static Color4f alpha255(Color4f color, float alpha) {
        return new Color4f(color.r, color.g, color.b, alpha / 255.0f);
    }

}
