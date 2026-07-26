package physics_demo;

import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.physics.PhysicsBody;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.types.Color4f;

public class PhysicsBox extends GameObject {

    private final float width;
    private final float height;
    private final Color4f color;
    public final PhysicsBody body;

    public PhysicsBox(float x, float y, float width, float height, boolean fixed, Color4f color) {
        this.width = width;
        this.height = height;
        this.color = color;
        this.transform.position.set(x, y);
        this.body = addComponent(fixed
                ? PhysicsBody.fixed(width, height)
                : PhysicsBody.dynamic(width, height));
    }

    @Override
    public void draw() {
        Render.drawRect(
                transform.position.x,
                transform.position.y,
                width,
                height,
                true,
                color);
        super.draw();
    }
}
