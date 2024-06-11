package de.kostari.cloud.demo2;

import de.kostari.cloud.core.utils.Colors;
import de.kostari.cloud.core.utils.math.MathUtil;
import de.kostari.cloud.core.utils.math.Vector2;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.types.Color4f;
import de.kostari.cloud.core.window.Window;

public class Ball {

    private float x;
    private float y;
    private int size;

    private float velocityX;
    private float velocityY;

    private Color4f color;

    private int textureId;

    public Ball(int textureID) {
        this.velocityX = MathUtil.random(-1, 1);
        this.velocityY = MathUtil.random(-1, 1);

        this.textureId = textureID;

        this.size = 20;
        this.x = (int) MathUtil.random(size / 2, Window.instance.getWindowWidth() - size / 2);
        this.y = (int) MathUtil.random(size / 2, Window.instance.getWindowHeight() - size / 2);

        this.color = Colors.random();
    }

    public void draw() {
        // Render.drawRect((int) x, (int) y, size, size, true, color);
        Render.drawTexture((int) x, (int) y, size, size, true, textureId);
    }

    public void update() {
        this.x += velocityX;
        this.y += velocityY;

        if (x < size / 2 || x > Window.instance.getWindowWidth() - size / 2) {
            velocityX *= -1;
        }

        if (y < size / 2 || y > Window.instance.getWindowHeight() - size / 2) {
            velocityY *= -1;
        }
    }

}
