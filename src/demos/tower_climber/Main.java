package tower_climber;

import de.kostari.cloud.core.scene.SceneManager;
import de.kostari.cloud.core.window.Window;
import tower_climber.main_menu.MainMenuScene;

public class Main {

    public static void main(String[] args) {
        Window window = Window.create(1280, 720, "Tower Climber");
        window.setResizable(true);

        SceneManager.setScene(MainMenuScene.class);

        window.show();
    }

}
