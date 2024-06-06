package de.kostari.cloud.demo.ui;

import de.kostari.cloud.core.utils.Colors;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.window.Input;

public abstract class ButtonBase {

    private float x = 200;
    private float y = 200;
    private float width = 200;
    private float height = 40;

    private String buttonText;

    private boolean hovered;

    public ButtonBase(String text, float x, float y) {
        this.buttonText = text;
        this.x = x;
        this.y = y;
    }

    public void update() {
        this.hovered = Input.getMouseX() > x && Input.getMouseX() < x + width && Input.getMouseY() > y
                && Input.getMouseY() < y + height;

        if (hovered) {
            if (Input.mouseButtonPressed(0)) {
                onClick();
            }
        }
    }

    public void draw() {
        Render.color(Colors.BLUE);
        Render.drawRect(x, y, width, height);
        Render.drawText(buttonText, x + width / 2 - Render.getTextWidth(buttonText),
                y + height / 2 - Render.getTextHeight(buttonText));
    }

    public abstract void onClick();

}
