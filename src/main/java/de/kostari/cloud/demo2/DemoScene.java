package de.kostari.cloud.demo2;

import java.util.List;
import java.util.ArrayList;

import de.kostari.cloud.core.scene.Scene;
import de.kostari.cloud.core.utils.Colors;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.render.Texture;
import de.kostari.cloud.core.window.Input;
import de.kostari.cloud.core.window.Window;
import de.kostari.cloud.core.window.WindowEvents;

public class DemoScene extends Scene {

    private Texture texture;

    private List<Ball> balls = new ArrayList<>();

    @Override
    public void init() {
        this.texture = new Texture("img.png");
        spawn();

        WindowEvents.onMouseScroll.join(this, "scroll");
        super.init();
    }

    private void spawn() {
        for (int i = 0; i < 200000; i++) {
            balls.add(new Ball(texture.getTextureId()));
        }
    }

    public void scroll(float x, float y) {
        if (y < 0) {
            for (int i = 0; i < 10; i++) {
                balls.remove(0);
            }
        } else if (y > 0) {
            spawn();
        }
    }

    @Override
    public void draw() {
        balls.forEach(Ball::draw);
        super.draw();
    }

    @Override
    public void update() {
        Window.instance.setWindowTitle("FPS: " + Window.instance.getFPS() + " | Balls: " + balls.size());
        balls.forEach(Ball::update);
        super.update();
    }

}
