package custom_shader;

import de.kostari.cloud.core.scene.SceneManager;
import de.kostari.cloud.core.window.Window;

public class Main {

    public static void main(String[] args) {
        Window window = Window.create(1280, 720, "Custom Shader Demo");
        window.setResizable(true);

        SceneManager.setScene(new ShaderDemoScene());
        window.show();
    }
}
