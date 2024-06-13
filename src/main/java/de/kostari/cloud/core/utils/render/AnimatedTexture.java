package de.kostari.cloud.core.utils.render;

public class AnimatedTexture extends Texture {

    private Texture[] frames;
    private int currentFrame;

    private float frameTime;
    private float time;

    public AnimatedTexture(String[] paths, float frameTime) {
        super();
        this.frames = new Texture[paths.length];
        for (int i = 0; i < paths.length; i++) {
            frames[i] = new Texture(paths[i]);
        }
        this.frameTime = frameTime;
    }

    @Override
    public AnimatedTexture load() {
        for (Texture frame : frames) {
            frame.load();
        }
        return this;
    }

    @Override
    public int getTextureId() {
        return frames[currentFrame].getTextureId();
    }

    public void update() {
        time++;
        if (time >= frameTime) {
            time = 0;
            currentFrame++;
            if (currentFrame >= frames.length) {
                currentFrame = 0;
            }
        }
    }

}
