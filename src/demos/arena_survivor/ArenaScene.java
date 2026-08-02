package arena_survivor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.joml.Vector2f;

import de.kostari.cloud.core.lighting.LightingEffect;
import de.kostari.cloud.core.scene.Scene;
import de.kostari.cloud.core.scene.SceneManager;
import de.kostari.cloud.core.ui.Button;
import de.kostari.cloud.core.ui.ButtonSkin;
import de.kostari.cloud.core.ui.Canvas;
import de.kostari.cloud.core.ui.Panel;
import de.kostari.cloud.core.ui.Text;
import de.kostari.cloud.core.ui.TextAlign;
import de.kostari.cloud.core.ui.UIMaterial;
import de.kostari.cloud.core.utils.Colors;
import de.kostari.cloud.core.utils.input.Keys;
import de.kostari.cloud.core.utils.math.Vector2;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.render.post.BloomEffect;
import de.kostari.cloud.core.utils.render.post.ColorGradingEffect;
import de.kostari.cloud.core.utils.render.post.VignetteEffect;
import de.kostari.cloud.core.utils.types.Color4f;
import de.kostari.cloud.core.window.Input;
import de.kostari.cloud.core.window.Time;
import de.kostari.cloud.core.window.Window;
import demo_ui.DemoUI;

public final class ArenaScene extends Scene {
    static final float ARENA_LEFT = 0;
    static final float ARENA_TOP = 0;
    static final float ARENA_RIGHT = 2200;
    static final float ARENA_BOTTOM = 1400;

    private enum State {
        PLAYING,
        SHOP,
        PAUSED,
        GAME_OVER
    }

    private enum UpgradeKind {
        WEAPON,
        MAX_HEALTH,
        DAMAGE,
        ATTACK_SPEED,
        RANGE,
        MOVE_SPEED,
        ARMOR,
        REGENERATION,
        PROJECTILE_SPEED,
        REPAIR
    }

    private record UpgradeOffer(UpgradeKind kind, Weapon.Type weapon, String label,
            String description, int cost) {
    }

    private final Random random = new Random();
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<Weapon> weapons = new ArrayList<>();
    private final UpgradeOffer[] offers = new UpgradeOffer[3];
    private final boolean[] boughtOffers = new boolean[3];

    private GameAssets assets;
    private GameAudio audio;
    private RunStats stats;
    private Player player;
    private EffectsPool effects;
    private State state = State.PLAYING;

    private ArenaShockwaveEffect shockwaveEffect;
    private ArenaAtmosphereEffect atmosphereEffect;
    private ArenaKineticEffect kineticEffect;
    private LightingEffect lightingEffect;
    private BloomEffect bloomEffect;
    private VignetteEffect vignetteEffect;
    private ColorGradingEffect colorEffect;

    private Canvas hud;
    private Text waveText;
    private Text scrapText;
    private Text healthText;
    private Text enemyText;
    private Text weaponText;
    private Text controlsText;
    private Text toastText;
    private Panel healthFill;
    private Panel dashTrack;
    private Panel dashFill;
    private Panel topRightPanel;
    private Panel shopShade;
    private Panel shopCard;
    private Text shopTitle;
    private Text shopSummary;
    private final Button[] offerButtons = new Button[3];
    private Button rerollButton;
    private Button nextWaveButton;
    private Panel pauseShade;
    private Text pauseText;
    private Panel gameOverShade;
    private Text gameOverTitle;
    private Text gameOverStats;
    private Button restartButton;

    private int wave;
    private int waveTarget;
    private int spawned;
    private int clearBonus;
    private float spawnClock;
    private float waveElapsed;
    private float toastTimer;
    private float cameraTrauma;
    private float hitAudioCooldown;
    private float ambientPulse;

    @Override
    public void init() {
        Window.get().setClearColor(Palette.VOID.r, Palette.VOID.g, Palette.VOID.b, 1);
        assets = new GameAssets();
        audio = new GameAudio();
        stats = new RunStats();

        atmosphereEffect = Render.postProcessing().add(new ArenaAtmosphereEffect());
        lightingEffect = Render.postProcessing().enableLighting()
                .ambientColor(0.72f, 0.82f, 1f)
                .ambientIntensity(0.72f);
        bloomEffect = Render.postProcessing().enableBloom(0.62f, 0.46f, 2.4f);
        colorEffect = Render.postProcessing().enableColorGrading()
                .contrast(1.08f).saturation(1.16f).temperature(-0.025f).gamma(0.98f);
        vignetteEffect = Render.postProcessing().enableVignette(0.36f, 0.67f, 0.3f)
                .color(0.01f, 0.015f, 0.04f);
        kineticEffect = Render.postProcessing().add(new ArenaKineticEffect());
        shockwaveEffect = Render.postProcessing().add(new ArenaShockwaveEffect());

        player = new Player(this, assets, stats,
                (ARENA_LEFT + ARENA_RIGHT) * 0.5f,
                (ARENA_TOP + ARENA_BOTTOM) * 0.5f);
        effects = new EffectsPool();
        addWeapon(Weapon.Type.PULSE_CARBINE);
        buildHud();
        startNextWave();
        super.init();

        getCamera().setZoomLimits(0.8f, 1.1f);
        getCamera().setZoom(1);
        getCamera().centerOn(player);
    }

