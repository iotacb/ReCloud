package drawing_stuff;

import de.kostari.cloud.core.scene.Scene;
import de.kostari.cloud.core.utils.Colors;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.window.Window;

public class MyScene extends Scene {

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void draw() {
        Render.drawRect(20, 40,
                200, 200, false,
                Colors.BLUE);
        Render.drawRect(20, 10,
                200, 31, false,
                Colors.RED);
        super.draw();
    }

    @Override
    public void update() {
        super.update();
    }

}
