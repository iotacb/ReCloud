package tower_climber.main_menu;

import org.lwjgl.glfw.GLFW;

import de.kostari.cloud.core.scene.Scene;
import de.kostari.cloud.core.scene.SceneManager;
import de.kostari.cloud.core.ui.AlignItems;
import de.kostari.cloud.core.ui.Button;
import de.kostari.cloud.core.ui.ButtonSkin;
import de.kostari.cloud.core.ui.Canvas;
import de.kostari.cloud.core.ui.Flex;
import de.kostari.cloud.core.ui.FlexDirection;
import de.kostari.cloud.core.ui.JustifyContent;
import de.kostari.cloud.core.ui.Panel;
import de.kostari.cloud.core.ui.Text;
import de.kostari.cloud.core.ui.UIMaterial;
import de.kostari.cloud.core.utils.Colors;
import de.kostari.cloud.core.utils.audio.Audio;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.render.Texture;
import de.kostari.cloud.core.utils.types.Color4f;
import de.kostari.cloud.core.window.Input;
import de.kostari.cloud.core.window.Time;
import de.kostari.cloud.core.window.Window;
import tower_climber.GameScene;
import tower_climber.HighScores;
import tower_climber.Enemy;
import tower_climber.TowerSprites;
import demo_ui.DemoUI;

public class MainMenuScene extends Scene {

    private static final float START_TRANSITION_DURATION = 0.58f;

    private static final Color4f BACKGROUND_TOP = Colors.from255(5, 11, 25, 255);
    private static final Color4f SKY_BAND_ONE = Colors.from255(8, 20, 39, 255);
    private static final Color4f SKY_BAND_TWO = Colors.from255(11, 28, 50, 255);
    private static final Color4f SKY_BAND_THREE = Colors.from255(14, 37, 59, 255);
    private static final Color4f STAR = Colors.from255(176, 244, 236, 180);
    private static final Color4f DISTANT_BUILDING = Colors.from255(7, 17, 31, 230);
    private static final Color4f TOWER_DARK = Colors.from255(8, 17, 28, 255);
    private static final Color4f TOWER_FACE = Colors.from255(15, 31, 44, 255);
    private static final Color4f TOWER_EDGE = Colors.from255(55, 91, 98, 210);
    private static final Color4f PLATFORM = Colors.from255(96, 134, 136, 255);
    private static final Color4f WINDOW_OFF = Colors.from255(30, 62, 71, 255);
    private static final Color4f WINDOW_ON = Colors.from255(255, 197, 92, 220);
    private static final Color4f FOG = Colors.from255(77, 126, 132, 25);

    private Canvas canvas;
    private Panel menuCard;
    private Panel briefingCard;
    private Panel transitionShade;
    private Text transitionText;

    private Button buttonPlay;
    private Button buttonExit;
    private Text bestScore;
    private Audio menuClick;
    private float startDelay;
    private boolean starting;
    private Texture[] climberFrames;

    @Override
    public void init() {
        Window.get().setClearColor(BACKGROUND_TOP.r, BACKGROUND_TOP.g, BACKGROUND_TOP.b, 1);
        menuClick = new Audio("./demo_assets/tower_climber/sfx-ui.ogg").load();
        menuClick.setGain(0.42f);
        TowerSprites.load();
        climberFrames = TowerSprites.playerRun();
        buildMenu();
        super.init();
    }

    @Override
    public void draw() {
        drawBackdrop();
        super.draw();
    }

    @Override
    public void update() {
        layoutMenu();
        if (starting) {
            startDelay -= Math.min(Time.delta, 0.05f);
            updateStartTransition();
            if (startDelay <= 0) {
                SceneManager.setScene(GameScene.class);
            }
            return;
        }

        if (Input.keyPressed(GLFW.GLFW_KEY_ENTER) || Input.keyPressed(GLFW.GLFW_KEY_SPACE)) {
            startGame();
        }
        if (Input.keyPressed(GLFW.GLFW_KEY_ESCAPE)) {
            Window.get().close();
        }

        super.update();
    }

