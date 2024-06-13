package flappy_bird_clone;

import java.util.ArrayList;
import java.util.List;

import de.kostari.cloud.core.events.EventInfo;
import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.scene.Scene;
import de.kostari.cloud.core.utils.Colors;
import de.kostari.cloud.core.utils.audio.Audio;
import de.kostari.cloud.core.utils.input.Keys;
import de.kostari.cloud.core.utils.math.MathUtil;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.render.Texture;
import de.kostari.cloud.core.utils.render.font.Font;
import de.kostari.cloud.core.window.Input;
import de.kostari.cloud.core.window.Time;
import de.kostari.cloud.core.window.Window;

import de.kostari.cloud.core.utils.types.Color4f;

public class GameScene extends Scene {

    private static long DAY_NIGHT_CYCLE = 1000 * 60;

    public static final int GROUND_HEIGHT = 100;

    private Texture backgroundDayTexture;
    private Texture backgroundNightTexture;

    private Texture groundTexture;

    private Bird bird;

    private float gameTime;
    private float scrollBackground;
    private float scrollGround;

    public static Color4f sun = Colors.WHITE;

    private List<Cloud> clouds = new ArrayList<>();

    private Audio scoreSound;

    private Font font;

    @Override
    public void init() {
        this.backgroundDayTexture = new Texture("./demo_assets/flappy_bird_clone/background-day.png").load();
        this.backgroundNightTexture = new Texture("./demo_assets/flappy_bird_clone/background-night.png").load();
        this.groundTexture = new Texture("./demo_assets/flappy_bird_clone/base.png").load();
        this.scoreSound = new Audio("./demo_assets/flappy_bird_clone/point.wav").load();
        this.bird = new Bird();

        // bugs at 88 font height
        this.font = new Font("./demo_assets/flappy_bird_clone/title.ttf", 87).load();

        spawnClouds();

        GameManager.spawnNewPipeEvent.join(this, new EventInfo("spawnPipe"));
        GameManager.gameStartedEvent.join(this, new EventInfo("spawnPipe"));
        GameManager.restartGameEvent.join(this, new EventInfo("clearPipes"));

        super.init();
    }

    @Override
    public void update() {
        Window.get().setTitle("Flappy Bird Clone | " + Window.get().getFPS());
        updateDayNightCycle();
        updateScrollingBackground();
        checkCollisions();
        if (Input.keyPressed(Keys.KEY_F2)) {
            GameManager.debugging = !GameManager.debugging;
        }

        clouds.forEach(Cloud::update);

        if (GameManager.debugging) {
            getCamera().drag(2);
            getCamera().handleScrolling(.1f);
        } else {
            getCamera().transform.position.set(0, 0);
            getCamera().setZoom(1);
        }
        super.update();
    }

    private float gameOverBounce = 0;

