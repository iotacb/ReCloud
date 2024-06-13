package flappy_bird_clone;

import de.kostari.cloud.core.scene.SceneManager;
import de.kostari.cloud.core.window.Window;

public class Main {

    public static void main(String[] args) {
        Window window = Window.create(600, 900, "Flappy Bird Clone");
        window.setCenterOnStart(false);
        GameScene scene = new GameScene();
        SceneManager.setScene(scene);
        window.show();
    }

}
