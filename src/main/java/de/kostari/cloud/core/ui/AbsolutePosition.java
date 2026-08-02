package de.kostari.cloud.core.ui;

public final class AbsolutePosition {

    private final Runnable changed;
    private float left = Float.NaN;
    private float right = Float.NaN;
    private float top = Float.NaN;
    private float bottom = Float.NaN;
    private float width = Float.NaN;
    private float height = Float.NaN;
    private float anchorX;
    private float anchorY;

    AbsolutePosition(Runnable changed) {
        this.changed = changed;
    }

    public AbsolutePosition left(float value) {
        left = value;
        changed.run();
        return this;
    }

    public AbsolutePosition right(float value) {
        right = value;
        changed.run();
        return this;
    }

    public AbsolutePosition top(float value) {
        top = value;
        changed.run();
        return this;
    }

    public AbsolutePosition bottom(float value) {
        bottom = value;
        changed.run();
        return this;
    }

    public AbsolutePosition width(float value) {
        width = Math.max(0, value);
        changed.run();
        return this;
    }

    public AbsolutePosition height(float value) {
        height = Math.max(0, value);
        changed.run();
        return this;
    }

    public AbsolutePosition size(float width, float height) {
        return width(width).height(height);
    }

    public AbsolutePosition anchor(float x, float y) {
        anchorX = Math.clamp(x, 0, 1);
        anchorY = Math.clamp(y, 0, 1);
        changed.run();
        return this;
    }

    public AbsolutePosition center() {
        return anchor(0.5f, 0.5f);
    }

    public AbsolutePosition clearHorizontal() {
        left = Float.NaN;
        right = Float.NaN;
        changed.run();
        return this;
    }

    public AbsolutePosition clearVertical() {
        top = Float.NaN;
        bottom = Float.NaN;
        changed.run();
        return this;
    }

    boolean hasLeft() {
        return Float.isFinite(left);
    }

    boolean hasRight() {
        return Float.isFinite(right);
    }

    boolean hasTop() {
        return Float.isFinite(top);
    }

    boolean hasBottom() {
        return Float.isFinite(bottom);
    }

    boolean hasWidth() {
        return Float.isFinite(width);
    }

    boolean hasHeight() {
        return Float.isFinite(height);
    }

    float left() {
        return left;
    }

    float right() {
        return right;
    }

    float top() {
        return top;
    }

    float bottom() {
        return bottom;
    }

    float width() {
        return width;
    }

    float height() {
        return height;
    }

    float anchorX() {
        return anchorX;
    }

    float anchorY() {
        return anchorY;
    }
}
