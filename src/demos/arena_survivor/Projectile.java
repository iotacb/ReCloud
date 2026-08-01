package arena_survivor;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import de.kostari.cloud.core.lighting.Light2D;
import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.utils.math.Vector2;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.render.Texture;
import de.kostari.cloud.core.utils.types.Color4f;
import de.kostari.cloud.core.window.Time;

final class Projectile extends GameObject {
    final boolean friendly;
    final int damage;
    final float radius;
    final float slowSeconds;

    private final Vector2 velocity;
    private final Color4f color;
    private final Texture texture;
    private float lifetime;
    private int pierce;
    private boolean alive = true;
    private final Set<Enemy> hitEnemies = Collections.newSetFromMap(new IdentityHashMap<>());

    Projectile(boolean friendly, Texture texture, float x, float y, Vector2 direction,
            float speed, int damage, float radius, float lifetime, int pierce,
            float slowSeconds, Color4f color) {
        this.friendly = friendly;
        this.texture = texture;
        this.damage = damage;
        this.radius = radius;
        this.lifetime = lifetime;
        this.pierce = pierce;
        this.slowSeconds = slowSeconds;
        this.color = color;
        velocity = new Vector2(direction);
        if (velocity.lengthSquared() > 0) {
            velocity.normalize();
        }
        velocity.multiply(speed);
        transform.position.set(x, y);
        addComponent(new Light2D(friendly ? 82 : 112, color)
                .intensity(friendly ? 0.52f : 0.68f)
                .falloff(2.35f)
                .castsShadows(false));
    }

    @Override
    public void update() {
        if (!alive) {
            return;
        }
        float delta = Math.min(Time.delta, 0.05f);
        lifetime -= delta;
        transform.position.add(new Vector2(velocity).multiply(delta));
        if (lifetime <= 0
                || transform.position.x < ArenaScene.ARENA_LEFT - 80
                || transform.position.x > ArenaScene.ARENA_RIGHT + 80
                || transform.position.y < ArenaScene.ARENA_TOP - 80
                || transform.position.y > ArenaScene.ARENA_BOTTOM + 80) {
            expire();
        }
        super.update();
    }

    @Override
    public void draw() {
        float angle = (float) Math.toDegrees(Math.atan2(velocity.y, velocity.x));
        Render.drawRotatedRect(transform.position.x - velocity.x * 0.018f,
                transform.position.y - velocity.y * 0.018f,
                radius * 3.6f, radius * 0.85f, true, Palette.alpha(color, 0.20f), angle);
        Render.drawRotatedRect(transform.position.x, transform.position.y,
                radius * 2.1f, radius * 1.25f, true, color, angle);
        if (texture != null && !friendly) {
            Render.drawTexture(texture, transform.position.x, transform.position.y,
                    radius * 3.1f, radius * 3.1f, true, Palette.WHITE);
        }
        super.draw();
    }

    void hit() {
        if (pierce > 0) {
            pierce--;
        } else {
            expire();
        }
    }

    boolean canHit(Enemy enemy) {
        return friendly && alive && hitEnemies.add(enemy);
    }

    void expire() {
        if (!alive) {
            return;
        }
        alive = false;
        destroy();
    }

    boolean alive() {
        return alive;
    }
}
