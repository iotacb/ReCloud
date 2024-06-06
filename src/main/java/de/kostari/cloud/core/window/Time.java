package de.kostari.cloud.core.window;

import org.lwjgl.glfw.GLFW;

public class Time {

    /**
     * The time between the last frame and the current frame.
     */
    public static float delta;

    /**
     * The time since the game started.
     */
    public static float time;

    protected static float lastTime;

    protected static void updateDelta() {
        float currentTime = (float) GLFW.glfwGetTime();
        delta = currentTime - lastTime;
        time += delta;
        lastTime = currentTime;
    }

    public static int getTime() {
        return (int) time;
    }

    public static void reset() {
        time = 0;
        lastTime = (float) GLFW.glfwGetTime();
    }

}
