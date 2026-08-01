package arena_survivor;

import de.kostari.cloud.core.utils.render.post.PostProcessingContext;
import de.kostari.cloud.core.utils.render.post.ShaderPostEffect;

final class ArenaKineticEffect extends ShaderPostEffect {
    private static final float DASH = 0;
    private static final float DAMAGE = 1;
    private static final float RIFT = 2;
    private static final float WAVE = 3;

    private float centerX = 0.5f;
    private float centerY = 0.5f;
    private float directionX = 1;
    private float directionY;
    private float progress = 1;
    private float duration = 0.5f;
    private float intensity;
    private float mode;

    ArenaKineticEffect() {
        setEnabled(false);
    }

    @Override
    protected String fragmentShaderPath() {
        return "./demo_assets/arena_survivor/shaders/aether-kinetic.glsl";
    }

    @Override
    protected void createUniforms() {
        shader.createUniforms("resolution", "effectTime", "center", "direction",
                "progress", "intensity", "mode");
    }

    @Override
    protected void updateUniforms(PostProcessingContext context) {
        shader.setUniform("resolution", (float) context.getWidth(), (float) context.getHeight());
        shader.setUniform("effectTime", context.getTime());
        shader.setUniform("center", centerX, centerY);
        shader.setUniform("direction", directionX, directionY);
        shader.setUniform("progress", progress);
        shader.setUniform("intensity", intensity);
        shader.setUniform("mode", mode);
    }

    void triggerDash(float screenX, float screenY, float viewportWidth, float viewportHeight,
            float directionX, float directionY) {
        trigger(screenX, screenY, viewportWidth, viewportHeight,
                directionX, -directionY, DASH, 0.42f, 0.78f);
    }

    void triggerDamage(float screenX, float screenY, float viewportWidth, float viewportHeight,
            float strength) {
        trigger(screenX, screenY, viewportWidth, viewportHeight,
                1, 0, DAMAGE, 0.50f, Math.clamp(strength, 0.45f, 1.25f));
    }

    void triggerRift(float screenX, float screenY, float viewportWidth, float viewportHeight,
            float strength) {
        trigger(screenX, screenY, viewportWidth, viewportHeight,
                1, 0, RIFT, 0.82f, Math.clamp(strength, 0.5f, 1.3f));
    }

    void triggerWave(float viewportWidth, float viewportHeight, float strength) {
        trigger(viewportWidth * 0.5f, viewportHeight * 0.5f, viewportWidth, viewportHeight,
                0, -1, WAVE, 0.72f, Math.clamp(strength, 0.35f, 1.15f));
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

    private void trigger(float screenX, float screenY, float viewportWidth, float viewportHeight,
            float directionX, float directionY, float mode, float duration, float intensity) {
        centerX = Math.clamp(screenX / Math.max(1, viewportWidth), 0, 1);
        centerY = Math.clamp(1 - screenY / Math.max(1, viewportHeight), 0, 1);
        float length = (float) Math.sqrt(directionX * directionX + directionY * directionY);
        if (length > 0.001f) {
            this.directionX = directionX / length;
            this.directionY = directionY / length;
        } else {
            this.directionX = 1;
            this.directionY = 0;
        }
        this.mode = mode;
        this.duration = duration;
        this.intensity = intensity;
        progress = 0;
        setEnabled(true);
    }
}
