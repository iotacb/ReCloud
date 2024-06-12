package balls_demo;

import de.kostari.cloud.core.scene.SceneManager;
import de.kostari.cloud.core.window.Window;

public class Main {

    public static void main(String[] args) {
        Window window = Window.create(1280, 720, "Balls demo");
        SceneManager.setScene(BallsScene.class);
        window.show();
    }

}
