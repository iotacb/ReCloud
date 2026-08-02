package drawing_stuff;

import de.kostari.cloud.core.scene.Scene;
import de.kostari.cloud.core.ui.Absolute;
import de.kostari.cloud.core.ui.Button;
import de.kostari.cloud.core.ui.Canvas;
import de.kostari.cloud.core.ui.Flex;
import de.kostari.cloud.core.ui.FlexDirection;
import de.kostari.cloud.core.ui.Grid;
import de.kostari.cloud.core.ui.Panel;
import de.kostari.cloud.core.ui.Text;
import de.kostari.cloud.core.ui.TextAlign;
import de.kostari.cloud.core.utils.Colors;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.window.Window;
import demo_ui.DemoUI;

public class MyScene extends Scene {

    private Canvas canvas;
    private Flex uiDemo;
    private Text buttonStatus;
    private int buttonClicks;

    @Override
    public void init() {
        canvas = new Canvas();
        uiDemo = new Flex(FlexDirection.COLUMN);
        uiDemo.layout().padding(16);
        uiDemo.gap(12).background(DemoUI.surface(DemoUI.VIOLET, 13));

        Text title = new Text("UI System");
        title.fontScale(1.4f).color(Colors.hex("#ffffff")).shadow(2);

        Text subtitle = new Text("Flex, Grid, Text, Button and Panel");
        subtitle.color(Colors.hex("#c7d2fe")).shadow(1);

        Grid swatches = new Grid(3);
        swatches.gap(8).rowHeight(52);
        swatches.add(
                swatch("#ef4444"),
                swatch("#f59e0b"),
                swatch("#10b981"),
                swatch("#06b6d4"),
                swatch("#6366f1"),
                swatch("#ec4899"));

        Button button = DemoUI.button("Click me", DemoUI.VIOLET, () -> {
            buttonClicks++;
            buttonStatus.text("Button clicks: " + buttonClicks);
        });
        button.layout().width(150);

        buttonStatus = new Text("Button clicks: 0");
        buttonStatus.layout().padding(6);
        buttonStatus.align(TextAlign.CENTER).background(Colors.hex("#00000033"));

        uiDemo.add(title, subtitle, swatches, button, buttonStatus);
        Absolute overlay = new Absolute();
        overlay.add(uiDemo);
        overlay.position(uiDemo).right(24).top(24).width(368);
        canvas.add(overlay);
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

    private Panel swatch(String color) {
        Panel panel = new Panel();
        panel.layout().height(52);
        panel.background(Colors.hex(color)).border(1, Colors.hex("#ffffff55"));
        return panel;
    }

}
