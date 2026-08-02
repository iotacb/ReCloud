package de.kostari.cloud.core.ui;

import java.util.IdentityHashMap;
import java.util.Map;

import de.kostari.cloud.core.scene.SceneManager;
import de.kostari.cloud.core.window.Window;

public class Canvas extends Panel {

    public static final float AUTO = Layout.AUTO;
    public static final float FILL = -2f;

    private final Map<UIElement, CanvasSlot> slots = new IdentityHashMap<>();
    private float x;
    private float y;
    private float width = FILL;
    private float height = FILL;
    private float lastWidth = -1;
    private float lastHeight = -1;
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
        invalidateLayout();
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

    @Override
    public Canvas append(UIElement... elements) {
        return add(elements);
    }

    public Canvas append(UIElement element, float x, float y, float width, float height) {
        add(element);
        return setBounds(element, x, y, width, height);
    }

    public Canvas setBounds(UIElement element, float x, float y, float width, float height) {
        if (element != null) {
            slots.put(element, new CanvasSlot(x, y, width, height));
            invalidateLayout();
        }
        return this;
    }

    @Override
    public Canvas remove(UIElement element) {
        super.remove(element);
        slots.remove(element);
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
        float resolvedHeight = resolve(height, Window.get().getHeight() - y, preferredHeight(resolvedWidth));
        if (isLayoutDirty() || resolvedWidth != lastWidth || resolvedHeight != lastHeight) {
            measure(UIConstraints.tight(resolvedWidth, resolvedHeight));
            arrange(new UIRect(x, y, resolvedWidth, resolvedHeight));
            lastWidth = resolvedWidth;
            lastHeight = resolvedHeight;
        }
        drawTree(0, 0, 1);
    }

    @Override
    protected void arrangeChildren(UIRect area) {
        for (UIElement child : children()) {
            if (!child.isVisible()) {
                continue;
            }
            CanvasSlot slot = slots.getOrDefault(child, new CanvasSlot(0, 0, FILL, FILL));
            Insets margin = child.layout().margin();
            float availableWidth = Math.max(0, area.width - slot.x - margin.horizontal());
            float availableHeight = Math.max(0, area.height - slot.y - margin.vertical());
            float childWidth = slot.width == FILL ? availableWidth
                    : slot.width == AUTO ? child.measure(UIConstraints.loose(availableWidth, availableHeight)).width()
                            : Math.max(0, slot.width - margin.horizontal());
            UISize desired = child.measure(UIConstraints.loose(childWidth, availableHeight));
            float childHeight = slot.height == FILL ? availableHeight
                    : slot.height == AUTO ? desired.height()
                            : Math.max(0, slot.height - margin.vertical());
            child.arrange(new UIRect(
                    area.x + slot.x + margin.left(),
                    area.y + slot.y + margin.top(),
                    childWidth,
                    childHeight));
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
