package de.kostari.cloud.core.scene;

import java.util.ArrayList;

import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.ui.Canvas;
import de.kostari.cloud.core.utils.render.Render;

public class Scene {

    private ArrayList<GameObject> gameObjects = new ArrayList<>();
    private ArrayList<GameObject> gameObjectsToDestroy = new ArrayList<>();
    private ArrayList<Canvas> canvases = new ArrayList<>();

    public boolean isInitialized;

    private Camera camera;

    /**
     * Called when the scene is loaded.
     */
    public void init() {
        if (camera == null) {
            initCamera();
        }
        this.isInitialized = true;
    }

    public void initCamera() {
        camera = new Camera();
    }

    /**
     * Called every frame.
     */
    public void update() {
        for (int i = 0; i < gameObjects.size(); i++) {
            GameObject gameObject = gameObjects.get(i);
            gameObject.update();

            if (gameObject.canBeDestroyed) {
                gameObjectsToDestroy.add(gameObject);
            }
        }

        for (int i = 0; i < gameObjectsToDestroy.size(); i++) {
            GameObject gameObject = gameObjectsToDestroy.get(i);
            gameObjects.remove(gameObject);
        }
        gameObjectsToDestroy.clear();

        if (camera != null) {
            camera.update();
        }
    }

    /**
     * Called every frame after update.
     */
    public void draw() {
        for (int i = 0; i < gameObjects.size(); i++) {
            GameObject gameObject = gameObjects.get(i);
            gameObject.draw();
        }
        Render.flush();
    }

    /**
     * Called when the scene is unloaded.
     */
    public void dispose() {
        ArrayList<Canvas> canvasesToDispose = new ArrayList<>(canvases);
        for (Canvas canvas : canvasesToDispose) {
            canvas.dispose();
        }
        canvases.clear();
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

    public void registerCanvas(Canvas canvas) {
        if (canvas != null && !canvases.contains(canvas)) {
            canvases.add(canvas);
        }
    }

    public void unregisterCanvas(Canvas canvas) {
        canvases.remove(canvas);
    }
}
