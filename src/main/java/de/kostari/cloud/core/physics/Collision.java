package de.kostari.cloud.core.physics;

import de.kostari.cloud.core.utils.math.Vector2;

/**
 * Information about one body's contact with another body.
 */
public final class Collision {

    private final PhysicsBody self;
    private final PhysicsBody other;
    private final Vector2 normal;
    private final float penetration;
    private final boolean sensor;

    Collision(PhysicsBody self, PhysicsBody other, Vector2 normal, float penetration, boolean sensor) {
        this.self = self;
        this.other = other;
        this.normal = normal;
        this.penetration = penetration;
        this.sensor = sensor;
    }

    public PhysicsBody self() {
        return self;
    }

    public PhysicsBody other() {
        return other;
    }

    /**
     * The direction in which this body is pushed out of the other body.
     */
    public Vector2 normal() {
        return normal.clone();
    }

    public float penetration() {
        return penetration;
    }

    public boolean isSensor() {
        return sensor;
    }
}
