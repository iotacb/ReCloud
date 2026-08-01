package tower_climber;

import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.types.Color4f;
import de.kostari.cloud.core.window.Time;

public final class AstralShockwave extends GameObject {

    private static final float DURATION = 0.9f;
    private float age;

    public AstralShockwave(float x, float y) {
        transform.position.set(x, y);
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
        float eased = 1 - (1 - progress) * (1 - progress) * (1 - progress);
        float fade = (1 - progress) * (1 - progress);
        float radius = 34 + eased * 760;
        Color4f outer = new Color4f(0.72f, 0.38f, 1f, fade * 0.19f);
        Color4f inner = new Color4f(0.55f, 1f, 0.93f, fade * 0.42f);
        Render.drawRotatedRect(transform.position.x, transform.position.y,
                radius, radius, true, outer, 45 + progress * 120);
        Render.drawRotatedRect(transform.position.x, transform.position.y,
                radius * 0.72f, radius * 0.72f, true,
                new Color4f(0.02f, 0.03f, 0.12f, fade * 0.34f), 45 - progress * 90);
        int rays = 20;
        for (int i = 0; i < rays; i++) {
            float angle = i * 360f / rays + progress * 18;
            double radians = Math.toRadians(angle);
            float x = transform.position.x + (float) Math.cos(radians) * radius * 0.48f;
            float y = transform.position.y + (float) Math.sin(radians) * radius * 0.48f;
            Render.drawRotatedRect(x, y, radius * 0.11f, 3 + fade * 4,
                    true, inner, angle);
        }
        super.draw();
    }
}
