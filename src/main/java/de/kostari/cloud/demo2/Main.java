package de.kostari.cloud.demo2;

import de.kostari.cloud.core.scene.SceneManager;
import de.kostari.cloud.core.window.Window;

public class Main {

    public static void main(String[] args) {
        Window window = Window.create(800, 600, "Demo 2 - Cloud Core");

        DemoScene scene = new DemoScene();
        SceneManager.setScene(scene);

        window.show();
    }

}