    @Override
    public void draw() {
        Render.drawTexture(scrollBackground, 0, Window.get().getWidth(),
                Window.get().getHeight(), false,
                backgroundDayTexture.getTextureId());
        Render.drawTexture(scrollBackground, 0, Window.get().getWidth(),
                Window.get().getHeight(), false,
                backgroundNightTexture.getTextureId(), sun);
        Render.drawTexture(Window.get().getWidth() + scrollBackground, 0,
                Window.get().getWidth(), Window.get().getHeight(),
                false,
                backgroundDayTexture.getTextureId());
        Render.drawTexture(Window.get().getWidth() + scrollBackground, 0,
                Window.get().getWidth(), Window.get().getHeight(),
                false,
                backgroundNightTexture.getTextureId(), sun);
        // Ground
        Render.drawTexture(scrollGround, Window.get().getHeight() - GROUND_HEIGHT,
                Window.get().getWidth(), GROUND_HEIGHT * 2,
                false,
                groundTexture.getTextureId());
        Render.drawTexture(Window.get().getWidth() + scrollGround, Window.get().getHeight() - GROUND_HEIGHT,
                Window.get().getWidth(), GROUND_HEIGHT * 2,
                false,
                groundTexture.getTextureId());
        GameManager.drawDebugRect(0, Window.get().getHeight() - GROUND_HEIGHT, Window.get().getWidth(),
                GROUND_HEIGHT * 2,
                false);
        clouds.forEach(Cloud::draw);

        if (!GameManager.gameRunning) {
            if (bird.isDead()) {
                gameOverBounce = 1 + ((float) (Math.sin(Time.timePassed * 20) + 1) * 0.01f);
                Render.drawTextShadow(font, "Game Over",
                        Window.get().getCenter().x - (Render.getTextWidth(font, "Game Over") * gameOverBounce) / 2,
                        Window.get().getCenter().y - (font.getFontHeight() * gameOverBounce), gameOverBounce, 6f,
                        Colors.CORAL);
            }
            Render.drawTextShadow(font, "Flappy Bird",
                    Window.get().getCenter().x - Render.getTextWidth(font, "Flappy Bird") / 2,
                    Window.get().getCenter().y / 2, 6, Colors.WHITE);
        }
        Render.drawTextShadow(font, String.valueOf(GameManager.score), 20, 15,
                2, Colors.WHITE);

        super.draw();
    }

    private void updateDayNightCycle() {
        this.gameTime = getBouncingValue(System.currentTimeMillis(), DAY_NIGHT_CYCLE);
        GameScene.sun = Colors.alpha(sun, gameTime);
    }

    private void updateScrollingBackground() {
        if (!GameManager.gameRunning) {
            return;
        }
        this.scrollBackground -= Time.delta * 20;
        this.scrollGround -= Time.delta * 100;
        if (scrollBackground < -Window.get().getWidth()) {
            scrollBackground = 0;
        }
        if (scrollGround < -Window.get().getWidth()) {
            scrollGround = 0;
        }
    }

    private void spawnClouds() {
        for (int i = 0; i < 5; i++) {
            Cloud cloud = new Cloud();
            cloud.transform.position.x = MathUtil.random(0, Window.get().getWidth() * 2);
            clouds.add(cloud);
        }
    }

    private boolean isInScore = false;

    private void checkCollisions() {
        for (GameObject gameObject : getGameObjects()) {
            if (gameObject instanceof Pipe pipe) {
                if (pipe.collidingWithPipe(bird.transform.position.x,
                        bird.transform.position.y, Bird.PLAYER_SIZE,
                        Bird.PLAYER_SIZE) && !bird.isDead()) {
                    bird.die();
                    break;
                }
                if (pipe.collidingWithScore(bird.transform.position.x, bird.transform.position.y, Bird.PLAYER_SIZE,
                        Bird.PLAYER_SIZE)) {
                    if (!pipe.pipeScored) {
                        isInScore = true;
                        pipe.pipeScored = true;
                    }
                } else {
                    if (isInScore) {
                        System.out.println("Scored!");
                        GameManager.score++;
                        GameManager.pipeSpeedFactor += 0.1f;
                        GameManager.playerScoreEvent.call();
                        scoreSound.play(true);
                        isInScore = false;
                    }
                }
            }
        }
    }

    public void clearPipes() {
        getGameObjects().forEach(go -> {
            if (go instanceof Pipe pipe)
                pipe.destroy();
        });
    }

    public void spawnPipe() {
        Pipe pipe = new Pipe();
        pipe.transform.position.x = Window.get().getWidth() + pipe.getPipeWidth();
    }

    public static float getBouncingValue(long currentTimeMillis, long durationMillis) {
        double elapsed = currentTimeMillis % durationMillis;
        double phase = (elapsed / durationMillis) * 2 * Math.PI;
        return (float) (0.5 * (Math.sin(phase) + 1)); // Scale sine wave to 0 to 1 range
    }

}
