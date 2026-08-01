package de.kostari.cloud.core.utils.render;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBImage;

import de.kostari.cloud.core.utils.math.Vector2;

import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL11.glTexImage2D;

import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_RGBA8;
import static org.lwjgl.opengl.GL11.GL_REPEAT;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;

import static org.lwjgl.opengl.GL12.GL_TEXTURE_WRAP_R;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;

import static org.lwjgl.opengl.GL13.GL_CLAMP_TO_BORDER;

public class Texture {

    private float width;
    private float height;
    private int textureId;
    private String filePath;
    private ByteBuffer imageData;
    private int filtering = GL_NEAREST;
    private float u0;
    private float v0;
    private float u1 = 1;
    private float v1 = 1;

    public Texture(String filePath) {
        this.filePath = filePath;
    }

    public Texture() {
        this.textureId = glGenTextures();
    }

    /**
     * Creates a view into an already loaded texture.
     *
     * The returned texture shares the source texture's OpenGL id and only changes
     * its size and UV coordinates. This keeps sprite-sheet cells cheap: no image
     * data is copied and no additional GPU texture is allocated.
     */
    static Texture region(Texture source, float x, float y, float width, float height) {
        if (source == null || source.getTextureId() <= 0) {
            throw new IllegalArgumentException("Source texture must be loaded before creating a region");
        }
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Texture region width and height must be greater than zero");
        }
        if (x < 0 || y < 0 || x + width > source.getWidth() || y + height > source.getHeight()) {
            throw new IndexOutOfBoundsException("Texture region lies outside the source texture");
        }

        Texture region = new Texture(source);
        region.width = width;
        region.height = height;
        region.textureId = source.textureId;
        region.filePath = source.filePath;

        float sourceWidth = source.getWidth();
        float sourceHeight = source.getHeight();
        region.u0 = source.u0 + (x / sourceWidth) * (source.u1 - source.u0);
        region.v0 = source.v0 + (y / sourceHeight) * (source.v1 - source.v0);
        region.u1 = source.u0 + ((x + width) / sourceWidth) * (source.u1 - source.u0);
        region.v1 = source.v0 + ((y + height) / sourceHeight) * (source.v1 - source.v0);
        return region;
    }

    private Texture(Texture source) {
        this.filtering = source.filtering;
    }

    public static Texture fromBuffer(int width, int height, ByteBuffer buffer) {
        Texture texture = new Texture();
        texture.width = width;
        texture.height = height;

        GL11.glBindTexture(GL_TEXTURE_2D, texture.getTextureId());

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        return texture;
    }

    public Texture load() {
        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_R, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filtering);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filtering);

        IntBuffer widthBuffer = BufferUtils.createIntBuffer(1);
        IntBuffer heightBuffer = BufferUtils.createIntBuffer(1);
        IntBuffer channelsBuffer = BufferUtils.createIntBuffer(1);

        this.imageData = STBImage.stbi_load(filePath, widthBuffer, heightBuffer, channelsBuffer, 4);

        if (imageData == null) {
            throw new RuntimeException("Failed to load texture file: " + filePath);
        }

        this.width = widthBuffer.get(0);
        this.height = heightBuffer.get(0);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, (int) width, (int) height, 0, GL_RGBA, GL_UNSIGNED_BYTE, imageData);

        STBImage.stbi_image_free(imageData);
        return this;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public Vector2 getSize() {
        return new Vector2(width, height);
    }

    public String getFilePath() {
        return filePath;
    }

    public int getTextureId() {
        return textureId;
    }

    public float getU0() {
        return u0;
    }

    public float getV0() {
        return v0;
    }

    public float getU1() {
        return u1;
    }

    public float getV1() {
        return v1;
    }

    public ByteBuffer getImageData() {
        return imageData;
    }

    public void setFiltering(int filtering) {
        this.filtering = filtering;
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, textureId);
    }
}
