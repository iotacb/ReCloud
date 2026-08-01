package tower_climber;

import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.lighting.LightOccluder2D;
import de.kostari.cloud.core.physics.AABB;
import de.kostari.cloud.core.physics.PhysicsBody;
import de.kostari.cloud.core.utils.Colors;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.types.Color4f;
import de.kostari.cloud.core.window.Time;

public class TowerPlatform extends GameObject {

    public enum Kind {
        START,
        NORMAL,
        REST,
        MILESTONE,
        MOVING,
        FRAGILE,
        BOOST,
        BONUS
    }

    private static final Color4f PLATFORM_TOP = Colors.from255(91, 231, 211, 255);
    private static final Color4f PLATFORM_FACE = Colors.from255(27, 57, 66, 255);
    private static final Color4f REACTOR_TOP = Colors.from255(112, 255, 170, 255);
    private static final Color4f REACTOR_FACE = Colors.from255(29, 70, 57, 255);
    private static final Color4f VOID_TOP = Colors.from255(190, 119, 255, 255);
    private static final Color4f VOID_FACE = Colors.from255(57, 36, 75, 255);
    private static final Color4f STORM_TOP = Colors.from255(123, 207, 255, 255);
    private static final Color4f STORM_FACE = Colors.from255(34, 53, 78, 255);
    private static final Color4f REST_TOP = Colors.from255(255, 190, 86, 255);
    private static final Color4f REST_FACE = Colors.from255(74, 53, 45, 255);
    private static final Color4f BONUS_TOP = Colors.from255(190, 119, 255, 255);
    private static final Color4f BONUS_FACE = Colors.from255(55, 34, 77, 255);
    private static final Color4f MILESTONE_TOP = Colors.from255(232, 249, 255, 255);
    private static final Color4f MILESTONE_FACE = Colors.from255(39, 67, 83, 255);
    private static final Color4f MOVING_TOP = Colors.from255(103, 184, 255, 255);
    private static final Color4f MOVING_FACE = Colors.from255(31, 51, 76, 255);
    private static final Color4f FRAGILE_TOP = Colors.from255(255, 126, 82, 255);
    private static final Color4f FRAGILE_FACE = Colors.from255(83, 43, 43, 255);
    private static final Color4f BOOST_TOP = Colors.from255(255, 232, 112, 255);
    private static final Color4f BOOST_FACE = Colors.from255(79, 58, 32, 255);
    private static final Color4f START_FACE = Colors.from255(32, 69, 75, 255);
    private static final Color4f DEBUG = Colors.from255(255, 90, 90, 230);

    private final float width;
    private final float height;
    private final int level;
    private final Kind kind;
    private final PhysicsBody body;
    private float movementOriginX;
    private float movementRange;
    private float movementSpeed;
    private float movementPhase;
    private float lastFrameDeltaX;
    private float crumbleTimer = -1;
    private float slingshotCharge;
    private float slingshotAim = 1;
    private boolean slingshotActive;
    private boolean restRewardClaimed;
    private boolean bonusRewardClaimed;
    private boolean debugCollider;

    public TowerPlatform(float x, float y, float width, float height, int level, boolean restPlatform) {
        this(x, y, width, height, level, restPlatform ? Kind.REST : Kind.NORMAL);
    }

    public TowerPlatform(float x, float y, float width, float height, int level, Kind kind) {
        transform.position.set(x, y);
        this.width = width;
        this.height = height;
        this.level = level;
        this.kind = kind;
        this.movementOriginX = x;
        this.body = addComponent((kind == Kind.MOVING
                ? PhysicsBody.kinematic(width, height)
                : PhysicsBody.fixed(width, height))
                .friction(0.8f)
                .oneWayPlatform(true));
        addComponent(new LightOccluder2D(width, height).opacity(0.88f));
    }

    @Override
    public void update() {
        float delta = Math.min(Time.delta, 0.05f);
        lastFrameDeltaX = 0;
        if (kind == Kind.MOVING && movementRange > 0 && delta > 0) {
            float targetX = movementOriginX
                    + (float) Math.sin(Time.timePassed * movementSpeed + movementPhase) * movementRange;
            float velocityX = (targetX - transform.position.x) / delta;
            body.velocity.set(velocityX, 0);
            lastFrameDeltaX = velocityX * delta;
        }

        if (kind == Kind.FRAGILE && crumbleTimer >= 0) {
            crumbleTimer += delta;
            if (crumbleTimer >= 0.72f) {
                body.enabled(false);
            }
            if (crumbleTimer >= 1.05f) {
                destroy();
            }
        }
        super.update();
    }

