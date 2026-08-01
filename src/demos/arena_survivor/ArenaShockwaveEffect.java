package arena_survivor;

import de.kostari.cloud.core.utils.render.post.PostProcessingContext;
import de.kostari.cloud.core.utils.render.post.ShaderPostEffect;
import de.kostari.cloud.core.utils.types.Color4f;

final class ArenaShockwaveEffect extends ShaderPostEffect {
    private float centerX = 0.5f;
    private float centerY = 0.5f;
    private float progress = 1;
    private float duration = 0.6f;
    private float strength = 1;
    private float damage;
    private Color4f tint = Palette.CYAN;

    ArenaShockwaveEffect() {
        setEnabled(false);
    }

    @Override
    protected String fragmentShaderPath() {
        return "./demo_assets/arena_survivor/shaders/aether-shockwave.glsl";
    }

    @Override
    protected void createUniforms() {
        shader.createUniforms("resolution", "center", "progress", "strength", "damage", "tint");
    }

    @Override
    protected void updateUniforms(PostProcessingContext context) {
        shader.setUniform("resolution", (float) context.getWidth(), (float) context.getHeight());
        shader.setUniform("center", centerX, centerY);
        shader.setUniform("progress", progress);
        shader.setUniform("strength", strength);
        shader.setUniform("damage", damage);
        shader.setUniform("tint", tint.r, tint.g, tint.b);
    }

    void trigger(float screenX, float screenY, float viewportWidth, float viewportHeight,
            Color4f tint, float strength, float duration, boolean damage) {
        centerX = Math.clamp(screenX / Math.max(1, viewportWidth), 0, 1);
        centerY = Math.clamp(1 - screenY / Math.max(1, viewportHeight), 0, 1);
        this.tint = tint;
        this.strength = strength;
        this.duration = Math.max(0.1f, duration);
        this.damage = damage ? 1 : 0;
        progress = 0;
        setEnabled(true);
    }

    void update(float delta) {
        if (!isEnabled()) {
            return;
        }
        progress += Math.max(0, delta) / duration;
        if (progress >= 1) {
            progress = 1;
            setEnabled(false);
        }
    }
}
