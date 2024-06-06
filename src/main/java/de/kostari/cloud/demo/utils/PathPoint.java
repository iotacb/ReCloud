package de.kostari.cloud.demo.utils;

import de.kostari.cloud.core.utils.Colors;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.window.Input;
import de.kostari.cloud.demo.editor.EditorOverlay;

public class PathPoint {

    private float x;
    private float y;

    private float pointRadius = 20;

    private boolean hovered;
    private boolean dragging;

    public PathPoint(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public boolean isHovered() {
        return hovered;
    }

    public void update() {
        hovered = Input.getMousePosition().distance(x, y) < pointRadius / 2;

        if (hovered && Input.mouseButtonDown(0)) {
            EditorOverlay.lastSelectedPoint = this;
            dragging = true;
        }

        if (dragging) {
            if (Input.mouseButtonReleased(0)) {
                dragging = false;
            }
            x = Input.getMouseX();
            y = Input.getMouseY();
        }
    }

    public void draw() {
        Render.color(Colors.BLUE);
        Render.drawRectCenter(x, y, 5, 5);

        if (hovered) {
            if (Input.mouseButtonDown(0)) {
                Render.color(.6f, 1, .6f, .6f);
            } else {
                Render.color(1, 1, 1, .6f);
            }
            Render.drawRectCenter(x, y, pointRadius, pointRadius);
        }
    }

    @Override
    public String toString() {
        return String.format("%s;%s", x, y);
    }

}
