package de.kostari.cloud.core.scene;

import de.kostari.cloud.core.window.Window;

public class SceneManager {

    private static Scene currentScene;
    private static Scene lastScene;

    /**
     * Change the current displayed scene.
     * 
     * @param scene The scene to display
     */
    public static void setScene(Scene scene) {
        if (currentScene != null) {
            lastScene = currentScene;
            currentScene = null;
            lastScene.dispose();
        }
        currentScene = scene;
        if (!currentScene.isInitialized && Window.instance.isInitialized())
            currentScene.init();
    }

    /**
     * Returns the current displayed scene.
     * 
     * @return The current scene
     */
    public static Scene current() {
        if (currentScene == null)
            throw new NullPointerException("No scene set!");
        return currentScene;
    }

    /**
     * Returns true if a scene is used
     * 
     * @return
     */
    public static boolean hasScene() {
        return currentScene != null;
    }
}
