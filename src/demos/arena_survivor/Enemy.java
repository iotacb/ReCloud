package arena_survivor;

import java.util.Random;

import de.kostari.cloud.core.lighting.Light2D;
import de.kostari.cloud.core.lighting.LightOccluder2D;
import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.utils.math.Vector2;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.render.Texture;
import de.kostari.cloud.core.utils.types.Color4f;
import de.kostari.cloud.core.window.Time;

final class Enemy extends GameObject {
    enum Type {
        SLIME("Gloop", 34, 108, 28, 94, 8, Palette.GREEN),
        RAT("Razor Rat", 23, 123, 21, 162, 7, Palette.GOLD),
        GOBLIN("Hex Slinger", 52, 112, 27, 90, 10, Palette.VIOLET),
        GHOST("Wisp", 40, 121, 25, 118, 9, Palette.CYAN),
        BRUTE("Iron Husk", 160, 122, 38, 62, 18, Palette.RED),
        DEMON("Rift Baron", 390, 110, 48, 72, 22, Palette.VIOLET);

        final String label;
        final int baseHealth;
        final int tile;
        final float radius;
        final float speed;
        final int contactDamage;
        final Color4f color;

        Type(String label, int baseHealth, int tile, float radius, float speed,
                int contactDamage, Color4f color) {
            this.label = label;
            this.baseHealth = baseHealth;
            this.tile = tile;
            this.radius = radius;
            this.speed = speed;
            this.contactDamage = contactDamage;
            this.color = color;
        }
    }

    private final ArenaScene scene;
    private final Player player;
    private final Texture texture;
    private final Random random;
    private final float seed;
    private final Light2D auraLight;

    final Type type;
    private final int maxHealth;
    private int health;
    private float attackClock;
    private float aiClock;
    private float chargeTelegraph;
    private float chargeTimer;
    private float slowTimer;
    private float hitFlash;
    private boolean alive = true;

    Enemy(ArenaScene scene, Player player, GameAssets assets, Type type, int wave,
            float x, float y, long seed) {
        this.scene = scene;
        this.player = player;
        this.type = type;
        this.texture = assets.tile(type.tile);
        random = new Random(seed);
        this.seed = random.nextFloat() * 20;
        float waveScale = 1 + Math.max(0, wave - 1) * 0.15f;
        maxHealth = Math.max(1, Math.round(type.baseHealth * waveScale));
        health = maxHealth;
        attackClock = random.nextFloat() * 1.2f;
        transform.position.set(x, y);
        addComponent(new LightOccluder2D(type.radius * 1.25f, type.radius * 1.55f)
                .opacity(type == Type.GHOST ? 0.18f : type == Type.SLIME ? 0.52f : 0.88f));
        auraLight = switch (type) {
            case GHOST -> addComponent(new Light2D(145, Palette.CYAN)
                    .intensity(0.28f).falloff(2.2f).castsShadows(false));
            case DEMON -> addComponent(new Light2D(330, Palette.VIOLET)
                    .intensity(0.78f).falloff(1.55f).softness(20).shadowStrength(0.9f));
            default -> null;
        };
    }

    @Override
    public void update() {
        float delta = Math.min(Time.delta, 0.05f);
        hitFlash = Math.max(0, hitFlash - delta * 7);
        slowTimer = Math.max(0, slowTimer - delta);
        aiClock += delta;
        if (auraLight != null) {
            float pulse = 0.5f + 0.5f * (float) Math.sin(
                    aiClock * (type == Type.DEMON ? 4.5f : 2.8f) + seed);
            auraLight.intensity(type == Type.DEMON ? 0.68f + pulse * 0.38f : 0.20f + pulse * 0.16f);
            auraLight.radius(type == Type.DEMON ? 310 + pulse * 55 : 130 + pulse * 30);
        }
        if (!scene.isPlaying() || !alive) {
            super.update();
            return;
        }

        Vector2 toPlayer = new Vector2(player.transform.position).sub(transform.position);
        float distance = Math.max(0.001f, toPlayer.length());
        Vector2 direction = new Vector2(toPlayer).divide(distance);
        float speedScale = slowTimer > 0 ? 0.56f : 1;
        float waveSpeed = 1 + Math.min(0.45f, (scene.wave() - 1) * 0.025f);

        switch (type) {
            case GOBLIN -> updateRanged(direction, distance, delta, speedScale * waveSpeed);
            case GHOST -> updateGhost(direction, distance, delta, speedScale * waveSpeed);
            case BRUTE -> updateBrute(direction, delta, speedScale * waveSpeed);
            case DEMON -> updateDemon(direction, distance, delta, speedScale * waveSpeed);
            case RAT -> {
                Vector2 weave = new Vector2(direction).rotate((float) Math.sin(aiClock * 5 + seed) * 0.38f);
                transform.position.add(weave.multiply(type.speed * speedScale * waveSpeed * delta));
            }
            default -> transform.position.add(direction.multiply(type.speed * speedScale * waveSpeed * delta));
        }
        keepInArena();
        super.update();
    }

