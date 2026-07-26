package flappy_bird_clone;

import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.physics.PhysicsBody;
import de.kostari.cloud.core.utils.math.MathUtil;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.render.Texture;
import de.kostari.cloud.core.window.Time;
import de.kostari.cloud.core.window.Window;

public class Pipe extends GameObject {

    static final int COLLISION_LAYER = 1 << 1;
    private static final float PIPE_SPEED = 100f;

    private Texture pipeTexture;
    private PipeCollider topCollider;
    private PipeCollider bottomCollider;
    private PipeCollider scoreCollider;

    private float gap;
    private float offset;
    private int pipeWidth;
    private int pipeHeight;

    public boolean passed = false;
    public boolean pipeScored = false;
    public boolean birdEnteredScore = false;

    public Pipe() {
        super();
        this.pipeTexture = new Texture("./demo_assets/flappy_bird_clone/pipe-green.png").load();
        setupPipe();
        createColliders();
    }

    @Override
    public void update() {
        if (!GameManager.gameRunning) {
            return;
        }
        if (transform.position.x < pipeWidth * 1.5f && !passed) {
            GameManager.spawnNewPipeEvent.call();
            passed = true;
        }
        if (transform.position.x < -pipeWidth) {
            destroy();
        }
        transform.position.x -= PIPE_SPEED * GameManager.pipeSpeedFactor * Time.delta;
        syncColliders();
        super.update();
    }

    @Override
    public void draw() {
        // top pipe
        Render.drawRotatedTexture(transform.position.x, transform.position.y - (gap / 2) - (pipeHeight / 2), pipeWidth,
                pipeHeight,
                true, pipeTexture.getTextureId(), 180);
        // bottom pipe
        Render.drawTexture(transform.position.x, transform.position.y + (gap / 2) + (pipeHeight / 2), pipeWidth,
                pipeHeight,
                true, pipeTexture.getTextureId());

        GameManager.drawDebugRect(transform.position.x, transform.position.y - (gap / 2) - pipeHeight / 2, pipeWidth,
                pipeHeight, true);
        GameManager.drawDebugRect(transform.position.x, transform.position.y + (gap / 2) + pipeHeight / 2, pipeWidth,
                pipeHeight, true);
        super.draw();
    }

    private void setupPipe() {
        this.offset = MathUtil.random(-200, 100);
        this.pipeHeight = Window.get().getHeight();
        this.pipeWidth = pipeHeight / 8;
        gap = MathUtil.random(150, 350);
        transform.position.set(transform.position.x, Window.get().getHeight() / 2 + offset);
    }

    private void createColliders() {
        topCollider = new PipeCollider(pipeWidth, pipeHeight);
        bottomCollider = new PipeCollider(pipeWidth, pipeHeight);
        scoreCollider = new PipeCollider(pipeWidth, gap);
        syncColliders();
    }

    private void syncColliders() {
        topCollider.transform.position.set(
                transform.position.x,
                transform.position.y - gap / 2 - pipeHeight / 2f);
        bottomCollider.transform.position.set(
                transform.position.x,
                transform.position.y + gap / 2 + pipeHeight / 2f);
        scoreCollider.transform.position.set(transform.position);
    }

    public boolean collidingWithPipe(PhysicsBody birdBody) {
        return topCollider.body.isTouching(birdBody)
                || bottomCollider.body.isTouching(birdBody);
    }

    public boolean collidingWithScore(PhysicsBody birdBody) {
        return scoreCollider.body.isTouching(birdBody);
    }

    @Override
    public void dispose() {
        if (canBeDestroyed) {
            return;
        }
        topCollider.destroy();
        bottomCollider.destroy();
        scoreCollider.destroy();
        super.dispose();
    }

    public int getPipeWidth() {
        return pipeWidth;
    }

    public int getPipeHeight() {
        return pipeHeight;
    }

    private static final class PipeCollider extends GameObject {

        private final PhysicsBody body;

        private PipeCollider(float width, float height) {
            body = addComponent(PhysicsBody.fixed(width, height)
                    .sensor(true)
                    .layer(COLLISION_LAYER)
                    .collisionMask(Bird.COLLISION_LAYER));
        }
    }

}
