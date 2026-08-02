package de.kostari.cloud.core.ui;

public final class Layout {

    static final float AUTO = -1f;

    private final UIElement owner;
    private float width = AUTO;
    private float height = AUTO;
    private float minWidth;
    private float minHeight;
    private float maxWidth = Float.POSITIVE_INFINITY;
    private float maxHeight = Float.POSITIVE_INFINITY;
    private float grow;
    private float shrink;
    private Insets padding = Insets.NONE;
    private Insets margin = Insets.NONE;

    Layout(UIElement owner) {
        this.owner = owner;
    }

    public Layout width(float value) {
        width = Math.max(0, value);
        changed();
        return this;
    }

    public Layout autoWidth() {
        width = AUTO;
        changed();
        return this;
    }

    public Layout height(float value) {
        height = Math.max(0, value);
        changed();
        return this;
    }

    public Layout autoHeight() {
        height = AUTO;
        changed();
        return this;
    }

    public Layout size(float width, float height) {
        return width(width).height(height);
    }

    public Layout minWidth(float value) {
        minWidth = Math.max(0, value);
        changed();
        return this;
    }

    public Layout minHeight(float value) {
        minHeight = Math.max(0, value);
        changed();
        return this;
    }

    public Layout minSize(float width, float height) {
        return minWidth(width).minHeight(height);
    }

    public Layout maxWidth(float value) {
        maxWidth = Math.max(0, value);
        changed();
        return this;
    }

    public Layout maxHeight(float value) {
        maxHeight = Math.max(0, value);
        changed();
        return this;
    }

    public Layout maxSize(float width, float height) {
        return maxWidth(width).maxHeight(height);
    }

    public Layout grow(float value) {
        grow = Math.max(0, value);
        changed();
        return this;
    }

    public Layout shrink(float value) {
        shrink = Math.max(0, value);
        changed();
        return this;
    }

    public Layout padding(float value) {
        return padding(Insets.all(value));
    }

    public Layout padding(float vertical, float horizontal) {
        return padding(Insets.symmetric(vertical, horizontal));
    }

    public Layout padding(float top, float right, float bottom, float left) {
        return padding(new Insets(top, right, bottom, left));
    }

    public Layout padding(Insets value) {
        padding = value == null ? Insets.NONE : value;
        changed();
        return this;
    }

    public Layout margin(float value) {
        return margin(Insets.all(value));
    }

    public Layout margin(float vertical, float horizontal) {
        return margin(Insets.symmetric(vertical, horizontal));
    }

    public Layout margin(float top, float right, float bottom, float left) {
        return margin(new Insets(top, right, bottom, left));
    }

    public Layout margin(Insets value) {
        margin = value == null ? Insets.NONE : value;
        changed();
        return this;
    }

    public boolean hasWidth() {
        return width >= 0;
    }

    public boolean hasHeight() {
        return height >= 0;
    }

    public float width() {
        return width;
    }

    public float height() {
        return height;
    }

    public float minWidth() {
        return minWidth;
    }

    public float minHeight() {
        return minHeight;
    }

    public float maxWidth() {
        return maxWidth;
    }

    public float maxHeight() {
        return maxHeight;
    }

    public float grow() {
        return grow;
    }

    public float shrink() {
        return shrink;
    }

    public Insets padding() {
        return padding;
    }

    public Insets margin() {
        return margin;
    }

    float clampWidth(float value) {
        return Math.max(minWidth, Math.min(maxWidth, value));
    }

    float clampHeight(float value) {
        return Math.max(minHeight, Math.min(maxHeight, value));
    }

    private void changed() {
        owner.invalidateLayout();
    }
}
