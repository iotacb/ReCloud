package de.kostari.cloud.core.ui;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.render.font.Font;
import de.kostari.cloud.core.window.Input;
import de.kostari.cloud.core.window.Window;

public final class UI {

    private static final List<Canvas> canvases = new ArrayList<>();
    private static final List<RenderCommand> renderCommands = new ArrayList<>();
    private static final Deque<UIRect> clips = new ArrayDeque<>();
    private static Font defaultFont;
    private static UIElement hovered;
    private static UIElement active;
    private static UIElement focused;

    private UI() {
    }

    public static void render(UIElement root) {
        render(root, 0, 0, Window.get().getWidth(), Window.get().getHeight());
    }

    public static void render(UIElement root, float x, float y, float width, float height) {
        if (root != null) {
            renderCommands.add(new RenderCommand(root, x, y, width, height));
        }
    }

    public static void beginFrame() {
        renderCommands.clear();
        processInput();
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
                command.root.measure(UIConstraints.tight(command.width, command.height));
                command.root.arrange(new UIRect(command.x, command.y, command.width, command.height));
                command.root.drawTree(0, 0, 1);
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
        for (Canvas canvas : canvases) {
            canvas.invalidateLayout();
        }
    }

    public static UIElement focusedElement() {
        return focused;
    }

    public static void cleanup() {
        UIShapeRenderer.cleanup();
        canvases.clear();
        renderCommands.clear();
        clips.clear();
        hovered = null;
        active = null;
        focused = null;
    }

    public static void focus(UIElement element) {
        setFocus(element != null && element.isFocusable() ? element : null);
    }

    static void registerCanvas(Canvas canvas) {
        if (canvas != null && !canvases.contains(canvas)) {
            canvases.add(canvas);
        }
    }

    static void unregisterCanvas(Canvas canvas) {
        canvases.remove(canvas);
        if (isDescendantOf(hovered, canvas)) {
            hovered = null;
        }
        if (isDescendantOf(active, canvas)) {
            active = null;
        }
        if (isDescendantOf(focused, canvas)) {
            setFocus(null);
        }
    }

    static void pushClip(UIRect requested) {
        UIRect clip = clips.isEmpty() ? requested.copy() : clips.peek().intersection(requested);
        clips.push(clip);
        Render.flush();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        applyClip(clip);
    }

    static void popClip() {
        if (clips.isEmpty()) {
            return;
        }
        Render.flush();
        clips.pop();
        if (clips.isEmpty()) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        } else {
            applyClip(clips.peek());
        }
    }

    private static void processInput() {
        float mouseX = Input.getMouseX();
        float mouseY = Input.getMouseY();
        UIElement hit = topmostHit(mouseX, mouseY);
        if (hit != hovered) {
            if (hovered != null) {
                hovered.onPointerExit();
            }
            hovered = hit;
            if (hovered != null) {
                hovered.onPointerEnter();
            }
        }

        if (Input.mouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
            active = hit;
            if (active != null) {
                active.onPointerDown(mouseX, mouseY);
                setFocus(active.isFocusable() ? active : null);
            } else {
                setFocus(null);
            }
        }
        if (active != null && Input.mouseButtonDown(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
            active.onPointerDrag(mouseX, mouseY);
        }
        if (active != null && Input.mouseButtonReleased(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
            UIElement released = active;
            active = null;
            released.onPointerUp(mouseX, mouseY, released.containsRenderPoint(mouseX, mouseY));
        }

        if (Input.keyPressed(GLFW.GLFW_KEY_TAB)) {
            focusNext();
        }
        if (focused != null) {
            for (int key = 32; key <= GLFW.GLFW_KEY_LAST; key++) {
                if (key != GLFW.GLFW_KEY_TAB && Input.keyPressed(key)) {
                    focused.onKeyPressed(key);
                }
            }
            for (int codepoint : Input.typedCodepoints()) {
                focused.onTextInput(codepoint);
            }
        }
    }

    private static UIElement topmostHit(float x, float y) {
        for (int i = canvases.size() - 1; i >= 0; i--) {
            Canvas canvas = canvases.get(i);
            if (!canvas.isDisposed()) {
                UIElement hit = canvas.hitTest(x, y);
                if (hit != null) {
                    return hit;
                }
            }
        }
        return null;
    }

    private static void setFocus(UIElement element) {
        if (focused == element) {
            return;
        }
        if (focused != null) {
            focused.onFocusChanged(false);
        }
        focused = element;
        if (focused != null) {
            focused.onFocusChanged(true);
        }
    }

    private static void focusNext() {
        List<UIElement> focusable = new ArrayList<>();
        for (Canvas canvas : canvases) {
            collectFocusable(canvas, focusable);
        }
        if (focusable.isEmpty()) {
            setFocus(null);
            return;
        }
        int index = focusable.indexOf(focused);
        setFocus(focusable.get((index + 1) % focusable.size()));
    }

    private static void collectFocusable(UIElement element, List<UIElement> output) {
        if (!element.isVisible()) {
            return;
        }
        if (element.isFocusable()) {
            output.add(element);
        }
        for (UIElement child : element.children()) {
            collectFocusable(child, output);
        }
    }

    private static boolean isDescendantOf(UIElement element, UIElement ancestor) {
        for (UIElement current = element; current != null; current = current.parent()) {
            if (current == ancestor) {
                return true;
            }
        }
        return false;
    }

    private static void applyClip(UIRect clip) {
        int x = Math.max(0, Math.round(clip.x));
        int y = Math.max(0, Math.round(Window.get().getHeight() - clip.bottom()));
        int width = Math.max(0, Math.round(clip.width));
        int height = Math.max(0, Math.round(clip.height));
        GL11.glScissor(x, y, width, height);
    }

    private record RenderCommand(UIElement root, float x, float y, float width, float height) {
    }
}
