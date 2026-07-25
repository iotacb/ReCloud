package ui_system;

import de.kostari.cloud.core.scene.SceneManager;
import de.kostari.cloud.core.window.Window;

public class Main {

    public static void main(String[] args) {
        Window window = Window.create(1180, 760, "UI System Demo");
        window.setResizable(true);
        SceneManager.setScene(UISystemScene.class);
        window.show();
    }
}
