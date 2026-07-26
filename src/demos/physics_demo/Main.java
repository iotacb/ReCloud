package physics_demo;

import de.kostari.cloud.core.scene.SceneManager;
import de.kostari.cloud.core.window.Window;

public class Main {

    public static void main(String[] args) {
        Window window = Window.create(960, 640, "AABB Physics Demo");
        SceneManager.setScene(PhysicsScene.class);
        window.show();
    }
}
