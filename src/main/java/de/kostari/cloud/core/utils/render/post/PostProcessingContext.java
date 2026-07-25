package de.kostari.cloud.core.utils.render.post;

import de.kostari.cloud.core.utils.render.Shader;

public class PostProcessingContext {

    private final PostProcessing postProcessing;

    PostProcessingContext(PostProcessing postProcessing) {
        this.postProcessing = postProcessing;
    }

    public int getWidth() {
        return postProcessing.getWidth();
    }

    public int getHeight() {
        return postProcessing.getHeight();
    }

    public float getTime() {
        return postProcessing.getTime();
    }

    public void drawFullscreen(int sourceTexture, Shader shader) {
        postProcessing.drawFullscreen(sourceTexture, shader);
    }
}
