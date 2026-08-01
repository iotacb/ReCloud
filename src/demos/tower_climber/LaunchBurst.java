package tower_climber;

import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.types.Color4f;
import de.kostari.cloud.core.window.Time;

public final class LaunchBurst extends GameObject {

    private static final float DURATION = 0.62f;

    private float age;

    public LaunchBurst(float x, float y) {
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
        float fade = 1 - progress;
        for (int i = -3; i <= 3; i++) {
            float lane = i / 3f;
            float stagger = Math.floorMod(i * 17, 31) / 31f;
            float travel = Math.min(1, progress * 1.4f + stagger * 0.24f);
            float x = transform.position.x + lane * (20 + travel * 48);
            float y = transform.position.y - 18 - travel * (105 + Math.abs(i) * 10);
            Color4f streak = new Color4f(1f, 0.91f, 0.38f, fade * (0.18f + (3 - Math.abs(i)) * 0.035f));
            Render.drawRotatedRect(x, y, 4 + fade * 3, 34 + fade * 38,
                    true, streak, lane * 9);
        }

        float ringSize = 32 + progress * 116;
        Render.drawRotatedRect(transform.position.x, transform.position.y - progress * 32,
                ringSize, ringSize, true,
                new Color4f(1f, 0.88f, 0.3f, fade * 0.055f), 45 + progress * 35);
        super.draw();
    }
}
