package tower_climber;

import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.lighting.Light2D;
import de.kostari.cloud.core.physics.AABB;
import de.kostari.cloud.core.utils.Colors;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.types.Color4f;
import de.kostari.cloud.core.window.Time;

/** A separately collected casting resource dropped by defeated enemies. */
public final class AetherOrb extends GameObject {

    private final int value;
    private final float phase;
    private float velocityX;
    private float velocityY;
    private float age;
    private boolean collected;
    private final Light2D light;

    public AetherOrb(float x, float y, float velocityX, float velocityY, int value, float phase) {
        transform.position.set(x, y);
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.value = Math.max(1, value);
        this.phase = phase;
        light = addComponent(new Light2D(92, new Color4f(0.69f, 0.34f, 1f, 1))
                .intensity(0.82f)
                .falloff(2.15f)
                .castsShadows(false));
    }

    @Override
    public void update() {
        float delta = Math.min(Time.delta, 0.05f);
        age += delta;
        velocityY += 300 * delta;
        velocityX *= 1f / (1f + 2.2f * delta);
        velocityY *= 1f / (1f + 1.4f * delta);
        transform.position.add(velocityX * delta, velocityY * delta);
        light.intensity(0.72f + 0.18f * (float) Math.sin(Time.timePassed * 9 + phase));
        super.update();
    }

    public void attractTo(Player player) {
        if (collected || age < 0.16f) {
            return;
        }
        float dx = player.transform.position.x - transform.position.x;
        float dy = player.transform.position.y - transform.position.y;
        float distanceSquared = dx * dx + dy * dy;
        if (distanceSquared > 190 * 190 || distanceSquared < 0.001f) {
            return;
        }
        float distance = (float) Math.sqrt(distanceSquared);
        float pull = 260 + 980 * (1 - distance / 190f);
        velocityX += dx / distance * pull * Math.min(Time.delta, 0.05f);
        velocityY += dy / distance * pull * Math.min(Time.delta, 0.05f);
    }

    @Override
    public void draw() {
        if (collected) {
            return;
        }
        float pulse = 0.5f + 0.5f * (float) Math.sin(Time.timePassed * 9 + phase);
        float size = 12 + pulse * 4;
        Render.drawRotatedRect(transform.position.x, transform.position.y,
                size + 13, size + 13, true,
                new Color4f(0.55f, 0.25f, 1f, 0.07f + pulse * 0.06f),
                Time.timePassed * 65 + phase * 17);
        Render.drawRotatedRect(transform.position.x, transform.position.y,
                size, size, true, Colors.from255(191, 113, 255, 235), 45);
        Render.drawRotatedRect(transform.position.x, transform.position.y,
                4, size + 6, true, Colors.from255(216, 244, 255, 245), 45);
        super.draw();
    }

    public boolean collectIfOverlapping(Player player) {
        if (collected || age < 0.1f || !bounds().overlaps(player.getBody().bounds())) {
            return false;
        }
        collected = true;
        light.enabled(false);
        destroy();
        return true;
    }

    public int value() {
        return value;
    }

    public AABB bounds() {
        return AABB.fromCenter(transform.position.x, transform.position.y, 25, 25);
    }
}
