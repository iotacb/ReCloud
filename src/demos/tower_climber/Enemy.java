package tower_climber;

import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.physics.AABB;
import de.kostari.cloud.core.utils.Colors;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.render.Texture;
import de.kostari.cloud.core.utils.types.Color4f;
import de.kostari.cloud.core.window.Time;

public class Enemy extends GameObject {

    public enum Type {
        BASIC,
        FLYING,
        EVADER
    }

    private static final float WIDTH = 38;
    private static final float HEIGHT = 30;

    private final Type type;
    private final float patrolLeft;
    private final float patrolRight;
    private final float baseY;
    private final float phase;
    private final float speed;
    private float direction;
    private float shrink = 1;
    private float evadeTimer;
    private boolean debugCollider;
    private boolean alive = true;

    public Enemy(Type type, TowerPlatform platform, float x, float direction, float difficulty, float phase) {
        this.type = type;
        this.direction = direction;
        this.phase = phase;
        this.speed = 64 + difficulty * 5 + (type == Type.FLYING ? 18 : 0);

        float margin = WIDTH * 0.5f + 8;
        patrolLeft = platform.left() + margin;
        patrolRight = platform.right() - margin;
        baseY = type == Type.FLYING ? platform.top() - 72 : platform.top();
        transform.position.set(x, type == Type.FLYING ? baseY : baseY - HEIGHT * 0.5f);
    }

    @Override
    public void update() {
        float delta = Math.min(Time.delta, 0.05f);
        transform.position.x += direction * speed * delta;
        if (transform.position.x <= patrolLeft) {
            transform.position.x = patrolLeft;
            direction = 1;
        } else if (transform.position.x >= patrolRight) {
            transform.position.x = patrolRight;
            direction = -1;
        }

        if (type == Type.FLYING) {
            transform.position.y = baseY + (float) Math.sin(Time.timePassed * 2.1f + phase) * 34;
        } else if (type == Type.EVADER) {
            evadeTimer = Math.max(0, evadeTimer - delta);
            float target = evadeTimer > 0 ? 0.24f : 1;
            shrink += (target - shrink) * Math.min(1, delta * 14);
            transform.position.y = baseY - currentHeight() * 0.5f;
        }
        super.update();
    }

    @Override
    public void draw() {
        if (!alive) {
            return;
        }

        float bob = type == Type.FLYING ? 0
                : (float) Math.sin(Time.timePassed * 6 + transform.position.x * 0.03f) * 2;
        float y = transform.position.y + bob;
        float height = currentHeight();
        Texture sprite = TowerSprites.enemy(type);
        float spriteSize = type == Type.FLYING ? 82 : 68;
        float spriteWidth = direction >= 0 ? spriteSize : -spriteSize;
        float spriteHeight = type == Type.EVADER ? spriteSize * shrink : spriteSize;
        float rotation = type == Type.FLYING
                ? (float) Math.sin(Time.timePassed * 5 + phase) * 4
                : 0;
        if (type == Type.FLYING) {
            drawFlightTrail(y);
        } else {
            float shadowWidth = type == Type.EVADER ? 34 * shrink : 34;
            Render.drawRect(transform.position.x, baseY - 1,
                    shadowWidth, 4, true, Colors.from255(1, 7, 13, 115));
        }

        Render.drawRotatedTexture(sprite, transform.position.x, y - (type == Type.FLYING ? 2 : 5),
                spriteWidth, spriteHeight, true, rotation, null);

        if (type == Type.EVADER && evadeTimer > 0) {
            float pulse = 0.5f + 0.5f * (float) Math.sin(Time.timePassed * 18 + phase);
            float size = 38 + pulse * 14;
            Color4f warning = new Color4f(0.75f, 0.32f, 1f, 0.1f + pulse * 0.08f);
            Render.drawRotatedRect(transform.position.x, y - 5,
                    size, size, true, warning, 45 + Time.timePassed * 90);
        }

        if (debugCollider) {
            drawCollider();
        }
        super.draw();
    }

    public void reactTo(Player player) {
        if (type != Type.EVADER || evadeTimer > 0 || player.getBody().velocity.y < 90) {
            return;
        }
        float horizontalDistance = Math.abs(player.transform.position.x - transform.position.x);
        float verticalDistance = transform.position.y - player.transform.position.y;
        if (horizontalDistance < WIDTH * 0.9f && verticalDistance > 8 && verticalDistance < 115) {
            evadeTimer = 0.62f;
        }
    }

    public boolean overlaps(Player player) {
        return alive && bounds().overlaps(player.getBody().bounds());
    }

    public boolean canBeStompedBy(Player player) {
        if (!overlaps(player) || player.getBody().velocity.y < 75) {
            return false;
        }
        if (type == Type.EVADER && shrink < 0.55f) {
            return false;
        }
        float playerBottom = player.getBody().bounds().bottom();
        return playerBottom <= bounds().top() + Math.max(15, currentHeight() * 0.62f);
    }

    public void kill() {
        alive = false;
        destroy();
    }

    public boolean isAlive() {
        return alive;
    }

    public Type type() {
        return type;
    }

    public float facingDirection() {
        return direction >= 0 ? 1 : -1;
    }

    public AABB bounds() {
        float width = type == Type.FLYING ? 42 : WIDTH;
        return AABB.fromCenter(transform.position.x, transform.position.y, width, currentHeight());
    }

    public void setDebugCollider(boolean debugCollider) {
        this.debugCollider = debugCollider;
    }

    private float currentHeight() {
        return type == Type.EVADER ? HEIGHT * shrink : (type == Type.FLYING ? 28 : HEIGHT);
    }

    private void drawCollider() {
        AABB bounds = bounds();
        var color = Colors.from255(255, 90, 90, 230);
        Render.drawRect(bounds.left(), bounds.top(), bounds.width(), 1, color);
        Render.drawRect(bounds.left(), bounds.bottom() - 1, bounds.width(), 1, color);
        Render.drawRect(bounds.left(), bounds.top(), 1, bounds.height(), color);
        Render.drawRect(bounds.right() - 1, bounds.top(), 1, bounds.height(), color);
    }

    private void drawFlightTrail(float y) {
        for (int i = 1; i <= 3; i++) {
            float fade = 1 - i / 4f;
            float size = 9 - i * 1.5f;
            Color4f color = new Color4f(0.42f, 0.94f, 1f, fade * 0.16f);
            Render.drawRotatedRect(
                    transform.position.x - direction * (22 + i * 10),
                    y + (float) Math.sin(Time.timePassed * 8 + phase + i) * 4,
                    size, size, true, color, 45);
        }
    }
}