    @Override
    public void update() {
        float delta = Math.min(Time.delta, 0.05f);
        shockwaveEffect.update(delta);
        kineticEffect.update(delta);
        hitAudioCooldown = Math.max(0, hitAudioCooldown - delta);
        toastTimer = Math.max(0, toastTimer - delta);
        cameraTrauma = Math.max(0, cameraTrauma - delta * 2.4f);
        ambientPulse += delta;

        handleGlobalInput();
        if (state == State.PLAYING) {
            updateWave(delta);
            super.update();
            resolveCollisions();
            cleanLists();
            if (spawned >= waveTarget && enemies.isEmpty()) {
                finishWave();
            }
        } else if (state == State.GAME_OVER) {
            super.update();
            cleanLists();
        }

        updateCamera();
        updatePostEffects(delta);
        updateHud();
    }

    @Override
    public void draw() {
        drawArena();
        super.draw();
    }

    @Override
    public void dispose() {
        if (shockwaveEffect != null) {
            Render.postProcessing().remove(shockwaveEffect);
            shockwaveEffect = null;
        }
        if (kineticEffect != null) {
            Render.postProcessing().remove(kineticEffect);
            kineticEffect = null;
        }
        if (atmosphereEffect != null) {
            Render.postProcessing().remove(atmosphereEffect);
            atmosphereEffect = null;
        }
        if (lightingEffect != null) {
            Render.postProcessing().remove(lightingEffect);
            lightingEffect = null;
        }
        if (vignetteEffect != null) {
            Render.postProcessing().remove(vignetteEffect);
            vignetteEffect = null;
        }
        if (colorEffect != null) {
            Render.postProcessing().remove(colorEffect);
            colorEffect = null;
        }
        if (bloomEffect != null) {
            Render.postProcessing().remove(bloomEffect);
            bloomEffect = null;
        }
        if (audio != null) {
            audio.dispose();
            audio = null;
        }
        super.dispose();
    }

    boolean isPlaying() {
        return state == State.PLAYING;
    }

    int wave() {
        return wave;
    }

    int weaponCount() {
        return weapons.size();
    }

    Enemy nearestEnemy(float x, float y, float range) {
        Enemy nearest = null;
        float nearestSquared = range * range;
        for (Enemy enemy : enemies) {
            if (!enemy.alive()) {
                continue;
            }
            float dx = enemy.transform.position.x - x;
            float dy = enemy.transform.position.y - y;
            float distanceSquared = dx * dx + dy * dy;
            if (distanceSquared < nearestSquared) {
                nearestSquared = distanceSquared;
                nearest = enemy;
            }
        }
        return nearest;
    }

    void spawnFriendlyProjectile(float x, float y, Vector2 direction, float speed,
            int damage, float radius, int pierce, float slow, Color4f color) {
        projectiles.add(new Projectile(true, null, x, y, direction,
                speed, damage, radius, 1.7f, pierce, slow, color));
    }

    void spawnEnemyProjectile(float x, float y, Vector2 direction, float speed, int damage) {
        projectiles.add(new Projectile(false, assets.orb, x, y, direction,
                speed, damage + wave / 4, 9, 5f, 0, 0, Palette.RED));
        audio.enemyShot();
    }

    void onWeaponFired(Weapon.Type type, float x, float y) {
        audio.playerShot();
        effects.burst(x, y, type.color, type == Weapon.Type.SCATTER_WAND ? 7 : 4, 90);
    }

    void onBladeSwing(float x, float y, float angle) {
        new ShockRing(x, y, Palette.RED, 78, 0.22f);
    }

    void onEnemyHit(Enemy enemy) {
        if (hitAudioCooldown <= 0) {
            audio.enemyHit();
            hitAudioCooldown = 0.045f;
        }
        effects.burst(enemy.transform.position.x, enemy.transform.position.y,
                enemy.type.color, 5, 95);
    }

    void onEnemyKilled(Enemy enemy) {
        stats.kills++;
        int value = switch (enemy.type) {
            case RAT, SLIME -> 1;
            case GOBLIN, GHOST -> 2;
            case BRUTE -> 4;
            case DEMON -> 14;
        };
        stats.scrap += value;
        audio.enemyHit();
        effects.burst(enemy.transform.position.x, enemy.transform.position.y,
                enemy.type.color, enemy.type == Enemy.Type.DEMON ? 70 : 22,
                enemy.type == Enemy.Type.DEMON ? 330 : 210);
        new ShockRing(enemy.transform.position.x, enemy.transform.position.y,
                enemy.type.color, enemy.type == Enemy.Type.DEMON ? 230 : 82,
                enemy.type == Enemy.Type.DEMON ? 0.72f : 0.34f);
        if (enemy.type == Enemy.Type.DEMON) {
            triggerPostShock(enemy.transform.position.x, enemy.transform.position.y,
                    enemy.type.color, 1.15f, 0.86f, false);
            triggerKineticRift(enemy.transform.position.x, enemy.transform.position.y, 1.2f);
            cameraTrauma = Math.max(cameraTrauma, 0.75f);
            showToast("RIFT BARON SHATTERED", 2.2f);
        }
    }

    void onPlayerDash() {
        Vector2 direction = player.facing();
        effects.burst(player.transform.position.x - direction.x * 12,
                player.transform.position.y - direction.y * 12, Palette.CYAN, 24, 220);
        new ShockRing(player.transform.position.x, player.transform.position.y,
                Palette.CYAN, 105, 0.34f);
        triggerPostShock(player.transform.position.x, player.transform.position.y,
                Palette.CYAN, 0.42f, 0.42f, false);
        triggerKineticDash(direction);
        cameraTrauma = Math.max(cameraTrauma, 0.18f);
    }

