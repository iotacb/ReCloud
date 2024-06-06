package de.kostari.cloud.core.utils.math;

public class Vector2i {

    public float x, y;

    public static final Vector2i ZERO = new Vector2i(0, 0);
    public static final Vector2i ONE = new Vector2i(1, 1);
    public static final Vector2i UP = new Vector2i(0, -1);
    public static final Vector2i DOWN = new Vector2i(0, 1);
    public static final Vector2i LEFT = new Vector2i(-1, 0);
    public static final Vector2i RIGHT = new Vector2i(1, 0);

    public Vector2i() {
        this.x = 0;
        this.y = 0;
    }

    public Vector2i(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vector2i add(Vector2i other) {
        return new Vector2i(this.x + other.x, this.y + other.y);
    }

    public Vector2i add(float x, float y) {
        return new Vector2i(this.x + x, this.y + y);
    }

    public Vector2i sub(Vector2i other) {
        return new Vector2i(this.x - other.x, this.y - other.y);
    }

    public Vector2i sub(float x, float y) {
        return new Vector2i(this.x - x, this.y - y);
    }

    public Vector2i mul(Vector2i other) {
        return new Vector2i(this.x * other.x, this.y * other.y);
    }

    public Vector2i mul(float x, float y) {
        return new Vector2i(this.x * x, this.y * y);
    }

    public Vector2i mul(float scalar) {
        return new Vector2i(this.x * scalar, this.y * scalar);
    }

    public Vector2i div(Vector2i other) {
        return new Vector2i(this.x / other.x, this.y / other.y);
    }

    public Vector2i div(float x, float y) {
        return new Vector2i(this.x / x, this.y / y);
    }

    public Vector2i div(float scalar) {
        return new Vector2i(this.x / scalar, this.y / scalar);
    }

    public float dot(Vector2i other) {
        return this.x * other.x + this.y * other.y;
    }

    public float dot(float x, float y) {
        return this.x * x + this.y * y;
    }

    public float cross(Vector2i other) {
        return this.x * other.y - this.y * other.x;
    }

    public float cross(float x, float y) {
        return this.x * y - this.y * x;
    }

    public float length() {
        return (float) Math.sqrt(this.x * this.x + this.y * this.y);
    }

    public float lengthSquared() {
        return this.x * this.x + this.y * this.y;
    }

    public Vector2i normalize() {
        float length = length();
        if (length != 0 && length != 1) {
            return div(length);
        } else {
            return new Vector2i();
        }
    }

    public Vector2i negate() {
        return new Vector2i(-this.x, -this.y);
    }

    public Vector2i abs() {
        return new Vector2i(Math.abs(this.x), Math.abs(this.y));
    }

    public Vector2i lerp(Vector2i other, float alpha) {
        return mul(1 - alpha).add(other.mul(alpha));
    }

    public Vector2i lerp(float x, float y, float alpha) {
        return mul(1 - alpha).add(new Vector2i(x, y).mul(alpha));
    }

    /**
     * Reflects this vector off the given normal vector.
     * 
     * @param normal the normal vector to reflect off of
     * @return the reflected vector
     */
    public Vector2i reflect(Vector2i normal) {
        return sub(normal.mul(2 * dot(normal)));
    }

    /**
     * Reflects this vector off the given normal vector.
     * 
     * @param x the x component of the normal vector to reflect off of
     * @param y the y component of the normal vector to reflect off of
     * @return the reflected vector
     */
    public Vector2i reflect(float x, float y) {
        return sub(new Vector2i(x, y).mul(2 * dot(x, y)));
    }

    /**
     * Calculates the bounce vector of this vector off a given surface normal.
     * 
     * @param normal the surface normal to bounce off of
     * @return the resulting bounce vector
     */
    public Vector2i bounce(Vector2i normal) {
        return reflect(normal).negate();
    }

    /**
     * Returns the direction of this vector in radians.
     * The direction is the angle between the vector and the positive x-axis.
     *
     * @return the direction of this vector in radians
     */
    public float direction() {
        return (float) Math.atan2(y, x);
    }

    /**
     * Returns a new vector with the same x and y values.
     */
    public Vector2i clone() {
        return new Vector2i(x, y);
    }

    @Override
    public String toString() {
        return String.format("{%s, %s}", x, y);
    }
}
