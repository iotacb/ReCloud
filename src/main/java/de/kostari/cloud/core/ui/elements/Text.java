package de.kostari.cloud.core.ui.elements;

import de.kostari.cloud.core.ui.UIElement;

public class Text extends UIElement {

    private String text;

    public Text(String text, float x, float y) {
        this.text = text;
        setX(x);
        setY(y);
        // setWidth(Render.getTextWidth(text) * 2);
        // setHeight(Render.getTextHeight(text) * 2);
    }

    @Override
    public void draw() {
        drawBounds();
        // Render.drawText(text, getX(), getY());
    }

    @Override
    public void update() {
    }

}
