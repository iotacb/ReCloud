package de.kostari.cloud.core.utils.render;

import org.lwjgl.opengl.GL11;

import de.kostari.cloud.core.utils.math.Vector2f;
import de.kostari.cloud.core.utils.render.font.EasyFont;
import de.kostari.cloud.core.utils.types.Color4f;

public class Render {

    private static EasyFont font = new EasyFont(18);

    /**
     * Sets the color of the next drawn object.
     * 
     * @param r The red value of the color.
     * @param g The green value of the color.
     * @param b The blue value of the color.
     * @param a The alpha value of the color.
     */
    public static void color(float r, float g, float b, float a) {
        GL11.glColor4f(r, g, b, a);
    }

    /**
     * Sets the color of the next drawn object.
     * Alpha will be set to 1.
     * 
     * @param r The red value of the color.
     * @param g The green value of the color.
     * @param b The blue value of the color.
     */
    public static void color(float r, float g, float b) {
        color(r, g, b, 1);
    }

    /**
     * Sets the color of the next drawn object.
     * Uses colors values from 0 to 255.
     * And converts them to values from 0 to 1.
     * 
     * @param r The red value of the color.
     * @param g The green value of the color.
     * @param b The blue value of the color.
     * @param a The alpha value of the color.
     */
    public static void color255(float r, float g, float b, float a) {
        color(r / 255f, g / 255f, b / 255f, a / 255f);
    }

    /**
     * Sets the color of the next drawn object.
     * Uses colors values from 0 to 255.
     * And converts them to values from 0 to 1.
     * 
     * @param r The red value of the color.
     * @param g The green value of the color.
     * @param b The blue value of the color.
     */
    public static void color255(float r, float g, float b) {
        color255(r, g, b);
    }

    /**
     * Sets the color of the next drawn object.
     * 
     * @see Color4f
     * 
     * @param color The color to set.
     */
    public static void color(Color4f color) {
        color(color.r, color.g, color.b, color.a);
    }

    public static void resetColor() {
        color(1, 1, 1, 1);
    }

    /**
     * Draws a rectangle to the screen at the specified position and size.
     * 
     * @param x      The x position of the rectangle.
     * @param y      The y position of the rectangle.
     * @param width  The width of the rectangle.
     * @param height The height of the rectangle.
     */
    public static void drawRect(float x, float y, float width, float height) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x + width, y);
        GL11.glVertex2f(x + width, y + height);
        GL11.glVertex2f(x, y + height);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    public static void drawRect(Vector2f position, Vector2f size) {
        drawRect(position.x, position.y, size.x, size.y);
    }

    public static void drawRect(Vector2f position, float width, float height) {
        drawRect(position.x, position.y, width, height);
    }

    public static void drawRectCenter(float x, float y, float width, float height) {
        drawRect(x - width / 2, y - height / 2, width, height);
    }

    public static void drawRectCenter(Vector2f position, Vector2f size) {
        drawRectCenter(position.x, position.y, size.x, size.y);
    }

    public static void drawRectCenter(Vector2f position, float width, float height) {
        drawRectCenter(position.x, position.y, width, height);
    }

    public static void drawTexture(float x, float y, float width, float height, int textureId) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2d(0, 0);
        GL11.glVertex2d(x, y);
        GL11.glTexCoord2d(1, 0);
        GL11.glVertex2d(x + width, y);
        GL11.glTexCoord2d(1, 1);
        GL11.glVertex2d(x + width, y + height);
        GL11.glTexCoord2d(0, 1);
        GL11.glVertex2d(x, y + height);
        GL11.glEnd();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    public static void drawTextureCenter(float x, float y, float width, float height, int textureId) {
        drawTexture(x - width / 2, y - height / 2, width, height, textureId);
    }

    public static void drawLine(float x1, float y1, float x2, float y2) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBegin(GL11.GL_LINES);
        {
            GL11.glVertex2f(x1, y1);
            GL11.glVertex2f(x2, y2);
        }
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    public static void drawText(String text, float x, float y) {
        font.drawText(text, x, y, null);
    }

    public static void drawTextCenter(String text, float x, float y) {
        font.drawText(text, x - getTextWidth(text), y - getTextHeight(text), null);
    }

    public static int getTextWidth(String text) {
        return font.getWidth(text);
    }

    public static int getTextHeight(String text) {
        return font.getHeight(text);
    }

}
