package lighting_demo;

import de.kostari.cloud.core.lighting.Light2D;
import de.kostari.cloud.core.lighting.LightOccluder2D;
import de.kostari.cloud.core.lighting.LightingEffect;
import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.scene.Scene;
import de.kostari.cloud.core.utils.math.Vector2;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.types.Color4f;
import de.kostari.cloud.core.window.Input;
import de.kostari.cloud.core.window.Time;
import de.kostari.cloud.core.window.Window;

public class LightingScene extends Scene {

    private LightingEffect lighting;

    @Override
    public void init() {
        lighting = Render.postProcessing().enableLighting()
                .ambientColor(0.16f, 0.22f, 0.38f)
                .ambientIntensity(0.2f);

        new Backdrop();
        new LitBlock(480, 318, 84, 250, 18, new Color4f(0.48f, 0.54f, 0.68f, 1));
        new LitBlock(690, 205, 210, 58, -12, new Color4f(0.66f, 0.34f, 0.28f, 1));
        new LitBlock(710, 470, 250, 48, 8, new Color4f(0.28f, 0.56f, 0.48f, 1));
        new LitBlock(250, 455, 135, 72, -24, new Color4f(0.62f, 0.48f, 0.24f, 1));
        new SpinningBlock(285, 235, 115, 34);

        new DemoLight(155, 150, 330, new Color4f(1f, 0.32f, 0.18f, 1), 1.45f, 1.5f, 0);
        new DemoLight(810, 355, 300, new Color4f(0.18f, 0.48f, 1f, 1), 1.35f, 2.2f, 14);
        new MouseLight();

        super.init();
    }

    @Override
    public void update() {
        Window.get().setTitle("2D Lighting | " + lighting.activeLightCount() + " lights, "
                + lighting.activeOccluderCount() + " ray-traced occluders | Move the mouse");
        super.update();
    }

    @Override
    public void dispose() {
        Render.postProcessing().remove(lighting);
        super.dispose();
    }

    private static final class Backdrop extends GameObject {

        @Override
        public void draw() {
            Render.drawRect(0, 0, 960, 640, false, new Color4f(0.32f, 0.38f, 0.5f, 1));

            Color4f lineColor = new Color4f(0.38f, 0.44f, 0.56f, 1);
            for (int x = 0; x <= 960; x += 64) {
                Render.drawRect(x, 0, 2, 640, false, lineColor);
            }
            for (int y = 0; y <= 640; y += 64) {
                Render.drawRect(0, y, 960, 2, false, lineColor);
            }
            super.draw();
        }
    }

    private static class LitBlock extends GameObject {

        private final float width;
        private final float height;
        private final Color4f color;

        LitBlock(float x, float y, float width, float height, float rotation, Color4f color) {
            this.width = width;
            this.height = height;
            this.color = color;
            transform.position.set(x, y);
            transform.rotation = rotation;
            addComponent(new LightOccluder2D(width, height));
        }

        @Override
        public void draw() {
            Render.drawRotatedRect(
                    transform.position.x,
                    transform.position.y,
                    width,
                    height,
                    true,
                    color,
                    transform.rotation);
            super.draw();
        }
    }

    private static final class SpinningBlock extends LitBlock {

        SpinningBlock(float x, float y, float width, float height) {
            super(x, y, width, height, 0, new Color4f(0.62f, 0.42f, 0.74f, 1));
        }

        @Override
        public void update() {
            transform.rotation += 35 * Time.delta;
            super.update();
        }
    }

    private static class DemoLight extends GameObject {

        private final Color4f color;

        DemoLight(float x, float y, float radius, Color4f color,
                float intensity, float falloff, float softness) {
            this.color = color;
            transform.position.set(x, y);
            addComponent(new Light2D(radius, color)
                    .intensity(intensity)
                    .falloff(falloff)
                    .softness(softness));
        }

        @Override
        public void draw() {
            Render.drawRotatedRect(
                    transform.position.x,
                    transform.position.y,
                    12,
                    12,
                    true,
                    color,
                    45);
            super.draw();
        }
    }

    private static final class MouseLight extends DemoLight {

        MouseLight() {
            super(480, 320, 260, new Color4f(1, 0.92f, 0.72f, 1), 1.25f, 1.7f, 10);
        }

        @Override
        public void update() {
            Vector2 mouse = Input.getWorldMousePosition();
            transform.position.set(mouse);
            super.update();
        }
    }
}
