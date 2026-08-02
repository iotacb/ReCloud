package de.kostari.cloud.core.ui;

import java.util.ArrayList;
import java.util.List;

public class Grid extends Panel {

    private int columns;
    private float rowGap;
    private float columnGap;
    private float rowHeight = Layout.AUTO;
    private AlignItems alignItems = AlignItems.STRETCH;

    public Grid() {
        this(2);
    }

    public Grid(int columns) {
        this.columns = Math.max(1, columns);
    }

    public Grid columns(int value) {
        columns = Math.max(1, value);
        invalidateLayout();
        return this;
    }

    public Grid gap(float value) {
        rowGap = Math.max(0, value);
        columnGap = Math.max(0, value);
        invalidateLayout();
        return this;
    }

    public Grid rowGap(float value) {
        rowGap = Math.max(0, value);
        invalidateLayout();
        return this;
    }

    public Grid columnGap(float value) {
        columnGap = Math.max(0, value);
        invalidateLayout();
        return this;
    }

    public Grid rowHeight(float value) {
        rowHeight = Math.max(0, value);
        invalidateLayout();
        return this;
    }

    public Grid autoRows() {
        rowHeight = Layout.AUTO;
        invalidateLayout();
        return this;
    }

    public Grid align(AlignItems value) {
        alignItems = value == null ? AlignItems.STRETCH : value;
        invalidateLayout();
        return this;
    }

    @Override
    protected UISize measureContent(UIConstraints constraints) {
        List<UIElement> elements = visibleChildren();
        if (elements.isEmpty()) {
            return UISize.ZERO;
        }

        float gapWidth = columnGap * Math.max(0, columns - 1);
        float offeredCellWidth = Float.isFinite(constraints.maxWidth())
                ? Math.max(0, (constraints.maxWidth() - gapWidth) / columns)
                : UIConstraints.INFINITY;
        int rows = (int) Math.ceil(elements.size() / (float) columns);
        float[] rowHeights = new float[rows];
        float maxCellWidth = 0;

        for (int i = 0; i < elements.size(); i++) {
            UIElement child = elements.get(i);
            Insets margin = child.layout().margin();
            float childMaxWidth = subtractFinite(offeredCellWidth, margin.horizontal());
            UISize size = child.measure(UIConstraints.loose(childMaxWidth, constraints.maxHeight()));
            maxCellWidth = Math.max(maxCellWidth, size.width() + margin.horizontal());
            rowHeights[i / columns] = Math.max(rowHeights[i / columns], size.height() + margin.vertical());
        }

        float width = maxCellWidth * columns + gapWidth;
        float height = rowGap * Math.max(0, rows - 1);
        for (float measuredRowHeight : rowHeights) {
            height += rowHeight >= 0 ? rowHeight : measuredRowHeight;
        }
        return new UISize(width, height);
    }

    @Override
    protected void arrangeChildren(UIRect area) {
        List<UIElement> elements = visibleChildren();
        if (elements.isEmpty()) {
            return;
        }

        float cellWidth = Math.max(0,
                (area.width - columnGap * Math.max(0, columns - 1)) / columns);
        float cursorY = area.y;

        for (int rowStart = 0; rowStart < elements.size(); rowStart += columns) {
            int rowEnd = Math.min(rowStart + columns, elements.size());
            float measuredRowHeight = 0;
            UISize[] sizes = new UISize[rowEnd - rowStart];
            for (int i = rowStart; i < rowEnd; i++) {
                UIElement child = elements.get(i);
                Insets margin = child.layout().margin();
                sizes[i - rowStart] = child.measure(UIConstraints.loose(
                        Math.max(0, cellWidth - margin.horizontal()), area.height));
                measuredRowHeight = Math.max(measuredRowHeight,
                        sizes[i - rowStart].height() + margin.vertical());
            }
            float currentRowHeight = rowHeight >= 0 ? rowHeight : measuredRowHeight;

            for (int i = rowStart; i < rowEnd; i++) {
                int column = i - rowStart;
                UIElement child = elements.get(i);
                Insets margin = child.layout().margin();
                UISize desired = sizes[column];
                float availableWidth = Math.max(0, cellWidth - margin.horizontal());
                float availableHeight = Math.max(0, currentRowHeight - margin.vertical());
                float childWidth = alignItems == AlignItems.STRETCH && !child.layout().hasWidth()
                        ? availableWidth
                        : Math.min(desired.width(), availableWidth);
                float childHeight = alignItems == AlignItems.STRETCH && !child.layout().hasHeight()
                        ? availableHeight
                        : Math.min(desired.height(), availableHeight);
                float offsetX = alignOffset(availableWidth, childWidth);
                float offsetY = alignOffset(availableHeight, childHeight);
                child.arrange(new UIRect(
                        area.x + column * (cellWidth + columnGap) + margin.left() + offsetX,
                        cursorY + margin.top() + offsetY,
                        childWidth,
                        childHeight));
            }
            cursorY += currentRowHeight + rowGap;
        }
    }

    private List<UIElement> visibleChildren() {
        List<UIElement> visible = new ArrayList<>();
        for (UIElement child : children()) {
            if (child.isVisible()) {
                visible.add(child);
            }
        }
        return visible;
    }

    private float alignOffset(float available, float size) {
        return switch (alignItems) {
            case CENTER -> Math.max(0, available - size) * 0.5f;
            case END -> Math.max(0, available - size);
            default -> 0;
        };
    }

    private static float subtractFinite(float value, float amount) {
        return Float.isFinite(value) ? Math.max(0, value - amount) : value;
    }
}
