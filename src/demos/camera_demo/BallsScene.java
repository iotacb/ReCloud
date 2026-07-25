package camera_demo;

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

public class BallsScene extends Scene {

    private static final int BALLS = 2000;
    private static final int BOUNDS_THICKNESS = 4;

    private Player cursor;
    private Canvas canvas;
    private Flex hud;
    private Text statsText;

    @Override
    public void init() {
        for (int i = 0; i < BALLS; i++) {
            new Ball();
        }

        this.cursor = new Player();
        canvas = new Canvas();
        createHud();
        canvas.append(hud, 16, 16, 300, Canvas.AUTO);
        super.init();
    }

    @Override
    public void update() {
        Window.get().setTitle(
                "Camera demo - " + getGameObjects().size() + " balls" + " - " + Window.get().getFPS() + " fps");
        statsText.text("Balls: " + getGameObjects().size()
                + "\nFPS: " + Math.round(Window.get().getFPS())
                + "\nZoom: " + String.format("%.2f", getCamera().getZoom()));

        if (Input.mouseButtonDown(0) && !hud.bounds().contains(Input.getMouseX(), Input.getMouseY())) {
            new Ball().transform.position = Input.getWorldMousePosition();
        }

        getCamera().drag(2);
        getCamera().handleScrolling(0.1f);

        if (!getCamera().isDragging())
            getCamera().followObject(cursor, .05f);

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
        hud.style().css(
                "padding: 14px; gap: 10px; background: #07111fcc; border: 1px solid #67e8f966; color: white;");

        Text title = new Text("Camera Demo");
        title.style().css("font-scale: 1.25; color: #67e8f9; shadow-depth: 2px;");

        statsText = new Text("");
        statsText.style().css("color: #e0f2fe; shadow-depth: 1px;");

        Flex actions = new Flex(FlexDirection.ROW);
        actions.style().css("gap: 8px;");

        Button burstButton = new Button("Burst 25").onClick(() -> spawnBalls(25));
        burstButton.style().css("grow: 1;");

        Button centerButton = new Button("Center").onClick(() -> {
            getCamera().setPosition(0, 0);
            getCamera().setZoom(1);
        });
        centerButton.style().css("grow: 1;");

        actions.add(burstButton, centerButton);
        hud.add(title, statsText, actions);
    }

    private void spawnBalls(int count) {
        for (int i = 0; i < count; i++) {
            new Ball().transform.position = Input.getWorldMousePosition();
        }
    }
}
