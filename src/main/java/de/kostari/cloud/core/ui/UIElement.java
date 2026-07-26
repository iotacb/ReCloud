package de.kostari.cloud.core.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.types.Color4f;

public abstract class UIElement {

    private final Style style = new Style();
    private final List<UIElement> children = new ArrayList<>();
    private UIElement parent;
    private boolean visible = true;

    protected final UIRect bounds = new UIRect();
    protected final UIRect contentBounds = new UIRect();

    public Style style() {
        return style;
    }

    public UIElement add(UIElement... elements) {
        if (elements == null) {
            return this;
        }

        for (UIElement element : elements) {
            if (element == null) {
                continue;
            }
            element.parent = this;
            children.add(element);
        }
        return this;
    }

    public UIElement append(UIElement... elements) {
        return add(elements);
    }

    public UIElement remove(UIElement element) {
        if (element != null && children.remove(element)) {
            element.parent = null;
        }
        return this;
    }

    public UIElement clear() {
        for (UIElement child : children) {
            child.parent = null;
        }
        children.clear();
        return this;
    }

    public UIElement visible(boolean visible) {
        this.visible = visible;
        return this;
    }

    public boolean isVisible() {
        return visible;
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

    public void layout(float x, float y, float availableWidth, float availableHeight) {
        float width = style.hasWidth() ? style.width() : availableWidth;
        float height = style.hasHeight() ? style.height() : availableHeight;

        width = style.clampWidth(width);
        height = style.clampHeight(height);

        bounds.set(x, y, width, height);
        updateContentBounds();
        layoutChildren(contentBounds.x, contentBounds.y, contentBounds.width, contentBounds.height);
    }

    public float preferredWidth() {
        if (style.hasWidth()) {
            return style.width();
        }

        float width = preferredInnerWidth() + style.horizontalInsets();
        return style.clampWidth(width);
    }

    public float preferredHeight() {
        if (style.hasHeight()) {
            return style.height();
        }

        float height = preferredInnerHeight() + style.verticalInsets();
        return style.clampHeight(height);
    }

    public float preferredHeight(float availableWidth) {
        if (style.hasHeight()) {
            return style.height();
        }

        float width = style.hasWidth() ? style.width() : Math.max(0, availableWidth);
        width = style.clampWidth(width);
        float innerWidth = Math.max(0, width - style.horizontalInsets());
        float height = preferredInnerHeight(innerWidth) + style.verticalInsets();
        return style.clampHeight(height);
    }

    final float outerPreferredWidth() {
        return preferredWidth() + style.margin().horizontal();
    }

    final float outerPreferredHeight() {
        return preferredHeight() + style.margin().vertical();
    }

    final float outerPreferredHeight(float availableWidth) {
        float innerAvailableWidth = Math.max(0, availableWidth - style.margin().horizontal());
        return preferredHeight(innerAvailableWidth) + style.margin().vertical();
    }

    final void drawTree() {
        if (!visible) {
            return;
        }

        drawSelf();
        for (UIElement child : children) {
            child.drawTree();
        }
    }

    protected void drawSelf() {
    }

    protected void layoutChildren(float x, float y, float width, float height) {
        for (UIElement child : children) {
            if (!child.isVisible()) {
                continue;
            }
            Spacing margin = child.style().margin();
            child.layout(
                    x + margin.left,
                    y + margin.top,
                    Math.max(0, width - margin.horizontal()),
                    Math.max(0, height - margin.vertical()));
        }
    }

    protected float preferredInnerWidth() {
        float width = 0;
        for (UIElement child : children) {
            if (child.isVisible()) {
                width = Math.max(width, child.outerPreferredWidth());
            }
        }
        return width;
    }

    protected float preferredInnerHeight() {
        float height = 0;
        for (UIElement child : children) {
            if (child.isVisible()) {
                height = Math.max(height, child.outerPreferredHeight());
            }
        }
        return height;
    }

    protected float preferredInnerHeight(float availableWidth) {
        float height = 0;
        for (UIElement child : children) {
            if (child.isVisible()) {
                height = Math.max(height, child.outerPreferredHeight(availableWidth));
            }
        }
        return height;
    }

    protected void paintBox() {
        paintBox(style.backgroundColor());
    }

    protected void paintBox(Color4f backgroundColor) {
        if (backgroundColor != null && backgroundColor.a > 0) {
            Render.drawRect(bounds.x, bounds.y, bounds.width, bounds.height, false, backgroundColor);
        }

        Color4f borderColor = style.borderColor();
        float borderWidth = style.borderWidth();
        if (borderColor == null || borderWidth <= 0 || borderColor.a <= 0) {
            return;
        }

        Render.drawRect(bounds.x, bounds.y, bounds.width, borderWidth, false, borderColor);
        Render.drawRect(bounds.x, bounds.bottom() - borderWidth, bounds.width, borderWidth, false, borderColor);
        Render.drawRect(bounds.x, bounds.y, borderWidth, bounds.height, false, borderColor);
        Render.drawRect(bounds.right() - borderWidth, bounds.y, borderWidth, bounds.height, false, borderColor);
    }

    protected void updateContentBounds() {
        float borderWidth = style.borderWidth();
        Spacing padding = style.padding();
        contentBounds.set(
                bounds.x + borderWidth + padding.left,
                bounds.y + borderWidth + padding.top,
                Math.max(0, bounds.width - style.horizontalInsets()),
                Math.max(0, bounds.height - style.verticalInsets()));
    }
}
