package de.kostari.cloud.core.utils.math;

public class Mathi {

    public static final float PI = (float) Math.PI;
    public static final float TAU = PI * 2;
    public static final float RAD2DEG = 360f / TAU;
    public static final float DEG2RAD = TAU / 360f;

    public static float moveToward(float from, float to, float delta) {
        if (Math.abs(to - from) <= delta) {
            return to;
        }
        return from + Math.signum(to - from) * delta;
    }

}
