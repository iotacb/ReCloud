package de.kostari.cloud.core.physics.colliders;

import de.kostari.cloud.core.utils.math.Vector2f;

public class Face {

    private Vector2f relativePoint;
    private Vector2f relativeFace;
    private Vector2f normal;

    public Face(Vector2f relativePoint, Vector2f relativeFace) {
        this.relativePoint = relativePoint;
        this.relativeFace = relativeFace;
        this.normal = relativeFace.perpendicular();
    }

    public Vector2f getRelativePoint() {
        return relativePoint;
    }

    public Vector2f getRelativeFace() {
        return relativeFace;
    }

    public Vector2f getNormal() {
        return normal;
    }

}
