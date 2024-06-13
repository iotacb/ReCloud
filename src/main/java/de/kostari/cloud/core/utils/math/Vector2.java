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

    public Vector2(Vector2 other) {
        this.x = other.x;
        this.y = other.y;
    }

    public Vector2 set(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Vector2 set(Vector2 other) {
        this.x = other.x;
        this.y = other.y;
        return this;
    }

    public Vector2 add(float x, float y) {
        this.x += x;
        this.y += y;
        return this;
    }

    public Vector2 add(Vector2 other) {
        this.x += other.x;
        this.y += other.y;
        return this;
    }

    public Vector2 sub(float x, float y) {
        this.x -= x;
        this.y -= y;
        return this;
    }

    public Vector2 sub(Vector2 other) {
        this.x -= other.x;
        this.y -= other.y;
        return this;
    }

    public Vector2 multiply(Vector2 other) {
        this.x *= other.x;
        this.y *= other.y;
        return this;
    }

    public Vector2 multiply(float x, float y) {
        this.x *= x;
        this.y *= y;
        return this;
    }

    public Vector2 multiply(float scalar) {
        this.x *= scalar;
        this.y *= scalar;
        return this;
    }

    public Vector2 divide(Vector2 other) {
        this.x /= other.x;
        this.y /= other.y;
        return this;
    }

    public Vector2 divide(float x, float y) {
        this.x /= x;
        this.y /= y;
        return this;
    }

    public Vector2 divide(float scalar) {
        this.x /= scalar;
        this.y /= scalar;
        return this;
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
            divide(length);
            return this;
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
        return this.clone().multiply(1 - alpha).add(other.clone().multiply(alpha));
    }

    public Vector2 lerp(float x, float y, float alpha) {
        return this.clone().multiply(1 - alpha).add(new Vector2(x, y).multiply(alpha));
    }

    public Vector2 perpendicular() {
        return new Vector2(-this.y, this.x);
    }

    public float distance(Vector2 other) {
        return this.clone().sub(other).length();
    }

    public float distance(float x, float y) {
        return this.clone().sub(new Vector2(x, y)).length();
    }

    /**
     * Reflects this vector off the given normal vector.
     * 
     * @param normal the normal vector to reflect off of
     * @return the reflected vector
     */
    public Vector2 reflect(Vector2 normal) {
        return this.clone().sub(normal.clone().multiply(2 * dot(normal)));
    }

    /**
     * Reflects this vector off the given normal vector.
     * 
     * @param x the x component of the normal vector to reflect off of
     * @param y the y component of the normal vector to reflect off of
     * @return the reflected vector
     */
    public Vector2 reflect(float x, float y) {
        return this.clone().sub(new Vector2(x, y).multiply(2 * dot(x, y)));
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
     * Calculates the angle between this vector and another vector in radians.
     * 
     * @param other The other vector.
     * @return The angle in radians.
     */
    public float angle(Vector2 other) {
        return (float) Math.acos(dot(other) / (length() * other.length()));
    }

    /**
     * Calculates the angle between this vector and another vector in degrees.
     * 
     * @param other The other vector.
     * @return The angle in degrees.
     */
    public float angleDegrees(Vector2 other) {
        return (float) Math.toDegrees(angle(other));
    }

    /**
     * Projects this vector onto another vector.
     * 
     * @param other The vector to project onto.
     * @return The projected vector.
     */
    public Vector2 projectOnto(Vector2 other) {
        float scalar = dot(other) / other.lengthSquared();
        return other.clone().multiply(scalar);
    }

    /**
     * Rotates this vector by the specified angle in radians.
     * 
     * @param radians The angle to rotate by in radians.
     * @return The rotated vector.
     */
    public Vector2 rotate(float radians) {
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);
        float newX = x * cos - y * sin;
        float newY = x * sin + y * cos;
        return new Vector2(newX, newY);
    }

    /**
     * Rotates this vector by the specified angle in degrees.
     * 
     * @param degrees The angle to rotate by in degrees.
     * @return The rotated vector.
     */
    public Vector2 rotateDegrees(float degrees) {
        return rotate((float) Math.toRadians(degrees));
    }

    /**
     * Creates a new vector with random x and y values between -1 and 1.
     * 
     * @return
     */
    public static Vector2 random() {
        return new Vector2((float) Math.random() * 2 - 1, (float) Math.random() * 2 - 1).normalize();
    }

    /**
     * Returns a new vector with the same x and y values.
     */
    public Vector2 clone() {
        return new Vector2(this.x, this.y);
    }

    @Override
    public String toString() {
        return String.format("{%s, %s}", x, y);
    }

    /**
     * Compares this vector with another vector.
     * 
     * @param other
     * @param tolerance
     * @return
     */
    public boolean equals(Vector2 other, float tolerance) {
        return Math.abs(this.x - other.x) < tolerance && Math.abs(this.y - other.y) < tolerance;
    }

}
