package ui_system;

import de.kostari.cloud.core.scene.Scene;
import de.kostari.cloud.core.ui.Absolute;
import de.kostari.cloud.core.ui.AlignItems;
import de.kostari.cloud.core.ui.Button;
import de.kostari.cloud.core.ui.ButtonSkin;
import de.kostari.cloud.core.ui.Canvas;
import de.kostari.cloud.core.ui.Flex;
import de.kostari.cloud.core.ui.FlexDirection;
import de.kostari.cloud.core.ui.Grid;
import de.kostari.cloud.core.ui.JustifyContent;
import de.kostari.cloud.core.ui.Panel;
import de.kostari.cloud.core.ui.Slider;
import de.kostari.cloud.core.ui.Text;
import de.kostari.cloud.core.ui.TextBox;
import de.kostari.cloud.core.ui.UIMaterial;
import de.kostari.cloud.core.ui.UIElement;
import de.kostari.cloud.core.utils.Colors;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.types.Color4f;
import de.kostari.cloud.core.window.Time;
import de.kostari.cloud.core.window.Window;

public class UISystemScene extends Scene {

    private static final Color4f BACKGROUND = Colors.hex("#050916");
    private static final Color4f GRID = Colors.hex("#29415f44");
    private static final Color4f CYAN = Colors.hex("#52e5ff");
    private static final Color4f VIOLET = Colors.hex("#a98cff");
    private static final Color4f TEXT = Colors.hex("#eaf7ff");
    private static final Color4f MUTED = Colors.hex("#8ea7b9");

    private Canvas canvas;
    private Absolute sceneLayer;
    private Flex root;
    private Grid componentGrid;
    private Text status;
    private Text counter;
    private Text inputPreview;
    private Text floatingText;
    private Panel floatingPanel;
    private int clicks;

    @Override
    public void init() {
        Window.get().setClearColor(BACKGROUND.r, BACKGROUND.g, BACKGROUND.b, 1);
        buildUi();
        super.init();
    }

    @Override
    public void update() {
        Window.get().setTitle("Cloud UI Lab | " + Math.round(Window.get().getFPS()) + " fps");
        status.text("Retained tree  " + countElements(root) + " elements    /    "
                + Math.round(Window.get().getFPS()) + " fps");
        counter.text("Interaction count  " + clicks);
        floatingText.text("SHADER UI\n" + String.format("%04.1fs", Time.timePassed));
        float bob = (float) Math.sin(Time.timePassed * 1.8f) * 5;
        floatingPanel.translate(0, bob);
        super.update();
    }

    @Override
    public void draw() {
        drawWorld();
        super.draw();
    }

    private void buildUi() {
        canvas = new Canvas();
        sceneLayer = new Absolute();
        root = new Flex(FlexDirection.COLUMN).gap(14).align(AlignItems.STRETCH);
        root.add(header(), body());
        root.children().get(1).layout().grow(1);
        sceneLayer.add(root);
        sceneLayer.position(root).left(24).right(24).top(24).bottom(24);

        floatingPanel = new Panel()
                .background(new UIMaterial()
                        .gradient(Colors.hex("#183b5fee"), Colors.hex("#12142cee"))
                        .border(1.5f, Colors.hex("#78ecffbb"))
                        .radius(13)
                        .glow(CYAN, 18, 0.8f)
                        .sheen(Colors.hex("#ffffffaa"), 0.16f, 0.17f, 0.7f));
        floatingPanel.layout().padding(11, 15);
        floatingText = new Text("").fontScale(0.72f).color(TEXT).lineHeight(1.15f).shadow(1);
        floatingPanel.add(floatingText);
        sceneLayer.add(floatingPanel);
        sceneLayer.position(floatingPanel).right(42).bottom(42).width(150);
        canvas.add(sceneLayer);
    }

