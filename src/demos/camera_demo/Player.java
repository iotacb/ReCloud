package camera_demo;

import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.utils.Colors;
import de.kostari.cloud.core.utils.input.Controllings;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.window.Window;

public class Player extends GameObject {

    public Player() {
        super();
        transform.position.set(Window.get().getCenter());
    }

    @Override
    public void update() {
        Controllings.moveWithWASD(400, this);
        super.update();
    }

    @Override
    public void draw() {
        Render.drawRect(transform.position.x, transform.position.y, 100, 100, true, Colors.WHITE);
        super.draw();
    }

}
