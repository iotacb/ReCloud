package de.kostari.cloud.core.physics;

/**
 * An axis-aligned rectangle represented by its center and size.
 */
public final class AABB {

    private final float centerX;
    private final float centerY;
    private final float width;
    private final float height;

    public AABB(float centerX, float centerY, float width, float height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("AABB width and height must be non-negative");
        }
        this.centerX = centerX;
        this.centerY = centerY;
        this.width = width;
        this.height = height;
    }

    public static AABB fromCenter(float centerX, float centerY, float width, float height) {
        return new AABB(centerX, centerY, width, height);
    }

    public static AABB fromTopLeft(float x, float y, float width, float height) {
        return new AABB(x + width / 2f, y + height / 2f, width, height);
    }

    public float centerX() {
        return centerX;
    }

    public float centerY() {
        return centerY;
    }

    public float width() {
        return width;
    }

    public float height() {
        return height;
    }

    public float left() {
        return centerX - width / 2f;
    }

    public float right() {
        return centerX + width / 2f;
    }

    public float top() {
        return centerY - height / 2f;
    }

    public float bottom() {
        return centerY + height / 2f;
    }

    public boolean overlaps(AABB other) {
        return right() > other.left()
                && left() < other.right()
                && bottom() > other.top()
                && top() < other.bottom();
    }

    public boolean contains(float x, float y) {
        return x >= left() && x <= right() && y >= top() && y <= bottom();
    }
}
