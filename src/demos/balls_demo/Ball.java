package balls_demo;

import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.utils.Colors;
import de.kostari.cloud.core.utils.math.Vector2;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.types.Color4f;
import de.kostari.cloud.core.window.Window;

public class Ball extends GameObject {

    private static final int SIZE = 20;

    private Color4f color;
    private Vector2 velocity;

    public Ball() {
        super();

        this.color = Colors.random();
        this.velocity = Vector2.fromRandomDirection().mul(1);
        this.transform.position = Window.get().getCenter();
    }

    @Override
    public void update() {
        transform.position = transform.position.add(velocity);

        if (transform.position.x < SIZE / 2
                || transform.position.x > Window.get().getWidth() - SIZE / 2) {
            velocity.x *= -1;
        }

        if (transform.position.y < SIZE / 2 || transform.position.y > Window.get().getHeight() - SIZE / 2) {
            velocity.y *= -1;
        }
        super.update();
    }

    @Override
    public void draw() {
        Render.drawRect(transform.position.x, transform.position.y, SIZE, SIZE, true, color);
        super.draw();
    }

}
