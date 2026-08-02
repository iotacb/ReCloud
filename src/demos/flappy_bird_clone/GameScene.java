package flappy_bird_clone;

import java.util.ArrayList;
import java.util.List;

import de.kostari.cloud.core.events.EventInfo;
import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.physics.PhysicsBody;
import de.kostari.cloud.core.scene.Scene;
import de.kostari.cloud.core.ui.Absolute;
import de.kostari.cloud.core.ui.Canvas;
import de.kostari.cloud.core.ui.Text;
import de.kostari.cloud.core.ui.TextAlign;
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
    private PhysicsBody groundCollider;

    private float gameTime;
    private float scrollBackground;
    private float scrollGround;

    public static Color4f sun = Colors.WHITE;

    private List<Cloud> clouds = new ArrayList<>();

    private Audio scoreSound;

    private Font font;
    private Canvas canvas;
    private Absolute uiLayer;
    private Text titleText;
    private Text gameOverText;
    private Text scoreText;

    @Override
    public void init() {
        this.backgroundDayTexture = new Texture("./demo_assets/flappy_bird_clone/background-day.png").load();
        this.backgroundNightTexture = new Texture("./demo_assets/flappy_bird_clone/background-night.png").load();
        this.groundTexture = new Texture("./demo_assets/flappy_bird_clone/base.png").load();
        this.scoreSound = new Audio("./demo_assets/flappy_bird_clone/point.wav").load();
        this.bird = new Bird();
        createGroundCollider();

        // bugs at 88 font height
        this.font = new Font("./demo_assets/flappy_bird_clone/title.ttf", 87).load();
        createUi();

        spawnClouds();

        GameManager.spawnNewPipeEvent.join(this, new EventInfo("spawnPipe"));
        GameManager.gameStartedEvent.join(this, new EventInfo("spawnPipe"));
        GameManager.restartGameEvent.join(this, new EventInfo("clearPipes"));

        Render.postProcessing().enableBloom(.65f, 1f, 1f);
        super.init();
    }

    @Override
    public void update() {
        Window.get().setTitle("Flappy Bird Clone | " + Window.get().getFPS());
        updateDayNightCycle();
        updateScrollingBackground();
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
        checkCollisions();
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
            }
        }
        updateUi();
        scoreText.text(String.valueOf(GameManager.score));

        super.draw();
    }

    private void createUi() {
        canvas = new Canvas();
        uiLayer = new Absolute();

        titleText = new Text("Flappy Bird");
        titleText.font(font)
                .align(TextAlign.CENTER)
                .verticalAlign(de.kostari.cloud.core.ui.AlignItems.CENTER)
                .color(new de.kostari.cloud.core.utils.types.Color4f(1, 1, 1, 1))
                .shadow(6);

        gameOverText = new Text("Game Over");
        gameOverText.font(font)
                .align(TextAlign.CENTER)
                .verticalAlign(de.kostari.cloud.core.ui.AlignItems.CENTER)
                .color(new de.kostari.cloud.core.utils.types.Color4f(1, 0.5f, 0.31f, 1))
                .shadow(6);

        scoreText = new Text("0");
        scoreText.font(font)
                .color(new de.kostari.cloud.core.utils.types.Color4f(1, 1, 1, 1))
                .shadow(2);

        uiLayer.add(scoreText, titleText, gameOverText);
        uiLayer.position(scoreText).left(20).top(15).width(160);
        uiLayer.position(titleText).left(0).right(0).anchor(0, 0.25f);
        uiLayer.position(gameOverText).left(0).right(0).anchor(0, 0.5f);
        canvas.add(uiLayer);
        updateUi();
    }

    private void updateUi() {
        boolean showTitle = !GameManager.gameRunning;
        boolean showGameOver = showTitle && bird.isDead();

        titleText.visible(showTitle);
        gameOverText.visible(showGameOver);
        scoreText.visible(true);

        if (showGameOver) {
            gameOverText.fontScale(gameOverBounce);
        }

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

    private void checkCollisions() {
        PhysicsBody birdBody = bird.collisionBody();
        if (groundCollider.isTouching(birdBody)) {
            bird.collideWithGround();
        }

        for (GameObject gameObject : getGameObjects()) {
            if (gameObject instanceof Pipe pipe) {
                if (pipe.collidingWithPipe(birdBody) && !bird.isDead()) {
                    bird.die();
                    break;
                }

                if (pipe.collidingWithScore(birdBody)) {
                    pipe.birdEnteredScore = true;
                } else if (pipe.birdEnteredScore && !pipe.pipeScored) {
                    pipe.pipeScored = true;
                    scorePoint();
                }
            }
        }
    }

    private void createGroundCollider() {
        GameObject ground = new GameObject();
        ground.transform.position.set(Window.get().getCenter().x, Window.get().getHeight());
        groundCollider = ground.addComponent(PhysicsBody.fixed(
                Window.get().getWidth(),
                GROUND_HEIGHT * 2)
                .sensor(true)
                .layer(Pipe.COLLISION_LAYER)
                .collisionMask(Bird.COLLISION_LAYER));
    }

    private void scorePoint() {
        System.out.println("Scored!");
        GameManager.score++;
        GameManager.pipeSpeedFactor += 0.1f;
        GameManager.playerScoreEvent.call();
        scoreSound.play(true);
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
