package de.kostari.cloud.core.utils.render.font;

import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
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

    private float ascent;
    private float descent;
    private float lineGap;
    private float lineHeight;

    private int textureId;

    public Font(String fontPath, int fontHeight) {
        this.fontHeight = fontHeight;
        try {
            byte[] fontBytes = Files.readAllBytes(Paths.get(fontPath));
            fontBuffer = MemoryUtil.memAlloc(fontBytes.length);
            fontBuffer.put(fontBytes).flip();

            readVerticalMetrics();

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

    private void readVerticalMetrics() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            STBTTFontinfo fontInfo = STBTTFontinfo.malloc(stack);
            int fontOffset = stbtt_GetFontOffsetForIndex(fontBuffer, 0);
            if (fontOffset < 0 || !stbtt_InitFont(fontInfo, fontBuffer, fontOffset)) {
                useFallbackVerticalMetrics();
                return;
            }

            IntBuffer rawAscent = stack.mallocInt(1);
            IntBuffer rawDescent = stack.mallocInt(1);
            IntBuffer rawLineGap = stack.mallocInt(1);
            stbtt_GetFontVMetrics(fontInfo, rawAscent, rawDescent, rawLineGap);

            float scale = stbtt_ScaleForPixelHeight(fontInfo, fontHeight);
            ascent = rawAscent.get(0) * scale;
            descent = -rawDescent.get(0) * scale;
            lineGap = Math.max(0, rawLineGap.get(0) * scale);
            lineHeight = Math.max(1, ascent + descent + lineGap);
        }
    }

    private void useFallbackVerticalMetrics() {
        lineHeight = Math.max(1, fontHeight);
        ascent = lineHeight * 0.8f;
        descent = lineHeight - ascent;
        lineGap = 0;
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

    /** Distance from the top of a line box to its baseline, in unscaled pixels. */
    public float getAscent() {
        return ascent;
    }

    /** Positive distance below the baseline, in unscaled pixels. */
    public float getDescent() {
        return descent;
    }

    public float getLineGap() {
        return lineGap;
    }

    /** Typographic line box height: ascent + descent + line gap. */
    public float getLineHeight() {
        return lineHeight;
    }

    public int getTextureId() {
        return textureId;
    }
}
