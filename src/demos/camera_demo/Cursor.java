package camera_demo;

import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.utils.Colors;
import de.kostari.cloud.core.utils.math.Vector2;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.window.Input;

public class Cursor extends GameObject {

    @Override
    public void update() {
        // this.transform.position.set(Input.getMousePosition());
        Vector2 lerped = transform.position.lerp(Input.getMousePosition(), .01f);
        transform.position.set(lerped);
        super.update();
    }

    @Override
    public void draw() {
        Render.drawRect(transform.position.x, transform.position.y, 100, 100, true, Colors.CYAN);
        super.draw();
    }

}
