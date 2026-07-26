package de.kostari.cloud.core.ui;

import java.util.ArrayList;
import java.util.List;

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

        float scale = style().fontScale();
        List<String> lines = lines(contentBounds.width);
        float lineHeight = lineHeight(font, scale);
        float totalHeight = lineHeight * lines.size();
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
        for (String line : logicalLines()) {
            width = Math.max(width, Render.getTextWidth(font, line, scale));
        }
        return width + style().shadowDepth();
    }

    @Override
    protected float preferredInnerHeight() {
        Font font = font();
        if (font == null || text.isEmpty()) {
            return 0;
        }
        return lineHeight(font, style().fontScale()) * logicalLines().length + style().shadowDepth();
    }

    @Override
    protected float preferredInnerHeight(float availableWidth) {
        Font font = font();
        if (font == null || text.isEmpty()) {
            return 0;
        }
        return lineHeight(font, style().fontScale()) * lines(availableWidth).size() + style().shadowDepth();
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

    private String[] logicalLines() {
        return text.split("\\R", -1);
    }

    private List<String> lines(float availableWidth) {
        String[] logicalLines = logicalLines();
        List<String> wrappedLines = new ArrayList<>(logicalLines.length);
        Font font = font();
        float scale = style().fontScale();
        float lineWidth = Math.max(0, availableWidth - style().shadowDepth());

        for (String line : logicalLines) {
            wrapLine(line, lineWidth, font, scale, wrappedLines);
        }
        return wrappedLines;
    }

    private void wrapLine(String line, float availableWidth, Font font, float scale, List<String> wrappedLines) {
        if (line.isEmpty() || availableWidth <= 0 || Render.getTextWidth(font, line, scale) <= availableWidth) {
            wrappedLines.add(line);
            return;
        }

        int start = 0;
        while (start < line.length()) {
            float width = 0;
            int lastWhitespace = -1;
            int cursor = start;

            while (cursor < line.length()) {
                char character = line.charAt(cursor);
                width += characterWidth(font, character, scale);
                if (Character.isWhitespace(character)) {
                    lastWhitespace = cursor;
                }
                if (width > availableWidth) {
                    break;
                }
                cursor++;
            }

            if (cursor == line.length()) {
                wrappedLines.add(line.substring(start));
                return;
            }

            int end = lastWhitespace >= start ? lastWhitespace : Math.max(start + 1, cursor);
            wrappedLines.add(line.substring(start, end).stripTrailing());
            start = lastWhitespace >= start ? lastWhitespace + 1 : end;
            while (start < line.length() && Character.isWhitespace(line.charAt(start))) {
                start++;
            }
        }
    }

    private float characterWidth(Font font, char character, float scale) {
        if (character < 32 || character >= 128) {
            return 0;
        }
        return font.getCharData().get(character - 32).xadvance() * scale;
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
