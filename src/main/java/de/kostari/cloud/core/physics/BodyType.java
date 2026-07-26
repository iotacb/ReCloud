package de.kostari.cloud.core.physics;

public enum BodyType {
    /** Moved by gravity, forces, impulses, and collisions. */
    DYNAMIC,
    /** Never moved by the physics solver. */
    STATIC,
    /** Moved by its velocity, but not by gravity or collision impulses. */
    KINEMATIC
}
