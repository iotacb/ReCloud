package flappy_bird_clone;

import de.kostari.cloud.core.events.EventInfo;
import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.utils.audio.Audio;
import de.kostari.cloud.core.utils.input.Keys;
import de.kostari.cloud.core.utils.math.Lerping;
import de.kostari.cloud.core.utils.render.AnimatedTexture;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.window.Input;
import de.kostari.cloud.core.window.Time;
import de.kostari.cloud.core.window.Window;

public class Bird extends GameObject {

    public static final int PLAYER_SIZE = 60;
    private static final float GRAVITY = 9.81f;
    private static final int JUMP_KEY = Keys.KEY_SPACE;

    private static final float JUMP_FORCE = 400;
    private static final float MAX_TILT = 60;

    private float velocity = 0;
    private float angle = 0;
    private float targetAngle = 0;

    private AnimatedTexture birdTexture;

    private Audio flapSound;
    private Audio dieSound;

    private boolean dead = false;
    private boolean gravity = false;

    public Bird() {
        super();
        setupBird();
        this.flapSound = new Audio("./demo_assets/flappy_bird_clone/flap.wav").load();
        this.dieSound = new Audio("./demo_assets/flappy_bird_clone/die.wav").load();
        this.birdTexture = new AnimatedTexture(new String[] { "./demo_assets/flappy_bird_clone/yellowbird-upflap.png",
                "./demo_assets/flappy_bird_clone/yellowbird-midflap.png",
                "./demo_assets/flappy_bird_clone/yellowbird-downflap.png",
                "./demo_assets/flappy_bird_clone/yellowbird-midflap.png" }, 15).load();
        GameManager.restartGameEvent.join(this, new EventInfo("setupBird"));
    }

    @Override
    public void draw() {
        Render.drawRotatedTexture(transform.position.x, transform.position.y,
                PLAYER_SIZE * 1.5f, PLAYER_SIZE, true,
                birdTexture.getTextureId(), angle);
        GameManager.drawDebugRect(transform.position.x, transform.position.y, PLAYER_SIZE, PLAYER_SIZE, true);
        super.draw();
    }

    @Override
    public void update() {
        if (!dead) {
            birdTexture.update();
        }
        if (transform.position.y < Window.get().getHeight() && gravity) {
            velocity += GRAVITY;
        }
        if (transform.position.y > Window.get().getHeight() - GameScene.GROUND_HEIGHT - PLAYER_SIZE / 2) {
            if (!isDead()) {
                velocity = -400;
                this.targetAngle = -180;
                die();
                return;
            }
            if (dead && velocity > 0) {
                velocity = 0;
            }
            transform.position.y = Window.get().getHeight() - GameScene.GROUND_HEIGHT - PLAYER_SIZE / 2;
        }
        updateJump();
        transform.position.y += velocity * Time.delta;
        if (!dead) {
            this.targetAngle = Math.min(MAX_TILT, Math.max(-MAX_TILT, velocity / 10));
        }
        this.angle = Lerping.lerp(angle, targetAngle, 0.1f);
        super.update();
    }

    public void updateJump() {
        if (Input.keyPressed(JUMP_KEY)) {
            if (dead) {
                GameManager.restartGameEvent.call();
                dead = false;
            }
            if (!GameManager.gameRunning) {
                GameManager.gameRunning = true;
                GameManager.gameStartedEvent.call();
                gravity = true;
            }
            velocity = -JUMP_FORCE;
            flapSound.play(true);
        }
    }

    public void setupBird() {
        transform.position.set(200, Window.get().getCenter().y);
        velocity = 0;
        angle = 0;
        targetAngle = 0;
        GameManager.gameRunning = false;
    }

    public void die() {
        this.dieSound.play();
        this.dead = true;
        GameManager.gameRunning = false;
    }

    public boolean isDead() {
        return dead;
    }

}
