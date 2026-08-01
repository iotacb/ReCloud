package tower_climber;

import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.types.Color4f;
import de.kostari.cloud.core.window.Time;

public final class StompShockwave extends GameObject {

    private static final float DURATION = 0.46f;

    private final Color4f color;
    private final int power;
    private float age;

    public StompShockwave(float x, float y, Color4f color, int combo) {
        transform.position.set(x, y);
        this.color = color;
        power = Math.max(1, combo);
    }

    @Override
    public void update() {
        age += Math.min(Time.delta, 0.05f);
        if (age >= DURATION) {
            destroy();
        }
        super.update();
    }

    @Override
    public void draw() {
        float progress = Math.min(1, age / DURATION);
        float eased = 1 - (1 - progress) * (1 - progress);
        float fade = (1 - progress) * (1 - progress);
        float span = 28 + eased * (122 + Math.min(70, power * 9));
        Color4f line = new Color4f(color.r, color.g, color.b, fade * 0.48f);
        Color4f glow = new Color4f(color.r, color.g, color.b, fade * 0.075f);

        Render.drawRect(transform.position.x, transform.position.y,
                span * 2, 4 + fade * 3, true, line);
        Render.drawRect(transform.position.x, transform.position.y + 7,
                span * 1.35f, 2, true, new Color4f(color.r, color.g, color.b, fade * 0.25f));
        Render.drawRotatedRect(transform.position.x, transform.position.y - eased * 10,
                28 + eased * 104, 28 + eased * 104, true, glow, 45);

        for (int side = -1; side <= 1; side += 2) {
            for (int i = 0; i < 3; i++) {
                float offset = span * (0.62f + i * 0.16f);
                float y = transform.position.y - 4 - i * 6 - eased * (8 + i * 3);
                Render.drawRotatedRect(transform.position.x + side * offset, y,
                        3, 18 + power * 1.5f, true, line, side * (58 + i * 7));
            }
        }
        super.draw();
    }
}
