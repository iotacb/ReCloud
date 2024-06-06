package de.kostari.cloud.demo.editor;

import de.kostari.cloud.core.ui.elements.Button;
import de.kostari.cloud.core.utils.Atlas;
import de.kostari.cloud.core.utils.Colors;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.render.Texture;
import de.kostari.cloud.core.utils.render.font.EasyFont;

public class ImageButton extends Button {

    private Texture buttonTexture;
    private EasyFont font;

    public ImageButton(String path, String text, float x, float y, float width, float height) {
        super(text, x, y, width, height);
        this.font = new EasyFont(6);
        this.buttonTexture = Atlas.addTexture(path);
    }

    public ImageButton(String path, String text, float width, float height) {
        super(text, width, height);
        this.font = new EasyFont(6);
        this.buttonTexture = Atlas.addTexture(path);
    }

    @Override
    public void draw() {
        Render.color(Colors.from255(20, 20, 20, isHovered() ? 200 : 100));
        Render.drawRect(getX() - getWidth() / 2, getY() - getHeight() / 2, getWidth(), getHeight());
        Render.resetColor();
        Render.drawTextureCenter(getX(), getY() - 5, getWidth() * .8f, getHeight() * .8f,
                buttonTexture.getTextureId());
        font.drawText(getButtonText(), getX() - getWidth() / 2 + 5,
                getY() + getHeight() / 2 - font.getHeight(getButtonText()),
                Colors.WHITE);
    }

    @Override
    public void update() {
        super.update();
    }

}