    @Override
    public void dispose() {
        if (menuClick != null) {
            menuClick.cleanUp();
            menuClick = null;
        }
        super.dispose();
    }

    private void buildMenu() {
        canvas = new Canvas();

        menuCard = new Panel();
        menuCard.layout().padding(32, 36);
        menuCard.background(DemoUI.surface(DemoUI.CYAN, 16));

        Flex content = new Flex(FlexDirection.COLUMN);
        content.align(AlignItems.STRETCH).justify(JustifyContent.CENTER).gap(15);

        Text eyebrow = new Text("A RECLOUD ASCENT  /  01");
        eyebrow.fontScale(0.42f).color(Colors.hex("#5be7d3")).align(de.kostari.cloud.core.ui.TextAlign.CENTER).shadow(1);

        Text title = new Text("TOWER\nCLIMBER");
        title.fontScale(1.85f).lineHeight(0.9f).color(Colors.hex("#f3f7f4"))
                .align(de.kostari.cloud.core.ui.TextAlign.CENTER).shadow(4, Colors.hex("#000000aa"));

        Text tagline = new Text("THE ONLY WAY OUT IS UP");
        tagline.fontScale(0.48f).color(Colors.hex("#a7c4c2"))
                .align(de.kostari.cloud.core.ui.TextAlign.CENTER).shadow(1);

        Panel accent = new Panel();
        accent.layout().height(3).margin(5, 74);
        accent.background(DemoUI.meterFill(DemoUI.RED));

        buttonPlay = new Button("START CLIMB").onClick(this::startGame);
        buttonPlay.layout().height(54);
        buttonPlay.panel().layout().padding(14);
        buttonPlay.fontScale(0.65f).skin(buttonSkin("#e85f43", "#ff7959", "#c64933", "#ffb29a", "#ffffff"));
        buttonPlay.textElement().shadow(2);

        buttonExit = new Button("EXIT TO DESKTOP").onClick(() -> Window.get().close());
        buttonExit.layout().height(48);
        buttonExit.panel().layout().padding(12);
        buttonExit.fontScale(0.55f).skin(buttonSkin("#10293a", "#183d50", "#0a1b28", "#5b8b9466", "#c7d9d8"));

        Flex stats = new Flex(FlexDirection.ROW);
        stats.justify(JustifyContent.SPACE_BETWEEN);
        stats.layout().margin(5, 1, 0, 1);

        bestScore = new Text("BEST  " + HighScores.load() + " PTS");
        bestScore.layout().padding(6, 8);
        bestScore.fontScale(0.38f).color(Colors.hex("#ffd782"))
                .background(DemoUI.surface(DemoUI.GOLD, 7));

        Text mode = new Text("SEEDED  /  ENDLESS");
        mode.fontScale(0.38f).color(Colors.hex("#668c8e")).align(de.kostari.cloud.core.ui.TextAlign.END);

        stats.add(bestScore, mode);
        content.add(eyebrow, title, tagline, accent, buttonPlay, buttonExit, stats);
        menuCard.add(content);
        canvas.append(menuCard, 88, 80, 438, 560);

        briefingCard = new Panel();
        briefingCard.layout().padding(18, 20);
        briefingCard.background(DemoUI.surface(DemoUI.CYAN, 12));
        Flex briefingContent = new Flex(FlexDirection.COLUMN);
        briefingContent.gap(9).align(AlignItems.STRETCH);
        Text briefingTitle = new Text("RUN BRIEFING  //  SURVIVE THE ASCENT");
        briefingTitle.fontScale(0.4f).color(Colors.hex("#8ff8e6")).shadow(1);
        Text briefingControls = new Text(
                "A D / ARROWS   MOVE      SPACE / W   JUMP / SLINGSHOT\n"
                        + "MOUSE   AIM 360      CLICK   ATTACK      SHIFT   DASH\n"
                        + "1 2 3   WEAPONS      RMB / F   NOVA      U   UPGRADES");
        briefingControls.fontScale(0.31f).lineHeight(1.45f).color(Colors.hex("#a7c4c2"));
        Panel briefingRule = new Panel();
        briefingRule.layout().height(2);
        briefingRule.background(DemoUI.meterFill(DemoUI.CYAN));
        Text legend = new Text(
                "PURPLE   RISK ROUTE     GOLD   GRAVITY SLINGSHOT\n"
                        + "VIOLET SHARDS   AETHER     ORANGE   RECOVERY     RED   VENT");
        legend.fontScale(0.3f).lineHeight(1.42f).color(Colors.hex("#d7e7e4"));
        briefingContent.add(briefingTitle, briefingControls, briefingRule, legend);
        briefingCard.add(briefingContent);
        canvas.append(briefingCard, 0, 0, 350, Canvas.AUTO);

        transitionShade = new Panel();
        transitionShade.background(new Color4f(2 / 255f, 7 / 255f, 17 / 255f, 0));
        transitionShade.visible(false);
        canvas.append(transitionShade, 0, 0, Canvas.FILL, Canvas.FILL);

        transitionText = new Text("ENTERING THE TOWER\nPREPARE TO ASCEND");
        transitionText.layout().padding(18, 24);
        transitionText.fontScale(0.62f).lineHeight(1.2f).color(Colors.hex("#8ff8e600"))
                .align(de.kostari.cloud.core.ui.TextAlign.CENTER).background(Colors.hex("#07152500"))
                .border(1, Colors.hex("#5be7d300")).shadow(2);
        transitionText.visible(false);
        canvas.append(transitionText, 0, 0, 420, Canvas.AUTO);
    }

