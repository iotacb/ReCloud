package de.kostari.cloud.core.scene;

import java.util.ArrayList;

import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.utils.render.Render;

public class Scene {

    private ArrayList<GameObject> gameObjects = new ArrayList<>();

    public boolean isInitialized;

    private Camera camera;

    /**
     * Called when the scene is loaded.
     */
    public void init() {
        this.camera = new Camera(0, 0);
        this.isInitialized = true;
    }

    /**
     * Called every frame.
     */
    public void update() {
        for (int i = 0; i < gameObjects.size(); i++) {
            GameObject gameObject = gameObjects.get(i);
            gameObject.update();
        }
    }

    /**
     * Called every frame after update.
     */
    public void draw() {
        // camera.applyTransforms();
        for (int i = 0; i < gameObjects.size(); i++) {
            GameObject gameObject = gameObjects.get(i);
            gameObject.draw();
        }
        Render.flush();
        // camera.resetTransforms();
    }

    /**
     * Called when the scene is unloaded.
     */
    public void dispose() {
        this.isInitialized = false;
    }

    /**
     * Adds one or more game objects to the scene.
     * 
     * @param objects The game object(s) to add
     */
    public void addGameObjects(GameObject... objects) {
        for (int i = 0; i < objects.length; i++) {
            GameObject gameObject = objects[i];
            gameObjects.add(gameObject);
        }
    }

    /**
     * Removes one or more game objects from the scene.
     * 
     * @param objects The game object(s) to remove
     */
    public void removeGameObjects(GameObject... objects) {
        for (int i = 0; i < objects.length; i++) {
            GameObject gameObject = objects[i];
            gameObject.dispose();
            gameObjects.remove(gameObject);
        }
    }

    public ArrayList<GameObject> getGameObjects() {
        return gameObjects;
    }

    public Camera getCamera() {
        return camera;
    }

}
