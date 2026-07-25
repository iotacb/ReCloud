package de.kostari.cloud.core.ui;

import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.render.font.Font;
import de.kostari.cloud.core.utils.types.Color4f;
import de.kostari.cloud.core.window.Input;

public class Button extends Panel {

    private String label;
    private Runnable onClick;
    private boolean enabled = true;
    private boolean hovered;
    private boolean pressed;

    public Button(String label) {
        this.label = label == null ? "" : label;
        style().css(
                "padding: 10px 14px; background: #1f2937e6; hover-background: #374151ff; active-background: #111827ff; border: 1px solid #ffffff29; color: white; text-align: center; align-items: center;");
    }

    public Button label(String label) {
        this.label = label == null ? "" : label;
        return this;
    }

    public Button onClick(Runnable onClick) {
        this.onClick = onClick;
        return this;
    }

    public Button enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public boolean isHovered() {
        return hovered;
    }

    public boolean isPressed() {
        return pressed;
    }

    @Override
    protected void drawSelf() {
        updateInteraction();
        paintBox(backgroundForState());
        drawLabel();
    }

    @Override
    protected float preferredInnerWidth() {
        Font font = font();
        return font == null ? 0 : Render.getTextWidth(font, label, style().fontScale());
    }

    @Override
    protected float preferredInnerHeight() {
        Font font = font();
        return font == null ? 0 : Render.getTextHeight(font) * style().fontScale();
    }

    private void updateInteraction() {
        hovered = enabled && bounds.contains(Input.getMouseX(), Input.getMouseY());
        pressed = hovered && Input.mouseButtonDown(0);

        if (hovered && Input.mouseButtonPressed(0) && onClick != null) {
            onClick.run();
        }
    }

    private Color4f backgroundForState() {
        if (pressed && style().activeBackgroundColor() != null) {
            return style().activeBackgroundColor();
        }
        if (hovered && style().hoverBackgroundColor() != null) {
            return style().hoverBackgroundColor();
        }
        return style().backgroundColor();
    }

    private void drawLabel() {
        Font font = font();
        if (font == null || label.isEmpty()) {
            return;
        }

        float scale = style().fontScale();
        float textWidth = Render.getTextWidth(font, label, scale);
        float textHeight = Render.getTextHeight(font) * scale;
        float x = contentBounds.x + Math.max(0, contentBounds.width - textWidth) * 0.5f;
        float y = contentBounds.y + Math.max(0, contentBounds.height - textHeight) * 0.5f;

        if (style().shadowDepth() > 0 && style().shadowColor() != null && style().shadowColor().a > 0) {
            Render.drawText(font, label, x + style().shadowDepth(), y + style().shadowDepth(), scale,
                    style().shadowColor());
        }
        Render.drawText(font, label, x, y, scale, style().color());
    }

    private Font font() {
        return style().font() == null ? UI.defaultFont() : style().font();
    }
}