    private void layoutMenu() {
        int width = Window.get().getWidth();
        int height = Window.get().getHeight();

        float cardWidth = Math.min(438, Math.max(320, width - 48));
        float cardHeight = Math.min(560, Math.max(500, height - 48));
        float cardX = width < 800 ? (width - cardWidth) * 0.5f : Math.max(48, width * 0.075f);
        float cardY = Math.max(24, (height - cardHeight) * 0.5f);
        canvas.setBounds(menuCard, cardX, cardY, cardWidth, cardHeight);

        boolean showBriefing = width >= 1_040 && height >= 620;
        briefingCard.visible(showBriefing);
        if (showBriefing) {
            float briefingWidth = Math.min(390, Math.max(350, width * 0.3f));
            canvas.setBounds(briefingCard,
                    width - briefingWidth - Math.max(38, width * 0.04f),
                    height - 206,
                    briefingWidth,
                    Canvas.AUTO);
        }
        canvas.setBounds(transitionShade, 0, 0, width, height);
        canvas.setBounds(transitionText,
                (width - 420) * 0.5f,
                Math.max(40, height * 0.5f - 54),
                420,
                Canvas.AUTO);
    }

    private void startGame() {
        if (starting) {
            return;
        }
        starting = true;
        startDelay = START_TRANSITION_DURATION;
        buttonPlay.label("ASCENDING...").enabled(false);
        buttonExit.enabled(false);
        transitionShade.visible(true);
        transitionText.visible(true);
        menuClick.play();
    }

    private void updateStartTransition() {
        float progress = 1 - Math.max(0, startDelay) / START_TRANSITION_DURATION;
        float eased = progress * progress * (3 - 2 * progress);
        transitionShade.background(new Color4f(2 / 255f, 7 / 255f, 17 / 255f,
                Math.min(0.96f, eased * 1.08f)));
        transitionText.color(new Color4f(0.56f, 0.97f, 0.9f, Math.min(1, progress * 2.5f)))
                .background(new Color4f(7 / 255f, 21 / 255f, 37 / 255f, eased * 0.9f))
                .borderColor(new Color4f(91 / 255f, 231 / 255f, 211 / 255f, eased * 0.65f));
    }

