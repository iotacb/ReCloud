package tower_climber;

import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.types.Color4f;
import de.kostari.cloud.core.window.Time;

public final class SlashEffect extends GameObject {

    private static final float DURATION = 0.22f;

    private final float aimX;
    private final float aimY;
    private final boolean hit;
    private float age;

    public SlashEffect(float x, float y, float facing, boolean hit) {
        this(x, y, facing, 0, hit);
    }

    public SlashEffect(float x, float y, float aimX, float aimY, boolean hit) {
        transform.position.set(x, y);
        float length = Math.max(0.001f, (float) Math.sqrt(aimX * aimX + aimY * aimY));
        this.aimX = aimX / length;
        this.aimY = aimY / length;
        this.hit = hit;
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
        float fade = 1 - progress;
        float radius = 25 + progress * 27;
        Color4f core = hit
                ? new Color4f(0.88f, 0.62f, 1f, fade * 0.9f)
                : new Color4f(0.56f, 1f, 0.92f, fade * 0.78f);
        Color4f glow = new Color4f(core.r, core.g, core.b, fade * 0.12f);
        float baseAngle = (float) Math.atan2(aimY, aimX);

        for (int i = 0; i < 7; i++) {
            float angle = baseAngle - 1.02f + i * 0.34f;
            float x = transform.position.x + (float) Math.cos(angle) * radius;
            float y = transform.position.y + (float) Math.sin(angle) * radius;
            float rotation = (float) Math.toDegrees(angle) + 90;
            Render.drawRotatedRect(x, y, 4 + fade * 2, 19 + fade * 10,
                    true, core, rotation);
        }
        Render.drawRotatedRect(transform.position.x + aimX * (35 + progress * 9),
                transform.position.y + aimY * (35 + progress * 9),
                44 + progress * 42, 44 + progress * 42,
                true, glow, 45 + (float) Math.toDegrees(baseAngle));
        super.draw();
    }
}
