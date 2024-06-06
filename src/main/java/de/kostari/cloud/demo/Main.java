package de.kostari.cloud.demo;

import de.kostari.cloud.core.Clogger;
import de.kostari.cloud.core.scene.SceneManager;
import de.kostari.cloud.core.window.Window;

public class Main {

    public static void main(String[] args) {
        Window window = Window.create(1280, 720, "Test");

        Clogger.DEBUG = true;
        MapScene scene = new MapScene();
        SceneManager.setScene(scene);

        window.show();
    }

}
