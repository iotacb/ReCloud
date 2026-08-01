package tower_climber;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

import de.kostari.cloud.core.physics.Collision;
import de.kostari.cloud.core.physics.AABB;
import de.kostari.cloud.core.lighting.LightingEffect;
import de.kostari.cloud.core.scene.Scene;
import de.kostari.cloud.core.scene.SceneManager;
import de.kostari.cloud.core.ui.Button;
import de.kostari.cloud.core.ui.Canvas;
import de.kostari.cloud.core.ui.Flex;
import de.kostari.cloud.core.ui.FlexDirection;
import de.kostari.cloud.core.ui.Panel;
import de.kostari.cloud.core.ui.Text;
import de.kostari.cloud.core.utils.Colors;
import de.kostari.cloud.core.utils.input.Keys;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.render.post.BloomEffect;
import de.kostari.cloud.core.utils.types.Color4f;
import de.kostari.cloud.core.window.Input;
import de.kostari.cloud.core.window.Time;
import de.kostari.cloud.core.window.Window;
import tower_climber.TowerGenerator.EnemyContact;
import tower_climber.RunProgression.Upgrade;
import tower_climber.RunProgression.Weapon;
import tower_climber.main_menu.MainMenuScene;

public class GameScene extends Scene {

    private static final Color4f[] SKY_PALETTE = {
            Colors.from255(4, 12, 24, 255),
            Colors.from255(3, 17, 20, 255),
            Colors.from255(12, 6, 25, 255),
            Colors.from255(5, 10, 31, 255)
    };
    private static final Color4f[] LOWER_SKY_PALETTE = {
            Colors.from255(8, 25, 43, 255),
            Colors.from255(7, 34, 34, 255),
            Colors.from255(30, 13, 45, 255),
            Colors.from255(10, 24, 54, 255)
    };
    private static final Color4f[] TOWER_PALETTE = {
            Colors.from255(8, 21, 34, 255),
            Colors.from255(7, 27, 27, 255),
            Colors.from255(20, 12, 34, 255),
            Colors.from255(9, 18, 42, 255)
    };
    private static final Color4f[] EDGE_PALETTE = {
            Colors.from255(49, 82, 89, 255),
            Colors.from255(57, 108, 88, 255),
            Colors.from255(91, 58, 115, 255),
            Colors.from255(64, 91, 135, 255)
    };
    private static final Color4f[] WINDOW_OFF_PALETTE = {
            Colors.from255(22, 49, 61, 255),
            Colors.from255(20, 61, 51, 255),
            Colors.from255(54, 31, 69, 255),
            Colors.from255(27, 49, 79, 255)
    };
    private static final Color4f[] WINDOW_ON_PALETTE = {
            Colors.from255(255, 190, 86, 210),
            Colors.from255(113, 255, 169, 215),
            Colors.from255(211, 135, 255, 215),
            Colors.from255(155, 220, 255, 220)
    };
    private static final int BASE_MAX_HEALTH = 100;
    private static final float SLINGSHOT_CHARGE_TIME = 1.25f;
    private static final float SLINGSHOT_MIN_VERTICAL_SPEED = 410;
    private static final float SLINGSHOT_MAX_VERTICAL_SPEED = 900;
    private static final float SLINGSHOT_MIN_HORIZONTAL_SPEED = 110;
    private static final float SLINGSHOT_MAX_HORIZONTAL_SPEED = 350;
    private static final float NOVA_CHARGE_DURATION = 1.35f;

    private Canvas hudCanvas;
    private Text statusText;
    private Text healthText;
    private Text xpText;
    private Text weaponText;
    private Text aetherText;
    private Text levelToast;
    private Text comboText;
    private Text controlsText;
    private Text gameOverStats;
    private Text gameOverTitle;
    private Panel healthFill;
    private Panel xpFill;
    private Panel gameOverCard;
    private Panel pauseShade;
    private Panel pauseCard;
    private Panel upgradeShade;
    private Panel upgradeCard;
    private Text upgradeCurrencyText;
    private Text upgradeHintText;
    private Button bladeButton;
    private Button bowButton;
    private Button shurikenButton;
    private Button doubleJumpButton;
    private Button novaButton;
    private Button reservoirButton;
    private Player player;
    private TowerGenerator tower;
    private TowerWarpEffect warpEffect;
    private TowerAtmosphereEffect atmosphereEffect;
    private TowerPixelationEffect pixelationEffect;
    private TowerImpactPostEffect impactPostEffect;
    private BloomEffect bloomEffect;
    private LightingEffect lightingEffect;
    private TowerLightingRig lightingRig;
    private GameAudio audio;
    private PlayerAura playerAura;
    private final RunProgression progression = new RunProgression();
    private final List<WeaponProjectile> projectiles = new ArrayList<>();

    private float checkpointX;
    private float checkpointY;
    private float startPlayerY;
    private int checkpointLevel;
    private float cameraTargetY;
    private float highestPlayerY;
    private int health = BASE_MAX_HEALTH;
    private int maxHealth = BASE_MAX_HEALTH;
    private int xp;
    private int xpToNextLevel = 30;
    private int level = 1;
    private int kills;
    private int combatScore;
    private int combo;
    private int bestCombo;
    private int score;
    private int highScore;
    private int bestAtRunStart;
    private float levelToastTimer;
    private float comboTimer;
    private float cameraTrauma;
    private float stormClock;
    private float nextLightning = 5.5f;
    private float lightningFlash;
    private float damageFlash;
    private float hitStopTimer;
    private float stompFlash;
    private int displayedZone;
    private int previousZone;
    private float zoneBlend = 1;
    private boolean gameOver;
    private boolean newHighScore;
    private boolean paused;
    private boolean debugColliders;
    private TowerPlatform slingshotPlatform;
    private boolean slingshotCharging;
    private float slingshotChargeTime;
    private float slingshotAim = 1;
    private float aimX = 1;
    private float aimY;
    private float weaponCooldown;
    private float novaChargeTime;
    private NovaChargeEffect novaChargeEffect;
    private final boolean replaySeed;
    private final long requestedSeed;

    private RainParticles rain;
    private boolean upgradeOpen;

    public GameScene() {
        replaySeed = false;
        requestedSeed = 0;
    }

    public GameScene(long seed) {
        replaySeed = true;
        requestedSeed = seed;
    }

    @Override
    public void init() {
        Color4f initialSky = SKY_PALETTE[0];
        Window.get().setClearColor(initialSky.r, initialSky.g, initialSky.b, initialSky.a);
        physics().gravity(0, 1_500).substeps(4);
        highScore = HighScores.load();
        bestAtRunStart = highScore;
        TowerSprites.load();
        audio = new GameAudio();
        lightingEffect = Render.postProcessing().enableLighting()
                .ambientColor(0.72f, 0.8f, 0.94f)
                .ambientIntensity(0.72f);
        lightingRig = new TowerLightingRig();
        atmosphereEffect = Render.postProcessing().add(new TowerAtmosphereEffect());
        pixelationEffect = Render.postProcessing().add(
                new TowerPixelationEffect().virtualHeight(360).strength(1f));
        bloomEffect = Render.postProcessing().enableBloom(0.7f, 0.34f, 1.8f);
        warpEffect = Render.postProcessing().add(new TowerWarpEffect());
        impactPostEffect = Render.postProcessing().add(new TowerImpactPostEffect());

        buildLevel();
        buildHud();
        levelToast.text("UPGRADE CORE AVAILABLE\nPRESS U TO OPEN THE ASCENSION GRID");
        levelToastTimer = 2.6f;
        levelToast.visible(true);
        super.init();

        getCamera().setPosition(0, 0);
        cameraTargetY = 0;

        rain = new RainParticles(240, 780);
    }

