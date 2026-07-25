package de.kostari.cloud.core.ui;

import java.util.Locale;

import de.kostari.cloud.core.utils.Colors;
import de.kostari.cloud.core.utils.render.font.Font;
import de.kostari.cloud.core.utils.types.Color4f;

public class Style {

    static final float AUTO = -1f;
    private static final float DEFAULT_LINE_HEIGHT = 1.18f;

    private float width = AUTO;
    private float height = AUTO;
    private float minWidth = 0;
    private float minHeight = 0;
    private float maxWidth = Float.MAX_VALUE;
    private float maxHeight = Float.MAX_VALUE;
    private float flexGrow = 0;

    private final Spacing padding = new Spacing();
    private final Spacing margin = new Spacing();

    private float gap = 0;
    private float rowGap = AUTO;
    private float columnGap = AUTO;
    private int columns = 1;
    private float rowHeight = AUTO;

    private Color4f backgroundColor;
    private Color4f hoverBackgroundColor;
    private Color4f activeBackgroundColor;
    private Color4f borderColor;
    private float borderWidth = 0;

    private Color4f color = Colors.WHITE;
    private Font font;
    private float fontScale = 1f;
    private float lineHeight = DEFAULT_LINE_HEIGHT;
    private float shadowDepth = 0;
    private Color4f shadowColor = new Color4f(0, 0, 0, 0.65f);

    private AlignItems alignItems = AlignItems.START;
    private JustifyContent justifyContent = JustifyContent.START;
    private TextAlign textAlign = TextAlign.START;

    public Style width(float width) {
        this.width = Math.max(AUTO, width);
        return this;
    }

    public Style height(float height) {
        this.height = Math.max(AUTO, height);
        return this;
    }

    public Style size(float width, float height) {
        return width(width).height(height);
    }

    public Style minWidth(float minWidth) {
        this.minWidth = Math.max(0, minWidth);
        return this;
    }

    public Style minHeight(float minHeight) {
        this.minHeight = Math.max(0, minHeight);
        return this;
    }

    public Style minSize(float width, float height) {
        return minWidth(width).minHeight(height);
    }

    public Style maxWidth(float maxWidth) {
        this.maxWidth = Math.max(0, maxWidth);
        return this;
    }

    public Style maxHeight(float maxHeight) {
        this.maxHeight = Math.max(0, maxHeight);
        return this;
    }

    public Style maxSize(float width, float height) {
        return maxWidth(width).maxHeight(height);
    }

    public Style flexGrow(float flexGrow) {
        this.flexGrow = Math.max(0, flexGrow);
        return this;
    }

    public Style grow(float flexGrow) {
        return flexGrow(flexGrow);
    }

    public Style padding(float all) {
        padding.set(Math.max(0, all));
        return this;
    }

    public Style padding(float vertical, float horizontal) {
        padding.set(Math.max(0, vertical), Math.max(0, horizontal));
        return this;
    }

    public Style padding(float top, float right, float bottom, float left) {
        padding.set(Math.max(0, top), Math.max(0, right), Math.max(0, bottom), Math.max(0, left));
        return this;
    }

    public Style margin(float all) {
        margin.set(Math.max(0, all));
        return this;
    }

    public Style margin(float vertical, float horizontal) {
        margin.set(Math.max(0, vertical), Math.max(0, horizontal));
        return this;
    }

    public Style margin(float top, float right, float bottom, float left) {
        margin.set(Math.max(0, top), Math.max(0, right), Math.max(0, bottom), Math.max(0, left));
        return this;
    }

    public Style gap(float gap) {
        this.gap = Math.max(0, gap);
        return this;
    }

    public Style rowGap(float rowGap) {
        this.rowGap = Math.max(0, rowGap);
        return this;
    }

    public Style columnGap(float columnGap) {
        this.columnGap = Math.max(0, columnGap);
        return this;
    }

    public Style columns(int columns) {
        this.columns = Math.max(1, columns);
        return this;
    }

    public Style rowHeight(float rowHeight) {
        this.rowHeight = Math.max(AUTO, rowHeight);
        return this;
    }

    public Style background(Color4f color) {
        this.backgroundColor = color;
        return this;
    }

    public Style background(int r, int g, int b, int a) {
        return background(Colors.from255(r, g, b, a));
    }

    public Style hoverBackground(Color4f color) {
        this.hoverBackgroundColor = color;
        return this;
    }

    public Style activeBackground(Color4f color) {
        this.activeBackgroundColor = color;
        return this;
    }

    public Style border(float width, Color4f color) {
        this.borderWidth = Math.max(0, width);
        this.borderColor = color;
        return this;
    }

    public Style borderWidth(float width) {
        this.borderWidth = Math.max(0, width);
        return this;
    }

    public Style borderColor(Color4f color) {
        this.borderColor = color;
        return this;
    }

    public Style color(Color4f color) {
        this.color = color;
        return this;
    }

    public Style font(Font font) {
        this.font = font;
        return this;
    }

