package de.kostari.cloud.core.utils.render.font;

import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.stb.STBTruetype.*;

public class Font {
    private static final int BITMAP_W = 512;
    private static final int BITMAP_H = 512;

    private int fontHeight;
    private ByteBuffer fontBuffer;
    private STBTTBakedChar.Buffer charData;
    private ByteBuffer bitmap;

    private int textureId;

    public Font(String fontPath, int fontHeight) {
        this.fontHeight = fontHeight;
        try {
            byte[] fontBytes = Files.readAllBytes(Paths.get(fontPath));
            fontBuffer = MemoryUtil.memAlloc(fontBytes.length);
            fontBuffer.put(fontBytes).flip();

            charData = STBTTBakedChar.malloc(96); // ASCII 32..126 is 95 glyphs
            ByteBuffer tempBitmap = MemoryUtil.memAlloc(BITMAP_W * BITMAP_H);

            stbtt_BakeFontBitmap(fontBuffer, fontHeight, tempBitmap, BITMAP_W, BITMAP_H, 32, charData);

            bitmap = MemoryUtil.memAlloc(BITMAP_W * BITMAP_H * 4);
            for (int i = 0; i < BITMAP_W * BITMAP_H; i++) {
                byte value = tempBitmap.get(i);
                bitmap.put((byte) 255); // R
                bitmap.put((byte) 255); // G
                bitmap.put((byte) 255); // B
                bitmap.put(value); // A
            }
            bitmap.flip();

            MemoryUtil.memFree(tempBitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Font load() {
        // Generate texture ID
        this.textureId = GL11.glGenTextures();
        // Bind the texture
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

        // Set texture parameters
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        // Upload the texture data
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, 512, 512, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE,
                bitmap);

        // Unbind the texture
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        return this;
    }

    public ByteBuffer getBitmap() {
        return bitmap;
    }

    public STBTTBakedChar.Buffer getCharData() {
        return charData;
    }

    public int getFontHeight() {
        return fontHeight;
    }

    public int getTextureId() {
        return textureId;
    }
}