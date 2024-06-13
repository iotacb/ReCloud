package flappy_bird_clone;

import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.utils.math.MathUtil;
import de.kostari.cloud.core.utils.math.Physics;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.render.Texture;
import de.kostari.cloud.core.window.Time;
import de.kostari.cloud.core.window.Window;

public class Pipe extends GameObject {

    private static final float PIPE_SPEED = 100f;

    private Texture pipeTexture;

    private float gap;
    private float offset;
    private int pipeWidth;
    private int pipeHeight;

    public boolean passed = false;
    public boolean pipeScored = false;

    public Pipe() {
        super();
        this.pipeTexture = new Texture("./demo_assets/flappy_bird_clone/pipe-green.png").load();
        setupPipe();
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

    public boolean collidingWithPipe(float x, float y, float width, float height) {
        return Physics.isColliding(x, y, width, height, transform.position.x,
                transform.position.y - (gap / 2) - pipeHeight / 2, pipeWidth, pipeHeight) ||
                Physics.isColliding(x, y, width, height, transform.position.x,
                        transform.position.y + (gap / 2) + pipeHeight / 2, pipeWidth, pipeHeight);
    }

    public boolean collidingWithScore(float x, float y, float width, float height) {
        return Physics.isColliding(x, y, width, height, transform.position.x, transform.position.y, pipeWidth, gap);
    }

    public int getPipeWidth() {
        return pipeWidth;
    }

    public int getPipeHeight() {
        return pipeHeight;
    }

}
