package de.kostari.cloud.core.physics;

public class CollisionInfo {

    protected Object data;
    protected boolean colliding;

    public CollisionInfo() {
        this.data = null;
        this.colliding = false;
    }

    public CollisionInfo(Object data, boolean colliding) {
        this.data = data;
        this.colliding = colliding;
    }

}