    @Override
    public void draw() {
        float crumble = crumbleProgress();
        float warningJitter = kind == Kind.FRAGILE && crumbleTimer >= 0
                ? (float) Math.sin(Time.timePassed * 58 + level) * crumble * 3
                : 0;
        Color4f face = faceColor();
        Color4f top = topColor();
        Render.drawRect(transform.position.x + warningJitter, transform.position.y,
                width, height, true, face);
        Render.drawRect(transform.position.x + warningJitter, top() + 2,
                width, 4, true, top);
        Render.drawRect(transform.position.x, transform.position.y + 3,
                Math.max(12, width - 22), 2, true, Colors.from255(7, 18, 29, 145));

        if (kind == Kind.REST || kind == Kind.BONUS || kind == Kind.MILESTONE) {
            float pulse = 0.5f + 0.5f * (float) Math.sin(Time.timePassed * 3.2f + level * 0.7f);
            Color4f glow = new Color4f(top.r, top.g, top.b, 0.08f + pulse * 0.12f);
            Render.drawRect(transform.position.x, top() - 3, width + 8, 3, true, glow);
            Render.drawRotatedRect(transform.position.x, transform.position.y + height * 0.5f + 8,
                    8 + pulse * 3, 8 + pulse * 3, true, glow, 45);
        }

        if (kind == Kind.MOVING) {
            float direction = body.velocity.x >= 0 ? 1 : -1;
            float pulse = 0.5f + 0.5f * (float) Math.sin(Time.timePassed * 7);
            Color4f signal = new Color4f(MOVING_TOP.r, MOVING_TOP.g, MOVING_TOP.b, 0.18f + pulse * 0.22f);
            for (int i = -1; i <= 1; i++) {
                Render.drawRotatedRect(transform.position.x + i * 22 - direction * 3,
                        transform.position.y + 1, 8, 8, true, signal, 45);
            }
        } else if (kind == Kind.FRAGILE) {
            Color4f crack = Colors.from255(28, 13, 23, 190);
            Render.drawRotatedRect(transform.position.x - width * 0.18f + warningJitter,
                    transform.position.y, 3, 16, true, crack, -32);
            Render.drawRotatedRect(transform.position.x + width * 0.12f + warningJitter,
                    transform.position.y + 1, 3, 13, true, crack, 39);
            if (crumbleTimer >= 0) {
                Color4f warning = new Color4f(1f, 0.27f, 0.12f, 0.08f + crumble * 0.3f);
                Render.drawRect(transform.position.x, top() - 5, width + 12, 4, true, warning);
            }
        } else if (kind == Kind.BOOST) {
            drawBoostEnergy();
        }

        Render.drawRect(transform.position.x - width * 0.5f + 9, transform.position.y + height * 0.5f,
                5, 12, true, face);
        Render.drawRect(transform.position.x + width * 0.5f - 9, transform.position.y + height * 0.5f,
                5, 12, true, face);
        if (width > 150) {
            Render.drawRotatedRect(transform.position.x - width * 0.28f,
                    transform.position.y + height * 0.5f + 7, 3, 22, true, face, -42);
            Render.drawRotatedRect(transform.position.x + width * 0.28f,
                    transform.position.y + height * 0.5f + 7, 3, 22, true, face, 42);
        }

        if (debugCollider) {
            drawCollider();
        }
        super.draw();
    }

    public AABB bounds() {
        return body.bounds();
    }

    public float left() {
        return transform.position.x - width * 0.5f;
    }

    public float right() {
        return transform.position.x + width * 0.5f;
    }

    public float top() {
        return transform.position.y - height * 0.5f;
    }

    public float width() {
        return width;
    }

    public int level() {
        return level;
    }

    public Kind kind() {
        return kind;
    }

    public TowerPlatform configureHorizontalMotion(float range, float speed, float phase) {
        if (kind == Kind.MOVING) {
            movementRange = Math.max(0, range);
            movementSpeed = Math.max(0.1f, speed);
            movementPhase = phase;
        }
        return this;
    }

    public float lastFrameDeltaX() {
        return kind == Kind.MOVING ? lastFrameDeltaX : 0;
    }

    public boolean triggerCrumble() {
        if (kind == Kind.FRAGILE && crumbleTimer < 0) {
            crumbleTimer = 0;
            return true;
        }
        return false;
    }

    public boolean claimRestReward() {
        if (kind != Kind.REST || restRewardClaimed) {
            return false;
        }
        restRewardClaimed = true;
        return true;
    }

