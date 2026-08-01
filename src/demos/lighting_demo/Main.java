package lighting_demo;

import de.kostari.cloud.core.scene.SceneManager;
import de.kostari.cloud.core.window.Window;

public class Main {

    public static void main(String[] args) {
        Window window = Window.create(960, 640, "2D Lighting | Move the mouse to aim the white light");
        window.setResizable(true);
        window.setClearColor(0.025f, 0.035f, 0.06f, 1);
        SceneManager.setScene(new LightingScene());
        window.show();
    }
}