    public Style fontScale(float fontScale) {
        this.fontScale = Math.max(0.01f, fontScale);
        return this;
    }

    public Style lineHeight(float lineHeight) {
        this.lineHeight = lineHeight <= 0 ? DEFAULT_LINE_HEIGHT : lineHeight;
        return this;
    }

    public Style normalLineHeight() {
        this.lineHeight = DEFAULT_LINE_HEIGHT;
        return this;
    }

    public Style shadow(float depth) {
        this.shadowDepth = Math.max(0, depth);
        return this;
    }

    public Style shadow(float depth, Color4f color) {
        this.shadowDepth = Math.max(0, depth);
        this.shadowColor = color;
        return this;
    }

    public Style noShadow() {
        this.shadowDepth = 0;
        return this;
    }

    public Style alignItems(AlignItems alignItems) {
        this.alignItems = alignItems == null ? AlignItems.START : alignItems;
        return this;
    }

    public Style justifyContent(JustifyContent justifyContent) {
        this.justifyContent = justifyContent == null ? JustifyContent.START : justifyContent;
        return this;
    }

    public Style textAlign(TextAlign textAlign) {
        this.textAlign = textAlign == null ? TextAlign.START : textAlign;
        return this;
    }

    public Style css(String declarations) {
        if (declarations == null || declarations.isBlank()) {
            return this;
        }

        String[] rules = declarations.split(";");
        for (String rule : rules) {
            int separator = rule.indexOf(':');
            if (separator <= 0) {
                continue;
            }
            applyDeclaration(rule.substring(0, separator).trim(), rule.substring(separator + 1).trim());
        }
        return this;
    }

    boolean hasWidth() {
        return width >= 0;
    }

    boolean hasHeight() {
        return height >= 0;
    }

    float width() {
        return width;
    }

    float height() {
        return height;
    }

    float minWidth() {
        return minWidth;
    }

    float minHeight() {
        return minHeight;
    }

    float maxWidth() {
        return maxWidth;
    }

    float maxHeight() {
        return maxHeight;
    }

    float flexGrow() {
        return flexGrow;
    }

    Spacing padding() {
        return padding;
    }

    Spacing margin() {
        return margin;
    }

    float gap() {
        return gap;
    }

    float rowGap() {
        return rowGap >= 0 ? rowGap : gap;
    }

    float columnGap() {
        return columnGap >= 0 ? columnGap : gap;
    }

    int columns() {
        return columns;
    }

    boolean hasRowHeight() {
        return rowHeight >= 0;
    }

    float rowHeight() {
        return rowHeight;
    }

    Color4f backgroundColor() {
        return backgroundColor;
    }

    Color4f hoverBackgroundColor() {
        return hoverBackgroundColor;
    }

    Color4f activeBackgroundColor() {
        return activeBackgroundColor;
    }

    Color4f borderColor() {
        return borderColor;
    }

    float borderWidth() {
        return borderWidth;
    }

    Color4f color() {
        return color == null ? Colors.WHITE : color;
    }

    Font font() {
        return font;
    }

    float fontScale() {
        return fontScale;
    }

    float lineHeight() {
        return lineHeight;
    }

    float shadowDepth() {
        return shadowDepth;
    }

    Color4f shadowColor() {
        return shadowColor;
    }

    AlignItems alignItems() {
        return alignItems;
    }

    JustifyContent justifyContent() {
        return justifyContent;
    }

    TextAlign textAlign() {
        return textAlign;
    }

    float horizontalInsets() {
        return padding.horizontal() + borderWidth * 2;
    }

    float verticalInsets() {
        return padding.vertical() + borderWidth * 2;
    }

    float clampWidth(float value) {
        return Math.max(minWidth, Math.min(maxWidth, value));
    }

    float clampHeight(float value) {
        return Math.max(minHeight, Math.min(maxHeight, value));
    }

    private void applyDeclaration(String property, String value) {
        String key = property.toLowerCase(Locale.ROOT);
        switch (key) {
            case "width" -> width(parseLength(value));
            case "height" -> height(parseLength(value));
            case "min-width" -> minWidth(parseLength(value));
            case "min-height" -> minHeight(parseLength(value));
            case "max-width" -> maxWidth(parseLength(value));
            case "max-height" -> maxHeight(parseLength(value));
            case "flex-grow", "grow" -> flexGrow(parseLength(value));
            case "padding" -> applyBox(value, true);
            case "margin" -> applyBox(value, false);
            case "gap" -> gap(parseLength(value));
            case "row-gap" -> rowGap(parseLength(value));
            case "column-gap" -> columnGap(parseLength(value));
            case "columns" -> columns((int) parseLength(value));
            case "row-height" -> rowHeight(parseLength(value));
            case "background", "background-color" -> background(parseColor(value));
            case "hover-background", "hover-background-color" -> hoverBackground(parseColor(value));
            case "active-background", "active-background-color" -> activeBackground(parseColor(value));
            case "border" -> applyBorder(value);
            case "border-width" -> borderWidth(parseLength(value));
            case "border-color" -> borderColor(parseColor(value));
            case "color" -> color(parseColor(value));
            case "font-scale" -> fontScale(parseLength(value));
            case "line-height" -> lineHeight(parseLineHeight(value));
            case "shadow-depth" -> shadow(parseLength(value));
            case "shadow-color" -> shadow(shadowDepth, parseColor(value));
            case "align-items" -> alignItems(parseAlignItems(value));
            case "justify-content" -> justifyContent(parseJustifyContent(value));
            case "text-align" -> textAlign(parseTextAlign(value));
            default -> {
            }
        }
    }

