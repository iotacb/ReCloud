package de.kostari.cloud.core.utils.math;

public class Lerping {

    /**
     * Linearly interpolates between two float values a and b by a factor of t.
     * 
     * @param a The start value.
     * @param b The end value.
     * @param t The interpolation factor between 0 and 1.
     * @return The interpolated value.
     */
    public static float lerp(float a, float b, float t) {
        return a + t * (b - a);
    }

    /**
     * Linearly interpolates between two double values a and b by a factor of t.
     * 
     * @param a The start value.
     * @param b The end value.
     * @param t The interpolation factor between 0 and 1.
     * @return The interpolated value.
     */
    public static double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }

    /**
     * Linearly interpolates between two int values a and b by a factor of t.
     * 
     * @param a The start value.
     * @param b The end value.
     * @param t The interpolation factor between 0 and 1.
     * @return The interpolated value as an int.
     */
    public static int lerp(int a, int b, float t) {
        return (int) (a + t * (b - a));
    }

    /**
     * Linearly interpolates between two points represented by x and y coordinates.
     * 
     * @param x1 The x-coordinate of the start point.
     * @param y1 The y-coordinate of the start point.
     * @param x2 The x-coordinate of the end point.
     * @param y2 The y-coordinate of the end point.
     * @param t  The interpolation factor between 0 and 1.
     * @return A float array containing the interpolated x and y coordinates.
     */
    public static float[] lerp(float x1, float y1, float x2, float y2, float t) {
        float[] result = new float[2];
        result[0] = lerp(x1, x2, t);
        result[1] = lerp(y1, y2, t);
        return result;
    }

}