    @Override
    public void draw() {
        float x = transform.position.x;
        float y = transform.position.y;
        float bob = (float) Math.sin(aiClock * 4 + seed) * (type == Type.GHOST ? 6 : 2);
        float size = type.radius * 2.15f;

        Render.drawRect(x, y + type.radius * 0.65f, size * 0.8f, type.radius * 0.45f,
                true, Palette.SHADOW);
        if (chargeTelegraph > 0) {
            float pulse = 0.5f + 0.5f * (float) Math.sin(chargeTelegraph * 30);
            Render.drawRotatedRect(x, y, size * (1.25f + pulse * 0.18f),
                    size * (1.25f + pulse * 0.18f), true, Palette.alpha(Palette.RED, 0.18f + pulse * 0.2f), 45);
        }
        Color4f tint = hitFlash > 0
                ? new Color4f(1, 0.5f + hitFlash * 0.5f, 0.5f + hitFlash * 0.5f, 1)
                : Palette.WHITE;
        Render.drawTexture(texture, x, y - 2 + bob, size, size, true, tint);

        if (health < maxHealth || type == Type.DEMON) {
            float ratio = health / (float) maxHealth;
            Render.drawRect(x, y - type.radius - 11, type.radius * 1.8f, 5, true, Palette.alpha(Palette.VOID, 0.9f));
            Render.drawRect(x - type.radius * 0.88f + type.radius * 0.88f * ratio,
                    y - type.radius - 11, type.radius * 1.72f * ratio, 3, true, type.color);
        }
        super.draw();
    }

    boolean damage(int amount, float slowSeconds) {
        if (!alive) {
            return false;
        }
        health -= Math.max(1, amount);
        slowTimer = Math.max(slowTimer, slowSeconds);
        hitFlash = 1;
        if (health <= 0) {
            alive = false;
            destroy();
            scene.onEnemyKilled(this);
            return true;
        }
        scene.onEnemyHit(this);
        return false;
    }

    boolean alive() {
        return alive;
    }

    float radius() {
        return type.radius;
    }

    int contactDamage() {
        return type.contactDamage + scene.wave() / 3;
    }

    private void updateRanged(Vector2 direction, float distance, float delta, float speedScale) {
        if (distance > 390) {
            transform.position.add(new Vector2(direction).multiply(type.speed * speedScale * delta));
        } else if (distance < 235) {
            transform.position.sub(new Vector2(direction).multiply(type.speed * speedScale * 0.8f * delta));
        } else {
            transform.position.add(new Vector2(direction).perpendicular()
                    .multiply(type.speed * speedScale * 0.5f * delta));
        }
        attackClock -= delta;
        if (attackClock <= 0 && distance < 580) {
            attackClock = Math.max(0.7f, 1.85f - scene.wave() * 0.045f);
            scene.spawnEnemyProjectile(transform.position.x, transform.position.y, direction, 245, 9);
        }
    }

    private void updateGhost(Vector2 direction, float distance, float delta, float speedScale) {
        Vector2 spiral = new Vector2(direction).rotate((float) Math.sin(aiClock * 1.7f + seed) * 0.7f);
        float speed = type.speed * (distance > 260 ? 1.15f : 0.7f);
        transform.position.add(spiral.multiply(speed * speedScale * delta));
    }

    private void updateBrute(Vector2 direction, float delta, float speedScale) {
        if (chargeTimer > 0) {
            chargeTimer -= delta;
            transform.position.add(new Vector2(direction).multiply(type.speed * 4.4f * speedScale * delta));
            return;
        }
        if (chargeTelegraph > 0) {
            chargeTelegraph -= delta;
            if (chargeTelegraph <= 0) {
                chargeTimer = 0.42f;
            }
            return;
        }
        transform.position.add(new Vector2(direction).multiply(type.speed * speedScale * delta));
        attackClock -= delta;
        if (attackClock <= 0) {
            attackClock = 4.0f + random.nextFloat() * 1.3f;
            chargeTelegraph = 0.72f;
        }
    }

    private void updateDemon(Vector2 direction, float distance, float delta, float speedScale) {
        if (distance > 330) {
            transform.position.add(new Vector2(direction).multiply(type.speed * speedScale * delta));
        } else {
            transform.position.add(new Vector2(direction).perpendicular()
                    .multiply(type.speed * speedScale * delta));
        }
        attackClock -= delta;
        if (attackClock <= 0) {
            attackClock = Math.max(0.85f, 1.7f - scene.wave() * 0.035f);
            for (int i = -2; i <= 2; i++) {
                scene.spawnEnemyProjectile(transform.position.x, transform.position.y,
                        new Vector2(direction).rotateDegrees(i * 13), 270, 11);
            }
        }
    }

    private void keepInArena() {
        transform.position.x = Math.clamp(transform.position.x,
                ArenaScene.ARENA_LEFT + type.radius, ArenaScene.ARENA_RIGHT - type.radius);
        transform.position.y = Math.clamp(transform.position.y,
                ArenaScene.ARENA_TOP + type.radius, ArenaScene.ARENA_BOTTOM - type.radius);
    }
}
