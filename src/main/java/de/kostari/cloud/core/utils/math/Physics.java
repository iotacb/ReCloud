package de.kostari.cloud.core.utils.math;

public class Physics {

    public static boolean isColliding(float x, float y, float width, float height, float x2, float y2, float width2,
            float height2) {
        return x + width / 2 > x2 - width2 / 2 && x - width / 2 < x2 + width2 / 2 && y + height / 2 > y2 - height2 / 2
                && y - height / 2 < y2 + height2 / 2;
    }

}
