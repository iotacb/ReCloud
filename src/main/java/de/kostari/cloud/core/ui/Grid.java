package de.kostari.cloud.core.ui;

import java.util.ArrayList;
import java.util.List;

public class Grid extends Panel {

    public Grid() {
        this(2);
    }

    public Grid(int columns) {
        style().columns(columns).alignItems(AlignItems.STRETCH);
    }

    public Grid columns(int columns) {
        style().columns(columns);
        return this;
    }

    @Override
    protected void layoutChildren(float x, float y, float width, float height) {
        List<UIElement> visibleChildren = visibleChildren();
        int count = visibleChildren.size();
        if (count == 0) {
            return;
        }

        int columns = style().columns();
        float columnGap = style().columnGap();
        float rowGap = style().rowGap();
        float cellWidth = Math.max(0, (width - columnGap * Math.max(0, columns - 1)) / columns);
        float cursorY = y;

        for (int rowStart = 0; rowStart < count; rowStart += columns) {
            int rowEnd = Math.min(rowStart + columns, count);
            float rowHeight = rowHeight(visibleChildren, rowStart, rowEnd, cellWidth);
            float cursorX = x;

            for (int i = rowStart; i < rowEnd; i++) {
                UIElement child = visibleChildren.get(i);
                Spacing margin = child.style().margin();
                float availableWidth = Math.max(0, cellWidth - margin.horizontal());
                float availableHeight = Math.max(0, rowHeight - margin.vertical());
                float childWidth = childSize(child, true, availableWidth);
                float childHeight = childHeight(child, availableWidth, availableHeight);
                float childX = cursorX + margin.left + alignOffset(availableWidth, childWidth);
                float childY = cursorY + margin.top + alignOffset(availableHeight, childHeight);

                child.layout(childX, childY, childWidth, childHeight);
                cursorX += cellWidth + columnGap;
            }

            cursorY += rowHeight + rowGap;
        }
    }

    @Override
    protected float preferredInnerWidth() {
        List<UIElement> visibleChildren = visibleChildren();
        int columns = style().columns();
        float maxCellWidth = 0;
        for (UIElement child : visibleChildren) {
            maxCellWidth = Math.max(maxCellWidth, child.outerPreferredWidth());
        }
        return maxCellWidth * columns + style().columnGap() * Math.max(0, columns - 1);
    }

    @Override
    protected float preferredInnerHeight() {
        List<UIElement> visibleChildren = visibleChildren();
        if (visibleChildren.isEmpty()) {
            return 0;
        }

        int columns = style().columns();
        int rows = (int) Math.ceil(visibleChildren.size() / (float) columns);
        if (style().hasRowHeight()) {
            return style().rowHeight() * rows + style().rowGap() * Math.max(0, rows - 1);
        }

        float height = 0;
        for (int rowStart = 0; rowStart < visibleChildren.size(); rowStart += columns) {
            int rowEnd = Math.min(rowStart + columns, visibleChildren.size());
            height += rowHeight(visibleChildren, rowStart, rowEnd);
        }
        height += style().rowGap() * Math.max(0, rows - 1);
        return height;
    }

    @Override
    protected float preferredInnerHeight(float availableWidth) {
        List<UIElement> visibleChildren = visibleChildren();
        if (visibleChildren.isEmpty()) {
            return 0;
        }

        int columns = style().columns();
        int rows = (int) Math.ceil(visibleChildren.size() / (float) columns);
        if (style().hasRowHeight()) {
            return style().rowHeight() * rows + style().rowGap() * Math.max(0, rows - 1);
        }

        float gapWidth = style().columnGap() * Math.max(0, columns - 1);
        float cellWidth = Math.max(0, (availableWidth - gapWidth) / columns);
        float height = 0;
        for (int rowStart = 0; rowStart < visibleChildren.size(); rowStart += columns) {
            int rowEnd = Math.min(rowStart + columns, visibleChildren.size());
            float rowHeight = 0;
            for (int i = rowStart; i < rowEnd; i++) {
                rowHeight = Math.max(rowHeight, visibleChildren.get(i).outerPreferredHeight(cellWidth));
            }
            height += rowHeight;
        }
        height += style().rowGap() * Math.max(0, rows - 1);
        return height;
    }

    private List<UIElement> visibleChildren() {
        List<UIElement> visibleChildren = new ArrayList<>();
        for (UIElement child : children()) {
            if (child.isVisible()) {
                visibleChildren.add(child);
            }
        }
        return visibleChildren;
    }

    private float rowHeight(List<UIElement> children, int start, int end) {
        if (style().hasRowHeight()) {
            return style().rowHeight();
        }

        float rowHeight = 0;
        for (int i = start; i < end; i++) {
            rowHeight = Math.max(rowHeight, children.get(i).outerPreferredHeight());
        }
        return rowHeight;
    }

    private float rowHeight(List<UIElement> children, int start, int end, float cellWidth) {
        if (style().hasRowHeight()) {
            return style().rowHeight();
        }

        float rowHeight = 0;
        for (int i = start; i < end; i++) {
            rowHeight = Math.max(rowHeight, children.get(i).outerPreferredHeight(cellWidth));
        }
        return rowHeight;
    }

    private float childSize(UIElement child, boolean width, float available) {
        boolean explicit = width ? child.style().hasWidth() : child.style().hasHeight();
        if (style().alignItems() == AlignItems.STRETCH && !explicit) {
            return available;
        }

        float preferred = width ? child.preferredWidth() : child.preferredHeight();
        return Math.min(preferred, available);
    }

    private float childHeight(UIElement child, float availableWidth, float availableHeight) {
        if (style().alignItems() == AlignItems.STRETCH && !child.style().hasHeight()) {
            return availableHeight;
        }
        return Math.min(child.preferredHeight(availableWidth), availableHeight);
    }

    private float alignOffset(float available, float childSize) {
        if (style().alignItems() == AlignItems.CENTER) {
            return Math.max(0, available - childSize) * 0.5f;
        }
        if (style().alignItems() == AlignItems.END) {
            return Math.max(0, available - childSize);
        }
        return 0;
    }
}
