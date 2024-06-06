package de.kostari.cloud.core.utils.math;

public class Vector2d {

    public float x, y;

    public static final Vector2d ZERO = new Vector2d(0, 0);
    public static final Vector2d ONE = new Vector2d(1, 1);
    public static final Vector2d UP = new Vector2d(0, -1);
    public static final Vector2d DOWN = new Vector2d(0, 1);
    public static final Vector2d LEFT = new Vector2d(-1, 0);
    public static final Vector2d RIGHT = new Vector2d(1, 0);

    public Vector2d() {
        this.x = 0;
        this.y = 0;
    }

    public Vector2d(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vector2d add(Vector2d other) {
        return new Vector2d(this.x + other.x, this.y + other.y);
    }

    public Vector2d add(float x, float y) {
        return new Vector2d(this.x + x, this.y + y);
    }

    public Vector2d sub(Vector2d other) {
        return new Vector2d(this.x - other.x, this.y - other.y);
    }

    public Vector2d sub(float x, float y) {
        return new Vector2d(this.x - x, this.y - y);
    }

    public Vector2d mul(Vector2d other) {
        return new Vector2d(this.x * other.x, this.y * other.y);
    }

    public Vector2d mul(float x, float y) {
        return new Vector2d(this.x * x, this.y * y);
    }

    public Vector2d mul(float scalar) {
        return new Vector2d(this.x * scalar, this.y * scalar);
    }

    public Vector2d div(Vector2d other) {
        return new Vector2d(this.x / other.x, this.y / other.y);
    }

    public Vector2d div(float x, float y) {
        return new Vector2d(this.x / x, this.y / y);
    }

    public Vector2d div(float scalar) {
        return new Vector2d(this.x / scalar, this.y / scalar);
    }

    public float dot(Vector2d other) {
        return this.x * other.x + this.y * other.y;
    }

    public float dot(float x, float y) {
        return this.x * x + this.y * y;
    }

    public float cross(Vector2d other) {
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

    public Vector2d normalize() {
        float length = length();
        if (length != 0 && length != 1) {
            return div(length);
        } else {
            return new Vector2d();
        }
    }

    public Vector2d negate() {
        return new Vector2d(-this.x, -this.y);
    }

    public Vector2d abs() {
        return new Vector2d(Math.abs(this.x), Math.abs(this.y));
    }

    public Vector2d lerp(Vector2d other, float alpha) {
        return mul(1 - alpha).add(other.mul(alpha));
    }

    public Vector2d lerp(float x, float y, float alpha) {
        return mul(1 - alpha).add(new Vector2d(x, y).mul(alpha));
    }

    /**
     * Reflects this vector off the given normal vector.
     * 
     * @param normal the normal vector to reflect off of
     * @return the reflected vector
     */
    public Vector2d reflect(Vector2d normal) {
        return sub(normal.mul(2 * dot(normal)));
    }

    /**
     * Reflects this vector off the given normal vector.
     * 
     * @param x the x component of the normal vector to reflect off of
     * @param y the y component of the normal vector to reflect off of
     * @return the reflected vector
     */
    public Vector2d reflect(float x, float y) {
        return sub(new Vector2d(x, y).mul(2 * dot(x, y)));
    }

    /**
     * Calculates the bounce vector of this vector off a given surface normal.
     * 
     * @param normal the surface normal to bounce off of
     * @return the resulting bounce vector
     */
    public Vector2d bounce(Vector2d normal) {
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
    public Vector2d clone() {
        return new Vector2d(x, y);
    }

    @Override
    public String toString() {
        return String.format("{%s, %s}", x, y);
    }
}
