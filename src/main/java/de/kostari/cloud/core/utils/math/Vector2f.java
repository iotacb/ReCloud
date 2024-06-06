package de.kostari.cloud.core.utils.math;

public class Vector2f {

    public float x, y;

    public static final Vector2f ZERO = new Vector2f(0, 0);
    public static final Vector2f ONE = new Vector2f(1, 1);
    public static final Vector2f UP = new Vector2f(0, -1);
    public static final Vector2f DOWN = new Vector2f(0, 1);
    public static final Vector2f LEFT = new Vector2f(-1, 0);
    public static final Vector2f RIGHT = new Vector2f(1, 0);

    public Vector2f() {
        this.x = 0;
        this.y = 0;
    }

    public Vector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void set(Vector2f other) {
        this.x = other.x;
        this.y = other.y;
    }

    public Vector2f add(Vector2f other) {
        return new Vector2f(this.x + other.x, this.y + other.y);
    }

    public Vector2f add(float x, float y) {
        return new Vector2f(this.x + x, this.y + y);
    }

    public Vector2f sub(Vector2f other) {
        return new Vector2f(this.x - other.x, this.y - other.y);
    }

    public Vector2f sub(float x, float y) {
        return new Vector2f(this.x - x, this.y - y);
    }

    public Vector2f mul(Vector2f other) {
        return new Vector2f(this.x * other.x, this.y * other.y);
    }

    public Vector2f mul(float x, float y) {
        return new Vector2f(this.x * x, this.y * y);
    }

    public Vector2f mul(float scalar) {
        return new Vector2f(this.x * scalar, this.y * scalar);
    }

    public Vector2f div(Vector2f other) {
        return new Vector2f(this.x / other.x, this.y / other.y);
    }

    public Vector2f div(float x, float y) {
        return new Vector2f(this.x / x, this.y / y);
    }

    public Vector2f div(float scalar) {
        return new Vector2f(this.x / scalar, this.y / scalar);
    }

    public float dot(Vector2f other) {
        return this.x * other.x + this.y * other.y;
    }

    public float dot(float x, float y) {
        return this.x * x + this.y * y;
    }

    public float cross(Vector2f other) {
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

    public Vector2f normalize() {
        float length = length();
        if (length != 0) {
            return div(length);
        } else {
            return new Vector2f();
        }
    }

    public Vector2f negate() {
        return new Vector2f(-this.x, -this.y);
    }

    public Vector2f abs() {
        return new Vector2f(Math.abs(this.x), Math.abs(this.y));
    }

    public Vector2f lerp(Vector2f other, float alpha) {
        return mul(1 - alpha).add(other.mul(alpha));
    }

    public Vector2f lerp(float x, float y, float alpha) {
        return mul(1 - alpha).add(new Vector2f(x, y).mul(alpha));
    }

    public Vector2f perpendicular() {
        return new Vector2f(y, x * -1);
    }

    public float distance(Vector2f other) {
        return sub(other).length();
    }

    public float distance(float x, float y) {
        return sub(new Vector2f(x, y)).length();
    }

    /**
     * Reflects this vector off the given normal vector.
     * 
     * @param normal the normal vector to reflect off of
     * @return the reflected vector
     */
    public Vector2f reflect(Vector2f normal) {
        return sub(normal.mul(2 * dot(normal)));
    }

    /**
     * Reflects this vector off the given normal vector.
     * 
     * @param x the x component of the normal vector to reflect off of
     * @param y the y component of the normal vector to reflect off of
     * @return the reflected vector
     */
    public Vector2f reflect(float x, float y) {
        return sub(new Vector2f(x, y).mul(2 * dot(x, y)));
    }

    /**
     * Calculates the bounce vector of this vector off a given surface normal.
     * 
     * @param normal the surface normal to bounce off of
     * @return the resulting bounce vector
     */
    public Vector2f bounce(Vector2f normal) {
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
    public Vector2f clone() {
        return new Vector2f(x, y);
    }

    @Override
    public String toString() {
        return String.format("{%s, %s}", x, y);
    }
}
