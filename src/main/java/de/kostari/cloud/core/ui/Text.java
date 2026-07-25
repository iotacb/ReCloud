package de.kostari.cloud.core.ui;

import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.render.font.Font;
import de.kostari.cloud.core.utils.types.Color4f;

public class Text extends UIElement {

    private String text;

    public Text(String text) {
        this.text = text == null ? "" : text;
    }

    public Text text(String text) {
        this.text = text == null ? "" : text;
        return this;
    }

    public String text() {
        return text;
    }

    @Override
    protected void drawSelf() {
        paintBox();

        Font font = font();
        if (text.isEmpty() || font == null) {
            return;
        }

        String[] lines = lines();
        float scale = style().fontScale();
        float lineHeight = lineHeight(font, scale);
        float totalHeight = lineHeight * lines.length;
        float y = contentBounds.y + verticalOffset(totalHeight);

        for (String line : lines) {
            float lineWidth = Render.getTextWidth(font, line, scale);
            float x = contentBounds.x + horizontalOffset(lineWidth);
            drawLine(font, line, x, y, scale);
            y += lineHeight;
        }
    }

    @Override
    protected float preferredInnerWidth() {
        Font font = font();
        if (font == null || text.isEmpty()) {
            return 0;
        }

        float width = 0;
        float scale = style().fontScale();
        for (String line : lines()) {
            width = Math.max(width, Render.getTextWidth(font, line, scale));
        }
        return width;
    }

    @Override
    protected float preferredInnerHeight() {
        Font font = font();
        if (font == null || text.isEmpty()) {
            return 0;
        }
        return lineHeight(font, style().fontScale()) * lines().length;
    }

    private void drawLine(Font font, String line, float x, float y, float scale) {
        Color4f shadowColor = style().shadowColor();
        float shadowDepth = style().shadowDepth();
        if (shadowDepth > 0 && shadowColor != null && shadowColor.a > 0) {
            Render.drawText(font, line, x + shadowDepth, y + shadowDepth, scale, shadowColor);
        }
        Render.drawText(font, line, x, y, scale, style().color());
    }

    private float horizontalOffset(float lineWidth) {
        return switch (style().textAlign()) {
            case CENTER -> Math.max(0, contentBounds.width - lineWidth) * 0.5f;
            case END -> Math.max(0, contentBounds.width - lineWidth);
            default -> 0;
        };
    }

    private float verticalOffset(float textHeight) {
        return switch (style().alignItems()) {
            case CENTER -> Math.max(0, contentBounds.height - textHeight) * 0.5f;
            case END -> Math.max(0, contentBounds.height - textHeight);
            default -> 0;
        };
    }

    private String[] lines() {
        return text.split("\\R", -1);
    }

    private Font font() {
        return style().font() == null ? UI.defaultFont() : style().font();
    }

    private float lineHeight(Font font, float scale) {
        float baseHeight = Math.max(1, Render.getTextHeight(font) * scale);
        float configuredLineHeight = style().lineHeight();
        if (configuredLineHeight <= 4f) {
            return baseHeight * configuredLineHeight;
        }
        return configuredLineHeight;
    }
}