    void onPlayerDamaged(int damage) {
        audio.playerHurt();
        effects.burst(player.transform.position.x, player.transform.position.y,
                Palette.RED, 28, 240);
        new ShockRing(player.transform.position.x, player.transform.position.y,
                Palette.RED, 120, 0.42f);
        triggerPostShock(player.transform.position.x, player.transform.position.y,
                Palette.RED, 0.78f, 0.52f, true);
        triggerKineticDamage(Math.min(1.2f, 0.62f + damage * 0.025f));
        cameraTrauma = Math.max(cameraTrauma, Math.min(0.8f, 0.28f + damage * 0.018f));
    }

    void endRun() {
        if (state == State.GAME_OVER) {
            return;
        }
        state = State.GAME_OVER;
        showToast("SIGNAL LOST", 3);
        gameOverShade.visible(true);
        gameOverTitle.visible(true);
        gameOverStats.visible(true);
        restartButton.visible(true);
        gameOverStats.text("SURVIVED " + wave + " WAVES\n"
                + stats.kills + " HOSTILES PURGED\n"
                + weapons.size() + "/8 WEAPON SLOTS FILLED\n\n"
                + "PRESS R OR CLICK REBOOT");
        triggerPostShock(player.transform.position.x, player.transform.position.y,
                Palette.RED, 1.2f, 0.9f, true);
        triggerKineticDamage(1.25f);
    }

    private void handleGlobalInput() {
        if (state == State.GAME_OVER) {
            if (Input.keyPressed(Keys.KEY_R) || Input.keyPressed(Keys.KEY_ENTER)) {
                restart();
            }
            return;
        }
        if (state == State.SHOP) {
            if (Input.keyPressed(Keys.KEY_ENTER)) {
                startNextWave();
            }
            return;
        }
        if (Input.keyPressed(Keys.KEY_ESCAPE) || Input.keyPressed(Keys.KEY_P)) {
            state = state == State.PAUSED ? State.PLAYING : State.PAUSED;
            pauseShade.visible(state == State.PAUSED);
            pauseText.visible(state == State.PAUSED);
        }
    }

    private void updateWave(float delta) {
        waveElapsed += delta;
        if (spawned >= waveTarget) {
            return;
        }
        spawnClock -= delta;
        if (spawnClock <= 0) {
            spawnEnemy();
            spawned++;
            float interval = Math.max(0.24f, 1.02f - wave * 0.055f);
            spawnClock = interval * (0.82f + random.nextFloat() * 0.36f);
        }
    }

    private void spawnEnemy() {
        Enemy.Type type = chooseEnemyType();
        Vector2 point = randomSpawnPoint(type.radius + 12);
        enemies.add(new Enemy(this, player, assets, type, wave,
                point.x, point.y, random.nextLong()));
        if (type == Enemy.Type.DEMON) {
            showToast("RIFT BARON INBOUND", 2.5f);
            triggerPostShock(point.x, point.y, Palette.VIOLET, 0.72f, 0.72f, false);
            triggerKineticRift(point.x, point.y, 1.05f);
        }
    }

    private Enemy.Type chooseEnemyType() {
        if (wave % 5 == 0 && spawned == waveTarget - 1) {
            return Enemy.Type.DEMON;
        }
        float roll = random.nextFloat();
        if (wave >= 4 && roll < Math.min(0.12f, 0.035f + wave * 0.006f)) {
            return Enemy.Type.BRUTE;
        }
        if (wave >= 3 && roll < 0.27f) {
            return Enemy.Type.GHOST;
        }
        if (wave >= 2 && roll < 0.49f) {
            return Enemy.Type.GOBLIN;
        }
        if (roll < 0.70f) {
            return Enemy.Type.RAT;
        }
        return Enemy.Type.SLIME;
    }

    private Vector2 randomSpawnPoint(float margin) {
        int edge = random.nextInt(4);
        return switch (edge) {
            case 0 -> new Vector2(ARENA_LEFT + margin,
                    ARENA_TOP + margin + random.nextFloat() * (ARENA_BOTTOM - ARENA_TOP - margin * 2));
            case 1 -> new Vector2(ARENA_RIGHT - margin,
                    ARENA_TOP + margin + random.nextFloat() * (ARENA_BOTTOM - ARENA_TOP - margin * 2));
            case 2 -> new Vector2(ARENA_LEFT + margin + random.nextFloat() * (ARENA_RIGHT - ARENA_LEFT - margin * 2),
                    ARENA_TOP + margin);
            default -> new Vector2(ARENA_LEFT + margin + random.nextFloat() * (ARENA_RIGHT - ARENA_LEFT - margin * 2),
                    ARENA_BOTTOM - margin);
        };
    }

