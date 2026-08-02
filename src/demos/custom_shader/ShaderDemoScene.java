package custom_shader;

import de.kostari.cloud.core.scene.Scene;
import de.kostari.cloud.core.ui.Absolute;
import de.kostari.cloud.core.ui.Canvas;
import de.kostari.cloud.core.ui.Flex;
import de.kostari.cloud.core.ui.FlexDirection;
import de.kostari.cloud.core.ui.Text;
import de.kostari.cloud.core.utils.Colors;
import de.kostari.cloud.core.utils.input.Keys;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.types.Color4f;
import de.kostari.cloud.core.window.Input;
import de.kostari.cloud.core.window.Time;
import de.kostari.cloud.core.window.Window;
import demo_ui.DemoUI;

public class ShaderDemoScene extends Scene {

    private static final Color4f BACKGROUND = new Color4f(0.025f, 0.035f, 0.075f, 1.0f);
    private static final Color4f GRID = new Color4f(0.12f, 0.25f, 0.38f, 0.35f);
    private static final Color4f CYAN = new Color4f(0.18f, 0.90f, 0.95f, 1.0f);
    private static final Color4f PINK = new Color4f(1.0f, 0.22f, 0.62f, 1.0f);
    private static final Color4f GOLD = new Color4f(1.0f, 0.72f, 0.18f, 1.0f);

    private InteractiveShaderEffect effect;
    private Text status;

    @Override
    public void init() {
        effect = Render.postProcessing().add(new InteractiveShaderEffect());
        createHud();
        super.init();
    }

    @Override
    public void update() {
        if (Input.keyPressed(Keys.KEY_1)) {
            effect.mode(0);
        }
        if (Input.keyPressed(Keys.KEY_2)) {
            effect.mode(1);
        }
        if (Input.keyPressed(Keys.KEY_3)) {
            effect.mode(2);
        }
        if (Input.keyPressed(Keys.KEY_SPACE)) {
            effect.setEnabled(!effect.isEnabled());
        }
        if (Input.keyPressed(Keys.KEY_R)) {
            effect.reset();
        }

        float direction = 0.0f;
        if (Input.keyDown(Keys.KEY_UP) || Input.keyDown(Keys.KEY_RIGHT)) {
            direction += 1.0f;
        }
        if (Input.keyDown(Keys.KEY_DOWN) || Input.keyDown(Keys.KEY_LEFT)) {
            direction -= 1.0f;
        }
        effect.strength(effect.getStrength() + direction * Time.delta * 0.7f);

        status.text(String.format(
                "Mode: %s\nStrength: %.0f%%\nShader: %s\nMouse: %d, %d",
                effect.getModeName(),
                effect.getStrength() * 100.0f,
                effect.isEnabled() ? "on" : "off",
                Input.getMouseX(),
                Input.getMouseY()));

        super.update();
    }

    @Override
    public void draw() {
        int width = Window.get().getWidth();
        int height = Window.get().getHeight();
        float centerX = width * 0.5f;
        float centerY = height * 0.5f;
        float time = Time.timePassed;

        Render.drawRect(0, 0, width, height, BACKGROUND);
        drawGrid(width, height);

        Render.drawRotatedRect(centerX, centerY, 330, 330, true,
                new Color4f(0.08f, 0.16f, 0.28f, 1.0f), time * 8.0f);
        Render.drawRotatedRect(centerX, centerY, 245, 245, true,
                new Color4f(0.04f, 0.07f, 0.14f, 1.0f), -time * 13.0f);

        drawOrbit(centerX, centerY, 245, time * 0.75f, CYAN, 50);
        drawOrbit(centerX, centerY, 170, time * -1.1f + 2.1f, PINK, 40);
        drawOrbit(centerX, centerY, 110, time * 1.45f + 4.2f, GOLD, 32);

        super.draw();
    }

    private void drawGrid(int width, int height) {
        for (int x = 0; x <= width; x += 48) {
            Render.drawRect(x, 0, 1, height, GRID);
        }
        for (int y = 0; y <= height; y += 48) {
            Render.drawRect(0, y, width, 1, GRID);
        }
    }

    private void drawOrbit(float centerX, float centerY, float radius, float angle, Color4f color, float size) {
        float x = centerX + (float) Math.cos(angle) * radius;
        float y = centerY + (float) Math.sin(angle) * radius;
        Render.drawRotatedRect(x, y, size, size, true, color, angle * 57.2958f);
    }

    private void createHud() {
        Canvas canvas = new Canvas();
        Absolute overlay = new Absolute();
        Flex hud = new Flex(FlexDirection.COLUMN);
        hud.layout().padding(16);
        hud.gap(9).background(DemoUI.surface(DemoUI.CYAN, 12));

        Text title = new Text("CUSTOM SHADER LAB");
        title.fontScale(1.25f).color(Colors.hex("#67e8f9")).shadow(2);

        Text help = new Text(
                "Move mouse   Move effect\n"
                        + "Left click   Boost effect\n"
                        + "1 / 2 / 3    Change mode\n"
                        + "Arrow keys   Change strength\n"
                        + "Space        Toggle shader\n"
                        + "R            Reset");
        help.color(Colors.hex("#dbeafe")).lineHeight(1.25f).shadow(1);

        status = new Text("");
        status.layout().padding(9);
        status.background(Colors.hex("#0e749055")).color(Colors.hex("#a5f3fc")).lineHeight(1.2f);

        hud.add(title, help, status);
        overlay.add(hud);
        overlay.position(hud).left(18).top(18).width(500);
        canvas.add(overlay);
    }
}
