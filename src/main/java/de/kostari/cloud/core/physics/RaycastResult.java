package de.kostari.cloud.core.physics;

import de.kostari.cloud.core.utils.math.Vector2f;

public class RaycastResult {

    private Vector2f point;
    private Vector2f normal;

    private float distance;

    private boolean colliding;

    public RaycastResult() {
        this.point = new Vector2f();
        this.normal = new Vector2f();
        this.distance = 0;
        this.colliding = false;
    }

    public RaycastResult(Vector2f point, Vector2f normal, float distance, boolean hit) {
        this.point = point;
        this.normal = normal;
        this.distance = distance;
        this.colliding = hit;
    }

    public Vector2f getPoint() {
        return point;
    }

    public Vector2f getNormal() {
        return normal;
    }

    public float getDistance() {
        return distance;
    }

    public boolean isColliding() {
        return colliding;
    }
}
