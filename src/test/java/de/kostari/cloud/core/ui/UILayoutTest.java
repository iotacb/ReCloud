package de.kostari.cloud.core.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.kostari.cloud.core.utils.render.font.Font;

class UILayoutTest {

    @BeforeAll
    static void configureFont() {
        UI.setDefaultFont(new Font("./arial.ttf", 38));
    }

    @Test
    void buttonIsComposedFromPanelAndTextAndMeasuresConsistently() {
        Button button = new Button("Launch");

        assertEquals(1, button.children().size());
        assertSame(button.panel(), button.children().getFirst());
        assertEquals(1, button.panel().children().size());
        assertSame(button.textElement(), button.panel().children().getFirst());

        float intrinsicHeight = button.preferredHeight();
        assertTrue(intrinsicHeight > button.panel().layout().padding().vertical());
        assertEquals(intrinsicHeight, button.preferredHeight(180), 0.001f);
    }

    @Test
    void rowFlexDistributesRemainingSpaceToGrowingChildren() {
        Flex row = new Flex(FlexDirection.ROW).gap(10).align(AlignItems.STRETCH);
        Panel growing = intrinsic(50, 20);
        growing.layout().grow(1);
        Panel fixed = intrinsic(40, 20);
        row.add(growing, fixed);

        row.measure(UIConstraints.tight(300, 60));
        row.arrange(new UIRect(0, 0, 300, 60));

        assertEquals(250, growing.bounds().width, 0.001f);
        assertEquals(40, fixed.bounds().width, 0.001f);
        assertEquals(260, fixed.bounds().x, 0.001f);
        assertEquals(60, growing.bounds().height, 0.001f);
    }

    @Test
    void gridCreatesStableEqualCells() {
        Grid grid = new Grid(2).gap(10).rowHeight(30);
        Panel first = intrinsic(15, 12);
        Panel second = intrinsic(15, 12);
        Panel third = intrinsic(15, 12);
        grid.add(first, second, third);

        grid.measure(UIConstraints.tight(210, 70));
        grid.arrange(new UIRect(0, 0, 210, 70));

        assertEquals(100, first.bounds().width, 0.001f);
        assertEquals(110, second.bounds().x, 0.001f);
        assertEquals(40, third.bounds().y, 0.001f);
    }

    @Test
    void absoluteContainerResolvesBottomRightAnchors() {
        Absolute absolute = new Absolute();
        Panel child = fixed(30, 40);
        absolute.add(child);
        absolute.position(child).right(10).bottom(20).size(30, 40);

        absolute.measure(UIConstraints.tight(200, 100));
        absolute.arrange(new UIRect(0, 0, 200, 100));

        assertEquals(160, child.bounds().x, 0.001f);
        assertEquals(40, child.bounds().y, 0.001f);
        assertEquals(30, child.bounds().width, 0.001f);
        assertEquals(40, child.bounds().height, 0.001f);
    }

    @Test
    void interactiveControlsExposeTheirPrimitiveParts() {
        Slider slider = new Slider();
        TextBox textBox = new TextBox("Name");

        assertEquals(3, slider.children().size());
        assertTrue(slider.children().stream().allMatch(Panel.class::isInstance));
        assertSame(textBox.panel(), textBox.children().getFirst());
        assertTrue(textBox.panel().children().contains(textBox.textElement()));
        assertTrue(textBox.panel().children().contains(textBox.placeholderElement()));
    }

    @Test
    void multilineTextUsesScaledFontMetricsForLineAdvance() {
        Font font = new Font("./arial.ttf", 38);
        float scale = 0.29f;
        float lineHeight = 1.35f;
        Text text = new Text("WEAPONS\nDMG 100%")
                .font(font)
                .fontScale(scale)
                .lineHeight(lineHeight);

        float expectedLineAdvance = font.getLineHeight() * scale * lineHeight;
        assertEquals(expectedLineAdvance * 2, text.preferredHeight(), 0.001f);
        assertTrue(expectedLineAdvance > font.getLineHeight() * scale);
        assertTrue(font.getAscent() > 0);
        assertTrue(font.getDescent() > 0);
    }

    private Panel fixed(float width, float height) {
        Panel panel = new Panel();
        panel.layout().size(width, height);
        return panel;
    }

    private Panel intrinsic(float width, float height) {
        return new Panel() {
            @Override
            protected UISize measureContent(UIConstraints constraints) {
                return new UISize(constraints.constrainWidth(width), constraints.constrainHeight(height));
            }
        };
    }
}
