package arena_survivor;

import de.kostari.cloud.core.scene.SceneManager;
import de.kostari.cloud.core.window.Window;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        Window window = Window.create(1280, 720, "Aether Swarm");
        window.setResizable(true);
        window.setCenterOnStart(true);
        SceneManager.setScene(ArenaScene.class);
        window.show();
    }
}
