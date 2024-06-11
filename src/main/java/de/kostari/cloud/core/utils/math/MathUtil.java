package de.kostari.cloud.core.utils.math;

public class MathUtil {

    public static final float PI = (float) Math.PI;
    public static final float TAU = PI * 2;
    public static final float RAD2DEG = 360f / TAU;
    public static final float DEG2RAD = TAU / 360f;

    public static float moveToward(float from, float to, float delta) {
        if (Math.abs(to - from) <= delta) {
            return to;
        }
        return from + Math.signum(to - from) * delta;
    }

    public static Vector2 maxDotPoint(Vector2[] convexShapePoints, Vector2 direction) {
        float maxDot = Float.NEGATIVE_INFINITY;
        Vector2 point = null;
        for (Vector2 p : convexShapePoints) {
            float dot = direction.dot(p);
            if (dot > maxDot) {
                maxDot = dot;
                point = p;
            }
        }
        return point;
    }

    public static float boundingSphere(Vector2 centroid, Vector2... vertices) {
        if (vertices.length < 1)
            return 0;
        if (vertices.length == 1)
            return centroid.sub(vertices[0]).length();
        float max = centroid.sub(vertices[0]).lengthSquared();
        float currDist;
        int current = 1;
        do {
            currDist = centroid.sub(vertices[current]).lengthSquared();
            if (currDist > max)
                max = currDist;
            current++;
        } while (vertices.length > current);
        return (float) Math.sqrt(max);
    }

    /**
     * Calculate the centroid of a polygon.
     *
     * @param vertices the vertices of the polygon
     * @return the centroid of a polygon
     */
    public static Vector2 polygonCentroid(Vector2... vertices) {
        if (vertices.length == 1)
            return vertices[0];
        if (vertices.length < 1)
            return null;
        Vector2 centroid = new Vector2();
        double signedArea = 0;
        Vector2 prev = vertices[vertices.length - 1];

        // for all vertices in a loop
        for (Vector2 next : vertices) {
            // partial area
            float a = prev.x * next.y - next.x * prev.y;
            // move centroid towards edge with weight relative to partial area a
            centroid.add((prev.x + next.x) * a, (prev.y + next.y) * a);
            // sum up area
            signedArea += a;
            prev = next;
        }

        // final summary according to polygon centroid calculation
        // by using gauss's area formula
        signedArea *= 0.5;
        centroid.x /= (6.0 * signedArea);
        centroid.y /= (6.0 * signedArea);
        return centroid;
    }

    public static Vector2 direction(float x1, float x2, float y1, float y2) {
        return new Vector2(x2 - x1, y2 - y1).normalize();
    }

    public static float random(float i, float j) {
        return (float) (Math.random() * (j - i) + i);
    }

}
