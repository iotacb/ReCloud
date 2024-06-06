package de.kostari.cloud.core.physics.colliders;

import de.kostari.cloud.core.utils.math.Vector2f;

public class CircleCollider extends ColliderScaffold {

    private float radius;
    private float radiusSquared;

    public CircleCollider(Vector2f relativeCenter, float radius) {
        super(ColliderTypes.CIRCLE);
        this.relativeCentroid = relativeCenter;
        this.radius = radius;
        this.radiusSquared = radius * radius;
        this.boundingCircle = this;
    }

    @Override
    public Vector2f support(Vector2f vector) {
        Vector2f normalized = vector.normalize();
        return absoluteCentroid.add(normalized);
    }

    public float getRadius() {
        return radius;
    }

    public float getRadiusSquared() {
        return radiusSquared;
    }

}