    private ButtonSkin buttonSkin(String normal, String hovered, String pressed, String border, String text) {
        Color4f accent = Colors.hex(border);
        UIMaterial normalMaterial = buttonMaterial(normal, accent, 0.14f);
        UIMaterial hoverMaterial = buttonMaterial(hovered, accent, 0.4f)
                .sheen(Colors.hex("#ffffffaa"), 0.16f, 0.38f, 0.68f);
        UIMaterial pressedMaterial = buttonMaterial(pressed, accent, 0.45f);
        UIMaterial disabledMaterial = new UIMaterial().fill(Colors.hex("#121923cc"))
                .border(1, Colors.hex("#8aa0b333")).radius(9);
        Color4f textColor = Colors.hex(text);
        return new ButtonSkin(normalMaterial, hoverMaterial, pressedMaterial, hoverMaterial, disabledMaterial,
                textColor, textColor, textColor, Colors.hex("#718090"));
    }

    private UIMaterial buttonMaterial(String fill, Color4f accent, float glow) {
        return new UIMaterial().fill(Colors.hex(fill)).border(1, accent).radius(9)
                .glow(accent, 10, glow);
    }

    private void drawBackdrop() {
        int width = Window.get().getWidth();
        int height = Window.get().getHeight();
        float time = Time.timePassed;

        Render.drawRect(0, height * 0.20f, width, height * 0.25f, SKY_BAND_ONE);
        Render.drawRect(0, height * 0.45f, width, height * 0.28f, SKY_BAND_TWO);
        Render.drawRect(0, height * 0.73f, width, height * 0.27f, SKY_BAND_THREE);

        drawStars(width, height, time);
        drawDistantCity(width, height);
        drawTower(width, height, time);
        drawFog(width, height, time);
        drawFrame(width, height);
    }

    private void drawStars(int width, int height, float time) {
        for (int i = 0; i < 42; i++) {
            float x = (i * 181 + 53) % Math.max(1, width);
            float y = 28 + (i * 97) % Math.max(1, (int) (height * 0.66f));
            float pulse = 0.45f + 0.55f * (float) Math.sin(time * (0.8f + i % 4 * 0.17f) + i);
            float size = i % 9 == 0 ? 3 : 1.5f;
            Render.drawRect(x, y, size, size, false,
                    new Color4f(STAR.r, STAR.g, STAR.b, 0.35f + pulse * 0.45f));
        }
    }

    private void drawDistantCity(int width, int height) {
        float ground = height - 42;
        int buildingWidth = 46;
        int count = width / buildingWidth + 2;

        for (int i = 0; i < count; i++) {
            float x = i * buildingWidth - 8;
            float buildingHeight = 52 + (i * 37 % 108);
            Render.drawRect(x, ground - buildingHeight, buildingWidth - 5, buildingHeight,
                    DISTANT_BUILDING);

            if (i % 3 == 0) {
                Render.drawRect(x + 10, ground - buildingHeight + 18, 4, 7, WINDOW_ON);
            }
        }
    }

