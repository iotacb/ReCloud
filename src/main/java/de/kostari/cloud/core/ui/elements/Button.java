package de.kostari.cloud.core.ui.elements;

import de.kostari.cloud.core.ui.UIElement;
import de.kostari.cloud.core.ui.listener.IClickListener;
import de.kostari.cloud.core.utils.Colors;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.types.Color4f;
import de.kostari.cloud.core.window.Input;

public class Button extends UIElement {

    private Color4f buttonColor;
    private Color4f hoverButtonColor;

    private String buttonText;

    private IClickListener clickListener;

    public Button(String text, float x, float y, float width, float height) {
        this.buttonText = text;
        this.buttonColor = Colors.from255(20, 20, 20, 200);
        this.hoverButtonColor = Colors.from255(30, 30, 100, 200);
        setX(x);
        setY(y);
        setWidth(width);
        setHeight(height);
    }

    public Button(String text, float width, float height) {
        this.buttonText = text;
        this.buttonColor = Colors.from255(20, 20, 20, 200);
        this.hoverButtonColor = Colors.from255(30, 30, 100, 200);
        setWidth(width);
        setHeight(height);
    }

    @Override
    public void draw() {
        Render.color(isHovered() ? hoverButtonColor : buttonColor);
        Render.drawRectCenter(getX(), getY(), getWidth(), getHeight());
        Render.drawTextCenter(buttonText, getX(),
                getY());
        Render.resetColor();
    }

    @Override
    public void update() {
        if (isHovered() && Input.mouseButtonPressed(0)) {
            clickListener.click();
        }
    }

    public void setClickListener(IClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public String getButtonText() {
        return buttonText;
    }

    public void setButtonText(String buttonText) {
        this.buttonText = buttonText;
    }

}
