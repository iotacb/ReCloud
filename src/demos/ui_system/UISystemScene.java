package ui_system;

import de.kostari.cloud.core.scene.Scene;
import de.kostari.cloud.core.ui.AlignItems;
import de.kostari.cloud.core.ui.Button;
import de.kostari.cloud.core.ui.Canvas;
import de.kostari.cloud.core.ui.Flex;
import de.kostari.cloud.core.ui.FlexDirection;
import de.kostari.cloud.core.ui.Grid;
import de.kostari.cloud.core.ui.JustifyContent;
import de.kostari.cloud.core.ui.Panel;
import de.kostari.cloud.core.ui.Text;
import de.kostari.cloud.core.ui.TextAlign;
import de.kostari.cloud.core.utils.Colors;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.types.Color4f;
import de.kostari.cloud.core.window.Time;
import de.kostari.cloud.core.window.Window;

public class UISystemScene extends Scene {

    private static final Color4f WORLD_BACKGROUND = Colors.from255(8, 13, 24, 255);
    private static final Color4f WORLD_GRID = Colors.from255(51, 65, 85, 90);

    private Canvas canvas;
    private Flex root;
    private Grid componentGrid;
    private Panel inspectorPanel;
    private Panel floatingPanel;

    private Text statusText;
    private Text counterText;
    private Text themeText;
    private Text layoutText;
    private Text floatingText;

    private int clicks;
    private int gap = 12;
    private int themeIndex;
    private boolean showFloating = true;

    @Override
    public void init() {
        Window.get().setClearColor(WORLD_BACKGROUND.r, WORLD_BACKGROUND.g, WORLD_BACKGROUND.b, 1);
        buildUi();
        super.init();
    }

    @Override
    public void update() {
        Window.get().setTitle("UI System Demo | " + Math.round(Window.get().getFPS()) + " fps");
        statusText.text("Canvas: auto overlay | Elements: " + countElements(root)
                + " | FPS: " + Math.round(Window.get().getFPS()));
        counterText.text("Button clicks: " + clicks);
        themeText.text("Theme: " + (themeIndex % 2 == 0 ? "midnight" : "daylight"));
        layoutText.text("Flex gap: " + gap + "px | Grid columns: 3 | Floating panel: "
                + (showFloating ? "visible" : "hidden"));
        floatingText.text("Canvas overlay\nscreen-space pinned\n" + Math.round(Time.timePassed * 10) / 10f + "s");

        canvas.setBounds(root, 24, 24, Window.get().getWidth() - 48, Window.get().getHeight() - 48);
        canvas.setBounds(floatingPanel, Window.get().getWidth() - 230, Window.get().getHeight() - 110, 206,
                Canvas.AUTO);
        super.update();
    }

    @Override
    public void draw() {
        drawWorld();
        super.draw();
    }

    private void buildUi() {
        canvas = new Canvas();

        root = new Flex(FlexDirection.COLUMN);
        root.style().css("gap: 14px; align-items: stretch; color: white;");

        Flex header = header();
        Flex body = body();
        body.style().grow(1);

        root.add(header, body);
        canvas.append(root, 24, 24, Window.get().getWidth() - 48, Window.get().getHeight() - 48);

        floatingPanel = new Panel();
        floatingPanel.style().css("padding: 12px; background: #0ea5e9dd; border: 1px solid #e0f2feaa;");
        floatingText = new Text("");
        floatingText.style().css("color: white; shadow-depth: 1px;");
        floatingPanel.add(floatingText);
        canvas.append(floatingPanel, Window.get().getWidth() - 230, Window.get().getHeight() - 110, 206,
                Canvas.AUTO);
    }