    @Override
    public void update() {
        if (Input.keyPressed(GLFW.GLFW_KEY_ESCAPE)) {
            if (upgradeOpen) {
                setUpgradeOpen(false);
            } else if (gameOver) {
                returnToMenu();
            } else {
                setPaused(!paused);
            }
            return;
        }
        if (!gameOver && !paused
                && (Input.keyPressed(Keys.KEY_U) || Input.keyPressed(Keys.KEY_TAB))) {
            setUpgradeOpen(!upgradeOpen);
            return;
        }
        if (!gameOver && Input.keyPressed(GLFW.GLFW_KEY_P)) {
            setPaused(!paused);
            return;
        }
        if (paused) {
            updateHud();
            return;
        }
        if (upgradeOpen) {
            updateHud();
            return;
        }
        if (hitStopTimer > 0) {
            hitStopTimer = Math.max(0, hitStopTimer - Math.min(Time.delta, 0.05f));
            updateHud();
            return;
        }
        cameraTrauma = Math.max(0, cameraTrauma - Math.min(Time.delta, 0.05f) * 1.8f);
        damageFlash = Math.max(0, damageFlash - Math.min(Time.delta, 0.05f) * 3.8f);
        stompFlash = Math.max(0, stompFlash - Math.min(Time.delta, 0.05f) * 6.5f);
        zoneBlend = Math.min(1, zoneBlend + Math.min(Time.delta, 0.05f) / 1.15f);
        if (gameOver) {
            if (Input.keyPressed(Keys.KEY_R) || Input.keyPressed(Keys.KEY_ENTER)) {
                restartSameTower();
                return;
            }
            if (Input.keyPressed(Keys.KEY_N)) {
                restartNewTower();
                return;
            }
            warpEffect.update(Time.delta);
            impactPostEffect.update(Time.delta);
            lightningFlash = Math.max(0, lightningFlash - Math.min(Time.delta, 0.05f) * 4.5f);
            updateAtmosphereEffect(Time.delta);
            updateCamera();
            updateHud();
            super.update();
            return;
        }
        if (Input.keyPressed(GLFW.GLFW_KEY_F3)) {
            setDebugColliders(!debugColliders);
        }

        warpEffect.update(Time.delta);
        impactPostEffect.update(Time.delta);
        weaponCooldown = Math.max(0, weaponCooldown - Math.min(Time.delta, 0.05f));
        updateAim();
        updateWeaponSelection();
        updateNova(Time.delta);
        if (player.consumeDashed()) {
            triggerDashFeedback();
        }
        boolean doubleJumped = player.consumeDoubleJumped();
        if (player.consumeJumped()) {
            audio.jump();
            new DustBurst(player.transform.position.x,
                    player.transform.position.y + Player.COLLIDER_HEIGHT * 0.46f,
                    doubleJumped ? 16 : 9, doubleJumped ? 195 : 125, true);
            if (doubleJumped) {
                new StompShockwave(player.transform.position.x,
                        player.transform.position.y + Player.COLLIDER_HEIGHT * 0.34f,
                        Colors.from255(157, 226, 255, 230), 2);
                playerAura.burst(12);
            }
            addCameraTrauma(doubleJumped ? 0.13f : 0.06f);
        }
        float landingStrength = player.consumeLandingStrength();
        if (landingStrength > 260) {
            float impact = Math.min(1, (landingStrength - 220) / 420f);
            new DustBurst(player.transform.position.x,
                    player.transform.position.y + Player.COLLIDER_HEIGHT * 0.48f,
                    7 + Math.round(impact * 11), 95 + impact * 110, false);
            addCameraTrauma(impact * 0.18f);
        }
        levelToastTimer = Math.max(0, levelToastTimer - Math.min(Time.delta, 0.05f));
        levelToast.visible(levelToastTimer > 0);
        comboTimer = Math.max(0, comboTimer - Math.min(Time.delta, 0.05f));
        if (comboTimer <= 0) {
            combo = 0;
        }
        comboText.visible(combo > 0 && comboTimer > 0);
        updateWeather();
        updateAtmosphereEffect(Time.delta);

        updateCheckpoint();
        updateSlingshot(Time.delta);
        tower.update(getCamera().transform.position.y, Window.get().getHeight(), player);
        updateWeaponAttack();
        resolveProjectiles();
        resolveEnemyContact();
        addXp(tower.collectXp(player));
        addAether(tower.collectAether(player));

        float cameraBottom = getCamera().transform.position.y + Window.get().getHeight();
        if (player.transform.position.y > cameraBottom + 135) {
            health = 0;
            endGame(true);
        }

        highestPlayerY = Math.min(highestPlayerY, player.transform.position.y);
        updateScore();
        updateCamera();
        super.update();
        if (!gameOver) {
            resolveHazardContact();
        }
        updateHud();
    }

    @Override
    public void draw() {
        drawBackdrop();
        drawLevelUpWave();
        super.draw();
        drawSlingshotPreview();
        drawAimReticle();
        drawNovaChargeOverlay();
        drawCombatFlash();
        drawDangerOverlay();
        Render.flush();
    }

    @Override
    public void dispose() {
        if (lightingEffect != null) {
            Render.postProcessing().remove(lightingEffect);
            lightingEffect = null;
        }
        if (atmosphereEffect != null) {
            Render.postProcessing().remove(atmosphereEffect);
            atmosphereEffect = null;
        }
        if (pixelationEffect != null) {
            Render.postProcessing().remove(pixelationEffect);
            pixelationEffect = null;
        }
        if (bloomEffect != null) {
            Render.postProcessing().remove(bloomEffect);
            bloomEffect = null;
        }
        if (warpEffect != null) {
            Render.postProcessing().remove(warpEffect);
            warpEffect = null;
        }
        if (impactPostEffect != null) {
            Render.postProcessing().remove(impactPostEffect);
            impactPostEffect = null;
        }
        if (audio != null) {
            audio.dispose();
            audio = null;
        }
        Window.get().setTitle("Tower Climber");
        super.dispose();
    }

    private void buildLevel() {
        float baseY = Window.get().getHeight() - 42;
        long seed = replaySeed ? requestedSeed : TowerGenerator.createRunSeed();
        tower = new TowerGenerator(Window.get().getWidth(), baseY, seed);

        TowerPlatform floor = tower.getStartingPlatform();
        checkpointX = floor.transform.position.x;
        checkpointY = floor.top() - Player.COLLIDER_HEIGHT * 0.5f - 2;
        startPlayerY = checkpointY;
        highestPlayerY = checkpointY;
        player = new Player(checkpointX, checkpointY);
        player.applyUpgrade(level);
        playerAura = new PlayerAura(player);
        playerAura.setPowerLevel(level);
    }

    private void buildHud() {
        hudCanvas = new Canvas();

        Panel statusPanel = new Panel();
        statusPanel.style().css("padding: 10px 14px; background: #071525e8; border: 1px solid #5be7d355;");
        statusText = new Text("");
        statusText.style().css("font-scale: 0.42; line-height: 1.25; color: #d9f4ef; shadow-depth: 1px;");
        statusPanel.add(statusText);
        hudCanvas.append(statusPanel, 20, 18, 470, Canvas.AUTO);

        healthText = new Text("");
        healthText.style().css("font-scale: 0.34; color: #ffb5a5; shadow-depth: 1px;");
        hudCanvas.append(healthText, 24, 84, 300, 22);

        Panel healthTrack = new Panel();
        healthTrack.style().css("background: #301b25e8; border: 1px solid #ff705055;");
        healthFill = new Panel();
        healthFill.style().css("background: #ef6549;");
        hudCanvas.append(healthTrack, 24, 108, 270, 13);
        hudCanvas.append(healthFill, 26, 110, 266, 9);

        xpText = new Text("");
        xpText.style().css("font-scale: 0.34; color: #8ff8e6; text-align: end; shadow-depth: 1px;");
        hudCanvas.append(xpText, 306, 84, 300, 22);

        Panel xpTrack = new Panel();
        xpTrack.style().css("background: #102c35e8; border: 1px solid #5be7d355;");
        xpFill = new Panel();
        xpFill.style().css("background: #5be7d3;");
        hudCanvas.append(xpTrack, 336, 108, 270, 13);
        hudCanvas.append(xpFill, 338, 110, 1, 9);

        weaponText = new Text("");
        weaponText.style().css("padding: 6px 10px; font-scale: 0.34; color: #c6fff5; background: #071525c8; border: 1px solid #5be7d344;");
        hudCanvas.append(weaponText, 24, 128, 300, Canvas.AUTO);

        aetherText = new Text("");
        aetherText.style().css("padding: 6px 10px; font-scale: 0.34; color: #dfb6ff; text-align: end; background: #160d29c8; border: 1px solid #be77ff55;");
        hudCanvas.append(aetherText, 336, 128, 270, Canvas.AUTO);

        controlsText = new Text("A D MOVE   SPACE JUMP   MOUSE AIM / CLICK ATTACK   1 2 3 WEAPONS   RMB / F NOVA   SHIFT DASH   U UPGRADES");
        controlsText.style().css("padding: 7px 10px; font-scale: 0.31; color: #7da8aa; text-align: center; background: #071525b8; border: 1px solid #5be7d322;");
        hudCanvas.append(controlsText, 20, Window.get().getHeight() - 42, 760, Canvas.AUTO);

        levelToast = new Text("");
        levelToast.style().css("padding: 12px; font-scale: 0.82; color: #d8fff5; text-align: center; background: #0c3942d9; border: 1px solid #5be7d3aa; shadow-depth: 2px;");
        levelToast.visible(false);
        hudCanvas.append(levelToast, 0, 150, 420, Canvas.AUTO);

        comboText = new Text("");
        comboText.style().css("padding: 9px 13px; font-scale: 0.52; color: #f2c5ff; text-align: center; background: #241435d9; border: 1px solid #be77ffaa; shadow-depth: 2px;");
        comboText.visible(false);
        hudCanvas.append(comboText, Window.get().getWidth() - 272, 22, 250, Canvas.AUTO);

        gameOverCard = new Panel();
        gameOverCard.style().css("padding: 24px 32px; background: #07111bf2; border: 2px solid #ff705099;");
        Flex gameOverContent = new Flex(FlexDirection.COLUMN);
        gameOverContent.style().css("gap: 12px; align-items: stretch;");
        gameOverTitle = new Text("RUN OVER");
        gameOverTitle.style().css("font-scale: 1.4; color: #ff8062; text-align: center; shadow-depth: 3px;");
        gameOverStats = new Text("");
        gameOverStats.style().css("font-scale: 0.5; line-height: 1.35; color: #d8e8e5; text-align: center;");
        Button retry = new Button("RETRY THIS TOWER  [R]").onClick(this::restartSameTower);
        retry.style().css("height: 48px; background: #d9583f; hover-background: #ff7050; active-background: #a83b2b; border: 1px solid #ffb39f; font-scale: 0.58;");
        Button fresh = new Button("GENERATE NEW TOWER  [N]").onClick(this::restartNewTower);
        fresh.style().css("height: 46px; background: #126b6b; hover-background: #198b87; active-background: #0c4b50; border: 1px solid #8ff8e699; font-scale: 0.5;");
        Button menu = new Button("RETURN TO MENU  [ESC]").onClick(this::returnToMenu);
        menu.style().css("height: 44px; background: #10293a; hover-background: #183d50; border: 1px solid #5b8b9466; font-scale: 0.48;");
        gameOverContent.add(gameOverTitle, gameOverStats, retry, fresh, menu);
        gameOverCard.add(gameOverContent);
        gameOverCard.visible(false);
        hudCanvas.append(gameOverCard, 0, 0, 430, Canvas.AUTO);

        pauseShade = new Panel();
        pauseShade.style().css("background: #020711b8;");
        pauseShade.visible(false);
        hudCanvas.append(pauseShade, 0, 0, Canvas.FILL, Canvas.FILL);

        pauseCard = new Panel();
        pauseCard.style().css("padding: 26px 32px; background: #071525f5; border: 2px solid #5be7d399;");
        Flex pauseContent = new Flex(FlexDirection.COLUMN);
        pauseContent.style().css("gap: 14px; align-items: stretch;");
        Text pauseTitle = new Text("RUN PAUSED");
        pauseTitle.style().css("font-scale: 1.1; color: #8ff8e6; text-align: center; shadow-depth: 2px;");
        Text pauseHint = new Text("THE TOWER IS HOLDING ITS BREATH");
        pauseHint.style().css("font-scale: 0.38; color: #8aaeb2; text-align: center;");
        Button resume = new Button("RESUME  [ESC]").onClick(() -> setPaused(false));
        resume.style().css("height: 50px; background: #167b75; hover-background: #20a49b; border: 1px solid #8ff8e6; font-scale: 0.56;");
        Button pauseMenu = new Button("RETURN TO MENU").onClick(this::returnToMenu);
        pauseMenu.style().css("height: 46px; background: #10293a; hover-background: #183d50; border: 1px solid #5b8b9466; font-scale: 0.46;");
        pauseContent.add(pauseTitle, pauseHint, resume, pauseMenu);
        pauseCard.add(pauseContent);
        pauseCard.visible(false);
        hudCanvas.append(pauseCard, 0, 0, 410, Canvas.AUTO);

        buildUpgradeScreen();
    }