    private void resolveCollisions() {
        for (Projectile projectile : projectiles) {
            if (!projectile.alive()) {
                continue;
            }
            if (projectile.friendly) {
                for (Enemy enemy : enemies) {
                    if (!enemy.alive() || !overlaps(projectile.transform.position.x,
                            projectile.transform.position.y, projectile.radius,
                            enemy.transform.position.x, enemy.transform.position.y, enemy.radius())) {
                        continue;
                    }
                    if (!projectile.canHit(enemy)) {
                        continue;
                    }
                    enemy.damage(projectile.damage, projectile.slowSeconds);
                    projectile.hit();
                    if (!projectile.alive()) {
                        break;
                    }
                }
            } else if (overlaps(projectile.transform.position.x, projectile.transform.position.y,
                    projectile.radius, player.transform.position.x, player.transform.position.y, Player.RADIUS)) {
                if (player.damage(projectile.damage, projectile.transform.position.x, projectile.transform.position.y)) {
                    projectile.expire();
                }
            }
        }

        for (Enemy enemy : enemies) {
            if (enemy.alive() && overlaps(enemy.transform.position.x, enemy.transform.position.y, enemy.radius(),
                    player.transform.position.x, player.transform.position.y, Player.RADIUS)) {
                player.damage(enemy.contactDamage(), enemy.transform.position.x, enemy.transform.position.y);
            }
        }
    }

    private void cleanLists() {
        for (Enemy enemy : enemies) {
            if (!enemy.alive()) {
                removeGameObjects(enemy);
            }
        }
        for (Projectile projectile : projectiles) {
            if (!projectile.alive()) {
                removeGameObjects(projectile);
            }
        }
        enemies.removeIf(enemy -> !enemy.alive());
        projectiles.removeIf(projectile -> !projectile.alive());
    }

    private void finishWave() {
        state = State.SHOP;
        clearBonus = 8 + wave * 4;
        stats.scrap += clearBonus;
        player.heal(Math.max(8, Math.round(stats.maxHealth * 0.14f)));
        audio.waveClear();
        effects.burst(player.transform.position.x, player.transform.position.y,
                Palette.GOLD, 65, 300);
        new ShockRing(player.transform.position.x, player.transform.position.y,
                Palette.GOLD, 260, 0.78f);
        triggerPostShock(player.transform.position.x, player.transform.position.y,
                Palette.GOLD, 1.02f, 0.84f, false);
        kineticEffect.triggerWave(Window.get().getWidth(), Window.get().getHeight(), 0.92f);
        cameraTrauma = Math.max(cameraTrauma, 0.5f);
        generateOffers();
        setShopVisible(true);
        showToast("WAVE " + wave + " CLEARED  +" + clearBonus + " SCRAP", 2.3f);
    }

    private void startNextWave() {
        if (state == State.GAME_OVER) {
            return;
        }
        setShopVisible(false);
        state = State.PLAYING;
        wave++;
        waveTarget = 8 + wave * 5 + (wave % 5 == 0 ? 1 : 0);
        spawned = 0;
        spawnClock = 0.75f;
        waveElapsed = 0;
        kineticEffect.triggerWave(Window.get().getWidth(), Window.get().getHeight(),
                wave % 5 == 0 ? 0.9f : 0.48f);
        showToast(wave % 5 == 0 ? "BOSS WAVE " + wave : "WAVE " + wave, 1.8f);
    }

    private void generateOffers() {
        List<UpgradeKind> candidates = new ArrayList<>(List.of(
                UpgradeKind.MAX_HEALTH, UpgradeKind.DAMAGE, UpgradeKind.ATTACK_SPEED,
                UpgradeKind.RANGE, UpgradeKind.MOVE_SPEED, UpgradeKind.ARMOR,
                UpgradeKind.REGENERATION, UpgradeKind.PROJECTILE_SPEED, UpgradeKind.REPAIR));
        if (weapons.size() < 8) {
            candidates.add(UpgradeKind.WEAPON);
            candidates.add(UpgradeKind.WEAPON);
        }
        Collections.shuffle(candidates, random);
        for (int i = 0; i < offers.length; i++) {
            offers[i] = createOffer(candidates.get(i));
            boughtOffers[i] = false;
            offerButtons[i].enabled(true);
        }
        refreshOfferButtons();
    }

    private UpgradeOffer createOffer(UpgradeKind kind) {
        int statCost = 7 + wave * 2;
        return switch (kind) {
            case WEAPON -> {
                Weapon.Type type = Weapon.Type.values()[random.nextInt(Weapon.Type.values().length)];
                yield new UpgradeOffer(kind, type, type.label, type.description, 10 + wave * 3);
            }
            case MAX_HEALTH -> new UpgradeOffer(kind, null, "Reinforced Heart", "+20 max health", statCost + 3);
            case DAMAGE -> new UpgradeOffer(kind, null, "Hot Capacitors", "+14% damage", statCost + 4);
            case ATTACK_SPEED -> new UpgradeOffer(kind, null, "Overclock", "+11% attack speed", statCost + 4);
            case RANGE -> new UpgradeOffer(kind, null, "Targeting Lens", "+16% attack range", statCost + 1);
            case MOVE_SPEED -> new UpgradeOffer(kind, null, "Phase Soles", "+8% move speed", statCost);
            case ARMOR -> new UpgradeOffer(kind, null, "Plated Weave", "+3 armor", statCost + 2);
            case REGENERATION -> new UpgradeOffer(kind, null, "Nanite Colony", "+0.7 health per second", statCost + 4);
            case PROJECTILE_SPEED -> new UpgradeOffer(kind, null, "Flux Barrel", "+13% projectile speed", statCost);
            case REPAIR -> new UpgradeOffer(kind, null, "Field Repair", "Restore 40 health", Math.max(5, statCost - 2));
        };
    }