    private Flex header() {
        Flex header = new Flex(FlexDirection.ROW);
        header.style().css(
                "padding: 16px 18px; gap: 16px; align-items: center; justify-content: space-between; background: #0f172add; border: 1px solid #38bdf855;");

        Flex copy = new Flex(FlexDirection.COLUMN);
        copy.style().css("gap: 4px;");

        Text title = new Text("UI System Demo");
        title.style().css("font-scale: 1.5; color: #f8fafc; shadow-depth: 2px;");

        statusText = new Text("");
        statusText.style().css("color: #bae6fd;");

        copy.add(title, statusText);

        Flex actions = new Flex(FlexDirection.ROW);
        actions.style().css("gap: 8px; align-items: center;");

        actions.add(
                new Button("Click").onClick(() -> clicks++),
                new Button("Theme").onClick(this::toggleTheme),
                new Button("Gap").onClick(this::cycleGap),
                new Button("Overlay").onClick(this::toggleFloating));

        header.add(copy, actions);
        return header;
    }

    private Flex body() {
        Flex body = new Flex(FlexDirection.ROW);
        body.style().css("gap: 14px; align-items: stretch;");

        Flex left = new Flex(FlexDirection.COLUMN);
        left.style().css("gap: 14px; grow: 1; align-items: stretch;");
        left.add(explanationPanel(), componentGrid());

        Flex right = new Flex(FlexDirection.COLUMN);
        right.style().css("gap: 14px; width: 360px; align-items: stretch;");
        right.add(inspectorPanel(), controlsPanel(), cssPanel());

        body.add(left, right);
        return body;
    }

    private Panel explanationPanel() {
        Panel panel = new Panel();
        panel.style().css("padding: 16px; background: #111827dd; border: 1px solid #ffffff22;");

        Text text = new Text("Build one Canvas and append UI elements once.\n"
                + "Update element state from code, and the canvas renders it.\n"
                + "The overlay pass stays above world rendering and post effects.");
        text.style().css("color: #e5e7eb; line-height: 1.35; shadow-depth: 1px;");

        panel.add(text);
        return panel;
    }

    private Grid componentGrid() {
        componentGrid = new Grid(3);
        componentGrid.style().css("gap: 12px; row-height: 168px; grow: 1;");
        componentGrid.add(
                componentCard("Canvas", "Retained overlay root\nAuto top layer\nScreen-space slots", "#0284c7"),
                componentCard("Flex", "Rows or columns\nGap, grow, stretch\nSimple HUD layout", "#7c3aed"),
                componentCard("Grid", "Fixed columns\nRow and column gaps\nCard collections", "#0891b2"),
                componentCard("Text", "Font, color, shadow\nAlignment\nLive updates", "#16a34a"),
                componentCard("Button", "Hover and press states\nonClick callbacks\nStyled like CSS", "#db2777"),
                componentCard("Panel", "Backgrounds\nBorders and padding\nGroup content", "#ea580c"));
        return componentGrid;
    }

    private Panel inspectorPanel() {
        inspectorPanel = new Panel();
        inspectorPanel.style().css("padding: 16px; background: #020617dd; border: 1px solid #64748b66;");

        Flex content = new Flex(FlexDirection.COLUMN);
        content.style().css("gap: 8px;");

        Text title = new Text("Live Inspector");
        title.style().css("font-scale: 1.25; color: #f8fafc; shadow-depth: 2px;");

        counterText = new Text("");
        themeText = new Text("");
        layoutText = new Text("");

        counterText.style().css("color: #bae6fd;");
        themeText.style().css("color: #c4b5fd;");
        layoutText.style().css("color: #bbf7d0;");

        content.add(title, counterText, themeText, layoutText);
        inspectorPanel.add(content);
        return inspectorPanel;
    }