    private void buildUpgradeScreen() {
        upgradeShade = new Panel();
        upgradeShade.style().css("background: #02040bd9;");
        upgradeShade.visible(false);
        hudCanvas.append(upgradeShade, 0, 0, Canvas.FILL, Canvas.FILL);

        upgradeCard = new Panel();
        upgradeCard.style().css("padding: 20px 26px; background: #090f20fa; border: 2px solid #be77ffbb;");
        Flex content = new Flex(FlexDirection.COLUMN);
        content.style().css("gap: 7px; align-items: stretch;");

        Text title = new Text("ASCENSION GRID");
        title.style().css("font-scale: 1.0; color: #e2c3ff; text-align: center; shadow-depth: 3px;");
        upgradeCurrencyText = new Text("");
        upgradeCurrencyText.style().css("font-scale: 0.43; color: #8ff8e6; text-align: center;");
        upgradeHintText = new Text("SPEND CORES ON WEAPONS AND ABILITIES  /  CLICK AN UNLOCKED WEAPON TO EQUIP");
        upgradeHintText.style().css("font-scale: 0.3; color: #789ba5; text-align: center;");

        Text weapons = new Text("WEAPONS  /  AIM 360 DEGREES WITH THE MOUSE");
        weapons.style().css("margin: 5px 0px 0px 0px; font-scale: 0.38; color: #ffd88b;");
        bladeButton = upgradeButton("RIFT BLADE", () -> equipWeapon(Weapon.BLADE));
        bowButton = upgradeButton("AETHER BOW", () -> unlockOrEquip(Upgrade.BOW, Weapon.BOW));
        shurikenButton = upgradeButton("STAR SHURIKEN", () -> unlockOrEquip(Upgrade.SHURIKEN, Weapon.SHURIKEN));

        Text abilities = new Text("ABILITIES  /  PERMANENT FOR THIS RUN");
        abilities.style().css("margin: 5px 0px 0px 0px; font-scale: 0.38; color: #c696ff;");
        doubleJumpButton = upgradeButton("DOUBLE JUMP", () -> unlockUpgrade(Upgrade.DOUBLE_JUMP));
        novaButton = upgradeButton("ASTRAL NOVA", () -> unlockUpgrade(Upgrade.ASTRAL_NOVA));
        reservoirButton = upgradeButton("AETHER RESERVOIR", () -> unlockUpgrade(Upgrade.AETHER_RESERVOIR));
        Button close = new Button("RETURN TO TOWER  [U / ESC]").onClick(() -> setUpgradeOpen(false));
        close.style().css("height: 44px; margin: 8px 0px 0px 0px; background: #167b75; hover-background: #20a49b; border: 1px solid #8ff8e6; font-scale: 0.48;");

        content.add(title, upgradeCurrencyText, upgradeHintText, weapons,
                bladeButton, bowButton, shurikenButton, abilities,
                doubleJumpButton, novaButton, reservoirButton, close);
        upgradeCard.add(content);
        upgradeCard.visible(false);
        hudCanvas.append(upgradeCard, 0, 0, 620, Canvas.AUTO);
        refreshUpgradeScreen();
    }

    private Button upgradeButton(String label, Runnable action) {
        Button button = new Button(label).onClick(action);
        button.style().css("height: 43px; background: #161d35; hover-background: #272d50; active-background: #0d1225; border: 1px solid #8e79bd88; font-scale: 0.39;");
        return button;
    }

    private void updateCheckpoint() {
        if (!player.getBody().isGrounded()) {
            setSlingshotPlatform(null);
            return;
        }

        TowerPlatform groundedSlingshot = null;
        for (Collision collision : player.getBody().collisions()) {
            if (collision.normal().y < -0.5f
                    && collision.other().gameObject instanceof TowerPlatform platform) {
                if (platform.kind() == TowerPlatform.Kind.BOOST) {
                    groundedSlingshot = platform;
                }
                player.transform.position.x += platform.lastFrameDeltaX();
                if (platform.level() < checkpointLevel) {
                    continue;
                }
                if (platform.triggerCrumble()) {
                    new ImpactBurst(platform.transform.position.x, platform.top(),
                            Colors.from255(255, 151, 84, 220), Colors.from255(111, 37, 44, 0),
                            12, 115);
                    addCameraTrauma(0.12f);
                }
                if (platform.claimRestReward()) {
                    claimRestReward(platform);
                }
                if (platform.claimBonusReward()) {
                    claimBonusReward(platform);
                }
                boolean advanced = platform.level() > checkpointLevel;
                checkpointLevel = platform.level();
                float margin = Player.COLLIDER_WIDTH * 0.5f + 3;
                checkpointX = clamp(player.transform.position.x,
                        platform.left() + margin,
                        platform.right() - margin);
                checkpointY = platform.top() - Player.COLLIDER_HEIGHT * 0.5f - 2;
                if (advanced && platform.kind() == TowerPlatform.Kind.MILESTONE) {
                    previousZone = displayedZone;
                    displayedZone = tower.zoneIndex(checkpointLevel);
                    zoneBlend = 0;
                    levelToast.text("ZONE BREACH\n" + tower.zoneName(checkpointLevel));
                    levelToastTimer = Math.max(levelToastTimer, 1.8f);
                    Color4f accent = WINDOW_ON_PALETTE[displayedZone];
                    new ImpactBurst(player.transform.position.x, platform.top(),
                            accent, new Color4f(accent.r, accent.g, accent.b, 0), 34, 280);
                    addCameraTrauma(0.2f);
                }
            }
        }
        setSlingshotPlatform(groundedSlingshot);
    }

    private void resolveEnemyContact() {
        EnemyContact contact = tower.resolveEnemyContact(player);
        if (contact == null) {
            return;
        }
        if (contact.stomped()) {
            player.bounceFromStomp();
            registerStomp();
            audio.stomp(combo);
            new EnemyDefeatEffect(contact.enemy());
            Color4f impactColor = combo >= 3
                    ? Colors.from255(223, 147, 255, 255)
                    : Colors.from255(112, 255, 222, 255);
            lightingRig.flashCombat(contact.enemy().transform.position.x,
                    contact.enemy().transform.position.y,
                    impactColor, 270, 1.55f + Math.min(0.55f, combo * 0.08f), 0.32f);
            int burstCount = 16 + Math.min(16, combo * 3);
            new ImpactBurst(contact.enemy().transform.position.x, contact.enemy().transform.position.y,
                    impactColor,
                    Colors.from255(76, 154, 172, 0), burstCount, 250 + combo * 12);
            new StompShockwave(contact.enemy().transform.position.x,
                    contact.enemy().transform.position.y + 12, impactColor, combo);
            hitStopTimer = Math.max(hitStopTimer, 0.035f + Math.min(0.05f, combo * 0.007f));
            stompFlash = 1;
            addCameraTrauma(0.18f + Math.min(0.18f, combo * 0.035f));
            return;
        }
        if (!player.isInvulnerable()) {
            damagePlayer(contact.enemy());
        }
    }

    private void updateAim() {
        float dx = Input.getWorldMouseX() - player.transform.position.x;
        float dy = Input.getWorldMouseY() - player.transform.position.y;
        float lengthSquared = dx * dx + dy * dy;
        if (lengthSquared > 16) {
            float length = (float) Math.sqrt(lengthSquared);
            aimX = dx / length;
            aimY = dy / length;
            player.faceAim(aimX);
        }
    }

    private void updateWeaponSelection() {
        if (Input.keyPressed(Keys.KEY_1)) {
            selectWeapon(Weapon.BLADE);
        } else if (Input.keyPressed(Keys.KEY_2)) {
            selectWeapon(Weapon.BOW);
        } else if (Input.keyPressed(Keys.KEY_3)) {
            selectWeapon(Weapon.SHURIKEN);
        } else if (Input.getScrollY() != 0) {
            progression.cycleWeapon(Input.getScrollY() > 0 ? -1 : 1);
            audio.unlock();
        }
    }

