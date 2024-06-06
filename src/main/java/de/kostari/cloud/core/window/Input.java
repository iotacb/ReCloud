package de.kostari.cloud.core.window;

import org.lwjgl.glfw.GLFW;

import de.kostari.cloud.core.utils.math.Vector2f;

public class Input {

    private static boolean keyPressed[] = new boolean[GLFW.GLFW_KEY_LAST];
    private static boolean keyPressedLast[] = new boolean[GLFW.GLFW_KEY_LAST];

    private static boolean mouseButtonsPressed[] = new boolean[GLFW.GLFW_MOUSE_BUTTON_LAST];
    private static boolean mouseButtonsPressedLast[] = new boolean[GLFW.GLFW_MOUSE_BUTTON_LAST];

    protected static float mouseX;
    protected static float mouseY;

    protected static float scrollX;
    protected static float scrollY;

    protected static void listenKeys(long window, int key, int scancode, int action, int mods) {
        if (action == GLFW.GLFW_PRESS) {
            keyPressed[key] = true;
        } else if (action == GLFW.GLFW_RELEASE) {
            keyPressed[key] = false;
        }
    }

    protected static void listenMouseButtons(long window, int button, int action, int mods) {
        if (action == GLFW.GLFW_PRESS) {
            mouseButtonsPressed[button] = true;
        } else if (action == GLFW.GLFW_RELEASE) {
            mouseButtonsPressed[button] = false;
        }
    }

    protected static void update() {
        for (int i = 32; i < GLFW.GLFW_KEY_LAST; i++) {
            keyPressedLast[i] = keyPressed[i];
        }
        for (int i = 0; i < GLFW.GLFW_MOUSE_BUTTON_LAST; i++) {
            mouseButtonsPressedLast[i] = mouseButtonsPressed[i];
        }
    }

    /**
     * Returns whether any key is currently pressed down.
     * 
     * @return true if any key is currently pressed down, false otherwise
     */
    public static boolean keysPressed() {
        for (int i = 32; i < GLFW.GLFW_KEY_LAST; i++) {
            if (keyDown(i)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether the specified key is currently pressed down.
     * 
     * @param key the key code to check
     * @return true if the key is currently pressed down, false otherwise
     */
    public static boolean keyDown(int key) {
        return keyPressed[key];
    }

    /**
     * Returns whether the specified key has been pressed down
     * 
     * @param key the key code to check
     * @return true if the key has been pressed down, false otherwise
     */
    public static boolean keyPressed(int key) {
        return keyPressed[key] && !keyPressedLast[key];
    }

    /**
     * Returns whether the specified key has been released
     * 
     * @param key the key code to check
     * @return true if the key has been released, false otherwise
     */
    public static boolean keyReleased(int key) {
        return !keyPressed[key] && keyPressedLast[key];
    }

    public static boolean mouseButtonDown(int button) {
        return mouseButtonsPressed[button];
    }

    public static boolean mouseButtonPressed(int button) {
        return mouseButtonsPressed[button] && !mouseButtonsPressedLast[button];
    }

    public static boolean mouseButtonReleased(int button) {
        return !mouseButtonsPressed[button] && mouseButtonsPressedLast[button];
    }

    /**
     * Returns the current mouse x position
     * 
     * @return the current mouse x position
     */
    public static float getMouseX() {
        return mouseX;
    }

    /**
     * Returns the current mouse y position
     * 
     * @return the current mouse y position
     */
    public static float getMouseY() {
        return mouseY;
    }

    /**
     * Returns the current mouse position as a Vector2i
     * 
     * @return the current mouse position as a Vector2i
     */
    public static Vector2f getMousePosition() {
        return new Vector2f(mouseX, mouseY);
    }

    /**
     * Returns the current scroll x position
     * 
     * @return the current scroll x position
     */
    public static float getScrollX() {
        return scrollX;
    }

    /**
     * Returns the current scroll y position
     * 
     * @return the current scroll y position
     */
    public static float getScrollY() {
        return scrollY;
    }

    /**
     * Returns the current scroll position as a Vector2i
     * 
     * @return the current scroll position as a Vector2i
     */
    public static Vector2f getScroll() {
        return new Vector2f(scrollX, scrollY);
    }

    public static int keyState(int keyCode) {
        return keyPressed[keyCode] ? 1 : 0;
    }

}
