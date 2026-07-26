package de.kostari.cloud.core.ui;

import java.util.IdentityHashMap;
import java.util.Map;

import de.kostari.cloud.core.scene.SceneManager;
import de.kostari.cloud.core.window.Window;

public class Canvas extends Panel {

    public static final float AUTO = Style.AUTO;
    public static final float FILL = -2f;

    private final Map<UIElement, CanvasSlot> slots = new IdentityHashMap<>();

    private float x;
    private float y;
    private float width = FILL;
    private float height = FILL;
    private boolean disposed;

    public Canvas() {
        UI.registerCanvas(this);
        if (SceneManager.hasScene()) {
            SceneManager.current().registerCanvas(this);
        }
    }

    public Canvas(float x, float y, float width, float height) {
        this();
        bounds(x, y, width, height);
    }

    public Canvas bounds(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        return this;
    }

    @Override
    public Canvas add(UIElement... elements) {
        super.add(elements);
        if (elements != null) {
            for (UIElement element : elements) {
                if (element != null) {
                    slots.putIfAbsent(element, new CanvasSlot(0, 0, FILL, FILL));
                }
            }
        }
        return this;
    }

    public Canvas add(UIElement element, float x, float y, float width, float height) {
        add(element);
        return setBounds(element, x, y, width, height);
    }

    @Override
    public Canvas append(UIElement... elements) {
        return add(elements);
    }

    public Canvas append(UIElement element, float x, float y, float width, float height) {
        return add(element, x, y, width, height);
    }

    public Canvas setBounds(UIElement element, float x, float y, float width, float height) {
        if (element != null) {
            slots.put(element, new CanvasSlot(x, y, width, height));
        }
        return this;
    }

    public Canvas remove(UIElement element) {
        if (element != null) {
            super.remove(element);
            slots.remove(element);
        }
        return this;
    }

    public void dispose() {
        if (disposed) {
            return;
        }

        disposed = true;
        UI.unregisterCanvas(this);
        if (SceneManager.hasScene()) {
            SceneManager.current().unregisterCanvas(this);
        }
    }

    boolean isDisposed() {
        return disposed;
    }

    void renderCanvas() {
        if (disposed || !isVisible()) {
            return;
        }

        float resolvedWidth = resolve(width, Window.get().getWidth() - x, preferredWidth());
        float resolvedHeight = resolve(height, Window.get().getHeight() - y, preferredHeight());
        layout(x, y, resolvedWidth, resolvedHeight);
        drawTree();
    }

    @Override
    protected void layoutChildren(float x, float y, float width, float height) {
        for (UIElement child : children()) {
            if (!child.isVisible()) {
                continue;
            }

            CanvasSlot slot = slots.getOrDefault(child, new CanvasSlot(0, 0, FILL, FILL));
            Spacing margin = child.style().margin();
            float availableWidth = Math.max(0, width - slot.x);
            float availableHeight = Math.max(0, height - slot.y);
            float childWidth = resolve(slot.width, availableWidth, child.preferredWidth());
            float childHeight = resolve(slot.height, availableHeight, child.outerPreferredHeight(childWidth));

            child.layout(
                    x + slot.x + margin.left,
                    y + slot.y + margin.top,
                    Math.max(0, childWidth - margin.horizontal()),
                    Math.max(0, childHeight - margin.vertical()));
        }
    }

    private float resolve(float value, float fillValue, float autoValue) {
        if (value == FILL) {
            return Math.max(0, fillValue);
        }
        if (value == AUTO) {
            return Math.max(0, autoValue);
        }
        return Math.max(0, value);
    }

    private record CanvasSlot(float x, float y, float width, float height) {
    }
}