    private void selectWeapon(Weapon weapon) {
        if (progression.equip(weapon)) {
            audio.unlock();
            levelToast.text("WEAPON EQUIPPED\n" + weapon.title());
            levelToastTimer = Math.max(levelToastTimer, 0.72f);
        } else {
            levelToast.text("WEAPON LOCKED\nPRESS U TO OPEN THE ASCENSION GRID");
            levelToastTimer = Math.max(levelToastTimer, 1.1f);
        }
    }

    private void updateWeaponAttack() {
        if (weaponCooldown > 0 || player.isChanneling()) {
            return;
        }
        boolean pressed = Input.mouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT)
                || Input.keyPressed(Keys.KEY_E)
                || Input.keyPressed(Keys.KEY_J);
        if (!pressed) {
            return;
        }
        switch (progression.equippedWeapon()) {
            case BLADE -> {
                weaponCooldown = 0.28f;
                resolveBladeAttack();
            }
            case BOW -> {
                weaponCooldown = 0.42f;
                projectiles.add(new WeaponProjectile(Weapon.BOW,
                        player.transform.position.x, player.transform.position.y - 3,
                        aimX, aimY, 0));
                audio.bow();
                new ImpactBurst(player.transform.position.x + aimX * 28,
                        player.transform.position.y + aimY * 28,
                        Colors.from255(145, 239, 255, 220), Colors.from255(91, 231, 211, 0),
                        7, 90);
            }
            case SHURIKEN -> {
                weaponCooldown = 0.22f;
                projectiles.add(new WeaponProjectile(Weapon.SHURIKEN,
                        player.transform.position.x, player.transform.position.y,
                        aimX, aimY, -0.16f));
                projectiles.add(new WeaponProjectile(Weapon.SHURIKEN,
                        player.transform.position.x, player.transform.position.y,
                        aimX, aimY, 0));
                projectiles.add(new WeaponProjectile(Weapon.SHURIKEN,
                        player.transform.position.x, player.transform.position.y,
                        aimX, aimY, 0.16f));
                audio.shuriken();
            }
        }
    }

    private void resolveBladeAttack() {
        List<Enemy> enemies = tower.resolveAttackCone(
                player.transform.position.x, player.transform.position.y,
                aimX, aimY, 104, 0.25f);
        boolean hit = !enemies.isEmpty();
        new SlashEffect(player.transform.position.x + aimX * 7,
                player.transform.position.y + aimY * 7 - 4, aimX, aimY, hit);
        audio.slash(hit);
        for (Enemy enemy : enemies) {
            registerWeaponDefeat(Weapon.BLADE);
            createWeaponHit(enemy, Weapon.BLADE);
        }
        if (hit) {
            Vector2f screen = getCamera().worldToScreen(
                    enemies.get(0).transform.position.x, enemies.get(0).transform.position.y);
            impactPostEffect.triggerSlash(screen.x, screen.y,
                    Window.get().getWidth(), Window.get().getHeight(), aimX, combo);
            hitStopTimer = Math.max(hitStopTimer, 0.045f);
            stompFlash = Math.max(stompFlash, 0.72f);
            addCameraTrauma(0.24f);
        }
    }

    private void resolveProjectiles() {
        Iterator<WeaponProjectile> iterator = projectiles.iterator();
        while (iterator.hasNext()) {
            WeaponProjectile projectile = iterator.next();
            if (projectile.isExpired()) {
                iterator.remove();
                continue;
            }
            Enemy enemy = tower.resolveProjectile(projectile.bounds());
            if (enemy == null) {
                continue;
            }
            registerWeaponDefeat(projectile.weapon());
            createWeaponHit(enemy, projectile.weapon());
            projectile.consumeHit();
            hitStopTimer = Math.max(hitStopTimer, projectile.weapon() == Weapon.BOW ? 0.04f : 0.018f);
            addCameraTrauma(projectile.weapon() == Weapon.BOW ? 0.2f : 0.09f);
            if (projectile.isExpired()) {
                iterator.remove();
            }
        }
    }

    private void createWeaponHit(Enemy enemy, Weapon weapon) {
        new EnemyDefeatEffect(enemy);
        Color4f impact = switch (weapon) {
            case BLADE -> Colors.from255(222, 155, 255, 255);
            case BOW -> Colors.from255(133, 239, 255, 255);
            case SHURIKEN -> Colors.from255(238, 174, 255, 255);
        };
        new ImpactBurst(enemy.transform.position.x, enemy.transform.position.y,
                impact, Colors.from255(91, 231, 211, 0),
                weapon == Weapon.BOW ? 25 : 19, weapon == Weapon.BOW ? 340 : 285);
        lightingRig.flashCombat(enemy.transform.position.x, enemy.transform.position.y,
                impact, weapon == Weapon.BOW ? 310 : 245,
                weapon == Weapon.BOW ? 1.9f : 1.35f, 0.28f);
        Vector2f screen = getCamera().worldToScreen(
                enemy.transform.position.x, enemy.transform.position.y);
        impactPostEffect.triggerSlash(screen.x, screen.y,
                Window.get().getWidth(), Window.get().getHeight(), aimX, combo);
    }

    private void updateNova(float delta) {
        boolean castPressed = Input.mouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_RIGHT)
                || Input.keyPressed(Keys.KEY_F);
        if (!player.isChanneling()) {
            if (castPressed) {
                tryStartNova();
            }
            return;
        }
        novaChargeTime = Math.min(NOVA_CHARGE_DURATION,
                novaChargeTime + Math.max(0, Math.min(delta, 0.05f)));
        lightingRig.updateNovaCharge(player.transform.position.x, player.transform.position.y,
                novaChargeTime / NOVA_CHARGE_DURATION, true);
        if (novaChargeEffect != null) {
            novaChargeEffect.progress(novaChargeTime / NOVA_CHARGE_DURATION);
        }
        if (novaChargeTime >= NOVA_CHARGE_DURATION) {
            releaseNova();
        }
    }

    private void tryStartNova() {
        if (!progression.has(Upgrade.ASTRAL_NOVA)) {
            levelToast.text("ASTRAL NOVA LOCKED\nPRESS U TO OPEN THE ASCENSION GRID");
            levelToastTimer = Math.max(levelToastTimer, 1.2f);
            return;
        }
        if (player.getBody().isGrounded()) {
            levelToast.text("ASTRAL NOVA\nCAST WHILE AIRBORNE");
            levelToastTimer = Math.max(levelToastTimer, 1.0f);
            return;
        }
        if (!progression.spendNova()) {
            levelToast.text("INSUFFICIENT AETHER\nCOLLECT " + progression.novaCost() + " VIOLET SHARDS");
            levelToastTimer = Math.max(levelToastTimer, 1.2f);
            return;
        }
        setSlingshotPlatform(null);
        novaChargeTime = 0;
        player.setChanneling(true);
        lightingRig.updateNovaCharge(player.transform.position.x, player.transform.position.y, 0, true);
        novaChargeEffect = new NovaChargeEffect(player);
        audio.novaCharge();
        levelToast.text("ASTRAL NOVA CHARGING\nMOVEMENT SUSPENDED");
        levelToastTimer = NOVA_CHARGE_DURATION;
        addCameraTrauma(0.16f);
    }

    private void releaseNova() {
        player.setChanneling(false);
        if (novaChargeEffect != null) {
            novaChargeEffect.destroy();
            novaChargeEffect = null;
        }
        float cameraX = getCamera().transform.position.x;
        float cameraY = getCamera().transform.position.y;
        AABB visible = AABB.fromTopLeft(cameraX - 40, cameraY - 40,
                Window.get().getWidth() + 80, Window.get().getHeight() + 80);
        List<Enemy> defeated = tower.defeatVisible(visible);
        for (Enemy enemy : defeated) {
            registerNovaDefeat();
            new EnemyDefeatEffect(enemy);
            new ImpactBurst(enemy.transform.position.x, enemy.transform.position.y,
                    Colors.from255(229, 188, 255, 255), Colors.from255(91, 231, 211, 0),
                    30, 390);
        }
        new AstralShockwave(player.transform.position.x, player.transform.position.y);
        lightingRig.releaseNova(player.transform.position.x, player.transform.position.y);
        new ImpactBurst(player.transform.position.x, player.transform.position.y,
                Colors.from255(235, 219, 255, 255), Colors.from255(120, 75, 255, 0),
                72, 540);
        Vector2f screen = getCamera().worldToScreen(
                player.transform.position.x, player.transform.position.y);
        warpEffect.trigger(screen.x, screen.y, Window.get().getWidth(), Window.get().getHeight());
        impactPostEffect.triggerBoost(screen.x, screen.y,
                Window.get().getWidth(), Window.get().getHeight());
        audio.novaImpact();
        stompFlash = 1.4f;
        hitStopTimer = Math.max(hitStopTimer, 0.09f);
        addCameraTrauma(1);
        levelToast.text("ASTRAL NOVA RELEASED\n" + defeated.size() + " ENEMIES ERASED");
        levelToastTimer = 1.8f;
        novaChargeTime = 0;
    }

    private void addAether(int amount) {
        if (amount <= 0) {
            return;
        }
        int added = progression.addAether(amount);
        if (added <= 0) {
            return;
        }
        audio.collectXp();
        playerAura.burst(10 + added * 4);
        new ImpactBurst(player.transform.position.x, player.transform.position.y,
                Colors.from255(197, 126, 255, 235), Colors.from255(91, 231, 211, 0),
                10 + added * 3, 145);
        lightingRig.flashCombat(player.transform.position.x, player.transform.position.y,
                Colors.from255(197, 126, 255, 255), 210, 1.2f, 0.3f);
        if (progression.canCastNova()) {
            levelToast.text("AETHER RESONANCE FULL\nAIRBORNE RMB / F  /  ASTRAL NOVA");
            levelToastTimer = Math.max(levelToastTimer, 1.35f);
        }
    }

    private void damagePlayer(Enemy enemy) {
        damagePlayer(enemy.transform.position.x, 25,
                Colors.from255(255, 112, 80, 255), Colors.from255(118, 35, 54, 0));
    }

    private void resolveHazardContact() {
        EnergyVent hazard = tower.resolveHazardContact(player);
        if (hazard == null || player.isInvulnerable()) {
            return;
        }
        damagePlayer(hazard.transform.position.x, 20,
                Colors.from255(145, 234, 255, 255), Colors.from255(76, 95, 172, 0));
    }

    private void damagePlayer(float sourceX, int amount, Color4f burstStart, Color4f burstEnd) {
        health = Math.max(0, health - amount);
        damageFlash = 1;
        audio.hurt();
        Vector2f screen = getCamera().worldToScreen(
                player.transform.position.x, player.transform.position.y);
        impactPostEffect.triggerDamage(screen.x, screen.y,
                Window.get().getWidth(), Window.get().getHeight());
        new ImpactBurst(player.transform.position.x, player.transform.position.y,
                burstStart, burstEnd, 14, 210);
        lightingRig.flashCombat(player.transform.position.x, player.transform.position.y,
                burstStart, 330, 2.15f, 0.42f);
        addCameraTrauma(0.58f);
        if (health <= 0) {
            endGame(false);
            return;
        }
        player.knockBack(sourceX);
    }

    private void addXp(int amount) {
        if (amount <= 0) {
            return;
        }
        audio.collectXp();
        playerAura.burst(Math.min(10, 2 + amount));
        new ImpactBurst(player.transform.position.x, player.transform.position.y,
                Colors.from255(255, 224, 122, 220), Colors.from255(91, 231, 211, 0),
                Math.min(12, 3 + amount / 2), 105);
        xp += amount;
        while (xp >= xpToNextLevel) {
            xp -= xpToNextLevel;
            level++;
            xpToNextLevel = 30 + (level - 1) * 18;
            maxHealth = BASE_MAX_HEALTH + (level - 1) * 8;
            health = Math.min(maxHealth, health + 28);
            player.applyUpgrade(level);
            triggerLevelUp();
        }
    }

    private void triggerLevelUp() {
        progression.addUpgradeCore();
        audio.levelUp();
        playerAura.setPowerLevel(level);
        playerAura.burst(42);
        levelToast.text("POWER SURGE  /  LEVEL " + level
                + "\nUPGRADE CORE ACQUIRED  /  PRESS U");
        levelToastTimer = 2.1f;
        Vector2f screen = getCamera().worldToScreen(player.transform.position.x, player.transform.position.y);
        warpEffect.trigger(screen.x, screen.y, Window.get().getWidth(), Window.get().getHeight());
        new ImpactBurst(player.transform.position.x, player.transform.position.y,
                Colors.from255(217, 255, 232, 255), Colors.from255(91, 231, 211, 0), 48, 390);
        lightingRig.flashCombat(player.transform.position.x, player.transform.position.y,
                Colors.from255(180, 255, 226, 255), 560, 3.2f, 0.7f);
        addCameraTrauma(1);
    }

    private void claimRestReward(TowerPlatform platform) {
        int recovery = Math.min(maxHealth - health, 18 + level * 2);
        health += recovery;
        int bonus = recovery > 0 ? 75 : 125;
        combatScore += bonus;
        audio.collectXp();
        playerAura.burst(18);
        levelToast.text(recovery > 0
                ? "SAFE FLOOR\nREPAIRED  +" + recovery + " HEALTH"
                : "SAFE FLOOR\nFULL HEALTH  +" + bonus + " PTS");
        levelToastTimer = Math.max(levelToastTimer, 1.65f);
        new ImpactBurst(player.transform.position.x, platform.top(),
                Colors.from255(255, 207, 105, 230), Colors.from255(91, 231, 211, 0),
                22, 185);
        addCameraTrauma(0.1f);
    }

    private void setSlingshotPlatform(TowerPlatform platform) {
        if (slingshotPlatform == platform) {
            player.setSlingshotMode(platform != null);
            return;
        }
        if (slingshotPlatform != null) {
            slingshotPlatform.setSlingshotPreview(0, slingshotAim, false);
        }
        slingshotPlatform = platform;
        slingshotCharging = false;
        slingshotChargeTime = 0;
        player.setSlingshotMode(platform != null);
        player.setSlingshotCharging(false);
        if (platform != null) {
            slingshotAim = player.facingDirection();
        }
    }

    private void updateSlingshot(float delta) {
        if (slingshotPlatform == null) {
            return;
        }

        boolean jumpHeld = Input.keyDown(Keys.KEY_SPACE)
                || Input.keyDown(Keys.KEY_W)
                || Input.keyDown(Keys.KEY_UP);
        float aimInput = Math.max(Input.keyState(Keys.KEY_D), Input.keyState(Keys.KEY_RIGHT))
                - Math.max(Input.keyState(Keys.KEY_A), Input.keyState(Keys.KEY_LEFT));

        if (!slingshotCharging) {
            slingshotPlatform.setSlingshotPreview(0, slingshotAim, false);
            if (!jumpHeld) {
                return;
            }
            slingshotCharging = true;
            slingshotChargeTime = 0;
            slingshotAim = aimInput == 0 ? player.facingDirection() : Math.signum(aimInput);
            player.setSlingshotCharging(true);
            levelToast.text("SLINGSHOT ARMED\nHOLD JUMP  /  AIM WITH A D");
            levelToastTimer = Math.max(levelToastTimer, 0.75f);
        }

        float frameDelta = Math.max(0, Math.min(delta, 0.05f));
        slingshotChargeTime = Math.min(SLINGSHOT_CHARGE_TIME, slingshotChargeTime + frameDelta);
        if (aimInput != 0) {
            float targetAim = Math.signum(aimInput);
            float aimEase = 1 - (float) Math.exp(-frameDelta * 7.5f);
            slingshotAim += (targetAim - slingshotAim) * aimEase;
        }
        float power = slingshotPower();
        slingshotPlatform.setSlingshotPreview(power, slingshotAim, true);

        if (!jumpHeld) {
            releaseSlingshot();
        }
    }

    private void releaseSlingshot() {
        if (!slingshotCharging || slingshotPlatform == null) {
            return;
        }
        TowerPlatform launchPlatform = slingshotPlatform;
        Vector2f velocity = slingshotVelocity();
        player.launchFromSlingshot(velocity.x, velocity.y);
        audio.boost();
        Vector2f screen = getCamera().worldToScreen(
                player.transform.position.x, player.transform.position.y);
        impactPostEffect.triggerBoost(screen.x, screen.y,
                Window.get().getWidth(), Window.get().getHeight());
        playerAura.burst(24);
        levelToast.text("SLINGSHOT RELEASE\nPOWER  " + Math.round(slingshotPower() * 100) + "%");
        levelToastTimer = Math.max(levelToastTimer, 0.9f);
        new LaunchBurst(player.transform.position.x, launchPlatform.top());
        new ImpactBurst(player.transform.position.x, launchPlatform.top(),
                Colors.from255(255, 238, 126, 235), Colors.from255(82, 226, 255, 0),
                26, 275);
        lightingRig.flashCombat(player.transform.position.x, launchPlatform.top(),
                Colors.from255(255, 228, 128, 255), 340, 2.25f, 0.46f);
        addCameraTrauma(0.28f);
        launchPlatform.setSlingshotPreview(0, slingshotAim, false);
        slingshotCharging = false;
        slingshotChargeTime = 0;
        slingshotPlatform = null;
    }

    private float slingshotPower() {
        float normalized = clamp(slingshotChargeTime / SLINGSHOT_CHARGE_TIME, 0, 1);
        return normalized * normalized * (3 - 2 * normalized);
    }

    private Vector2f slingshotVelocity() {
        float power = slingshotPower();
        float horizontal = SLINGSHOT_MIN_HORIZONTAL_SPEED
                + (SLINGSHOT_MAX_HORIZONTAL_SPEED - SLINGSHOT_MIN_HORIZONTAL_SPEED) * power;
        float vertical = SLINGSHOT_MIN_VERTICAL_SPEED
                + (SLINGSHOT_MAX_VERTICAL_SPEED - SLINGSHOT_MIN_VERTICAL_SPEED) * power;
        return new Vector2f(slingshotAim * horizontal, -vertical);
    }

    private void drawSlingshotPreview() {
        if (!slingshotCharging || slingshotPlatform == null) {
            return;
        }
        float power = slingshotPower();
        Vector2f velocity = slingshotVelocity();
        int dots = 7 + Math.round(power * 12);
        float duration = 0.55f + power * 0.65f;
        float startX = player.transform.position.x;
        float startY = player.transform.position.y - 8;
        for (int index = 1; index <= dots; index++) {
            float fraction = index / (float) dots;
            float time = duration * fraction;
            float x = startX + velocity.x * time;
            float y = startY + velocity.y * time + 750 * time * time;
            float pulse = 0.72f + 0.28f * (float) Math.sin(Time.timePassed * 10 + index * 0.8f);
            Color4f color = new Color4f(1f, 0.88f, 0.32f,
                    (1 - fraction * 0.55f) * pulse * 0.72f);
            float size = 4 + power * 3 + (1 - fraction) * 2;
            Render.drawRotatedRect(x, y, size, size, true, color, 45);
        }
        float meterWidth = 84;
        float meterY = player.transform.position.y - Player.COLLIDER_HEIGHT * 0.75f;
        Render.drawRect(player.transform.position.x, meterY, meterWidth, 6, true,
                Colors.from255(20, 25, 31, 185));
        float fill = Math.max(2, meterWidth * power);
        Render.drawRect(player.transform.position.x - meterWidth * 0.5f + fill * 0.5f,
                meterY, fill, 4, true, Colors.from255(255, 220, 82, 230));
    }

    private void triggerDashFeedback() {
        float direction = player.dashDirection();
        audio.dash();
        new DashBurst(player.transform.position.x, player.transform.position.y, direction);
        playerAura.burst(12);
        addCameraTrauma(0.1f);
    }

    private void claimBonusReward(TowerPlatform platform) {
        int reward = 125 + Math.min(175, platform.level() * 5);
        combatScore += reward;
        audio.collectXp();
        playerAura.burst(14);
        levelToast.text("RISK ROUTE CLEARED\n+" + reward + " PTS");
        levelToastTimer = Math.max(levelToastTimer, 1.15f);
        new ImpactBurst(player.transform.position.x, platform.top(),
                Colors.from255(222, 145, 255, 235), Colors.from255(91, 231, 211, 0),
                18, 190);
        addCameraTrauma(0.1f);
    }

    private void updateScore() {
        int height = currentHeight();
        score = Math.max(score, height * 10 + combatScore + (level - 1) * 100);
        if (score > highScore) {
            highScore = score;
        }
    }

    private void updateCamera() {
        float desiredY = player.transform.position.y - Window.get().getHeight() * 0.62f;
        cameraTargetY = Math.min(cameraTargetY, desiredY);

        float delta = Math.min(Time.delta, 0.05f);
        float follow = 1 - (float) Math.exp(-6.5f * delta);
        float cameraY = getCamera().transform.position.y;
        cameraY += (cameraTargetY - cameraY) * follow;

        float warpShake = warpEffect.isEnabled()
                ? (1 - warpEffect.progress()) * (float) Math.sin(Time.timePassed * 88) * 8
                : 0;
        float trauma = cameraTrauma * cameraTrauma;
        float shakeX = trauma * (float) Math.sin(Time.timePassed * 79) * 9;
        float shakeY = trauma * (float) Math.sin(Time.timePassed * 103 + 1.7f) * 6;
        getCamera().setPosition(warpShake + shakeX, cameraY + warpShake * 0.35f + shakeY);
    }

    private void updateHud() {
        int height = currentHeight();
        String debug = debugColliders ? "  /  HITBOXES ON" : "";
        statusText.text("SCORE  " + score + "    BEST  " + highScore
                + "\nHEIGHT  " + height + " M    LEVEL  " + level + debug
                + "\nZONE  " + tower.zoneName(checkpointLevel) + "    TOWER  " + tower.routeCode());
        healthText.text("HEALTH  " + health + " / " + maxHealth);
        String dashState = player.dashCooldownRatio() <= 0 ? "READY"
                : Math.round((1 - player.dashCooldownRatio()) * 100) + "%";
        xpText.text("POWER  " + xp + " / " + xpToNextLevel + "    DASH  " + dashState);
        weaponText.text("WEAPON  " + progression.equippedWeapon().title()
                + "  [" + (progression.equippedWeapon().ordinal() + 1) + "]");
        String novaState = player.isChanneling()
                ? "CHARGING " + Math.round(novaChargeTime / NOVA_CHARGE_DURATION * 100) + "%"
                : progression.has(Upgrade.ASTRAL_NOVA)
                        ? progression.canCastNova() ? "NOVA READY" : "NOVA NEEDS " + progression.novaCost()
                        : "NOVA LOCKED";
        aetherText.text("AETHER  " + aetherGauge()
                + "    " + novaState + "    CORES " + progression.upgradeCores());

        float healthRatio = maxHealth == 0 ? 0 : health / (float) maxHealth;
        float xpRatio = xpToNextLevel == 0 ? 0 : xp / (float) xpToNextLevel;
        hudCanvas.setBounds(healthFill, 26, 110, Math.max(0, 266 * healthRatio), 9);
        hudCanvas.setBounds(xpFill, 338, 110, Math.max(1, 266 * xpRatio), 9);

        float centerX = Window.get().getWidth() * 0.5f;
        hudCanvas.setBounds(levelToast, centerX - 210, 178, 420, Canvas.AUTO);
        hudCanvas.setBounds(comboText, Window.get().getWidth() - 272, 22, 250, Canvas.AUTO);
        hudCanvas.setBounds(gameOverCard,
                centerX - 215,
                Math.max(24, Window.get().getHeight() * 0.5f - 220),
                430,
                Canvas.AUTO);
        hudCanvas.setBounds(pauseShade, 0, 0, Window.get().getWidth(), Window.get().getHeight());
        hudCanvas.setBounds(pauseCard,
                centerX - 205,
                Math.max(60, Window.get().getHeight() * 0.5f - 145),
                410,
                Canvas.AUTO);
        hudCanvas.setBounds(upgradeShade, 0, 0, Window.get().getWidth(), Window.get().getHeight());
        hudCanvas.setBounds(upgradeCard,
                centerX - 310,
                Math.max(14, Window.get().getHeight() * 0.5f - 315),
                620,
                Canvas.AUTO);
        hudCanvas.setBounds(controlsText, 20,
                Window.get().getHeight() - 42, Math.min(1080, Window.get().getWidth() - 40), Canvas.AUTO);
        refreshUpgradeScreen();
        Window.get().setTitle(paused || upgradeOpen
                ? "Tower Climber | " + (upgradeOpen ? "Ascension Grid" : "Paused")
                : "Tower Climber | " + score + " pts | " + height + " m");
    }

    private int currentHeight() {
        return Math.max(0, Math.round((startPlayerY - highestPlayerY) / 10));
    }

    private String aetherGauge() {
        StringBuilder gauge = new StringBuilder("[");
        for (int i = 0; i < progression.maxAether(); i++) {
            gauge.append(i < progression.aether() ? '#' : '-');
        }
        return gauge.append(']').toString();
    }

    private void endGame(boolean fell) {
        if (gameOver) {
            return;
        }
        gameOver = true;
        if (novaChargeEffect != null) {
            novaChargeEffect.destroy();
            novaChargeEffect = null;
        }
        player.setChanneling(false);
        lightingRig.updateNovaCharge(player.transform.position.x, player.transform.position.y, 0, false);
        health = 0;
        setSlingshotPlatform(null);
        player.setControlsEnabled(false);
        playerAura.setActive(false);
        player.getBody().velocity.set(0, 0);
        physics().enabled(false);
        newHighScore = HighScores.isNewRecord(score, bestAtRunStart);
        highScore = HighScores.submit(score);
        audio.gameOver(newHighScore);
        gameOverTitle.text(newHighScore ? "NEW TOWER RECORD" : "RUN OVER");
        Color4f resultAccent = newHighScore
                ? Colors.from255(255, 218, 112, 255)
                : Colors.from255(255, 128, 98, 255);
        gameOverTitle.style().color(resultAccent);
        gameOverCard.style().borderColor(new Color4f(
                resultAccent.r, resultAccent.g, resultAccent.b, 0.68f));
        gameOverStats.text((fell ? "YOU FELL INTO THE CLOUDS" : "THE TOWER FOUGHT BACK")
                + "\n\nSCORE  " + score
                + "\nHEIGHT  " + currentHeight() + " M"
                + "\nLEVEL  " + level + "    ENEMIES  " + kills
                + "\nBEST CHAIN  X" + bestCombo
                + "\nWEAPON  " + progression.equippedWeapon().title()
                + "    AETHER  " + progression.aether() + " / " + progression.maxAether()
                + "\nTOWER  " + tower.routeCode()
                + (newHighScore
                        ? "\nPREVIOUS BEST  " + bestAtRunStart
                                + "\nNEW BEST  " + highScore + "  /  +" + (score - bestAtRunStart)
                        : "\nBEST  " + highScore));
        gameOverCard.visible(true);
        levelToast.visible(false);
        if (newHighScore) {
            float celebrationX = getCamera().transform.position.x + Window.get().getWidth() * 0.5f;
            float celebrationY = getCamera().transform.position.y + Window.get().getHeight() * 0.42f;
            new RecordCelebration(celebrationX, celebrationY);
            new ImpactBurst(celebrationX, celebrationY,
                    Colors.from255(255, 225, 122, 255), Colors.from255(91, 231, 211, 0),
                    44, 360);
            addCameraTrauma(1);
        } else {
            new ImpactBurst(player.transform.position.x, player.transform.position.y,
                    Colors.from255(255, 112, 80, 255), Colors.from255(32, 10, 22, 0), 30, 310);
            addCameraTrauma(0.85f);
        }
    }

    private void setDebugColliders(boolean enabled) {
        debugColliders = enabled;
        player.setDebugCollider(enabled);
        tower.setDebugColliders(enabled);
    }

    private void drawBackdrop() {
        int viewportWidth = Window.get().getWidth();
        int viewportHeight = Window.get().getHeight();
        float cameraTop = getCamera().transform.position.y;
        float towerLeft = tower.getTowerLeft();
        float towerRight = tower.getTowerRight();
        float towerWidth = towerRight - towerLeft;
        Color4f sky = zoneColor(SKY_PALETTE);
        Color4f lowerSky = zoneColor(LOWER_SKY_PALETTE);
        Color4f towerColor = zoneColor(TOWER_PALETTE);
        Color4f edge = zoneColor(EDGE_PALETTE);
        Color4f windowOff = zoneColor(WINDOW_OFF_PALETTE);
        Color4f windowOn = zoneColor(WINDOW_ON_PALETTE);

        Render.drawRect(-20, cameraTop - 20, viewportWidth + 40, viewportHeight + 40, sky);
        Render.drawRect(-20, cameraTop + viewportHeight * 0.48f,
                viewportWidth + 40, viewportHeight * 0.54f, lowerSky);
        drawDistantClouds(cameraTop, viewportHeight, towerLeft, towerRight);
        drawLightning(cameraTop, viewportHeight, towerLeft, towerRight);
        Render.drawRect(towerLeft, cameraTop, towerWidth, viewportHeight, towerColor);
        Render.drawRect(towerLeft, cameraTop, 3, viewportHeight, edge);
        Render.drawRect(towerRight - 3, cameraTop, 3, viewportHeight, edge);

        int firstRow = (int) Math.floor((cameraTop - 94) / 94f);
        int lastRow = (int) Math.ceil((cameraTop + viewportHeight + 94) / 94f);
        for (int row = firstRow; row <= lastRow; row++) {
            float y = row * 94f;
            Render.drawRect(towerLeft, y, towerWidth, 2,
                    new Color4f(edge.r, edge.g, edge.b, 0.29f));

            if ((row & 1) == 0) {
                Color4f brace = new Color4f(edge.r, edge.g, edge.b, 0.28f);
                Render.drawRotatedRect(towerLeft + 34, y + 47, 3, 88, true, brace, -24);
                Render.drawRotatedRect(towerRight - 34, y + 47, 3, 88, true, brace, 24);
            }

            int column = 0;
            for (float x = towerLeft + 54; x < towerRight - 30; x += 116) {
                Color4f color = Math.floorMod(row * 7 + column * 11, 9) == 0
                        ? windowOn
                        : windowOff;
                Render.drawRect(x, y + 27, 18, 26, color);
                column++;
            }
        }
    }

    private void drawDistantClouds(float cameraTop, int viewportHeight, float towerLeft, float towerRight) {
        int firstBand = (int) Math.floor((cameraTop - 180) / 230f);
        int lastBand = (int) Math.ceil((cameraTop + viewportHeight + 180) / 230f);
        for (int band = firstBand; band <= lastBand; band++) {
            float y = band * 230f;
            float drift = (float) Math.sin(Time.timePassed * 0.12f + band * 2.3f) * 18;
            float leftX = towerLeft - 95 + Math.floorMod(band * 37, 70) + drift;
            float rightX = towerRight + 35 - Math.floorMod(band * 53, 65) + drift * 0.7f;
            Color4f shadow = Colors.from255(27, 54, 75, 42);
            Color4f mist = Colors.from255(64, 105, 126, 34);
            drawCloudCluster(leftX, y + 42, shadow, mist);
            drawCloudCluster(rightX, y - 34, shadow, mist);
        }
    }

    private void drawCloudCluster(float x, float y, Color4f shadow, Color4f mist) {
        Render.drawRect(x, y, 142, 24, true, shadow);
        Render.drawRect(x - 28, y - 8, 72, 20, true, mist);
        Render.drawRect(x + 35, y - 5, 92, 27, true, mist);
    }

    private void drawLightning(float cameraTop, int viewportHeight, float towerLeft, float towerRight) {
        if (lightningFlash <= 0) {
            return;
        }
        float alpha = lightningFlash * lightningFlash;
        Render.drawRect(-20, cameraTop - 20,
                Window.get().getWidth() + 40, viewportHeight + 40,
                Colors.from255(125, 183, 226, Math.round(alpha * 42)));

        float side = ((checkpointLevel / 12) & 1) == 0 ? -1 : 1;
        float x = side < 0 ? towerLeft - 58 : towerRight + 58;
        float y = cameraTop + 38;
        Color4f bolt = Colors.from255(204, 239, 255, Math.round(alpha * 220));
        for (int segment = 0; segment < 5; segment++) {
            float offset = ((segment & 1) == 0 ? 1 : -1) * (10 + segment * 2) * side;
            Render.drawRotatedRect(x + offset * 0.5f, y + 34,
                    3, 72, true, bolt, -offset * 0.38f);
            x += offset;
            y += 65;
        }
    }

    private void drawLevelUpWave() {
        if (!warpEffect.isEnabled()) {
            return;
        }
        float radius = 30 + warpEffect.progress() * 520;
        float alpha = Math.max(0, 1 - warpEffect.progress());
        Color4f color = new Color4f(0.36f, 0.95f, 0.84f, alpha * 0.12f);
        Render.drawRotatedRect(player.transform.position.x, player.transform.position.y,
                radius, radius, true, color, 45);
    }

    private void drawAimReticle() {
        if (paused || upgradeOpen || gameOver || player.isChanneling()) {
            return;
        }
        float x = Input.getWorldMouseX();
        float y = Input.getWorldMouseY();
        Color4f color = switch (progression.equippedWeapon()) {
            case BLADE -> new Color4f(0.76f, 1f, 0.9f, 0.58f);
            case BOW -> new Color4f(0.48f, 0.92f, 1f, 0.68f);
            case SHURIKEN -> new Color4f(0.88f, 0.56f, 1f, 0.68f);
        };
        float pulse = 0.5f + 0.5f * (float) Math.sin(Time.timePassed * 8);
        float size = 19 + pulse * 3;
        for (int i = 0; i < 4; i++) {
            float angle = i * (float) Math.PI * 0.5f;
            float px = x + (float) Math.cos(angle) * size;
            float py = y + (float) Math.sin(angle) * size;
            Render.drawRotatedRect(px, py, 3, 9, true, color,
                    (float) Math.toDegrees(angle) + 90);
        }
        Render.drawRotatedRect(x, y, 5, 5, true, color, 45);
        float guideX = player.transform.position.x + aimX * 56;
        float guideY = player.transform.position.y + aimY * 56;
        Render.drawRotatedRect(guideX, guideY, 16, 2, true,
                new Color4f(color.r, color.g, color.b, color.a * 0.42f),
                (float) Math.toDegrees(Math.atan2(aimY, aimX)));
    }

    private void drawNovaChargeOverlay() {
        if (!player.isChanneling()) {
            return;
        }
        float progress = Math.min(1, novaChargeTime / NOVA_CHARGE_DURATION);
        float cameraX = getCamera().transform.position.x;
        float cameraY = getCamera().transform.position.y;
        float pulse = 0.5f + 0.5f * (float) Math.sin(Time.timePassed * (11 + progress * 18));
        Render.drawRect(cameraX, cameraY, Window.get().getWidth(), Window.get().getHeight(),
                new Color4f(0.04f, 0.01f, 0.13f, 0.08f + progress * 0.18f));
        Color4f line = new Color4f(0.78f, 0.46f, 1f, 0.18f + progress * 0.38f);
        float radius = 130 + pulse * 12 - progress * 52;
        for (int i = 0; i < 20; i++) {
            float angle = i * (float) Math.PI * 0.1f + Time.timePassed * 1.4f;
            Render.drawRotatedRect(
                    player.transform.position.x + (float) Math.cos(angle) * radius,
                    player.transform.position.y + (float) Math.sin(angle) * radius,
                    3, 19 + progress * 18, true, line,
                    (float) Math.toDegrees(angle) + 90);
        }
    }

    private void drawDangerOverlay() {
        float healthRatio = maxHealth <= 0 ? 0 : health / (float) maxHealth;
        if (healthRatio > 0.32f && damageFlash <= 0) {
            return;
        }
        float cameraX = getCamera().transform.position.x;
        float cameraY = getCamera().transform.position.y;
        float width = Window.get().getWidth();
        float height = Window.get().getHeight();
        float pulse = 0.5f + 0.5f * (float) Math.sin(Time.timePassed * 6.8f);
        float danger = healthRatio >= 0.32f ? 0 : (0.32f - healthRatio) / 0.32f;
        Color4f edge = new Color4f(1f, 0.12f, 0.08f,
                Math.min(0.34f, damageFlash * 0.18f + danger * (0.08f + pulse * 0.1f)));
        float thickness = 12 + danger * 16;
        Render.drawRect(cameraX, cameraY, width, thickness, edge);
        Render.drawRect(cameraX, cameraY + height - thickness, width, thickness, edge);
        Render.drawRect(cameraX, cameraY, thickness, height, edge);
        Render.drawRect(cameraX + width - thickness, cameraY, thickness, height, edge);
        if (damageFlash > 0) {
            Render.drawRect(cameraX, cameraY, width, height,
                    new Color4f(1f, 0.16f, 0.1f, damageFlash * 0.07f));
        }
    }

    private void drawCombatFlash() {
        if (stompFlash <= 0) {
            return;
        }
        float cameraX = getCamera().transform.position.x;
        float cameraY = getCamera().transform.position.y;
        Color4f accent = combo >= 3
                ? new Color4f(0.83f, 0.5f, 1f, stompFlash * 0.045f)
                : new Color4f(0.45f, 1f, 0.9f, stompFlash * 0.032f);
        Render.drawRect(cameraX, cameraY,
                Window.get().getWidth(), Window.get().getHeight(), accent);
    }

    private float clamp(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private void addCameraTrauma(float amount) {
        cameraTrauma = Math.min(1, cameraTrauma + Math.max(0, amount));
    }

    private void setPaused(boolean paused) {
        if (gameOver || this.paused == paused) {
            return;
        }
        this.paused = paused;
        physics().enabled(!paused && !upgradeOpen);
        player.setControlsEnabled(!paused && !upgradeOpen);
        pauseShade.visible(paused);
        pauseCard.visible(paused);
        audio.setPaused(paused);
        if (paused) {
            Window.get().setTitle("Tower Climber | Paused");
        }
    }

    private void setUpgradeOpen(boolean open) {
        if (gameOver || upgradeOpen == open) {
            return;
        }
        upgradeOpen = open;
        physics().enabled(!open && !paused);
        player.setControlsEnabled(!open && !paused);
        upgradeShade.visible(open);
        upgradeCard.visible(open);
        audio.setPaused(open || paused);
        refreshUpgradeScreen();
    }

    private void unlockOrEquip(Upgrade upgrade, Weapon weapon) {
        if (progression.has(upgrade)) {
            equipWeapon(weapon);
            return;
        }
        if (unlockUpgrade(upgrade)) {
            progression.equip(weapon);
            refreshUpgradeScreen();
        }
    }

    private boolean unlockUpgrade(Upgrade upgrade) {
        if (progression.has(upgrade)) {
            upgradeHintText.text(upgrade.title() + " IS ALREADY ONLINE");
            return false;
        }
        if (!progression.unlock(upgrade)) {
            String reason = upgrade == Upgrade.AETHER_RESERVOIR
                    && !progression.has(Upgrade.ASTRAL_NOVA)
                            ? "UNLOCK ASTRAL NOVA FIRST"
                            : "NEED " + upgrade.cost() + " UPGRADE CORE"
                                    + (upgrade.cost() == 1 ? "" : "S");
            upgradeHintText.text(reason);
            return false;
        }
        if (upgrade == Upgrade.DOUBLE_JUMP) {
            player.setDoubleJumpUnlocked(true);
        }
        audio.unlock();
        playerAura.burst(28);
        new ImpactBurst(player.transform.position.x, player.transform.position.y,
                Colors.from255(221, 178, 255, 245), Colors.from255(91, 231, 211, 0),
                28, 250);
        upgradeHintText.text(upgrade.title() + "  /  UNLOCKED");
        levelToast.text("ASCENSION UNLOCKED\n" + upgrade.title());
        levelToastTimer = Math.max(levelToastTimer, 1.5f);
        refreshUpgradeScreen();
        return true;
    }

    private void equipWeapon(Weapon weapon) {
        if (!progression.equip(weapon)) {
            return;
        }
        audio.unlock();
        upgradeHintText.text(weapon.title() + "  /  EQUIPPED");
        refreshUpgradeScreen();
    }

    private void refreshUpgradeScreen() {
        if (upgradeCurrencyText == null) {
            return;
        }
        upgradeCurrencyText.text("UPGRADE CORES  " + progression.upgradeCores()
                + "    /    AETHER  " + progression.aether() + " / " + progression.maxAether());
        bladeButton.label(weaponLabel(Weapon.BLADE, null));
        bowButton.label(weaponLabel(Weapon.BOW, Upgrade.BOW));
        shurikenButton.label(weaponLabel(Weapon.SHURIKEN, Upgrade.SHURIKEN));
        doubleJumpButton.label(upgradeLabel(Upgrade.DOUBLE_JUMP));
        novaButton.label(upgradeLabel(Upgrade.ASTRAL_NOVA));
        reservoirButton.label(upgradeLabel(Upgrade.AETHER_RESERVOIR));
    }

    private String weaponLabel(Weapon weapon, Upgrade upgrade) {
        if (!progression.isWeaponUnlocked(weapon)) {
            return weapon.title() + "  /  " + upgrade.description() + "  /  "
                    + upgrade.cost() + " CORE" + (upgrade.cost() == 1 ? "" : "S");
        }
        return weapon.title() + (progression.equippedWeapon() == weapon
                ? "  /  EQUIPPED"
                : "  /  CLICK TO EQUIP");
    }

    private String upgradeLabel(Upgrade upgrade) {
        if (progression.has(upgrade)) {
            return upgrade.title() + "  /  UNLOCKED";
        }
        String prerequisite = upgrade == Upgrade.AETHER_RESERVOIR
                && !progression.has(Upgrade.ASTRAL_NOVA) ? "  /  REQUIRES NOVA" : "";
        return upgrade.title() + "  /  " + upgrade.description() + "  /  "
                + upgrade.cost() + " CORE" + (upgrade.cost() == 1 ? "" : "S") + prerequisite;
    }

    private void updateWeather() {
        float delta = Math.min(Time.delta, 0.05f);
        int zone = tower.zoneIndex(checkpointLevel);
        rain.setIntensity(1 + zone * 0.17f + Math.min(0.18f, checkpointLevel / 240f));
        lightningFlash = Math.max(0, lightningFlash - delta * 4.5f);
        float strikeSide = ((checkpointLevel / 12) & 1) == 0 ? -1 : 1;
        float strikeX = strikeSide < 0 ? tower.getTowerLeft() - 58 : tower.getTowerRight() + 58;
        lightingRig.updateStorm(strikeX,
                getCamera().transform.position.y + Window.get().getHeight() * 0.34f,
                lightningFlash, zone);
        if (zone != 3) {
            return;
        }
        stormClock += delta;
        if (stormClock < nextLightning) {
            return;
        }
        stormClock = 0;
        nextLightning = 5.2f + Math.floorMod(checkpointLevel * 17 + score, 38) * 0.1f;
        lightningFlash = 1;
        addCameraTrauma(0.07f);
    }

    private void updateAtmosphereEffect(float delta) {
        float healthRatio = maxHealth <= 0 ? 0 : health / (float) maxHealth;
        float danger = clamp((0.38f - healthRatio) / 0.38f, 0, 1);
        float previousStorm = Math.floorMod(previousZone, 4) == 3 ? 1 : 0;
        float currentStorm = Math.floorMod(displayedZone, 4) == 3 ? 1 : 0;
        float smoothBlend = zoneBlend * zoneBlend * (3 - 2 * zoneBlend);
        float storm = previousStorm + (currentStorm - previousStorm) * smoothBlend;
        atmosphereEffect.update(delta, previousZone, displayedZone, zoneBlend,
                danger, storm, lightningFlash);
        Color4f ambientFrom = lightingAmbient(previousZone);
        Color4f ambientTo = lightingAmbient(displayedZone);
        lightingEffect.ambientColor(
                ambientFrom.r + (ambientTo.r - ambientFrom.r) * smoothBlend,
                ambientFrom.g + (ambientTo.g - ambientFrom.g) * smoothBlend,
                ambientFrom.b + (ambientTo.b - ambientFrom.b) * smoothBlend)
                .ambientIntensity(0.72f - danger * 0.08f + lightningFlash * 0.04f);
    }

    private Color4f lightingAmbient(int zone) {
        return switch (Math.floorMod(zone, 4)) {
            case 1 -> new Color4f(0.62f, 0.86f, 0.76f, 1);
            case 2 -> new Color4f(0.82f, 0.64f, 0.94f, 1);
            case 3 -> new Color4f(0.66f, 0.76f, 1f, 1);
            default -> new Color4f(0.72f, 0.8f, 0.94f, 1);
        };
    }

    private Color4f zoneColor(Color4f[] palette) {
        Color4f from = palette[Math.floorMod(previousZone, palette.length)];
        Color4f to = palette[Math.floorMod(displayedZone, palette.length)];
        float t = zoneBlend * zoneBlend * (3 - 2 * zoneBlend);
        return new Color4f(
                from.r + (to.r - from.r) * t,
                from.g + (to.g - from.g) * t,
                from.b + (to.b - from.b) * t,
                from.a + (to.a - from.a) * t);
    }

    private void registerStomp() {
        registerDefeat("CRUSH", 250);
    }

    private void registerWeaponDefeat(Weapon weapon) {
        String action = switch (weapon) {
            case BLADE -> "SLASH";
            case BOW -> "PIERCE";
            case SHURIKEN -> "STAR CUT";
        };
        registerDefeat(action, weapon == Weapon.BOW ? 300 : 250);
    }

    private void registerNovaDefeat() {
        registerDefeat("ERASURE", 400);
    }

    private void registerDefeat(String action, int basePoints) {
        combo = comboTimer > 0 ? combo + 1 : 1;
        comboTimer = 3 + Math.min(0.8f, combo * 0.12f);
        bestCombo = Math.max(bestCombo, combo);
        kills++;
        int points = basePoints * combo;
        combatScore += points;
        String title = switch (combo) {
            case 1 -> "CLEAN " + action;
            case 2 -> "DOUBLE " + action + "  X2";
            case 3 -> "TRIPLE " + action + "  X3";
            case 4, 5 -> "RAMPAGE  X" + combo;
            default -> "TOWER BREAKER  X" + combo;
        };
        Color4f comboColor = combo >= 3
                ? Colors.from255(230, 165, 255, 255)
                : Colors.from255(143, 248, 230, 255);
        comboText.style()
                .fontScale(0.5f + Math.min(0.1f, combo * 0.012f))
                .color(comboColor)
                .borderColor(new Color4f(comboColor.r, comboColor.g, comboColor.b, 0.72f));
        comboText.text(title + "\n+" + points + " PTS");
        comboText.visible(true);
    }

    private void restartSameTower() {
        SceneManager.setScene(new GameScene(tower.seed()));
    }

    private void restartNewTower() {
        long freshSeed = TowerGenerator.createRunSeed(tower.seed());
        SceneManager.setScene(new GameScene(freshSeed));
    }

    private void returnToMenu() {
        SceneManager.setScene(MainMenuScene.class);
    }
}
