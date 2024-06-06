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

}
