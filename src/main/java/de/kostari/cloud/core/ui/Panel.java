package de.kostari.cloud.core.ui;

import de.kostari.cloud.core.utils.types.Color4f;

public class Panel extends UIElement {

    private Drawable background = Drawables.none();
    private Drawable border = Drawables.none();
    private float borderWidth;
    private Color4f borderColor = new Color4f(0, 0, 0, 0);

    public Panel background(Color4f color) {
        return background(Drawables.solid(color));
    }

    public Panel background(Drawable drawable) {
        background = drawable == null ? Drawables.none() : drawable;
        invalidatePaint();
        return this;
    }

    public Panel border(float width, Color4f color) {
        borderWidth = Math.max(0, width);
        borderColor = color == null ? new Color4f(0, 0, 0, 0) : color;
        border = Drawables.border(borderWidth, borderColor);
        invalidatePaint();
        return this;
    }

    public Panel borderColor(Color4f color) {
        return border(borderWidth, color);
    }

    public Panel border(Drawable drawable) {
        border = drawable == null ? Drawables.none() : drawable;
        borderWidth = 0;
        invalidatePaint();
        return this;
    }

    public Drawable background() {
        return background;
    }

    @Override
    protected void drawSelf() {
        background.draw(renderBounds(), renderOpacity());
        border.draw(renderBounds(), renderOpacity());
    }
}
