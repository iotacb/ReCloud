package balls_demo;

import de.kostari.cloud.core.events.EventInfo;
import de.kostari.cloud.core.scene.Scene;
import de.kostari.cloud.core.utils.Colors;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.window.Input;
import de.kostari.cloud.core.window.Window;
import de.kostari.cloud.core.window.WindowEvents;

public class BallsScene extends Scene {

    private static final int BALLS = 2000;
    private static final int BOUNDS_THICKNESS = 4;

    @Override
    public void init() {
        for (int i = 0; i < BALLS; i++) {
            new Ball();
        }

        WindowEvents.onMouseScroll.join(this, new EventInfo("zoomCam"));
        super.init();
    }

    float lastMouseX = 0;
    float lastMouseY = 0;

    @Override
    public void update() {
        Window.get().setTitle(
                "Balls demo - " + getGameObjects().size() + " balls" + " - " + Window.get().getFPS() + " fps");
        if (Input.mouseButtonDown(0)) {
            new Ball().transform.position = Input.getWorldMousePosition();
        }
        super.update();
    }

    @Override
    public void draw() {
        // TOP
        Render.drawRect(0, 0, Window.get().getWidth(), BOUNDS_THICKNESS, false, Colors.CYAN);
        // BOTTOM
        Render.drawRect(0, Window.get().getHeight() - BOUNDS_THICKNESS, Window.get().getWidth(), BOUNDS_THICKNESS,
                false, Colors.CYAN);
        // LEFT
        Render.drawRect(0, 0, BOUNDS_THICKNESS, Window.get().getHeight(), false, Colors.CYAN);
        // RIGHT
        Render.drawRect(Window.get().getWidth() - BOUNDS_THICKNESS, 0, BOUNDS_THICKNESS, Window.get().getHeight(),
                false, Colors.CYAN);

        Render.drawRect(Window.get().getCenter().x, Window.get().getCenter().y, 200, 200, true, Colors.RED);
        super.draw();
    }

}