    private void applyBox(String value, boolean paddingBox) {
        String[] parts = value.trim().split("\\s+");
        float top = parseLength(parts[0]);
        float right = parts.length > 1 ? parseLength(parts[1]) : top;
        float bottom = parts.length > 2 ? parseLength(parts[2]) : top;
        float left = parts.length > 3 ? parseLength(parts[3]) : right;

        if (paddingBox) {
            padding(top, right, bottom, left);
        } else {
            margin(top, right, bottom, left);
        }
    }

    private void applyBorder(String value) {
        String[] parts = value.trim().split("\\s+");
        for (String part : parts) {
            if (part.equalsIgnoreCase("solid")) {
                continue;
            }
            if (part.startsWith("#") || isColorFunction(part) || isNamedColor(part)) {
                borderColor(parseColor(part));
            } else {
                borderWidth(parseLength(part));
            }
        }
    }

    private float parseLength(String value) {
        String normalized = value.trim().toLowerCase(Locale.ROOT)
                .replace("px", "")
                .replace("rem", "")
                .replace("em", "");
        if (normalized.equals("auto")) {
            return AUTO;
        }
        if (normalized.isBlank()) {
            return 0;
        }
        return Float.parseFloat(normalized);
    }

    private float parseLineHeight(String value) {
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals("normal") || normalized.equals("auto")) {
            return DEFAULT_LINE_HEIGHT;
        }
        return parseLength(value);
    }

    private Color4f parseColor(String value) {
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("#")) {
            return parseHexColor(normalized);
        }
        if (normalized.startsWith("rgba(") || normalized.startsWith("rgb(")) {
            return parseRgbColor(normalized);
        }

        return switch (normalized) {
            case "transparent" -> Colors.TRANSPARENT;
            case "black" -> Colors.BLACK;
            case "blue" -> Colors.BLUE;
            case "coral" -> Colors.CORAL;
            case "cyan" -> Colors.CYAN;
            case "gray", "grey" -> Colors.GRAY;
            case "green" -> Colors.GREEN;
            case "red" -> Colors.RED;
            case "white" -> Colors.WHITE;
            case "yellow" -> Colors.YELLOW;
            default -> Colors.WHITE;
        };
    }

    private Color4f parseHexColor(String value) {
        String hex = value.substring(1);
        if (hex.length() == 3 || hex.length() == 4) {
            StringBuilder expanded = new StringBuilder();
            for (int i = 0; i < hex.length(); i++) {
                expanded.append(hex.charAt(i)).append(hex.charAt(i));
            }
            hex = expanded.toString();
        }

        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        int a = hex.length() >= 8 ? Integer.parseInt(hex.substring(6, 8), 16) : 255;
        return Colors.from255(r, g, b, a);
    }

    private Color4f parseRgbColor(String value) {
        String content = value.substring(value.indexOf('(') + 1, value.lastIndexOf(')'));
        String[] parts = content.split(",");
        int r = (int) parseLength(parts[0]);
        int g = (int) parseLength(parts[1]);
        int b = (int) parseLength(parts[2]);
        float alpha = parts.length > 3 ? Float.parseFloat(parts[3].trim()) : 1f;
        int a = alpha <= 1f ? Math.round(alpha * 255f) : Math.round(alpha);
        return Colors.from255(r, g, b, a);
    }

    private boolean isColorFunction(String value) {
        String normalized = value.toLowerCase(Locale.ROOT);
        return normalized.startsWith("rgb(") || normalized.startsWith("rgba(");
    }

    private boolean isNamedColor(String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "transparent", "black", "blue", "coral", "cyan", "gray", "grey", "green", "red", "white",
                    "yellow" -> true;
            default -> false;
        };
    }

    private AlignItems parseAlignItems(String value) {
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "center" -> AlignItems.CENTER;
            case "end", "flex-end" -> AlignItems.END;
            case "stretch" -> AlignItems.STRETCH;
            default -> AlignItems.START;
        };
    }

    private JustifyContent parseJustifyContent(String value) {
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "center" -> JustifyContent.CENTER;
            case "end", "flex-end" -> JustifyContent.END;
            case "space-between" -> JustifyContent.SPACE_BETWEEN;
            case "space-around" -> JustifyContent.SPACE_AROUND;
            default -> JustifyContent.START;
        };
    }

    private TextAlign parseTextAlign(String value) {
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "center" -> TextAlign.CENTER;
            case "end", "right" -> TextAlign.END;
            default -> TextAlign.START;
        };
    }
}
