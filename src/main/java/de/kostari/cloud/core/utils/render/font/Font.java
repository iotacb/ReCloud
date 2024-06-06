package de.kostari.cloud.core.utils.render.font;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

import de.kostari.cloud.core.utils.Colors;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.render.Texture;
import de.kostari.cloud.core.utils.types.Color4f;

public class Font {

    private java.awt.Font font;
    private boolean antiAliasing;

    private Map<Character, FontGlyph> glyphs;

    private Texture texture;

    private int fontHeight;

    public Font() {
        this.glyphs = new HashMap<>();
        this.font = new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.BOLD, 16);
        this.antiAliasing = true;
    }

    public Font(java.awt.Font font, boolean antiAliasing) {
        this.glyphs = new HashMap<>();
        this.font = font;
        this.antiAliasing = antiAliasing;
    }

    public void create() {
        try {
            this.texture = createFontTexture(font, antiAliasing);
        } catch (Exception e) {
            e.printStackTrace();
            // Handle texture creation failure
        }
    }

    private BufferedImage imageFromChar(java.awt.Font font, char character, boolean antiAliasing) {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2d = image.createGraphics();
        if (antiAliasing) {
            graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        graphics2d.setFont(font);
        FontMetrics fontMetrics = graphics2d.getFontMetrics();
        graphics2d.dispose();

        int charWidth = fontMetrics.charWidth(character);
        int charHeight = fontMetrics.getHeight();

        if (charWidth == 0) {
            return null;
        }

        image = new BufferedImage(charWidth, charHeight, BufferedImage.TYPE_INT_ARGB);
        graphics2d = image.createGraphics();
        if (antiAliasing) {
            graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        graphics2d.setFont(font);
        graphics2d.setPaint(java.awt.Color.WHITE);
        graphics2d.drawString(String.valueOf(character), 0, fontMetrics.getAscent());
        graphics2d.dispose();
        return image;
    }

    private Texture createFontTexture(java.awt.Font font, boolean antiAliasing) throws Exception {
        int glyphWidth = 0;
        int glyphHeight = 0;

        for (int i = 32; i < 256; i++) {
            if (i == 127) {
                continue;
            }

            char character = (char) i;
            BufferedImage charImage = imageFromChar(font, character, antiAliasing);
            if (charImage == null) {
                continue;
            }

            glyphWidth += charImage.getWidth();
            glyphHeight = Math.max(glyphHeight, charImage.getHeight());
        }

        this.fontHeight = glyphHeight;

        BufferedImage image = new BufferedImage(glyphWidth, glyphHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2d = image.createGraphics();

        int x = 0;

        for (int i = 32; i < 256; i++) {
            if (i == 127) {
                continue;
            }

            char character = (char) i;
            BufferedImage charImage = imageFromChar(font, character, antiAliasing);
            if (charImage == null) {
                continue;
            }

            int characterWidth = charImage.getWidth();
            int characterHeight = charImage.getHeight();

            FontGlyph glyph = new FontGlyph(characterWidth, characterHeight, x,
                    charImage.getHeight() - characterHeight);

            graphics2d.drawImage(charImage, x, 0, null);
            x += glyph.width;
            glyphs.put(character, glyph);
        }

        AffineTransform transform = AffineTransform.getScaleInstance(1f, -1f);
        transform.translate(0, -image.getHeight());
        AffineTransformOp operation = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        image = operation.filter(image, null);

        int width = image.getWidth();
        int height = image.getHeight();

        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);

        ByteBuffer buffer = MemoryUtil.memAlloc(width * height * 4);
        try {
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    int pixel = pixels[i * width + j];
                    buffer.put((byte) ((pixel >> 16) & 0xFF));
                    buffer.put((byte) ((pixel >> 8) & 0xFF));
                    buffer.put((byte) (pixel & 0xFF));
                    buffer.put((byte) ((pixel >> 24) & 0xFF));
                }
            }
            buffer.flip();
            Texture fontTexture = Texture.fromBuffer(width, height, buffer);
            return fontTexture;
        } finally {
            MemoryUtil.memFree(buffer);
        }
    }

    public int getWidth(String text) {
        int width = 0;
        int lineWidth = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                width = Math.max(width, lineWidth);
                lineWidth = 0;
                continue;
            }
            if (c == '\r') {
                continue;
            }
            FontGlyph g = glyphs.get(c);
            lineWidth += g.width;
        }
        width = Math.max(width, lineWidth);
        return width;
    }

    public int getHeight(String text) {
        int height = 0;
        int lineHeight = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                height += lineHeight;
                lineHeight = 0;
                continue;
            }
            if (c == '\r') {
                continue;
            }
            FontGlyph g = glyphs.get(c);
            lineHeight = Math.max(lineHeight, g.height);
        }
        height += lineHeight;
        return height;
    }

    public int getHeight() {
        return fontHeight;
    }

    public void drawText(String text, float x, float y, Color4f color) {
        int textHeight = getHeight(text);

        float drawX = x;
        float drawY = y;

        if (textHeight > fontHeight) {
            drawY += textHeight - fontHeight;
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getTextureId());
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            if (character == '\n') {
                drawY += fontHeight;
                drawX = x;
                continue;
            }
            if (character == '\r') {
                continue;
            }

            FontGlyph glyph = glyphs.get(character);
            drawTextureRegion(texture, drawX, drawY - (text.contains("\n") ? glyph.height : 0), glyph.x, glyph.y,
                    glyph.width, glyph.height, color);
            drawX += glyph.width;
        }
    }

    private void drawTextureRegion(Texture texture, float x, float y, float regionX, float regionY, float regionWidth,
            float regionHeight, Color4f color) {
        float x2 = x + regionWidth;
        float y2 = y + regionHeight;

        float s1 = regionX / texture.getWidth();
        float t1 = regionY / texture.getHeight();
        float s2 = (regionX + regionWidth) / texture.getWidth();
        float t2 = (regionY + regionHeight) / texture.getHeight();

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_LIGHTING);
        Render.color(color);
        GL11.glBegin(GL11.GL_QUADS);
        {
            GL11.glTexCoord2f(s1, t1);
            GL11.glVertex2f(x, y2);
            GL11.glTexCoord2f(s2, t1);
            GL11.glVertex2f(x2, y2);
            GL11.glTexCoord2f(s2, t2);
            GL11.glVertex2f(x2, y);
            GL11.glTexCoord2f(s1, t2);
            GL11.glVertex2f(x, y);
        }
        GL11.glEnd();
        Render.color(Colors.WHITE);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }
}
