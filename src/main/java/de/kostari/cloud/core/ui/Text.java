package de.kostari.cloud.core.ui;

import java.util.ArrayList;
import java.util.List;

import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.render.font.Font;
import de.kostari.cloud.core.utils.types.Color4f;

public class Text extends UIElement {

    private String text;
    private Font font;
    private Color4f color = new Color4f(1, 1, 1, 1);
    private Color4f shadowColor = new Color4f(0, 0, 0, 0.65f);
    private float fontScale = 1;
    private float lineHeight = 1.18f;
    private float shadowDepth;
    private boolean wrap = true;
    private TextAlign textAlign = TextAlign.START;
    private AlignItems verticalAlign = AlignItems.START;
    private Drawable background = Drawables.none();
    private Drawable border = Drawables.none();
    private float borderWidth;
    private Color4f borderColor = new Color4f(0, 0, 0, 0);

    public Text(String text) {
        this.text = text == null ? "" : text;
    }

    public Text text(String value) {
        String safe = value == null ? "" : value;
        if (!text.equals(safe)) {
            text = safe;
            invalidateLayout();
        }
        return this;
    }

    public String text() {
        return text;
    }

    public Text font(Font value) {
        font = value;
        invalidateLayout();
        return this;
    }

    public Font font() {
        return font == null ? UI.defaultFont() : font;
    }

    public Text color(Color4f value) {
        color = value == null ? new Color4f(1, 1, 1, 1) : value;
        invalidatePaint();
        return this;
    }

    public Color4f color() {
        return color;
    }

    public Text fontScale(float value) {
        fontScale = Math.max(0.01f, value);
        invalidateLayout();
        return this;
    }

    public float fontScale() {
        return fontScale;
    }

    public Text lineHeight(float value) {
        lineHeight = value <= 0 ? 1.18f : value;
        invalidateLayout();
        return this;
    }

    public Text wrap(boolean value) {
        wrap = value;
        invalidateLayout();
        return this;
    }

    public Text align(TextAlign value) {
        textAlign = value == null ? TextAlign.START : value;
        invalidatePaint();
        return this;
    }

    public Text verticalAlign(AlignItems value) {
        verticalAlign = value == null ? AlignItems.START : value;
        invalidatePaint();
        return this;
    }

    public Text shadow(float depth, Color4f value) {
        shadowDepth = Math.max(0, depth);
        shadowColor = value == null ? new Color4f(0, 0, 0, 0) : value;
        invalidateLayout();
        return this;
    }

    public Text shadow(float depth) {
        return shadow(depth, new Color4f(0, 0, 0, 0.65f));
    }

    public Text background(Color4f value) {
        background = Drawables.solid(value);
        invalidatePaint();
        return this;
    }

    public Text background(Drawable value) {
        background = value == null ? Drawables.none() : value;
        invalidatePaint();
        return this;
    }

    public Text border(float width, Color4f value) {
        borderWidth = Math.max(0, width);
        borderColor = value == null ? new Color4f(0, 0, 0, 0) : value;
        border = Drawables.border(borderWidth, borderColor);
        invalidatePaint();
        return this;
    }

    public Text borderColor(Color4f value) {
        return border(borderWidth, value);
    }

    @Override
    protected UISize measureContent(UIConstraints constraints) {
        Font activeFont = font();
        if (activeFont == null || text.isEmpty()) {
            return UISize.ZERO;
        }

        float naturalWidth = 0;
        for (String line : logicalLines()) {
            naturalWidth = Math.max(naturalWidth, Render.getTextWidth(activeFont, line, fontScale));
        }
        float width = Float.isFinite(constraints.maxWidth())
                ? Math.min(naturalWidth + shadowDepth, constraints.maxWidth())
                : naturalWidth + shadowDepth;
        List<String> measuredLines = lines(width);
        float height = lineAdvance(activeFont) * measuredLines.size() + shadowDepth;
        return new UISize(width, height);
    }

    @Override
    protected void drawSelf() {
        background.draw(renderBounds(), renderOpacity());
        border.draw(renderBounds(), renderOpacity());
        Font activeFont = font();
        if (activeFont == null || text.isEmpty()) {
            return;
        }

        UIRect area = renderContentBounds();
        List<String> renderedLines = lines(area.width);
        float naturalLineHeight = Render.getTextHeight(activeFont) * fontScale;
        float lineAdvance = lineAdvance(activeFont);
        float totalHeight = lineAdvance * renderedLines.size() + shadowDepth;
        float leading = lineAdvance - naturalLineHeight;
        float y = area.y + verticalOffset(area.height, totalHeight) + leading * 0.5f;
        Color4f textColor = Drawables.alpha(color, renderOpacity());
        Color4f renderedShadow = Drawables.alpha(shadowColor, renderOpacity());

        for (String line : renderedLines) {
            float lineWidth = Render.getTextWidth(activeFont, line, fontScale);
            float x = area.x + horizontalOffset(area.width, lineWidth);
            if (shadowDepth > 0 && renderedShadow.a > 0) {
                Render.drawText(activeFont, line, x + shadowDepth, y + shadowDepth, fontScale, renderedShadow);
            }
            Render.drawText(activeFont, line, x, y, fontScale, textColor);
            y += lineAdvance;
        }
    }

    private String[] logicalLines() {
        return text.split("\\R", -1);
    }

    private List<String> lines(float availableWidth) {
        String[] logical = logicalLines();
        List<String> result = new ArrayList<>(logical.length);
        Font activeFont = font();
        float lineWidth = Math.max(0, availableWidth - shadowDepth);
        for (String line : logical) {
            wrapLine(line, lineWidth, activeFont, result);
        }
        return result;
    }

    private void wrapLine(String line, float availableWidth, Font activeFont, List<String> output) {
        if (!wrap || line.isEmpty() || !Float.isFinite(availableWidth) || availableWidth <= 0
                || Render.getTextWidth(activeFont, line, fontScale) <= availableWidth) {
            output.add(line);
            return;
        }

        int start = 0;
        while (start < line.length()) {
            float width = 0;
            int lastWhitespace = -1;
            int cursor = start;
            while (cursor < line.length()) {
                char character = line.charAt(cursor);
                width += characterWidth(activeFont, character);
                if (Character.isWhitespace(character)) {
                    lastWhitespace = cursor;
                }
                if (width > availableWidth) {
                    break;
                }
                cursor++;
            }

            if (cursor == line.length()) {
                output.add(line.substring(start));
                return;
            }

            int end = lastWhitespace >= start ? lastWhitespace : Math.max(start + 1, cursor);
            output.add(line.substring(start, end).stripTrailing());
            start = lastWhitespace >= start ? lastWhitespace + 1 : end;
            while (start < line.length() && Character.isWhitespace(line.charAt(start))) {
                start++;
            }
        }
    }

    private float characterWidth(Font activeFont, char character) {
        if (character < 32 || character >= 128) {
            return 0;
        }
        return activeFont.getCharData().get(character - 32).xadvance() * fontScale;
    }

    private float lineAdvance(Font activeFont) {
        float base = Math.max(1, Render.getTextHeight(activeFont) * fontScale);
        return base * lineHeight;
    }

    private float horizontalOffset(float available, float width) {
        return switch (textAlign) {
            case CENTER -> Math.max(0, available - width) * 0.5f;
            case END -> Math.max(0, available - width);
            default -> 0;
        };
    }

    private float verticalOffset(float available, float height) {
        return switch (verticalAlign) {
            case CENTER -> Math.max(0, available - height) * 0.5f;
            case END -> Math.max(0, available - height);
            default -> 0;
        };
    }
}
