package de.kostari.cloud.core.ui;

import de.kostari.cloud.core.utils.Colors;
import de.kostari.cloud.core.window.Input;

public abstract class UIElement {

    private float x;
    private float y;

    private float width;
    private float height;

    private boolean draggable;
    private boolean dragging;

    private int layer;

    private UIElement parent;

    public abstract void draw();

    public abstract void update();

    public boolean isHovered() {
        return Input.getMouseX() > x - width / 2 && Input.getMouseX() < x + width / 2
                && Input.getMouseY() > y - height / 2
                && Input.getMouseY() < y + height / 2;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void setDraggable(boolean draggable) {
        this.draggable = draggable;
    }

    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    public boolean isDraggable() {
        return draggable;
    }

    public boolean isDragging() {
        return dragging;
    }

    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setParent(UIElement parent) {
        this.parent = parent;
    }

    public UIElement getParent() {
        return parent;
    }

    public void parentLock() {
        if (parent != null)
            setPosition(parent.getX(), parent.getY());
    }

    public void drawBounds() {
        // Render.color(isHovered() ? Colors.GREEN : Colors.BLUE);
        // Render.drawLine(x - width / 2, y - height / 2, x + width / 2, y - height /
        // 2);
        // Render.drawLine(x + width / 2, y - height / 2, x + width / 2, y + height /
        // 2);
        // Render.drawLine(x + width / 2, y + height / 2, x - width / 2, y + height /
        // 2);
        // Render.drawLine(x - width / 2, y + height / 2, x - width / 2, y - height /
        // 2);
        // Render.resetColor();
    }

}
