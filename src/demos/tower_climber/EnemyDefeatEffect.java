package tower_climber;

import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.render.Texture;
import de.kostari.cloud.core.utils.types.Color4f;
import de.kostari.cloud.core.window.Time;

public final class EnemyDefeatEffect extends GameObject {

    private static final float DURATION = 0.52f;

    private final Texture sprite;
    private final float facing;
    private final float baseSize;
    private float age;

    public EnemyDefeatEffect(Enemy enemy) {
        transform.position.set(enemy.transform.position);
        sprite = TowerSprites.enemy(enemy.type());
        facing = enemy.facingDirection();
        baseSize = enemy.type() == Enemy.Type.FLYING ? 82 : 68;
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
        float squash = Math.min(1, progress / 0.22f);
        float dissipate = Math.max(0, (progress - 0.22f) / 0.78f);
        float width = baseSize * (1 + squash * 0.42f + dissipate * 0.12f);
        float height = baseSize * (1 - squash * 0.74f) * (1 - dissipate * 0.92f);
        float alpha = 1 - dissipate;
        float y = transform.position.y + squash * baseSize * 0.16f + dissipate * 8;
        Color4f tint = new Color4f(1f, 1f, 1f, alpha);
        Render.drawRotatedTexture(sprite, transform.position.x, y,
                facing * width, Math.max(2, height), true, dissipate * facing * 7, tint);
        super.draw();
    }
}