    private Panel header() {
        Panel shell = glassPanel(14);
        shell.layout().padding(16, 18);

        Flex row = new Flex(FlexDirection.ROW).align(AlignItems.CENTER).justify(JustifyContent.SPACE_BETWEEN);
        Flex copy = new Flex(FlexDirection.COLUMN).gap(3);
        copy.layout().shrink(1);
        Text title = new Text("CLOUD UI LAB").fontScale(1.42f).color(TEXT).shadow(2, Colors.hex("#21c8ff99"));
        status = new Text("").fontScale(0.72f).color(Colors.hex("#8bdff1"));
        copy.add(title, status);

        Flex actions = new Flex(FlexDirection.ROW).gap(8).align(AlignItems.CENTER);
        actions.add(
                accentButton("Pulse", CYAN, () -> clicks++),
                accentButton("Reset", VIOLET, () -> clicks = 0));
        row.add(copy, actions);
        shell.add(row);
        return shell;
    }

    private Flex body() {
        Flex body = new Flex(FlexDirection.ROW).gap(14).align(AlignItems.STRETCH);
        Flex left = new Flex(FlexDirection.COLUMN).gap(12).align(AlignItems.STRETCH);
        left.layout().grow(1).shrink(1);
        left.add(introPanel(), componentGrid());
        left.children().get(1).layout().grow(1);

        Flex right = new Flex(FlexDirection.COLUMN).gap(12).align(AlignItems.STRETCH);
        right.layout().width(360);
        right.add(interactionPanel(), layoutPanel(), materialPanel());
        body.add(left, right);
        return body;
    }

    private Panel introPanel() {
        Panel panel = glassPanel(10);
        panel.layout().padding(14, 16);
        Flex content = new Flex(FlexDirection.COLUMN).gap(5);
        content.add(
                new Text("COMPOSITION FIRST").fontScale(0.78f).color(CYAN).shadow(1),
                new Text("Buttons, sliders and text boxes are assembled from Panel and Text primitives.\n"
                        + "Layout, rendering and input remain independent.")
                        .fontScale(0.72f).lineHeight(1.3f).color(Colors.hex("#b9cedb")));
        panel.add(content);
        return panel;
    }

    private Grid componentGrid() {
        componentGrid = new Grid(3).gap(10).rowHeight(132).align(AlignItems.STRETCH);
        componentGrid.add(
                card("FLEX", "Row + column\nGrow and alignment", CYAN),
                card("GRID", "Equal tracks\nResponsive cells", VIOLET),
                card("ABSOLUTE", "Anchors + overlay\nStable positioning", Colors.hex("#ffbf69")),
                card("TEXT", "Wrapping + metrics\nAlignment + shadow", Colors.hex("#70e6a1")),
                card("CONTROLS", "Focus + capture\nKeyboard input", Colors.hex("#ff7bbd")),
                card("MATERIAL", "Radius + glow\nAnimated shader", Colors.hex("#78a8ff")));
        return componentGrid;
    }

    private Panel interactionPanel() {
        Panel panel = glassPanel(10);
        panel.layout().padding(14);
        Flex content = new Flex(FlexDirection.COLUMN).gap(9).align(AlignItems.STRETCH);
        Text title = sectionTitle("INTERACTION");
        counter = new Text("").fontScale(0.72f).color(Colors.hex("#bfefff"));

        Flex buttons = new Flex(FlexDirection.ROW).gap(8).align(AlignItems.STRETCH);
        Button countButton = accentButton("+ Count", CYAN, () -> clicks++);
        countButton.layout().grow(1);
        Button resetButton = accentButton("Reset", VIOLET, () -> clicks = 0);
        resetButton.layout().grow(1);
        buttons.add(countButton, resetButton);

        TextBox input = new TextBox("Pilot callsign").maxLength(24)
                .onChange(text -> inputPreview.text(text.isBlank() ? "Waiting for input..." : "Hello, " + text));
        inputPreview = new Text("Waiting for input...").fontScale(0.66f).color(MUTED);
        content.add(title, counter, buttons, input, inputPreview);
        panel.add(content);
        return panel;
    }

    private Panel layoutPanel() {
        Panel panel = glassPanel(10);
        panel.layout().padding(14);
        Flex content = new Flex(FlexDirection.COLUMN).gap(8).align(AlignItems.STRETCH);
        Text value = new Text("Grid gap  10px").fontScale(0.7f).color(Colors.hex("#c9c2ff"));
        Slider slider = new Slider().range(4, 24).step(1).value(10).onChange(next -> {
            componentGrid.gap(next);
            value.text("Grid gap  " + Math.round(next) + "px");
        });
        content.add(sectionTitle("LIVE LAYOUT"), value, slider);
        panel.add(content);
        return panel;
    }

