package drawing_stuff;

import de.kostari.cloud.core.scene.SceneManager;
import de.kostari.cloud.core.window.Window;

public class Main {

    public static void main(String[] args) {
        Window window = Window.create(1280, 720, "Drawing stuff!");

        MyScene scene = new MyScene();
        SceneManager.setScene(scene);

        window.show();
    }

}
