package de.kostari.cloud.core.utils.render;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBImage;

import de.kostari.cloud.Cloud;
import de.kostari.cloud.core.utils.Atlas;
import de.kostari.cloud.core.utils.math.Vector2f;

import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL11.glTexImage2D;

import static org.lwjgl.opengl.GL11.GL_RGB;
import static org.lwjgl.opengl.GL11.GL_RGB8;
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

    public Texture(String filePath) {
        this.filePath = filePath;
        Atlas.addTexture(this);
    }

    public Texture() {
        this.textureId = glGenTextures();
    }

    /**
     * Create a texture from a buffer
     * 
     * @param width
     * @param height
     * @param buffer
     * @return
     */
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

    /**
     * Load the texture
     * 
     * @return
     */
    public Texture load() {
        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_R, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filtering);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filtering);

        IntBuffer width = BufferUtils.createIntBuffer(1);
        IntBuffer height = BufferUtils.createIntBuffer(1);
        IntBuffer channels = BufferUtils.createIntBuffer(1);

        this.imageData = STBImage.stbi_load(filePath, width, height, channels, 0);

        if (imageData == null) {
            // THROW ERROR
        }

        this.width = width.get(0);
        this.height = height.get(0);

        if (channels.get(0) == 3) {
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB8, width.get(0), height.get(0), 0, GL_RGB, GL_UNSIGNED_BYTE,
                    imageData);
        } else if (channels.get(0) == 4) {
            Cloud.print("4 channels");
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width.get(0), height.get(0), 0, GL_RGBA, GL_UNSIGNED_BYTE,
                    imageData);
        } else {
            // THROW ERROR
        }

        STBImage.stbi_image_free(imageData);
        return this;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public Vector2f getSize() {
        return new Vector2f(width, height);
    }

    public String getFilePath() {
        return filePath;
    }

    public int getTextureId() {
        return textureId;
    }

    public ByteBuffer getImageData() {
        return imageData;
    }

    public void setFiltering(int filtering) {
        this.filtering = filtering;
    }

}
