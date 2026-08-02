package de.kostari.cloud.core.ui;

import java.util.IdentityHashMap;
import java.util.Map;

public class Absolute extends Panel {

    private final Map<UIElement, AbsolutePosition> positions = new IdentityHashMap<>();

    @Override
    public Absolute add(UIElement... elements) {
        super.add(elements);
        if (elements != null) {
            for (UIElement element : elements) {
                if (element != null) {
                    positions.computeIfAbsent(element, ignored -> new AbsolutePosition(this::invalidateLayout));
                }
            }
        }
        return this;
    }

    public Absolute add(UIElement element, AbsolutePosition position) {
        add(element);
        if (element != null && position != null) {
            positions.put(element, position);
            invalidateLayout();
        }
        return this;
    }

    public AbsolutePosition position(UIElement element) {
        if (element == null) {
            throw new IllegalArgumentException("Element cannot be null");
        }
        if (!children().contains(element)) {
            add(element);
        }
        return positions.computeIfAbsent(element, ignored -> new AbsolutePosition(this::invalidateLayout));
    }

    @Override
    public Absolute remove(UIElement element) {
        super.remove(element);
        positions.remove(element);
        return this;
    }

    @Override
    public Absolute clear() {
        super.clear();
        positions.clear();
        return this;
    }

    @Override
    protected UISize measureContent(UIConstraints constraints) {
        float width = 0;
        float height = 0;
        for (UIElement child : children()) {
            if (!child.isVisible()) {
                continue;
            }
            AbsolutePosition position = positions.getOrDefault(child, new AbsolutePosition(this::invalidateLayout));
            Insets margin = child.layout().margin();
            float offeredWidth = position.hasWidth() ? position.width() : constraints.maxWidth();
            float offeredHeight = position.hasHeight() ? position.height() : constraints.maxHeight();
            if (position.hasLeft() && position.hasRight() && Float.isFinite(constraints.maxWidth())) {
                offeredWidth = Math.max(0, constraints.maxWidth() - position.left() - position.right());
            }
            if (position.hasTop() && position.hasBottom() && Float.isFinite(constraints.maxHeight())) {
                offeredHeight = Math.max(0, constraints.maxHeight() - position.top() - position.bottom());
            }
            UISize desired = child.measure(UIConstraints.loose(
                    Math.max(0, offeredWidth - margin.horizontal()),
                    Math.max(0, offeredHeight - margin.vertical())));
            float horizontalOffset = position.hasLeft() ? position.left() : 0;
            float verticalOffset = position.hasTop() ? position.top() : 0;
            width = Math.max(width, horizontalOffset + desired.width() + margin.horizontal());
            height = Math.max(height, verticalOffset + desired.height() + margin.vertical());
        }
        return new UISize(width, height);
    }

    @Override
    protected void arrangeChildren(UIRect area) {
        for (UIElement child : children()) {
            if (!child.isVisible()) {
                continue;
            }
            AbsolutePosition position = positions.computeIfAbsent(child,
                    ignored -> new AbsolutePosition(this::invalidateLayout));
            Insets margin = child.layout().margin();
            float offeredWidth = Math.max(0, area.width - margin.horizontal());
            float offeredHeight = Math.max(0, area.height - margin.vertical());
            if (position.hasLeft() && position.hasRight()) {
                offeredWidth = Math.max(0, area.width - position.left() - position.right() - margin.horizontal());
            } else if (position.hasWidth()) {
                offeredWidth = position.width();
            }
            if (position.hasTop() && position.hasBottom()) {
                offeredHeight = Math.max(0, area.height - position.top() - position.bottom() - margin.vertical());
            } else if (position.hasHeight()) {
                offeredHeight = position.height();
            }

            UISize desired = child.measure(UIConstraints.loose(offeredWidth, offeredHeight));
            float width = position.hasWidth() || position.hasLeft() && position.hasRight()
                    ? offeredWidth
                    : desired.width();
            float height = position.hasHeight() || position.hasTop() && position.hasBottom()
                    ? offeredHeight
                    : desired.height();
            float x = position.hasLeft()
                    ? area.x + position.left()
                    : position.hasRight()
                            ? area.right() - position.right() - width
                            : area.x + area.width * position.anchorX() - width * position.anchorX();
            float y = position.hasTop()
                    ? area.y + position.top()
                    : position.hasBottom()
                            ? area.bottom() - position.bottom() - height
                            : area.y + area.height * position.anchorY() - height * position.anchorY();
            child.arrange(new UIRect(x + margin.left(), y + margin.top(), width, height));
        }
    }
}
