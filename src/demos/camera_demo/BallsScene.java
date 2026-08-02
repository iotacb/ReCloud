package camera_demo;

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
import demo_ui.DemoUI;

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
        Absolute overlay = new Absolute();
        overlay.add(hud);
        overlay.position(hud).left(18).top(18).width(310);
        canvas.add(overlay);
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
        hud.layout().padding(14);
        hud.gap(10).background(DemoUI.surface(DemoUI.CYAN, 12));

        Text title = new Text("Camera Demo");
        title.fontScale(1.25f).color(Colors.hex("#67e8f9")).shadow(2);

        statsText = new Text("");
        statsText.color(Colors.hex("#e0f2fe")).shadow(1);

        Flex actions = new Flex(FlexDirection.ROW);
        actions.gap(8);

        Button burstButton = DemoUI.button("Burst 25", DemoUI.CYAN, () -> spawnBalls(25));
        burstButton.layout().grow(1);

        Button centerButton = DemoUI.button("Center", DemoUI.VIOLET, () -> {
            getCamera().setPosition(0, 0);
            getCamera().setZoom(1);
        });
        centerButton.layout().grow(1);

        actions.add(burstButton, centerButton);
        hud.add(title, statsText, actions);
    }

    private void spawnBalls(int count) {
        for (int i = 0; i < count; i++) {
            new Ball().transform.position = Input.getWorldMousePosition();
        }
    }
}