    private void buyOffer(int index) {
        if (state != State.SHOP || index < 0 || index >= offers.length || boughtOffers[index]) {
            return;
        }
        UpgradeOffer offer = offers[index];
        if (offer.kind == UpgradeKind.WEAPON && weapons.size() >= 8) {
            showToast("ALL 8 WEAPON SLOTS ARE FULL", 1.2f);
            return;
        }
        if (stats.scrap < offer.cost) {
            showToast("NOT ENOUGH SCRAP", 1.1f);
            return;
        }
        stats.scrap -= offer.cost;
        applyOffer(offer);
        boughtOffers[index] = true;
        offerButtons[index].enabled(false);
        audio.shopBuy();
        effects.burst(player.transform.position.x, player.transform.position.y,
                Palette.GOLD, 18, 180);
        refreshOfferButtons();
    }

    private void applyOffer(UpgradeOffer offer) {
        switch (offer.kind) {
            case WEAPON -> addWeapon(offer.weapon);
            case MAX_HEALTH -> player.increaseMaxHealth(20);
            case DAMAGE -> stats.damageMultiplier *= 1.14f;
            case ATTACK_SPEED -> stats.attackSpeedMultiplier *= 1.11f;
            case RANGE -> stats.rangeMultiplier *= 1.16f;
            case MOVE_SPEED -> stats.moveSpeed *= 1.08f;
            case ARMOR -> stats.armor += 3;
            case REGENERATION -> stats.regeneration += 0.7f;
            case PROJECTILE_SPEED -> stats.projectileSpeedMultiplier *= 1.13f;
            case REPAIR -> player.heal(40);
        }
    }

    private void rerollOffers() {
        int cost = 4 + wave;
        if (state != State.SHOP || stats.scrap < cost) {
            showToast("REROLL COSTS " + cost + " SCRAP", 1.2f);
            return;
        }
        stats.scrap -= cost;
        audio.shopBuy();
        generateOffers();
    }

    private void addWeapon(Weapon.Type type) {
        if (weapons.size() >= 8) {
            return;
        }
        weapons.add(new Weapon(this, player, assets, stats, type, weapons.size()));
        for (int i = 0; i < weapons.size(); i++) {
            weapons.get(i).setSlot(i);
        }
    }

    private void updateCamera() {
        getCamera().followObject(player, 0.085f);
        Vector2 viewport = getCamera().getViewportWorldSize();
        float maxX = Math.max(ARENA_LEFT, ARENA_RIGHT - viewport.x);
        float maxY = Math.max(ARENA_TOP, ARENA_BOTTOM - viewport.y);
        float x = Math.clamp(getCamera().transform.position.x, ARENA_LEFT, maxX);
        float y = Math.clamp(getCamera().transform.position.y, ARENA_TOP, maxY);
        if (cameraTrauma > 0) {
            float amplitude = cameraTrauma * cameraTrauma * 18;
            x += (random.nextFloat() * 2 - 1) * amplitude;
            y += (random.nextFloat() * 2 - 1) * amplitude;
        }
        getCamera().setPosition(x, y);
    }

    private void updatePostEffects(float delta) {
        if (atmosphereEffect == null || getCamera() == null) {
            return;
        }
        Vector2f playerScreen = getCamera().worldToScreen(
                player.transform.position.x, player.transform.position.y);
        float danger = 1 - player.healthRatio();
        float bossEnergy = state == State.PLAYING && wave % 5 == 0 ? 1 : 0;
        float combat = state == State.PLAYING
                ? Math.min(1, enemies.size() / 18f + projectiles.size() / 42f)
                : 0.12f;
        atmosphereEffect.update(delta, playerScreen.x, playerScreen.y,
                Window.get().getWidth(), Window.get().getHeight(), danger, bossEnergy, combat);
        bloomEffect.threshold(0.62f - combat * 0.07f - bossEnergy * 0.04f)
                .intensity(0.44f + combat * 0.12f + bossEnergy * 0.10f)
                .radius(2.35f + combat * 0.35f);
        vignetteEffect.intensity(0.32f + danger * 0.18f + bossEnergy * 0.05f)
                .radius(0.68f - danger * 0.05f)
                .smoothness(0.30f + danger * 0.08f);
        colorEffect.saturation(1.13f + combat * 0.08f - danger * 0.05f)
                .contrast(1.07f + combat * 0.025f)
                .temperature(-0.025f + bossEnergy * 0.035f - danger * 0.02f);
        lightingEffect
                .ambientColor(0.72f + danger * 0.08f,
                        0.82f - danger * 0.14f,
                        1f - bossEnergy * 0.08f)
                .ambientIntensity(0.72f - danger * 0.08f + combat * 0.03f);
    }

    private void triggerPostShock(float worldX, float worldY, Color4f color,
            float strength, float duration, boolean damage) {
        if (getCamera() == null || shockwaveEffect == null) {
            return;
        }
        Vector2f screen = getCamera().worldToScreen(worldX, worldY);
        shockwaveEffect.trigger(screen.x, screen.y, Window.get().getWidth(), Window.get().getHeight(),
                color, strength, duration, damage);
    }

    private void triggerKineticDash(Vector2 direction) {
        if (kineticEffect == null || getCamera() == null) {
            return;
        }
        Vector2f screen = getCamera().worldToScreen(
                player.transform.position.x, player.transform.position.y);
        kineticEffect.triggerDash(screen.x, screen.y,
                Window.get().getWidth(), Window.get().getHeight(), direction.x, direction.y);
    }

