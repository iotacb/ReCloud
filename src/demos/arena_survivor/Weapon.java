package arena_survivor;

import de.kostari.cloud.core.lighting.Light2D;
import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.utils.math.Vector2;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.render.Texture;
import de.kostari.cloud.core.utils.types.Color4f;
import de.kostari.cloud.core.window.Time;

final class Weapon extends GameObject {
    enum Type {
        PULSE_CARBINE("Pulse Carbine", "Reliable aether bolts", 18, 0.72f, 440, 660, 0, 0, Palette.CYAN),
        SCATTER_WAND("Scatter Wand", "Three-way close burst", 11, 1.02f, 370, 570, 0, 0, Palette.GOLD),
        RAIL_NEEDLE("Rail Needle", "Long range, pierces 3", 31, 1.48f, 590, 900, 2, 0, Palette.VIOLET),
        FROST_ORB("Frost Orb", "Slows enemies on hit", 14, 0.94f, 420, 500, 0, 1.25f, Palette.BLUE),
        RIFT_BLADE("Rift Blade", "Fast, brutal short range", 27, 0.66f, 175, 520, 1, 0, Palette.RED);

        final String label;
        final String description;
        final int damage;
        final float cooldown;
        final float range;
        final float projectileSpeed;
        final int pierce;
        final float slow;
        final Color4f color;

        Type(String label, String description, int damage, float cooldown, float range,
                float projectileSpeed, int pierce, float slow, Color4f color) {
            this.label = label;
            this.description = description;
            this.damage = damage;
            this.cooldown = cooldown;
            this.range = range;
            this.projectileSpeed = projectileSpeed;
            this.pierce = pierce;
            this.slow = slow;
            this.color = color;
        }
    }

    private final ArenaScene scene;
    private final Player player;
    private final RunStats stats;
    private final Texture texture;
    private final Light2D muzzleLight;
    final Type type;

    private int slot;
    private float cooldown;
    private float recoil;
    private float aimAngle;

    Weapon(ArenaScene scene, Player player, GameAssets assets, RunStats stats, Type type, int slot) {
        this.scene = scene;
        this.player = player;
        this.stats = stats;
        this.type = type;
        this.slot = slot;
        texture = switch (type) {
            case PULSE_CARBINE -> assets.wand;
            case SCATTER_WAND -> assets.dagger;
            case RAIL_NEEDLE -> assets.sword;
            case FROST_ORB -> assets.orb;
            case RIFT_BLADE -> assets.axe;
        };
        transform.position.set(player.transform.position);
        muzzleLight = addComponent(new Light2D(115, type.color)
                .intensity(0)
                .falloff(2.1f)
                .softness(7)
                .shadowStrength(0.58f));
    }

    @Override
    public void update() {
        float delta = Math.min(Time.delta, 0.05f);
        cooldown -= delta;
        recoil = Math.max(0, recoil - delta * 6.5f);
        muzzleLight.radius(115 + recoil * 125);
        muzzleLight.intensity(recoil * 1.05f);
        updateOrbit();

        if (scene.isPlaying() && cooldown <= 0) {
            Enemy target = scene.nearestEnemy(transform.position.x, transform.position.y,
                    type.range * stats.rangeMultiplier);
            if (target != null) {
                fire(target);
                cooldown = type.cooldown / Math.max(0.2f, stats.attackSpeedMultiplier);
            }
        }
        super.update();
    }

    @Override
    public void draw() {
        float angle = aimAngle + 90;
        float kick = recoil * 8;
        float x = transform.position.x - (float) Math.cos(Math.toRadians(aimAngle)) * kick;
        float y = transform.position.y - (float) Math.sin(Math.toRadians(aimAngle)) * kick;
        Render.drawRotatedRect(x, y, 43, 43, true, Palette.alpha(type.color, 0.13f), 45);
        Render.drawRotatedTexture(texture, x, y, 38, 38, true, angle, Palette.WHITE);
        super.draw();
    }

    void setSlot(int slot) {
        this.slot = slot;
    }

    private void updateOrbit() {
        int count = Math.max(1, scene.weaponCount());
        float base = (float) (Math.PI * 2 * slot / count);
        float breathing = 3 * (float) Math.sin(Time.timePassed * 2.2f + slot);
        float radius = 57 + breathing;
        transform.position.set(
                player.transform.position.x + (float) Math.cos(base) * radius,
                player.transform.position.y + (float) Math.sin(base) * radius);
    }

    private void fire(Enemy target) {
        Vector2 direction = new Vector2(target.transform.position).sub(transform.position);
        if (direction.lengthSquared() == 0) {
            direction.set(1, 0);
        } else {
            direction.normalize();
        }
        aimAngle = (float) Math.toDegrees(Math.atan2(direction.y, direction.x));
        recoil = 1;

        int damage = stats.damage(type.damage);
        float speed = type.projectileSpeed * stats.projectileSpeedMultiplier;
        switch (type) {
            case SCATTER_WAND -> {
                scene.spawnFriendlyProjectile(transform.position.x, transform.position.y,
                        new Vector2(direction).rotateDegrees(-10), speed, damage, 6, 0, 0, type.color);
                scene.spawnFriendlyProjectile(transform.position.x, transform.position.y,
                        direction, speed, damage, 6, 0, 0, type.color);
                scene.spawnFriendlyProjectile(transform.position.x, transform.position.y,
                        new Vector2(direction).rotateDegrees(10), speed, damage, 6, 0, 0, type.color);
            }
            case RIFT_BLADE -> {
                scene.spawnFriendlyProjectile(transform.position.x, transform.position.y,
                        direction, speed, damage, 13, type.pierce, 0, type.color);
                scene.onBladeSwing(transform.position.x, transform.position.y, aimAngle);
            }
            default -> scene.spawnFriendlyProjectile(transform.position.x, transform.position.y,
                    direction, speed, damage, type == Type.RAIL_NEEDLE ? 8 : 7,
                    type.pierce, type.slow, type.color);
        }
        scene.onWeaponFired(type, transform.position.x, transform.position.y);
    }
}
