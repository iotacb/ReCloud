package de.kostari.cloud.core.ui.elements;

import de.kostari.cloud.core.ui.UIElement;
import de.kostari.cloud.core.utils.Colors;
import de.kostari.cloud.core.utils.types.Color4f;

public class Panel extends UIElement {

    private Color4f panelColor = Colors.WHITE;

    @Override
    public void draw() {
        // Render.color(panelColor);
        // Render.drawRect(getX(), getY(), getWidth(), getHeight());
    }

    @Override
    public void update() {
    }

    public void setPanelColor(Color4f panelColor) {
        this.panelColor = panelColor;
    }

    public Color4f getPanelColor() {
        return panelColor;
    }

}
