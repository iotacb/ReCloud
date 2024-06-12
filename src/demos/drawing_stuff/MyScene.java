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
        Render.drawRect(Window.get().getWidth() / 2, Window.get().getHeight() / 2, 200, 200, true,
                Colors.BLUE);
        super.draw();
    }

    @Override
    public void update() {
        super.update();
    }

}
