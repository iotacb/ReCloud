package de.kostari.cloud.core.ui;

import java.util.ArrayList;
import java.util.List;

public class Flex extends Panel {

    private FlexDirection direction;

    public Flex() {
        this(FlexDirection.ROW);
    }

    public Flex(FlexDirection direction) {
        this.direction = direction == null ? FlexDirection.ROW : direction;
    }

    public Flex direction(FlexDirection direction) {
        this.direction = direction == null ? FlexDirection.ROW : direction;
        return this;
    }

    public FlexDirection direction() {
        return direction;
    }

    @Override
    protected void layoutChildren(float x, float y, float width, float height) {
        List<UIElement> visibleChildren = visibleChildren();
        int count = visibleChildren.size();
        if (count == 0) {
            return;
        }

        boolean row = direction == FlexDirection.ROW;
        float mainSize = row ? width : height;
        float crossSize = row ? height : width;
        float gap = row ? style().columnGap() : style().rowGap();
        float baseGapTotal = gap * Math.max(0, count - 1);
        float totalGrow = 0;
        float usedMain = 0;
        float[] mainSizes = new float[count];

        for (int i = 0; i < count; i++) {
            UIElement child = visibleChildren.get(i);
            mainSizes[i] = preferredMain(child, row);
            usedMain += mainSizes[i] + mainMargin(child, row);
            totalGrow += child.style().flexGrow();
        }

        float growSpace = Math.max(0, mainSize - usedMain - baseGapTotal);
        if (totalGrow > 0) {
            for (int i = 0; i < count; i++) {
                UIElement child = visibleChildren.get(i);
                mainSizes[i] += growSpace * (child.style().flexGrow() / totalGrow);
            }
            usedMain += growSpace;
        }

        float remaining = Math.max(0, mainSize - usedMain - baseGapTotal);
        float mainCursor = justifyOffset(remaining, count);
        float itemGap = gap + justifyGap(remaining, count);

        for (int i = 0; i < count; i++) {
            UIElement child = visibleChildren.get(i);
            Spacing margin = child.style().margin();
            float childMain = Math.max(0, mainSizes[i]);
            float childCross = crossSizeFor(child, row, Math.max(0, crossSize - crossMargin(child, row)));
            float crossOffset = crossOffset(crossSize, childCross, child, row);

            if (row) {
                child.layout(
                        x + mainCursor + margin.left,
                        y + margin.top + crossOffset,
                        childMain,
                        childCross);
                mainCursor += margin.left + childMain + margin.right + itemGap;
            } else {
                child.layout(
                        x + margin.left + crossOffset,
                        y + mainCursor + margin.top,
                        childCross,
                        childMain);
                mainCursor += margin.top + childMain + margin.bottom + itemGap;
            }
        }
    }

    @Override
    protected float preferredInnerWidth() {
        List<UIElement> visibleChildren = visibleChildren();
        if (direction == FlexDirection.ROW) {
            return sumPreferred(visibleChildren, true, style().columnGap());
        }
        return maxPreferred(visibleChildren, true);
    }

    @Override
    protected float preferredInnerHeight() {
        List<UIElement> visibleChildren = visibleChildren();
        if (direction == FlexDirection.COLUMN) {
            return sumPreferred(visibleChildren, false, style().rowGap());
        }
        return maxPreferred(visibleChildren, false);
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

    private float sumPreferred(List<UIElement> elements, boolean width, float gap) {
        float size = 0;
        for (UIElement child : elements) {
            size += width ? child.outerPreferredWidth() : child.outerPreferredHeight();
        }
        size += gap * Math.max(0, elements.size() - 1);
        return size;
    }

    private float maxPreferred(List<UIElement> elements, boolean width) {
        float size = 0;
        for (UIElement child : elements) {
            size = Math.max(size, width ? child.outerPreferredWidth() : child.outerPreferredHeight());
        }
        return size;
    }

    private float preferredMain(UIElement child, boolean row) {
        return row ? child.preferredWidth() : child.preferredHeight();
    }

    private float preferredCross(UIElement child, boolean row) {
        return row ? child.preferredHeight() : child.preferredWidth();
    }

    private float mainMargin(UIElement child, boolean row) {
        Spacing margin = child.style().margin();
        return row ? margin.horizontal() : margin.vertical();
    }

    private float crossMargin(UIElement child, boolean row) {
        Spacing margin = child.style().margin();
        return row ? margin.vertical() : margin.horizontal();
    }

    private float crossSizeFor(UIElement child, boolean row, float availableCross) {
        boolean hasExplicitCross = row ? child.style().hasHeight() : child.style().hasWidth();
        if (style().alignItems() == AlignItems.STRETCH && !hasExplicitCross) {
            return availableCross;
        }
        return Math.min(preferredCross(child, row), availableCross);
    }

    private float crossOffset(float crossSize, float childCross, UIElement child, boolean row) {
        if (style().alignItems() == AlignItems.STRETCH) {
            return 0;
        }

        float available = Math.max(0, crossSize - crossMargin(child, row));
        float remaining = Math.max(0, available - childCross);
        return switch (style().alignItems()) {
            case CENTER -> remaining * 0.5f;
            case END -> remaining;
            default -> 0;
        };
    }

    private float justifyOffset(float remaining, int count) {
        return switch (style().justifyContent()) {
            case CENTER -> remaining * 0.5f;
            case END -> remaining;
            case SPACE_AROUND -> count > 0 ? remaining / count * 0.5f : 0;
            default -> 0;
        };
    }

    private float justifyGap(float remaining, int count) {
        return switch (style().justifyContent()) {
            case SPACE_BETWEEN -> count > 1 ? remaining / (count - 1) : 0;
            case SPACE_AROUND -> count > 0 ? remaining / count : 0;
            default -> 0;
        };
    }
}
