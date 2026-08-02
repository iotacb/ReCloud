package physics_demo;

import de.kostari.cloud.core.scene.Scene;
import de.kostari.cloud.core.ui.Absolute;
import de.kostari.cloud.core.ui.Canvas;
import de.kostari.cloud.core.ui.Panel;
import de.kostari.cloud.core.ui.Text;
import de.kostari.cloud.core.utils.Colors;
import de.kostari.cloud.core.window.Window;
import demo_ui.DemoUI;

public class PhysicsScene extends Scene {

    private Canvas canvas;
    private Text fpsText;

    @Override
    public void init() {
        physics().gravity(0, 1100).substeps(4);

        new Player(160, 120);

        new PhysicsBox(480, 610, 960, 60, true, Colors.WHITE);
        new PhysicsBox(20, 320, 40, 640, true, Colors.WHITE);
        new PhysicsBox(940, 320, 40, 640, true, Colors.WHITE);
        new PhysicsBox(450, 470, 260, 24, true, Colors.CORAL);
        new PhysicsBox(720, 350, 220, 24, true, Colors.CORAL);

        for (int i = 0; i < 6; i++) {
            PhysicsBox box = new PhysicsBox(500 + i * 48, 100, 38, 38, false, Colors.random());
            box.body.bounce(0.1f).friction(0.7f);
        }

        canvas = new Canvas();
        fpsText = new Text("FPS: 0");
        fpsText.color(Colors.hex("#ffffff")).shadow(1);
        Panel badge = DemoUI.badge(DemoUI.GREEN);
        badge.add(fpsText);
        Absolute overlay = new Absolute();
        overlay.add(badge);
        overlay.position(badge).left(16).top(16).width(150);
        canvas.add(overlay);

        super.init();
    }

    @Override
    public void update() {
        Window.get().setTitle("AABB Physics Demo | A/D: move | Space: jump");
        fpsText.text("FPS: " + Math.round(Window.get().getFPS()));
        super.update();
    }
}
