package tower_climber;

import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.physics.AABB;
import de.kostari.cloud.core.utils.Colors;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.render.Texture;
import de.kostari.cloud.core.window.Time;

public class XpOrb extends GameObject {

    private static final float SIZE = 15;

    private final int value;
    private final float phase;
    private float velocityX;
    private float velocityY;
    private final float hoverY;
    private float age;
    private boolean hovering;
    private boolean collected;
    private boolean debugCollider;

    public XpOrb(float x, float y, float velocityX, float velocityY, int value, float phase) {
        this(x, y, velocityX, velocityY, value, phase, false);
    }

    private XpOrb(float x, float y, float velocityX, float velocityY, int value, float phase,
            boolean hovering) {
        transform.position.set(x, y);
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.value = value;
        this.phase = phase;
        this.hoverY = y;
        this.hovering = hovering;
    }

    public static XpOrb hovering(float x, float y, int value, float phase) {
        return new XpOrb(x, y, 0, 0, value, phase, true);
    }

    @Override
    public void update() {
        float delta = Math.min(Time.delta, 0.05f);
        age += delta;
        if (hovering) {
            transform.position.y = hoverY + (float) Math.sin(Time.timePassed * 3.6f + phase) * 5;
            super.update();
            return;
        }
        velocityY += 420 * delta;
        velocityX *= 1f / (1f + 2.4f * delta);
        velocityY *= 1f / (1f + 1.2f * delta);
        transform.position.add(velocityX * delta, velocityY * delta);
        super.update();
    }

    public void attractTo(Player player) {
        if (collected || age < 0.18f) {
            return;
        }
        float dx = player.transform.position.x - transform.position.x;
        float dy = player.transform.position.y - transform.position.y;
        float distanceSquared = dx * dx + dy * dy;
        if (distanceSquared > 145 * 145 || distanceSquared < 0.001f) {
            return;
        }
        float distance = (float) Math.sqrt(distanceSquared);
        float strength = 780 * (1 - distance / 145f) + 210;
        hovering = false;
        velocityX += dx / distance * strength * Math.min(Time.delta, 0.05f);
        velocityY += dy / distance * strength * Math.min(Time.delta, 0.05f);
    }

    @Override
    public void draw() {
        if (collected) {
            return;
        }
        float pulse = 0.82f + (float) Math.sin(Time.timePassed * 8 + phase) * 0.18f;
        Texture sprite = TowerSprites.xpCrystal();
        Render.drawRotatedTexture(sprite, transform.position.x, transform.position.y,
                34 * pulse, 34 * pulse, true, Time.timePassed * 48 + phase * 8, null);
        if (debugCollider) {
            AABB box = bounds();
            Render.drawRect(box.left(), box.top(), box.width(), 1, Colors.from255(255, 0, 255, 255));
            Render.drawRect(box.left(), box.bottom() - 1, box.width(), 1, Colors.from255(255, 0, 255, 255));
        }
        super.draw();
    }

    public boolean collectIfOverlapping(Player player) {
        if (collected || age < 0.12f || !bounds().overlaps(player.getBody().bounds())) {
            return false;
        }
        collected = true;
        destroy();
        return true;
    }

    public int value() {
        return value;
    }

    public AABB bounds() {
        return AABB.fromCenter(transform.position.x, transform.position.y, SIZE + 6, SIZE + 6);
    }

    public void setDebugCollider(boolean debugCollider) {
        this.debugCollider = debugCollider;
    }
}
