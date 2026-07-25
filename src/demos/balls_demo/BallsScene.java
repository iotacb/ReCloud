package balls_demo;

import de.kostari.cloud.core.events.EventInfo;
import de.kostari.cloud.core.scene.Scene;
import de.kostari.cloud.core.ui.Button;
import de.kostari.cloud.core.ui.Canvas;
import de.kostari.cloud.core.ui.Flex;
import de.kostari.cloud.core.ui.FlexDirection;
import de.kostari.cloud.core.ui.Text;
import de.kostari.cloud.core.utils.Colors;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.window.Input;
import de.kostari.cloud.core.window.Window;
import de.kostari.cloud.core.window.WindowEvents;

public class BallsScene extends Scene {

    private static final int BALLS = 2000;
    private static final int BOUNDS_THICKNESS = 4;
    private Canvas canvas;
    private Flex hud;
    private Text statsText;

    @Override
    public void init() {
        for (int i = 0; i < BALLS; i++) {
            new Ball();
        }

        canvas = new Canvas();
        createHud();
        canvas.append(hud, 16, 16, 260, Canvas.AUTO);
        WindowEvents.onMouseScroll.join(this, new EventInfo("zoomCam"));
        super.init();
    }

    float lastMouseX = 0;
    float lastMouseY = 0;

    @Override
    public void update() {
        Window.get().setTitle(
                "Balls demo - " + getGameObjects().size() + " balls" + " - " + Window.get().getFPS() + " fps");
        statsText.text("Balls: " + getGameObjects().size() + "\nFPS: " + Math.round(Window.get().getFPS()));
        if (Input.mouseButtonDown(0) && !hud.bounds().contains(Input.getMouseX(), Input.getMouseY())) {
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

    private void createHud() {
        hud = new Flex(FlexDirection.COLUMN);
        hud.style().css("padding: 14px; gap: 10px; background: #111827cc; border: 1px solid #22d3ee66;");

        Text title = new Text("Balls Demo");
        title.style().css("font-scale: 1.2; color: #67e8f9; shadow-depth: 2px;");

        statsText = new Text("");
        statsText.style().css("color: white; shadow-depth: 1px;");

        Button addButton = new Button("Add 100").onClick(() -> spawnBalls(100));
        hud.add(title, statsText, addButton);
    }

    private void spawnBalls(int count) {
        for (int i = 0; i < count; i++) {
            new Ball().transform.position = Input.getWorldMousePosition();
        }
    }
}
