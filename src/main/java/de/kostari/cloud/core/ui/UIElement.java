package de.kostari.cloud.core.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class UIElement {

    private final Layout layout = new Layout(this);
    private final List<UIElement> children = new ArrayList<>();
    private UIElement parent;
    private boolean visible = true;
    private boolean pointerEvents;
    private boolean clipChildren;
    private boolean layoutDirty = true;
    private boolean paintDirty = true;
    private float translateX;
    private float translateY;
    private float opacity = 1;

    protected final UIRect bounds = new UIRect();
    protected final UIRect contentBounds = new UIRect();
    private final UIRect renderBounds = new UIRect();
    private final UIRect renderContentBounds = new UIRect();
    private UISize measuredSize = UISize.ZERO;
    private float inheritedOpacity = 1;

    public Layout layout() {
        return layout;
    }

    public UIElement add(UIElement... elements) {
        if (elements == null) {
            return this;
        }

        for (UIElement element : elements) {
            if (element == null || element == this) {
                continue;
            }
            if (element.parent != null) {
                element.parent.remove(element);
            }
            element.parent = this;
            children.add(element);
        }
        invalidateLayout();
        return this;
    }

    public UIElement append(UIElement... elements) {
        return add(elements);
    }

    public UIElement remove(UIElement element) {
        if (element != null && children.remove(element)) {
            element.parent = null;
            invalidateLayout();
        }
        return this;
    }

    public UIElement clear() {
        for (UIElement child : children) {
            child.parent = null;
        }
        children.clear();
        invalidateLayout();
        return this;
    }

    public UIElement visible(boolean value) {
        if (visible != value) {
            visible = value;
            invalidateLayout();
        }
        return this;
    }

    public boolean isVisible() {
        return visible;
    }

    public UIElement pointerEvents(boolean value) {
        pointerEvents = value;
        return this;
    }

    public boolean acceptsPointerEvents() {
        return pointerEvents;
    }

    public UIElement clipChildren(boolean value) {
        clipChildren = value;
        invalidatePaint();
        return this;
    }

    public boolean clipsChildren() {
        return clipChildren;
    }

    public UIElement translate(float x, float y) {
        translateX = x;
        translateY = y;
        invalidatePaint();
        return this;
    }

    public UIElement opacity(float value) {
        opacity = Math.clamp(value, 0, 1);
        invalidatePaint();
        return this;
    }

    public float opacity() {
        return opacity;
    }

    public UIElement parent() {
        return parent;
    }

    public List<UIElement> children() {
        return Collections.unmodifiableList(children);
    }

    public UIRect bounds() {
        return bounds.copy();
    }

    public UIRect contentBounds() {
        return contentBounds.copy();
    }

    public UISize measuredSize() {
        return measuredSize;
    }

    public final UISize measure(UIConstraints constraints) {
        UIConstraints safe = constraints == null ? UIConstraints.unconstrained() : constraints;
        Insets padding = layout.padding();

        float maximumWidth = Math.min(safe.maxWidth(), layout.maxWidth());
        float maximumHeight = Math.min(safe.maxHeight(), layout.maxHeight());
        if (layout.hasWidth()) {
            maximumWidth = Math.min(maximumWidth, layout.width());
        }
        if (layout.hasHeight()) {
            maximumHeight = Math.min(maximumHeight, layout.height());
        }

        UIConstraints contentConstraints = new UIConstraints(
                0,
                subtractFinite(maximumWidth, padding.horizontal()),
                0,
                subtractFinite(maximumHeight, padding.vertical()));
        UISize content = measureContent(contentConstraints);

        float width = layout.hasWidth() ? layout.width() : content.width() + padding.horizontal();
        float height = layout.hasHeight() ? layout.height() : content.height() + padding.vertical();
        width = safe.constrainWidth(layout.clampWidth(width));
        height = safe.constrainHeight(layout.clampHeight(height));
        measuredSize = new UISize(width, height);
        return measuredSize;
    }

    public final void arrange(UIRect rectangle) {
        if (rectangle == null) {
            return;
        }
        bounds.set(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
        updateContentBounds();
        renderBounds.set(bounds.x + translateX, bounds.y + translateY, bounds.width, bounds.height);
        renderContentBounds.set(contentBounds.x + translateX, contentBounds.y + translateY,
                contentBounds.width, contentBounds.height);
        arrangeChildren(contentBounds);
        layoutDirty = false;
    }

    public final void layout(float x, float y, float width, float height) {
        measure(UIConstraints.tight(width, height));
        arrange(new UIRect(x, y, width, height));
    }

    public float preferredWidth() {
        return measure(UIConstraints.unconstrained()).width();
    }

    public float preferredHeight() {
        return measure(UIConstraints.unconstrained()).height();
    }

    public float preferredHeight(float availableWidth) {
        return measure(UIConstraints.loose(availableWidth, UIConstraints.INFINITY)).height();
    }

    public void invalidateLayout() {
        if (!layoutDirty) {
            layoutDirty = true;
            if (parent != null) {
                parent.invalidateLayout();
            }
        } else if (parent != null && !parent.layoutDirty) {
            parent.invalidateLayout();
        }
        paintDirty = true;
    }

    public void invalidatePaint() {
        paintDirty = true;
    }

    public boolean isLayoutDirty() {
        return layoutDirty;
    }

    final void drawTree(float parentTranslateX, float parentTranslateY, float parentOpacity) {
        if (!visible) {
            return;
        }

        float totalTranslateX = parentTranslateX + translateX;
        float totalTranslateY = parentTranslateY + translateY;
        inheritedOpacity = parentOpacity * opacity;
        renderBounds.set(bounds.x + totalTranslateX, bounds.y + totalTranslateY, bounds.width, bounds.height);
        renderContentBounds.set(contentBounds.x + totalTranslateX, contentBounds.y + totalTranslateY,
                contentBounds.width, contentBounds.height);

        drawSelf();
        if (clipChildren) {
            UI.pushClip(renderContentBounds);
        }
        for (UIElement child : children) {
            child.drawTree(totalTranslateX, totalTranslateY, inheritedOpacity);
        }
        if (clipChildren) {
            UI.popClip();
        }
        drawOverlay();
        paintDirty = false;
    }

    final UIElement hitTest(float x, float y) {
        if (!visible || inheritedOpacity <= 0 || !renderBounds.contains(x, y)) {
            return null;
        }
        for (int i = children.size() - 1; i >= 0; i--) {
            UIElement hit = children.get(i).hitTest(x, y);
            if (hit != null) {
                return hit;
            }
        }
        return pointerEvents ? this : null;
    }

    final boolean containsRenderPoint(float x, float y) {
        return visible && renderBounds.contains(x, y);
    }

    protected UISize measureContent(UIConstraints constraints) {
        float width = 0;
        float height = 0;
        for (UIElement child : children) {
            if (!child.isVisible()) {
                continue;
            }
            Insets margin = child.layout.margin();
            UIConstraints childConstraints = constraints.inset(margin);
            UISize childSize = child.measure(childConstraints);
            width = Math.max(width, childSize.width() + margin.horizontal());
            height = Math.max(height, childSize.height() + margin.vertical());
        }
        return new UISize(width, height);
    }

    protected void arrangeChildren(UIRect area) {
        for (UIElement child : children) {
            if (!child.isVisible()) {
                continue;
            }
            Insets margin = child.layout.margin();
            float width = Math.max(0, area.width - margin.horizontal());
            float height = Math.max(0, area.height - margin.vertical());
            child.arrange(new UIRect(area.x + margin.left(), area.y + margin.top(), width, height));
        }
    }

    protected void drawSelf() {
    }

    protected void drawOverlay() {
    }

    protected UIRect renderBounds() {
        return renderBounds;
    }

    protected UIRect renderContentBounds() {
        return renderContentBounds;
    }

    protected float renderOpacity() {
        return inheritedOpacity;
    }

    protected boolean isFocusable() {
        return false;
    }

    protected void onPointerEnter() {
    }

    protected void onPointerExit() {
    }

    protected void onPointerDown(float x, float y) {
    }

    protected void onPointerDrag(float x, float y) {
    }

    protected void onPointerUp(float x, float y, boolean inside) {
    }

    protected void onFocusChanged(boolean focused) {
    }

    protected void onKeyPressed(int key) {
    }

    protected void onTextInput(int codepoint) {
    }

    private void updateContentBounds() {
        Insets padding = layout.padding();
        contentBounds.set(
                bounds.x + padding.left(),
                bounds.y + padding.top(),
                Math.max(0, bounds.width - padding.horizontal()),
                Math.max(0, bounds.height - padding.vertical()));
    }

    private static float subtractFinite(float value, float amount) {
        return Float.isFinite(value) ? Math.max(0, value - amount) : value;
    }
}
