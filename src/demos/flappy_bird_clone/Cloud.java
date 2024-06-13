package flappy_bird_clone;

import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.utils.math.MathUtil;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.render.Texture;
import de.kostari.cloud.core.window.Time;
import de.kostari.cloud.core.window.Window;

public class Cloud extends GameObject {

    private int cloudSize;
    private float cloudSpeed;

    private Texture cloudDayTexture;
    private Texture cloudNightTexture;

    public Cloud() {
        super(false);
        this.cloudDayTexture = new Texture("./demo_assets/flappy_bird_clone/cloud-day.png").load();
        this.cloudNightTexture = new Texture("./demo_assets/flappy_bird_clone/cloud-night.png").load();
        setupCloud();
    }

    @Override
    public void update() {
        transform.position.x -= cloudSpeed * Time.delta;
        if (transform.position.x + cloudSize * 2 < 0) {
            setupCloud();
        }
        super.update();
    }

    @Override
    public void draw() {
        Render.drawTexture(transform.position.x, transform.position.y, cloudSize * 2, cloudSize, true,
                cloudDayTexture.getTextureId());
        Render.drawTexture(transform.position.x, transform.position.y, cloudSize * 2, cloudSize, true,
                cloudNightTexture.getTextureId(), GameScene.sun);
        super.draw();
    }

    public void setupCloud() {
        this.cloudSize = (int) MathUtil.random(50, 100);
        this.transform.position.x = Window.get().getWidth() +
                MathUtil.random(cloudSize * 2, Window.get().getWidth());
        this.transform.position.y = MathUtil.random(cloudSize * 2,
                Window.get().getHeight() - GameScene.GROUND_HEIGHT - cloudSize * 2);
        this.cloudSpeed = Math.clamp(MathUtil.random(90 - cloudSize, 90), 20, 90);
    }

}
