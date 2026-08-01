package tower_climber;

import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.lighting.Light2D;
import de.kostari.cloud.core.physics.AABB;
import de.kostari.cloud.core.utils.Colors;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.types.Color4f;
import de.kostari.cloud.core.window.Time;
import tower_climber.RunProgression.Weapon;

public final class WeaponProjectile extends GameObject {

    private final Weapon weapon;
    private final float velocityX;
    private final float velocityY;
    private final float lifetime;
    private int hitsRemaining;
    private float age;
    private float rotation;
    private final Light2D light;

    public WeaponProjectile(Weapon weapon, float x, float y,
            float directionX, float directionY, float angleOffset) {
        this.weapon = weapon;
        float angle = (float) Math.atan2(directionY, directionX) + angleOffset;
        float speed = weapon == Weapon.BOW ? 820 : 570;
        velocityX = (float) Math.cos(angle) * speed;
        velocityY = (float) Math.sin(angle) * speed;
        lifetime = weapon == Weapon.BOW ? 1.35f : 0.95f;
        hitsRemaining = weapon == Weapon.BOW ? 1 : 2;
        rotation = (float) Math.toDegrees(angle);
        transform.position.set(x + (float) Math.cos(angle) * 34,
                y + (float) Math.sin(angle) * 34);
        Color4f lightColor = weapon == Weapon.BOW
                ? new Color4f(0.42f, 0.9f, 1f, 1)
                : new Color4f(0.82f, 0.4f, 1f, 1);
        light = addComponent(new Light2D(weapon == Weapon.BOW ? 125 : 92, lightColor)
                .intensity(weapon == Weapon.BOW ? 1.25f : 0.8f)
                .falloff(2f)
                .castsShadows(false));
    }

    @Override
    public void update() {
        float delta = Math.min(Time.delta, 0.05f);
        age += delta;
        transform.position.add(velocityX * delta, velocityY * delta);
        if (weapon == Weapon.SHURIKEN) {
            rotation += delta * 960;
        }
        if (age >= lifetime) {
            light.enabled(false);
            destroy();
        }
        super.update();
    }

    @Override
    public void draw() {
        float fade = Math.min(1, (lifetime - age) * 5);
        if (weapon == Weapon.BOW) {
            Color4f trail = new Color4f(0.38f, 0.95f, 1f, fade * 0.16f);
            Render.drawRotatedRect(transform.position.x - velocityX * 0.025f,
                    transform.position.y - velocityY * 0.025f,
                    48, 7, true, trail, rotation);
            Render.drawRotatedRect(transform.position.x, transform.position.y,
                    33, 4, true, Colors.from255(172, 245, 255, Math.round(fade * 255)), rotation);
            Render.drawRotatedRect(transform.position.x + velocityX * 0.009f,
                    transform.position.y + velocityY * 0.009f,
                    10, 10, true, Colors.from255(225, 255, 248, Math.round(fade * 255)),
                    rotation + 45);
        } else {
            Color4f glow = new Color4f(0.86f, 0.48f, 1f, fade * 0.14f);
            Render.drawRotatedRect(transform.position.x, transform.position.y,
                    34, 34, true, glow, rotation);
            for (int i = 0; i < 4; i++) {
                Render.drawRotatedRect(transform.position.x, transform.position.y,
                        5, 26, true,
                        Colors.from255(225, 180, 255, Math.round(fade * 245)),
                        rotation + 45 + i * 90);
            }
            Render.drawRotatedRect(transform.position.x, transform.position.y,
                    7, 7, true, Colors.from255(240, 255, 252, Math.round(fade * 255)), 45);
        }
        super.draw();
    }

    public AABB bounds() {
        float size = weapon == Weapon.BOW ? 20 : 28;
        return AABB.fromCenter(transform.position.x, transform.position.y, size, size);
    }

    public boolean consumeHit() {
        hitsRemaining--;
        if (hitsRemaining <= 0) {
            light.enabled(false);
            destroy();
            return true;
        }
        return false;
    }

    public boolean isExpired() {
        return canBeDestroyed;
    }

    public Weapon weapon() {
        return weapon;
    }
}