    private void triggerKineticDamage(float strength) {
        if (kineticEffect == null || getCamera() == null) {
            return;
        }
        Vector2f screen = getCamera().worldToScreen(
                player.transform.position.x, player.transform.position.y);
        kineticEffect.triggerDamage(screen.x, screen.y,
                Window.get().getWidth(), Window.get().getHeight(), strength);
    }

    private void triggerKineticRift(float worldX, float worldY, float strength) {
        if (kineticEffect == null || getCamera() == null) {
            return;
        }
        Vector2f screen = getCamera().worldToScreen(worldX, worldY);
        kineticEffect.triggerRift(screen.x, screen.y,
                Window.get().getWidth(), Window.get().getHeight(), strength);
    }

    private void drawArena() {
        Render.drawRect(ARENA_LEFT - 600, ARENA_TOP - 600,
                ARENA_RIGHT - ARENA_LEFT + 1200, ARENA_BOTTOM - ARENA_TOP + 1200, Palette.VOID);

        int tileSize = 64;
        for (int y = (int) ARENA_TOP; y < ARENA_BOTTOM; y += tileSize) {
            for (int x = (int) ARENA_LEFT; x < ARENA_RIGHT; x += tileSize) {
                boolean alternate = ((x / tileSize) + (y / tileSize)) % 2 == 0;
                Color4f tint = alternate ? Palette.FLOOR_A : Palette.FLOOR_B;
                Render.drawRect(x, y, tileSize, tileSize, tint);
                Render.drawTexture(assets.floor, x, y, tileSize, tileSize, false,
                        new Color4f(0.48f, 0.72f, 0.88f, alternate ? 0.16f : 0.11f));
            }
        }

        float pulse = 0.60f + 0.25f * (float) Math.sin(ambientPulse * 2.1f);
        Color4f borderGlow = Palette.alpha(Palette.CYAN, pulse * 0.16f);
        Color4f border = Palette.alpha(Palette.BORDER, 0.88f);
        Render.drawRect(ARENA_LEFT, ARENA_TOP, ARENA_RIGHT - ARENA_LEFT, 22, borderGlow);
        Render.drawRect(ARENA_LEFT, ARENA_BOTTOM - 22, ARENA_RIGHT - ARENA_LEFT, 22, borderGlow);
        Render.drawRect(ARENA_LEFT, ARENA_TOP, 22, ARENA_BOTTOM - ARENA_TOP, borderGlow);
        Render.drawRect(ARENA_RIGHT - 22, ARENA_TOP, 22, ARENA_BOTTOM - ARENA_TOP, borderGlow);
        Render.drawRect(ARENA_LEFT, ARENA_TOP, ARENA_RIGHT - ARENA_LEFT, 4, border);
        Render.drawRect(ARENA_LEFT, ARENA_BOTTOM - 4, ARENA_RIGHT - ARENA_LEFT, 4, border);
        Render.drawRect(ARENA_LEFT, ARENA_TOP, 4, ARENA_BOTTOM - ARENA_TOP, border);
        Render.drawRect(ARENA_RIGHT - 4, ARENA_TOP, 4, ARENA_BOTTOM - ARENA_TOP, border);

        for (int x = 128; x < ARENA_RIGHT; x += 256) {
            Render.drawRect(x, ARENA_TOP + 34, 1, ARENA_BOTTOM - ARENA_TOP - 68, Palette.GRID);
        }
        for (int y = 128; y < ARENA_BOTTOM; y += 256) {
            Render.drawRect(ARENA_LEFT + 34, y, ARENA_RIGHT - ARENA_LEFT - 68, 1, Palette.GRID);
        }
    }

    private void buildHud() {
        hud = new Canvas();

        Panel topLeft = new Panel();
        topLeft.background(DemoUI.surface(DemoUI.CYAN, 10));
        hud.append(topLeft, 18, 16, 420, 108);
        waveText = new Text("").fontScale(0.50f).color(Colors.hex("#48f4da")).shadow(2);
        hud.append(waveText, 34, 28, 380, 28);
        enemyText = new Text("").fontScale(0.31f).color(Colors.hex("#d6edff")).shadow(1);
        hud.append(enemyText, 34, 64, 380, 20);
        healthText = new Text("").fontScale(0.31f).color(Colors.hex("#ff9aa9")).shadow(1);
        hud.append(healthText, 34, 91, 160, 18);

        Panel healthTrack = new Panel();
        healthTrack.background(DemoUI.meterTrack(DemoUI.RED));
        healthFill = new Panel();
        healthFill.background(DemoUI.meterFill(DemoUI.RED));
        hud.append(healthTrack, 195, 91, 220, 14);
        hud.append(healthFill, 197, 93, 216, 10);

        topRightPanel = new Panel();
        topRightPanel.background(DemoUI.surface(DemoUI.GOLD, 10));
        hud.append(topRightPanel, 0, 16, 340, 108);
        scrapText = new Text("").fontScale(0.46f).color(Colors.hex("#ffc74e")).align(TextAlign.END).shadow(2);
        hud.append(scrapText, 0, 28, 305, 26);
        weaponText = new Text("").fontScale(0.29f).color(Colors.hex("#d6edff"))
                .align(TextAlign.END).lineHeight(1.35f);
        hud.append(weaponText, 0, 64, 305, 48);

        dashTrack = new Panel();
        dashTrack.background(DemoUI.meterTrack(DemoUI.CYAN));
        dashFill = new Panel();
        dashFill.background(DemoUI.meterFill(DemoUI.CYAN));
        hud.append(dashTrack, 18, 0, 240, 10);
        hud.append(dashFill, 20, 0, 236, 6);

        controlsText = new Text("WASD / ARROWS  MOVE     SPACE  PHASE DASH     ESC  PAUSE")
                .fontScale(0.27f).color(Colors.hex("#8da4bb")).align(TextAlign.CENTER).shadow(1);
        hud.append(controlsText, 0, 0, 760, 20);

        toastText = new Text("").fontScale(0.76f).color(Colors.hex("#ffffff"))
                .align(TextAlign.CENTER).shadow(3);
        hud.append(toastText, 0, 148, 900, 60);

        buildShopUi();
        buildPauseUi();
        buildGameOverUi();
        updateHudBounds();
    }

