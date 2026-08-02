package balls_demo;

import de.kostari.cloud.core.events.EventInfo;
import de.kostari.cloud.core.scene.Scene;
import de.kostari.cloud.core.ui.Absolute;
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
import demo_ui.DemoUI;

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
        Absolute overlay = new Absolute();
        overlay.add(hud);
        overlay.position(hud).left(18).top(18).width(270);
        canvas.add(overlay);
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
        hud.layout().padding(14);
        hud.gap(10).background(DemoUI.surface(DemoUI.CYAN, 12));

        Text title = new Text("Balls Demo");
        title.fontScale(1.2f).color(Colors.hex("#67e8f9")).shadow(2);

        statsText = new Text("");
        statsText.color(Colors.hex("#ffffff")).shadow(1);

        Button addButton = DemoUI.button("Add 100", DemoUI.CYAN, () -> spawnBalls(100));
        hud.add(title, statsText, addButton);
    }

    private void spawnBalls(int count) {
        for (int i = 0; i < count; i++) {
            new Ball().transform.position = Input.getWorldMousePosition();
        }
    }
}
