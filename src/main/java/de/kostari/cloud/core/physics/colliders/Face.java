package de.kostari.cloud.core.physics.colliders;

import de.kostari.cloud.core.utils.math.Vector2;

public class Face {

    private Vector2 relativePoint;
    private Vector2 relativeFace;
    private Vector2 normal;

    public Face(Vector2 relativePoint, Vector2 relativeFace) {
        this.relativePoint = relativePoint;
        this.relativeFace = relativeFace;
        this.normal = relativeFace.perpendicular();
    }

    public Vector2 getRelativePoint() {
        return relativePoint;
    }

    public Vector2 getRelativeFace() {
        return relativeFace;
    }

    public Vector2 getNormal() {
        return normal;
    }

}