    private void buildShopUi() {
        shopShade = new Panel();
        shopShade.background(Colors.hex("#02050ddc"));
        hud.append(shopShade, 0, 0, Canvas.FILL, Canvas.FILL);

        shopCard = new Panel();
        shopCard.background(DemoUI.surface(DemoUI.GOLD, 16));
        hud.append(shopCard, 0, 0, 900, 570);

        shopTitle = new Text("AETHER EXCHANGE").fontScale(0.72f).color(Colors.hex("#ffc74e"))
                .align(TextAlign.CENTER).shadow(2);
        hud.append(shopTitle, 0, 0, 840, 38);
        shopSummary = new Text("").fontScale(0.30f).color(Colors.hex("#cfe9ff"))
                .align(TextAlign.CENTER).lineHeight(1.5f);
        hud.append(shopSummary, 0, 0, 820, 48);

        for (int i = 0; i < offerButtons.length; i++) {
            final int index = i;
            offerButtons[i] = new Button("").onClick(() -> buyOffer(index));
            offerButtons[i].panel().layout().padding(14);
            offerButtons[i].fontScale(0.35f).skin(buttonSkin("#13243bee", "#1e4160ff", "#0d1726ff",
                    "#48f4da88", "#ffffff"));
            offerButtons[i].textElement().shadow(1);
            hud.append(offerButtons[i], 0, 0, 780, 66);
        }

        rerollButton = new Button("REROLL STOCK").onClick(this::rerollOffers);
        rerollButton.fontScale(0.31f).skin(buttonSkin("#201631ee", "#49306aff", "#201631ee",
                "#c069ff88", "#e8c8ff"));
        hud.append(rerollButton, 0, 0, 240, 50);
        nextWaveButton = new Button("DEPLOY NEXT WAVE").onClick(this::startNextWave);
        nextWaveButton.fontScale(0.34f).skin(buttonSkin("#123629ee", "#1e6147ff", "#123629ee",
                "#48f4dabb", "#baffed"));
        hud.append(nextWaveButton, 0, 0, 350, 50);
        setShopVisible(false);
    }

    private void buildPauseUi() {
        pauseShade = new Panel();
        pauseShade.background(Colors.hex("#02050dbb"));
        hud.append(pauseShade, 0, 0, Canvas.FILL, Canvas.FILL);
        pauseText = new Text("SIGNAL PAUSED\n\nPRESS ESC OR P TO RESUME")
                .fontScale(0.58f).lineHeight(1.8f).color(Colors.hex("#48f4da"))
                .align(TextAlign.CENTER).background(DemoUI.surface(DemoUI.CYAN, 14)).shadow(2);
        pauseText.layout().padding(30);
        hud.append(pauseText, 0, 0, 650, 180);
        pauseShade.visible(false);
        pauseText.visible(false);
    }

    private void buildGameOverUi() {
        gameOverShade = new Panel();
        gameOverShade.background(Colors.hex("#10040be5"));
        hud.append(gameOverShade, 0, 0, Canvas.FILL, Canvas.FILL);
        gameOverTitle = new Text("CORE BREACHED").fontScale(0.86f).color(Colors.hex("#ff5268"))
                .align(TextAlign.CENTER).shadow(3);
        hud.append(gameOverTitle, 0, 0, 760, 50);
        gameOverStats = new Text("").fontScale(0.38f).color(Colors.hex("#eaf6ff"))
                .align(TextAlign.CENTER).lineHeight(1.7f);
        hud.append(gameOverStats, 0, 0, 650, 210);
        restartButton = new Button("REBOOT RUN").onClick(this::restart);
        restartButton.fontScale(0.38f).skin(buttonSkin("#461524ee", "#7a223aff", "#461524ee",
                "#ff5268aa", "#ffd9df"));
        hud.append(restartButton, 0, 0, 300, 58);
        gameOverShade.visible(false);
        gameOverTitle.visible(false);
        gameOverStats.visible(false);
        restartButton.visible(false);
    }