    public void setSlingshotPreview(float charge, float aim, boolean active) {
        if (kind != Kind.BOOST) {
            return;
        }
        slingshotCharge = Math.max(0, Math.min(1, charge));
        slingshotAim = Math.max(-1, Math.min(1, aim));
        slingshotActive = active;
    }

    public boolean claimBonusReward() {
        if (kind != Kind.BONUS || bonusRewardClaimed) {
            return false;
        }
        bonusRewardClaimed = true;
        return true;
    }

    public void setDebugCollider(boolean debugCollider) {
        this.debugCollider = debugCollider;
    }

    private void drawCollider() {
        AABB bounds = body.bounds();
        Render.drawRect(bounds.left(), bounds.top(), bounds.width(), 1, DEBUG);
        Render.drawRect(bounds.left(), bounds.bottom() - 1, bounds.width(), 1, DEBUG);
        Render.drawRect(bounds.left(), bounds.top(), 1, bounds.height(), DEBUG);
        Render.drawRect(bounds.right() - 1, bounds.top(), 1, bounds.height(), DEBUG);
    }

    private Color4f faceColor() {
        return switch (kind) {
            case START -> START_FACE;
            case REST -> REST_FACE;
            case MILESTONE -> MILESTONE_FACE;
            case MOVING -> MOVING_FACE;
            case FRAGILE -> FRAGILE_FACE;
            case BOOST -> BOOST_FACE;
            case BONUS -> BONUS_FACE;
            case NORMAL -> normalFaceColor();
        };
    }

    private Color4f topColor() {
        return switch (kind) {
            case REST -> REST_TOP;
            case MILESTONE -> MILESTONE_TOP;
            case MOVING -> MOVING_TOP;
            case FRAGILE -> FRAGILE_TOP;
            case BOOST -> BOOST_TOP;
            case BONUS -> BONUS_TOP;
            case NORMAL -> normalTopColor();
            case START -> PLATFORM_TOP;
        };
    }

    private Color4f normalFaceColor() {
        return switch (zoneIndex()) {
            case 1 -> REACTOR_FACE;
            case 2 -> VOID_FACE;
            case 3 -> STORM_FACE;
            default -> PLATFORM_FACE;
        };
    }

    private Color4f normalTopColor() {
        return switch (zoneIndex()) {
            case 1 -> REACTOR_TOP;
            case 2 -> VOID_TOP;
            case 3 -> STORM_TOP;
            default -> PLATFORM_TOP;
        };
    }

    private int zoneIndex() {
        return Math.floorMod(Math.max(0, level) / 12, 4);
    }

    private float crumbleProgress() {
        return crumbleTimer < 0 ? 0 : Math.min(1, crumbleTimer / 0.72f);
    }

    private void drawBoostEnergy() {
        float pulse = 0.5f + 0.5f * (float) Math.sin(Time.timePassed * 8.5f + level);
        float chargePulse = slingshotActive ? slingshotCharge : 0;
        Color4f glow = new Color4f(BOOST_TOP.r, BOOST_TOP.g, BOOST_TOP.b,
                0.12f + pulse * 0.12f + chargePulse * 0.2f);
        Render.drawRect(transform.position.x, top() - 5 - pulse * 2,
                width + 10, 4, true, glow);
        if (slingshotActive) {
            float meterWidth = Math.max(8, (width - 18) * slingshotCharge);
            float meterX = left() + 9 + meterWidth * 0.5f;
            Color4f meter = new Color4f(1f, 0.89f, 0.34f, 0.38f + slingshotCharge * 0.5f);
            Render.drawRect(meterX, top() - 8, meterWidth, 4, true, meter);
            Render.drawRotatedRect(transform.position.x + slingshotAim * 22, top() - 18,
                    5, 34, true, meter, slingshotAim * 34);
        }
        for (int i = -2; i <= 2; i++) {
            float phase = Math.floorMod((int) (Time.timePassed * 72 + i * 13 + level * 7), 38);
            float x = transform.position.x + i * Math.min(28, width * 0.12f);
            float y = top() - 12 - phase;
            float fade = 1 - phase / 38f;
            Color4f spark = new Color4f(1f, 0.91f, 0.4f, fade * 0.32f);
            Render.drawRotatedRect(x, y, 7, 7, true, spark, 45);
        }
        for (int i = -1; i <= 1; i++) {
            Render.drawRotatedRect(transform.position.x + i * 28,
                    transform.position.y + 1, 9, 9, true, glow, 45);
        }
    }
}
