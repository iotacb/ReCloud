package de.kostari.cloud.core.utils.math;

public class Vector2 {

    public float x, y;

    public static final Vector2 ZERO = new Vector2(0, 0);
    public static final Vector2 ONE = new Vector2(1, 1);
    public static final Vector2 UP = new Vector2(0, -1);
    public static final Vector2 DOWN = new Vector2(0, 1);
    public static final Vector2 LEFT = new Vector2(-1, 0);
    public static final Vector2 RIGHT = new Vector2(1, 0);

    public Vector2() {
        this.x = 0;
        this.y = 0;
    }

    public Vector2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void set(Vector2 other) {
        this.x = other.x;
        this.y = other.y;
    }

    public Vector2 add(Vector2 other) {
        return new Vector2(this.x + other.x, this.y + other.y);
    }

    public Vector2 add(float x, float y) {
        return new Vector2(this.x + x, this.y + y);
    }

    public Vector2 sub(Vector2 other) {
        return new Vector2(this.x - other.x, this.y - other.y);
    }

    public Vector2 sub(float x, float y) {
        return new Vector2(this.x - x, this.y - y);
    }

    public Vector2 mul(Vector2 other) {
        return new Vector2(this.x * other.x, this.y * other.y);
    }

    public Vector2 mul(float x, float y) {
        return new Vector2(this.x * x, this.y * y);
    }

    public Vector2 mul(float scalar) {
        return new Vector2(this.x * scalar, this.y * scalar);
    }

    public Vector2 div(Vector2 other) {
        return new Vector2(this.x / other.x, this.y / other.y);
    }

    public Vector2 div(float x, float y) {
        return new Vector2(this.x / x, this.y / y);
    }

    public Vector2 div(float scalar) {
        return new Vector2(this.x / scalar, this.y / scalar);
    }

    public float dot(Vector2 other) {
        return this.x * other.x + this.y * other.y;
    }

    public float dot(float x, float y) {
        return this.x * x + this.y * y;
    }

    public float cross(Vector2 other) {
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

    public Vector2 normalize() {
        float length = length();
        if (length != 0) {
            return div(length);
        } else {
            return new Vector2();
        }
    }

    public Vector2 negate() {
        return new Vector2(-this.x, -this.y);
    }

    public Vector2 abs() {
        return new Vector2(Math.abs(this.x), Math.abs(this.y));
    }

    public Vector2 lerp(Vector2 other, float alpha) {
        return mul(1 - alpha).add(other.mul(alpha));
    }

    public Vector2 lerp(float x, float y, float alpha) {
        return mul(1 - alpha).add(new Vector2(x, y).mul(alpha));
    }

    public Vector2 perpendicular() {
        return new Vector2(y, x * -1);
    }

    public float distance(Vector2 other) {
        return sub(other).length();
    }

    public float distance(float x, float y) {
        return sub(new Vector2(x, y)).length();
    }

    /**
     * Reflects this vector off the given normal vector.
     * 
     * @param normal the normal vector to reflect off of
     * @return the reflected vector
     */
    public Vector2 reflect(Vector2 normal) {
        return sub(normal.mul(2 * dot(normal)));
    }

    /**
     * Reflects this vector off the given normal vector.
     * 
     * @param x the x component of the normal vector to reflect off of
     * @param y the y component of the normal vector to reflect off of
     * @return the reflected vector
     */
    public Vector2 reflect(float x, float y) {
        return sub(new Vector2(x, y).mul(2 * dot(x, y)));
    }

    /**
     * Calculates the bounce vector of this vector off a given surface normal.
     * 
     * @param normal the surface normal to bounce off of
     * @return the resulting bounce vector
     */
    public Vector2 bounce(Vector2 normal) {
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
    public Vector2 clone() {
        return new Vector2(x, y);
    }

    @Override
    public String toString() {
        return String.format("{%s, %s}", x, y);
    }

    public static Vector2 fromRandomDirection() {
        return new Vector2((float) Math.random() * 2 - 1, (float) Math.random() * 2 - 1).normalize();
    }

}
