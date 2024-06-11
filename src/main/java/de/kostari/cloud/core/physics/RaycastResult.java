package de.kostari.cloud.core.physics;

import de.kostari.cloud.core.utils.math.Vector2;

public class RaycastResult {

    private Vector2 point;
    private Vector2 normal;

    private float distance;

    private boolean colliding;

    public RaycastResult() {
        this.point = new Vector2();
        this.normal = new Vector2();
        this.distance = 0;
        this.colliding = false;
    }

    public RaycastResult(Vector2 point, Vector2 normal, float distance, boolean hit) {
        this.point = point;
        this.normal = normal;
        this.distance = distance;
        this.colliding = hit;
    }

    public Vector2 getPoint() {
        return point;
    }

    public Vector2 getNormal() {
        return normal;
    }

    public float getDistance() {
        return distance;
    }

    public boolean isColliding() {
        return colliding;
    }
}
