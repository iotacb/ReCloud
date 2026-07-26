package de.kostari.cloud.core.utils.math;

import de.kostari.cloud.core.physics.AABB;

public class Physics {

    public static boolean isColliding(float x, float y, float width, float height, float x2, float y2, float width2,
            float height2) {
        return AABB.fromCenter(x, y, width, height)
                .overlaps(AABB.fromCenter(x2, y2, width2, height2));
    }

}
