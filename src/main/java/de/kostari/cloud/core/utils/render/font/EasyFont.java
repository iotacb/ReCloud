package de.kostari.cloud.core.utils.render.font;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBEasyFont;

import de.kostari.cloud.core.utils.Colors;
import de.kostari.cloud.core.utils.types.Color4f;

public class EasyFont {

    int textSize = 16;

    public EasyFont(int textSize) {
        this.textSize = textSize;
    }

    /**
     * Draw text on the screen using the STB Easy Font.
     * 
     * @param text
     * @param x
     * @param y
     * @param color
     */
    public void drawText(String text, float x, float y, Color4f color) {
        if (color == null)
            color = Colors.WHITE;

        ByteBuffer buffer = BufferUtils.createByteBuffer(text.length() * 300);

        int quads = STBEasyFont.stb_easy_font_print(0, 0, text, null, buffer);

        double sizeBuffer = textSize * .18;

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glTranslatef(x, y, 0);
        GL11.glColor4f(color.r, color.g, color.b, color.a);
        GL11.glScaled(sizeBuffer, sizeBuffer, 1);
        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glVertexPointer(GL11.GL_POINT_BIT, GL11.GL_FLOAT, GL11.GL_POLYGON_STIPPLE_BIT, buffer);
        GL11.glDrawArrays(GL11.GL_QUADS, 0, quads * 4);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    public int getWidth(String text) {
        return (int) (STBEasyFont.stb_easy_font_width(text) * 1.6f);
    }

    public int getHeight(String text) {
        return STBEasyFont.stb_easy_font_height(text);
    }

}
