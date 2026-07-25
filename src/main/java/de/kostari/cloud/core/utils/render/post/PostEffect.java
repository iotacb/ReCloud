package de.kostari.cloud.core.utils.render.post;

public interface PostEffect {

    void init();

    void apply(PostProcessingContext context, int sourceTexture);

    void cleanup();

    boolean isEnabled();

    void setEnabled(boolean enabled);
}
