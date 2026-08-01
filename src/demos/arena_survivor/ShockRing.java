package arena_survivor;

import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.types.Color4f;
import de.kostari.cloud.core.window.Time;

final class ShockRing extends GameObject {
    private final Color4f color;
    private final float maxRadius;
    private final float duration;
    private float age;

    ShockRing(float x, float y, Color4f color, float maxRadius, float duration) {
        transform.position.set(x, y);
        this.color = color;
        this.maxRadius = maxRadius;
        this.duration = duration;
    }

    @Override
    public void update() {
        age += Math.min(Time.delta, 0.05f);
        if (age >= duration) {
            destroy();
        }
        super.update();
    }

    @Override
    public void draw() {
        float progress = Math.clamp(age / duration, 0, 1);
        float eased = 1 - (1 - progress) * (1 - progress);
        float radius = 12 + maxRadius * eased;
        float fade = (1 - progress) * (1 - progress);
        Color4f line = new Color4f(color.r, color.g, color.b, fade * 0.75f);
        Color4f glow = new Color4f(color.r, color.g, color.b, fade * 0.12f);

        Render.drawRotatedRect(transform.position.x, transform.position.y,
                radius * 1.42f, radius * 1.42f, true, glow, 45);
        Render.drawRotatedRect(transform.position.x, transform.position.y,
                radius, radius, true, Palette.alpha(Palette.VOID, fade * 0.22f), 45);
        float thickness = 3 + fade * 5;
        for (int i = 0; i < 4; i++) {
            float angle = 45 + i * 90;
            float radians = (float) Math.toRadians(angle);
            float x = transform.position.x + (float) Math.cos(radians) * radius * 0.71f;
            float y = transform.position.y + (float) Math.sin(radians) * radius * 0.71f;
            Render.drawRotatedRect(x, y, radius, thickness, true, line, angle + 45);
        }
        super.draw();
    }
}
