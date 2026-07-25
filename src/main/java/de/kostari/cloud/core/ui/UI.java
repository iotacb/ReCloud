package de.kostari.cloud.core.ui;

import java.util.ArrayList;
import java.util.List;

import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.render.font.Font;
import de.kostari.cloud.core.window.Window;

public final class UI {

    private static final List<Canvas> canvases = new ArrayList<>();
    private static final List<RenderCommand> renderCommands = new ArrayList<>();
    private static Font defaultFont;

    private UI() {
    }

    public static void render(UIElement root) {
        render(root, 0, 0, Window.get().getWidth(), Window.get().getHeight());
    }

    public static void render(UIElement root, float x, float y, float width, float height) {
        if (root == null) {
            return;
        }
        renderCommands.add(new RenderCommand(root, x, y, width, height));
    }

    public static void beginFrame() {
        renderCommands.clear();
    }

    public static void flush() {
        if (canvases.isEmpty() && renderCommands.isEmpty()) {
            return;
        }

        List<Canvas> canvasSnapshot = new ArrayList<>(canvases);
        List<RenderCommand> commands = new ArrayList<>(renderCommands);
        renderCommands.clear();
        Render.screenSpace(() -> {
            for (Canvas canvas : canvasSnapshot) {
                canvas.renderCanvas();
            }
            for (RenderCommand command : commands) {
                command.root.layout(command.x, command.y, command.width, command.height);
                command.root.drawTree();
            }
        });
    }

    public static Font defaultFont() {
        if (defaultFont == null) {
            defaultFont = new Font("./arial.ttf", 38).load();
        }
        return defaultFont;
    }

    public static void setDefaultFont(Font font) {
        defaultFont = font;
    }

    static void registerCanvas(Canvas canvas) {
        if (canvas != null && !canvases.contains(canvas)) {
            canvases.add(canvas);
        }
    }

    static void unregisterCanvas(Canvas canvas) {
        canvases.remove(canvas);
    }

    private record RenderCommand(UIElement root, float x, float y, float width, float height) {
    }
}
