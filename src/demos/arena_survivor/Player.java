package arena_survivor;

import de.kostari.cloud.core.lighting.Light2D;
import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.utils.input.Keys;
import de.kostari.cloud.core.utils.math.Vector2;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.types.Color4f;
import de.kostari.cloud.core.window.Input;
import de.kostari.cloud.core.window.Time;

final class Player extends GameObject {
    static final float RADIUS = 23;

    private final ArenaScene scene;
    private final GameAssets assets;
    private final RunStats stats;
    private final Vector2 facing = new Vector2(1, 0);
    private final Light2D light;

    private int health;
    private float invulnerability;
    private float dashCooldown;
    private float dashTimer;
    private float regenClock;
    private float walkClock;

    Player(ArenaScene scene, GameAssets assets, RunStats stats, float x, float y) {
        this.scene = scene;
        this.assets = assets;
        this.stats = stats;
        health = stats.maxHealth;
        transform.position.set(x, y);
        light = addComponent(new Light2D(250, Palette.CYAN)
                .intensity(0.52f)
                .falloff(1.65f)
                .softness(12)
                .shadowStrength(0.82f));
    }

    @Override
    public void update() {
        float delta = Math.min(Time.delta, 0.05f);
        invulnerability = Math.max(0, invulnerability - delta);
        dashCooldown = Math.max(0, dashCooldown - delta);
        dashTimer = Math.max(0, dashTimer - delta);
        float dangerPulse = healthRatio() < 0.35f
                ? 0.10f * (0.5f + 0.5f * (float) Math.sin(Time.timePassed * 10))
                : 0;
        light.radius(250 + (dashTimer > 0 ? 130 : 0));
        light.intensity(0.52f + dangerPulse + (dashTimer > 0 ? 0.92f : 0));

        if (scene.isPlaying()) {
            float x = Input.keyState(Keys.KEY_D) + Input.keyState(Keys.KEY_RIGHT)
                    - Input.keyState(Keys.KEY_A) - Input.keyState(Keys.KEY_LEFT);
            float y = Input.keyState(Keys.KEY_S) + Input.keyState(Keys.KEY_DOWN)
                    - Input.keyState(Keys.KEY_W) - Input.keyState(Keys.KEY_UP);
            Vector2 direction = new Vector2(x, y);
            if (direction.lengthSquared() > 0) {
                direction.normalize();
                facing.set(direction);
                walkClock += delta * 10;
            }

            if (Input.keyPressed(Keys.KEY_SPACE) && dashCooldown <= 0) {
                dashCooldown = 1.35f;
                dashTimer = 0.18f;
                invulnerability = Math.max(invulnerability, 0.28f);
                scene.onPlayerDash();
            }

            float speed = stats.moveSpeed * (dashTimer > 0 ? 2.75f : 1);
            transform.position.add(direction.multiply(speed * delta));
            clampToArena();
            regenerate(delta);
        }
        super.update();
    }

    @Override
    public void draw() {
        float x = transform.position.x;
        float y = transform.position.y;
        float bob = (float) Math.sin(walkClock) * 2;
        float flash = invulnerability > 0 && ((int) (invulnerability * 22) & 1) == 0 ? 0.35f : 1;

        Render.drawRect(x, y + 20, 48, 17, true, Palette.SHADOW);
        Render.drawRotatedRect(x, y, 57, 57, true, Palette.alpha(Palette.CYAN, 0.10f), 45);
        Render.drawTexture(assets.player, x, y - 3 + bob, 55, 55, true,
                new Color4f(flash, flash, flash, 1));

        float hp = health / (float) stats.maxHealth;
        Render.drawRect(x, y + 37, 52, 6, true, Palette.alpha(Palette.VOID, 0.9f));
        Render.drawRect(x - 25 + 25 * hp, y + 37, 50 * hp, 4, true,
                hp > 0.3f ? Palette.GREEN : Palette.RED);
        super.draw();
    }

    boolean damage(int amount, float sourceX, float sourceY) {
        if (invulnerability > 0 || !scene.isPlaying()) {
            return false;
        }
        int applied = stats.incomingDamage(amount);
        health = Math.max(0, health - applied);
        invulnerability = 0.62f;
        Vector2 knockback = new Vector2(transform.position.x - sourceX, transform.position.y - sourceY);
        if (knockback.lengthSquared() > 0) {
            knockback.normalize().multiply(24);
            transform.position.add(knockback);
            clampToArena();
        }
        scene.onPlayerDamaged(applied);
        if (health <= 0) {
            scene.endRun();
        }
        return true;
    }

    void heal(int amount) {
        health = Math.min(stats.maxHealth, health + Math.max(0, amount));
    }

    void increaseMaxHealth(int amount) {
        stats.maxHealth += amount;
        health += amount;
    }

    int health() {
        return health;
    }

    float healthRatio() {
        return health / (float) stats.maxHealth;
    }

    float dashCooldownRatio() {
        return Math.clamp(dashCooldown / 1.35f, 0, 1);
    }

    Vector2 facing() {
        return new Vector2(facing);
    }

    private void regenerate(float delta) {
        if (stats.regeneration <= 0 || health >= stats.maxHealth) {
            return;
        }
        regenClock += delta * stats.regeneration;
        int points = (int) regenClock;
        if (points > 0) {
            heal(points);
            regenClock -= points;
        }
    }

    private void clampToArena() {
        transform.position.x = Math.clamp(transform.position.x,
                ArenaScene.ARENA_LEFT + RADIUS, ArenaScene.ARENA_RIGHT - RADIUS);
        transform.position.y = Math.clamp(transform.position.y,
                ArenaScene.ARENA_TOP + RADIUS, ArenaScene.ARENA_BOTTOM - RADIUS);
    }
}
