package de.kostari.cloud.core.ui;

public class UIRect {

    public float x;
    public float y;
    public float width;
    public float height;

    public UIRect() {
        this(0, 0, 0, 0);
    }

    public UIRect(float x, float y, float width, float height) {
        set(x, y, width, height);
    }

    public UIRect(UIRect rect) {
        this(rect.x, rect.y, rect.width, rect.height);
    }

    UIRect set(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = Math.max(0, width);
        this.height = Math.max(0, height);
        return this;
    }

    public boolean contains(float pointX, float pointY) {
        return pointX >= x && pointX <= right() && pointY >= y && pointY <= bottom();
    }

    public float right() {
        return x + width;
    }

    public float bottom() {
        return y + height;
    }

    public UIRect copy() {
        return new UIRect(this);
    }

    public UIRect intersection(UIRect other) {
        if (other == null) {
            return copy();
        }
        float left = Math.max(x, other.x);
        float top = Math.max(y, other.y);
        float right = Math.min(right(), other.right());
        float bottom = Math.min(bottom(), other.bottom());
        return new UIRect(left, top, Math.max(0, right - left), Math.max(0, bottom - top));
    }
}