    private Panel materialPanel() {
        Panel panel = new Panel().background(new UIMaterial()
                .gradient(Colors.hex("#2a174ddd"), Colors.hex("#101c39ee"))
                .border(1, Colors.hex("#c19cff99"))
                .radius(12)
                .glow(VIOLET, 15, 0.45f)
                .pulse(0.32f, 0.06f));
        panel.layout().padding(14);
        panel.add(new Text("GPU MATERIAL\nRounded SDF / border / glow / pulse / sheen")
                .fontScale(0.7f).lineHeight(1.28f).color(Colors.hex("#e6dcff")).shadow(1));
        return panel;
    }

    private Panel card(String title, String description, Color4f accent) {
        Panel card = new Panel().background(new UIMaterial()
                .gradient(Colors.hex("#10192aee"), Colors.hex("#090e19f5"))
                .border(1, new Color4f(accent.r, accent.g, accent.b, 0.48f))
                .radius(10)
                .glow(accent, 8, 0.18f));
        card.layout().padding(12);
        Flex content = new Flex(FlexDirection.COLUMN).gap(6);
        content.add(
                new Text(title).fontScale(0.82f).color(accent).shadow(1),
                new Text(description).fontScale(0.68f).lineHeight(1.25f).color(Colors.hex("#b8c9d8")));
        card.add(content);
        return card;
    }

    private Panel glassPanel(float radius) {
        return new Panel().background(new UIMaterial()
                .gradient(Colors.hex("#111c30e8"), Colors.hex("#080e1bed"))
                .border(1, Colors.hex("#7bc8e93d"))
                .radius(radius));
    }

    private Text sectionTitle(String text) {
        return new Text(text).fontScale(0.72f).color(VIOLET).shadow(1);
    }

    private Button accentButton(String label, Color4f accent, Runnable action) {
        UIMaterial normal = buttonMaterial(accent, 0.18f, 0.42f);
        UIMaterial hovered = buttonMaterial(accent, 0.28f, 0.8f)
                .sheen(Colors.hex("#ffffffbb"), 0.18f, 0.4f, 0.7f);
        UIMaterial pressed = buttonMaterial(accent, 0.12f, 0.9f);
        ButtonSkin skin = new ButtonSkin(normal, hovered, pressed, hovered,
                new UIMaterial().fill(Colors.hex("#1a1e27aa")).radius(8),
                TEXT, TEXT, TEXT, MUTED);
        return new Button(label).skin(skin).fontScale(0.72f).onClick(action);
    }

    private UIMaterial buttonMaterial(Color4f accent, float fillAlpha, float borderAlpha) {
        return new UIMaterial()
                .gradient(new Color4f(accent.r, accent.g, accent.b, fillAlpha + 0.08f),
                        new Color4f(accent.r * 0.24f, accent.g * 0.24f, accent.b * 0.24f, 0.96f))
                .border(1, new Color4f(accent.r, accent.g, accent.b, borderAlpha))
                .radius(8)
                .glow(accent, 9, fillAlpha);
    }

    private void drawWorld() {
        int width = Window.get().getWidth();
        int height = Window.get().getHeight();
        for (int x = 0; x < width; x += 44) {
            Render.drawRect(x, 0, 1, height, false, GRID);
        }
        for (int y = 0; y < height; y += 44) {
            Render.drawRect(0, y, width, 1, false, GRID);
        }
        float orbit = Time.timePassed * 0.55f;
        for (int i = 0; i < 5; i++) {
            float angle = orbit + i * 1.256f;
            float x = width * 0.5f + (float) Math.cos(angle) * 360;
            float y = height * 0.5f + (float) Math.sin(angle * 1.2f) * 210;
            Render.drawRect(x, y, 90, 90, true, new Color4f(0.16f, 0.64f, 0.95f, 0.035f));
        }
    }

    private int countElements(UIElement element) {
        int count = 1;
        for (UIElement child : element.children()) {
            count += countElements(child);
        }
        return count;
    }
}
