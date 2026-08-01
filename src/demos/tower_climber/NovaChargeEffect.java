package tower_climber;

import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.types.Color4f;
import de.kostari.cloud.core.window.Time;

public final class NovaChargeEffect extends GameObject {

    private final Player player;
    private float progress;

    public NovaChargeEffect(Player player) {
        this.player = player;
    }

    @Override
    public void update() {
        transform.position.set(player.transform.position);
        if (!player.isChanneling()) {
            destroy();
        }
        super.update();
    }

    @Override
    public void draw() {
        float eased = progress * progress * (3 - 2 * progress);
        float pulse = 0.5f + 0.5f * (float) Math.sin(Time.timePassed * (12 + progress * 28));
        float radius = 78 - eased * 48 + pulse * 4;
        Color4f energy = new Color4f(0.72f, 0.36f, 1f, 0.22f + eased * 0.5f);
        Color4f core = new Color4f(0.72f, 1f, 0.96f, 0.08f + eased * 0.16f);
        Render.drawRotatedRect(transform.position.x, transform.position.y,
                42 + eased * 28, 42 + eased * 28, true, core,
                45 + Time.timePassed * 95);
        int motes = 14;
        for (int i = 0; i < motes; i++) {
            float angle = i / (float) motes * (float) (Math.PI * 2)
                    - Time.timePassed * (1.5f + eased * 4);
            float x = transform.position.x + (float) Math.cos(angle) * radius;
            float y = transform.position.y + (float) Math.sin(angle) * radius;
            Render.drawRotatedRect(x, y, 4 + eased * 3, 13 + eased * 8,
                    true, energy, (float) Math.toDegrees(angle) + 90);
        }
        super.draw();
    }

    public void progress(float progress) {
        this.progress = Math.max(0, Math.min(1, progress));
    }
}
