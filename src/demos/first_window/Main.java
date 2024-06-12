package first_window;

import de.kostari.cloud.core.scene.EmptyScene;
import de.kostari.cloud.core.scene.SceneManager;
import de.kostari.cloud.core.window.Window;

/**
 * Main class of the application.
 * This class is responsible for initializing and displaying the main window of
 * the application.
 */
public class Main {

    /**
     * The entry point of the application.
     * 
     * @param args Command line arguments (not used in this application).
     */
    public static void main(String[] args) {
        // Create a new window with a width of 1280 pixels, a height of 720 pixels, and
        // a title "My First Window".
        Window window = Window.create(1280, 720, "My First Window");

        // Create a new instance of EmptyScene. This represents the initial scene to be
        // displayed in the window.
        EmptyScene scene = new EmptyScene();

        // Set the current scene of the application to the newly created EmptyScene.
        SceneManager.setScene(scene);

        // Make the window visible to the user.
        window.show();
    }

}