    private Panel controlsPanel() {
        Panel panel = new Panel();
        panel.style().css("padding: 16px; background: #111827dd; border: 1px solid #ffffff22;");

        Flex content = new Flex(FlexDirection.COLUMN);
        content.style().css("gap: 10px;");

        Text title = new Text("Button Callbacks");
        title.style().css("font-scale: 1.15; color: white;");

        Flex row = new Flex(FlexDirection.ROW);
        row.style().css("gap: 8px;");

        Button add = new Button("+ Count").onClick(() -> clicks++);
        add.style().grow(1);

        Button reset = new Button("Reset").onClick(() -> clicks = 0);
        reset.style().grow(1);

        row.add(add, reset);
        content.add(title, row);
        panel.add(content);
        return panel;
    }

    private Panel cssPanel() {
        Panel panel = new Panel();
        panel.style().css("padding: 16px; background: #111827dd; border: 1px solid #ffffff22;");

        Flex content = new Flex(FlexDirection.COLUMN);
        content.style().css("gap: 10px;");

        Text title = new Text("CSS-like Styling");
        title.style().css("font-scale: 1.15; color: white;");

        Panel sample = new Panel();
        sample.style().css("padding: 12px; background: #22c55e33; border: 2px solid #86efacaa;");
        Text sampleText = new Text(
                "padding: 12px;\nline-height: 1.35;\nbackground: #22c55e33;\nborder: 2px solid #86efacaa;");
        sampleText.style().css("color: #dcfce7; line-height: 1.35; shadow-depth: 1px;");
        sample.add(sampleText);

        content.add(title, sample);
        panel.add(content);
        return panel;
    }

    private Panel componentCard(String title, String description, String accent) {
        Panel card = new Panel();
        card.style().css("padding: 12px; background: #0b1220dd; border: 1px solid " + accent + "aa;");

        Flex content = new Flex(FlexDirection.COLUMN);
        content.style().css("gap: 7px;");

        Text titleText = new Text(title);
        titleText.style().css("font-scale: 1.15; color: " + accent + "; shadow-depth: 1px;");

        Text bodyText = new Text(description);
        bodyText.style().css("color: #dbeafe; line-height: 1.28;");

        content.add(titleText, bodyText);
        card.add(content);
        return card;
    }

    private void toggleTheme() {
        themeIndex++;
        if (themeIndex % 2 == 0) {
            root.style().css("color: white;");
            inspectorPanel.style().css("background: #020617dd; border: 1px solid #64748b66;");
            floatingPanel.style().css("background: #0ea5e9dd; border: 1px solid #e0f2feaa;");
            return;
        }

        root.style().css("color: #111827;");
        inspectorPanel.style().css("background: #f8faf0ee; border: 1px solid #14b8a655;");
        floatingPanel.style().css("background: #f97316dd; border: 1px solid #ffedd5aa;");
    }

    private void cycleGap() {
        gap += 6;
        if (gap > 24) {
            gap = 6;
        }
        componentGrid.style().gap(gap);
    }

    private void toggleFloating() {
        showFloating = !showFloating;
        floatingPanel.visible(showFloating);
    }

    private void drawWorld() {
        float time = Time.timePassed;
        int width = Window.get().getWidth();
        int height = Window.get().getHeight();

        for (int x = 0; x < width; x += 48) {
            Render.drawRect(x, 0, 1, height, false, WORLD_GRID);
        }
        for (int y = 0; y < height; y += 48) {
            Render.drawRect(0, y, width, 1, false, WORLD_GRID);
        }

        for (int i = 0; i < 8; i++) {
            float x = ((time * 38) + i * 152) % (width + 80) - 40;
            float y = 98 + (float) Math.sin(time * 1.4f + i) * 34 + i * 58;
            float size = 32 + (i % 3) * 12;
            Color4f color = Colors.from255(56 + i * 20, 189, 248, 90);
            Render.drawRotatedRect(x, y, size, size, true, color, time * 28 + i * 17);
        }
    }

    private int countElements(de.kostari.cloud.core.ui.UIElement element) {
        int count = 1;
        for (de.kostari.cloud.core.ui.UIElement child : element.children()) {
            count += countElements(child);
        }
        return count;
    }
}
