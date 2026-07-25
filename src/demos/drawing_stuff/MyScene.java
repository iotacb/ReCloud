package drawing_stuff;

import de.kostari.cloud.core.scene.Scene;
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

public class MyScene extends Scene {

    private Canvas canvas;
    private Flex uiDemo;
    private Text buttonStatus;
    private int buttonClicks;

    @Override
    public void init() {
        canvas = new Canvas();
        uiDemo = new Flex(FlexDirection.COLUMN);
        uiDemo.style().css(
                "padding: 16px; gap: 12px; background: #101827dd; border: 1px solid #ffffff33; color: white;");

        Text title = new Text("UI System");
        title.style().css("font-scale: 1.4; color: white; shadow-depth: 2px;");

        Text subtitle = new Text("Flex, Grid, Text, Button and Panel");
        subtitle.style().css("color: #c7d2fe; shadow-depth: 1px;");

        Grid swatches = new Grid(3);
        swatches.style().css("gap: 8px; row-height: 52px;");
        swatches.add(
                swatch("#ef4444"),
                swatch("#f59e0b"),
                swatch("#10b981"),
                swatch("#06b6d4"),
                swatch("#6366f1"),
                swatch("#ec4899"));

        Button button = new Button("Click me").onClick(() -> {
            buttonClicks++;
            buttonStatus.text("Button clicks: " + buttonClicks);
        });
        button.style().css("width: 150px;");

        buttonStatus = new Text("Button clicks: 0");
        buttonStatus.style().textAlign(TextAlign.CENTER).css("padding: 6px; background: #00000033;");

        uiDemo.add(title, subtitle, swatches, button, buttonStatus);
        canvas.append(uiDemo, Window.get().getWidth() - 392, 24, 368, Canvas.AUTO);
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
        panel.style().css("height: 52px; background: " + color + "; border: 1px solid #ffffff55;");
        return panel;
    }

}
