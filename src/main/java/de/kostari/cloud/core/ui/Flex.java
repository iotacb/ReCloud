package de.kostari.cloud.core.ui;

import java.util.ArrayList;
import java.util.List;

public class Flex extends Panel {

    private FlexDirection direction;
    private AlignItems alignItems = AlignItems.START;
    private JustifyContent justifyContent = JustifyContent.START;
    private float rowGap;
    private float columnGap;

    public Flex() {
        this(FlexDirection.ROW);
    }

    public Flex(FlexDirection direction) {
        this.direction = direction == null ? FlexDirection.ROW : direction;
    }

    public Flex direction(FlexDirection value) {
        direction = value == null ? FlexDirection.ROW : value;
        invalidateLayout();
        return this;
    }

    public FlexDirection direction() {
        return direction;
    }

    public Flex gap(float value) {
        rowGap = Math.max(0, value);
        columnGap = Math.max(0, value);
        invalidateLayout();
        return this;
    }

    public Flex rowGap(float value) {
        rowGap = Math.max(0, value);
        invalidateLayout();
        return this;
    }

    public Flex columnGap(float value) {
        columnGap = Math.max(0, value);
        invalidateLayout();
        return this;
    }

    public Flex align(AlignItems value) {
        alignItems = value == null ? AlignItems.START : value;
        invalidateLayout();
        return this;
    }

    public Flex justify(JustifyContent value) {
        justifyContent = value == null ? JustifyContent.START : value;
        invalidateLayout();
        return this;
    }

    public AlignItems alignment() {
        return alignItems;
    }

    public JustifyContent justification() {
        return justifyContent;
    }

    @Override
    protected UISize measureContent(UIConstraints constraints) {
        List<UIElement> elements = visibleChildren();
        if (elements.isEmpty()) {
            return UISize.ZERO;
        }

        boolean row = direction == FlexDirection.ROW;
        float main = 0;
        float cross = 0;
        for (UIElement child : elements) {
            Insets margin = child.layout().margin();
            UIConstraints childConstraints = constraints.inset(margin);
            UISize size = child.measure(childConstraints);
            main += main(size, row) + main(margin, row);
            cross = Math.max(cross, cross(size, row) + cross(margin, row));
        }
        main += gap(row) * Math.max(0, elements.size() - 1);
        return row ? new UISize(main, cross) : new UISize(cross, main);
    }

    @Override
    protected void arrangeChildren(UIRect area) {
        List<UIElement> elements = visibleChildren();
        if (elements.isEmpty()) {
            return;
        }

        boolean row = direction == FlexDirection.ROW;
        float availableMain = row ? area.width : area.height;
        float availableCross = row ? area.height : area.width;
        float gap = gap(row);
        float gapTotal = gap * Math.max(0, elements.size() - 1);
        float[] sizes = new float[elements.size()];
        UISize[] measured = new UISize[elements.size()];
        float used = gapTotal;
        float totalGrow = 0;
        float totalShrink = 0;

        for (int i = 0; i < elements.size(); i++) {
            UIElement child = elements.get(i);
            Insets margin = child.layout().margin();
            float childWidth = Math.max(0, area.width - margin.horizontal());
            float childHeight = Math.max(0, area.height - margin.vertical());
            measured[i] = child.measure(UIConstraints.loose(childWidth, childHeight));
            sizes[i] = main(measured[i], row);
            used += sizes[i] + main(margin, row);
            totalGrow += child.layout().grow();
            totalShrink += child.layout().shrink();
        }

        float delta = availableMain - used;
        if (delta > 0 && totalGrow > 0) {
            for (int i = 0; i < elements.size(); i++) {
                sizes[i] += delta * elements.get(i).layout().grow() / totalGrow;
            }
            used = availableMain;
        } else if (delta < 0 && totalShrink > 0) {
            float shortage = -delta;
            float removed = 0;
            for (int i = 0; i < elements.size(); i++) {
                UIElement child = elements.get(i);
                float share = shortage * child.layout().shrink() / totalShrink;
                float minimum = row ? child.layout().minWidth() : child.layout().minHeight();
                float next = Math.max(minimum, sizes[i] - share);
                removed += sizes[i] - next;
                sizes[i] = next;
            }
            used -= removed;
        }

        float remaining = Math.max(0, availableMain - used);
        float cursor = mainStart(remaining, elements.size());
        float itemGap = gap + distributedGap(remaining, elements.size());

        for (int i = 0; i < elements.size(); i++) {
            UIElement child = elements.get(i);
            Insets margin = child.layout().margin();
            float crossAvailable = Math.max(0, availableCross - cross(margin, row));
            boolean explicitCross = row ? child.layout().hasHeight() : child.layout().hasWidth();
            float childCross = alignItems == AlignItems.STRETCH && !explicitCross
                    ? crossAvailable
                    : Math.min(cross(measured[i], row), crossAvailable);
            float crossOffset = crossOffset(crossAvailable, childCross);
            float childMain = Math.max(0, sizes[i]);

            if (row) {
                child.arrange(new UIRect(
                        area.x + cursor + margin.left(),
                        area.y + margin.top() + crossOffset,
                        childMain,
                        childCross));
                cursor += margin.left() + childMain + margin.right() + itemGap;
            } else {
                child.arrange(new UIRect(
                        area.x + margin.left() + crossOffset,
                        area.y + cursor + margin.top(),
                        childCross,
                        childMain));
                cursor += margin.top() + childMain + margin.bottom() + itemGap;
            }
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

    private float gap(boolean row) {
        return row ? columnGap : rowGap;
    }

    private float mainStart(float remaining, int count) {
        return switch (justifyContent) {
            case CENTER -> remaining * 0.5f;
            case END -> remaining;
            case SPACE_AROUND -> count == 0 ? 0 : remaining / count * 0.5f;
            default -> 0;
        };
    }

    private float distributedGap(float remaining, int count) {
        return switch (justifyContent) {
            case SPACE_BETWEEN -> count > 1 ? remaining / (count - 1) : 0;
            case SPACE_AROUND -> count > 0 ? remaining / count : 0;
            default -> 0;
        };
    }

    private float crossOffset(float available, float size) {
        float remaining = Math.max(0, available - size);
        return switch (alignItems) {
            case CENTER -> remaining * 0.5f;
            case END -> remaining;
            default -> 0;
        };
    }

    private static float main(UISize size, boolean row) {
        return row ? size.width() : size.height();
    }

    private static float cross(UISize size, boolean row) {
        return row ? size.height() : size.width();
    }

    private static float main(Insets insets, boolean row) {
        return row ? insets.horizontal() : insets.vertical();
    }

    private static float cross(Insets insets, boolean row) {
        return row ? insets.vertical() : insets.horizontal();
    }
}