    private void drawTower(int width, int height, float time) {
        float ground = height - 37;
        float centerX = width < 800 ? width * 0.82f : width * 0.76f;
        float towerWidth = Math.min(330, width * 0.29f);
        float towerHeight = Math.min(610, height * 0.86f);
        float left = centerX - towerWidth * 0.5f;
        float top = ground - towerHeight;

        Render.drawRect(left - 14, top + 35, towerWidth + 28, towerHeight - 35, TOWER_DARK);
        Render.drawRect(left, top, towerWidth, towerHeight, TOWER_FACE);
        Render.drawRect(left, top, 3, towerHeight, TOWER_EDGE);
        Render.drawRect(left + towerWidth - 3, top, 3, towerHeight, TOWER_EDGE);

        int floors = 9;
        float floorHeight = towerHeight / floors;
        for (int floor = 0; floor < floors; floor++) {
            float y = ground - floor * floorHeight;
            float ledge = floor % 3 == 1 ? 25 : 12;
            Render.drawRect(left - ledge, y - 5, towerWidth + ledge * 2, 7, PLATFORM);

            for (int window = 0; window < 4; window++) {
                float windowX = left + 26 + window * ((towerWidth - 52) / 4);
                Color4f color = (floor * 3 + window) % 7 == 0 ? WINDOW_ON : WINDOW_OFF;
                Render.drawRect(windowX, y - floorHeight + 22, 14, 19, color);
            }
        }

        Render.drawRect(centerX - 5, top - 42, 10, 42, TOWER_EDGE);
        Render.drawRect(centerX - 36, top - 3, 72, 6, PLATFORM);

        float climb = (time * 42) % Math.max(1, towerHeight - 90);
        float climberY = ground - 30 - climb;
        float climberX = left - 23 + (float) Math.sin(time * 2.3f) * 5;
        int frameIndex = Math.floorMod((int) (time / 0.07f), climberFrames.length);
        Render.drawTexture(climberFrames[frameIndex], climberX, climberY,
                116, 116, true);

        drawTowerActors(left, ground, towerWidth, floorHeight, time);

        Render.drawRect(0, ground, width, height - ground, TOWER_DARK);
        Render.drawRect(0, ground, width, 3, TOWER_EDGE);
    }

    private void drawTowerActors(float left, float ground, float towerWidth,
            float floorHeight, float time) {
        float basicX = left + towerWidth * 0.72f;
        float basicY = ground - floorHeight * 2 - 28;
        Render.drawTexture(TowerSprites.enemy(Enemy.Type.BASIC), basicX, basicY,
                62, 62, true);

        float evadePulse = 0.88f + (float) Math.sin(time * 4.2f) * 0.08f;
        float evaderX = left + towerWidth * 0.28f;
        float evaderY = ground - floorHeight * 5 - 27;
        Render.drawTexture(TowerSprites.enemy(Enemy.Type.EVADER), evaderX, evaderY,
                60, 60 * evadePulse, true);

        float flyingX = left + towerWidth * 0.66f + (float) Math.sin(time * 1.7f) * 38;
        float flyingY = ground - floorHeight * 7.15f + (float) Math.sin(time * 2.4f) * 12;
        Render.drawRotatedTexture(TowerSprites.enemy(Enemy.Type.FLYING), flyingX, flyingY,
                72, 72, true, (float) Math.sin(time * 3.2f) * 5, null);

        Texture crystal = TowerSprites.xpCrystal();
        for (int i = 0; i < 3; i++) {
            float x = left + towerWidth * 0.5f + (i - 1) * 25;
            float y = ground - floorHeight * 4 - 28
                    + (float) Math.sin(time * 3.6f + i) * 4;
            Render.drawRotatedTexture(crystal, x, y, 25, 25, true,
                    time * 45 + i * 30, null);
        }
    }

    private void drawFog(int width, int height, float time) {
        for (int i = 0; i < 5; i++) {
            float fogWidth = width * (0.25f + i * 0.035f);
            float travel = width + fogWidth;
            float x = (time * (9 + i * 2) + i * width * 0.31f) % travel - fogWidth;
            float y = height * (0.25f + i * 0.14f);
            Render.drawRect(x, y, fogWidth, 22 + i * 5, FOG);
        }
    }

    private void drawFrame(int width, int height) {
        Color4f frame = Colors.from255(91, 231, 211, 55);
        Render.drawRect(18, 18, width - 36, 1, frame);
        Render.drawRect(18, height - 19, width - 36, 1, frame);
        Render.drawRect(18, 18, 1, height - 36, frame);
        Render.drawRect(width - 19, 18, 1, height - 36, frame);
    }
}