    private ButtonSkin buttonSkin(String normal, String hovered, String pressed, String border, String text) {
        Color4f accent = Colors.hex(border);
        UIMaterial normalMaterial = new UIMaterial().fill(Colors.hex(normal))
                .border(1, accent).radius(9).glow(accent, 8, 0.12f);
        UIMaterial hoverMaterial = new UIMaterial().fill(Colors.hex(hovered))
                .border(1, accent).radius(9).glow(accent, 10, 0.36f)
                .sheen(Colors.hex("#ffffffaa"), 0.16f, 0.38f, 0.65f);
        UIMaterial pressedMaterial = new UIMaterial().fill(Colors.hex(pressed))
                .border(1.5f, accent).radius(9).glow(accent, 6, 0.42f);
        UIMaterial disabledMaterial = new UIMaterial().fill(Colors.hex("#121923cc"))
                .border(1, Colors.hex("#8aa0b333")).radius(9);
        Color4f textColor = Colors.hex(text);
        return new ButtonSkin(normalMaterial, hoverMaterial, pressedMaterial, hoverMaterial, disabledMaterial,
                textColor, textColor, textColor, Colors.hex("#718090"));
    }

    private void updateHud() {
        updateHudBounds();
        int remainingToSpawn = Math.max(0, waveTarget - spawned);
        waveText.text("WAVE " + wave + (wave % 5 == 0 ? "  //  BOSS" : ""));
        enemyText.text(enemies.size() + " ACTIVE     " + remainingToSpawn + " INBOUND     "
                + Math.round(waveElapsed) + "s");
        healthText.text(player.health() + " / " + stats.maxHealth);
        scrapText.text("SCRAP  " + stats.scrap);
        weaponText.text("WEAPONS  " + weapons.size() + " / 8\n"
                + "DMG " + Math.round(stats.damageMultiplier * 100) + "%   SPD "
                + Math.round(stats.attackSpeedMultiplier * 100) + "%");

        float healthWidth = 216 * player.healthRatio();
        hud.setBounds(healthFill, 197, 93, healthWidth, 10);
        float dashReady = 1 - player.dashCooldownRatio();
        hud.setBounds(dashFill, 20, Window.get().getHeight() - 35, 236 * dashReady, 6);
        toastText.visible(toastTimer > 0);

        shopSummary.text("WAVE " + wave + " SECURED  //  CLEAR BONUS +" + clearBonus
                + " SCRAP\nBUY ANY NUMBER OF UPGRADES, THEN DEPLOY");
        rerollButton.label("REROLL  [" + (4 + wave) + "]");
    }

    private void updateHudBounds() {
        int width = Window.get().getWidth();
        int height = Window.get().getHeight();
        hud.setBounds(topRightPanel, Math.max(18, width - 358), 16, 340, 108);
        hud.setBounds(scrapText, Math.max(34, width - 340), 28, 305, 26);
        hud.setBounds(weaponText, Math.max(34, width - 340), 64, 305, 48);
        hud.setBounds(dashTrack, 18, height - 37, 240, 10);
        hud.setBounds(dashFill, 20, height - 35, 236, 6);

        hud.setBounds(controlsText, Math.max(0, width * 0.5f - 380), height - 32, 760, 20);
        hud.setBounds(toastText, Math.max(0, width * 0.5f - 450), 148, 900, 60);

        float cardX = Math.max(20, width * 0.5f - 450);
        float cardY = Math.max(20, height * 0.5f - 285);
        hud.setBounds(shopCard, cardX, cardY, 900, 570);
        hud.setBounds(shopTitle, cardX + 30, cardY + 30, 840, 38);
        hud.setBounds(shopSummary, cardX + 40, cardY + 82, 820, 48);
        for (int i = 0; i < offerButtons.length; i++) {
            hud.setBounds(offerButtons[i], cardX + 60, cardY + 160 + i * 82, 780, 66);
        }
        hud.setBounds(rerollButton, cardX + 60, cardY + 498, 240, 50);
        hud.setBounds(nextWaveButton, cardX + 490, cardY + 498, 350, 50);

        hud.setBounds(pauseText, Math.max(0, width * 0.5f - 325), Math.max(0, height * 0.5f - 90), 650, 180);
        hud.setBounds(gameOverTitle, Math.max(0, width * 0.5f - 380), Math.max(20, height * 0.5f - 190), 760, 50);
        hud.setBounds(gameOverStats, Math.max(0, width * 0.5f - 325), Math.max(90, height * 0.5f - 115), 650, 210);
        hud.setBounds(restartButton, Math.max(0, width * 0.5f - 150), Math.max(320, height * 0.5f + 130), 300, 58);
    }

    private void setShopVisible(boolean visible) {
        shopShade.visible(visible);
        shopCard.visible(visible);
        shopTitle.visible(visible);
        shopSummary.visible(visible);
        rerollButton.visible(visible);
        nextWaveButton.visible(visible);
        for (Button button : offerButtons) {
            button.visible(visible);
        }
    }

    private void refreshOfferButtons() {
        for (int i = 0; i < offerButtons.length; i++) {
            UpgradeOffer offer = offers[i];
            if (offer == null) {
                continue;
            }
            offerButtons[i].label(boughtOffers[i]
                    ? "SOLD  //  " + offer.label
                    : offer.label.toUpperCase() + "  //  " + offer.description.toUpperCase()
                            + "  [" + offer.cost + "]");
        }
    }

    private void showToast(String message, float duration) {
        toastText.text(message);
        toastTimer = duration;
        toastText.visible(true);
    }

    private void restart() {
        SceneManager.setScene(ArenaScene.class);
    }

    private static boolean overlaps(float ax, float ay, float ar, float bx, float by, float br) {
        float dx = ax - bx;
        float dy = ay - by;
        float radius = ar + br;
        return dx * dx + dy * dy <= radius * radius;
    }
}
