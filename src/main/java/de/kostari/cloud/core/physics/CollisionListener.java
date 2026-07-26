package de.kostari.cloud.core.physics;

@FunctionalInterface
public interface CollisionListener {
    void onCollision(Collision collision);
}
