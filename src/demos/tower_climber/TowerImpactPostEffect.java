package tower_climber;

import de.kostari.cloud.core.utils.render.post.PostProcessingContext;
import de.kostari.cloud.core.utils.render.post.ShaderPostEffect;

public final class TowerImpactPostEffect extends ShaderPostEffect {

    public enum Mode {
        SLASH,
        DAMAGE,
        BOOST
    }

    private float centerX = 0.5f;
    private float centerY = 0.5f;
    private float progress = 1;
    private float duration = 0.4f;
    private float facing = 1;
    private float strength = 1;
    private Mode mode = Mode.SLASH;

    public TowerImpactPostEffect() {
        setEnabled(false);
    }

    @Override
    protected String fragmentShaderPath() {
        return "demo_assets/tower_climber/combat_rift.glsl";
    }

    @Override
    protected void createUniforms() {
        shader.createUniforms("resolution", "center", "progress", "mode", "facing", "strength");
    }

    @Override
    protected void updateUniforms(PostProcessingContext context) {
        shader.setUniform("resolution", (float) context.getWidth(), (float) context.getHeight());
        shader.setUniform("center", centerX, centerY);
        shader.setUniform("progress", progress);
        shader.setUniform("mode", (float) mode.ordinal());
        shader.setUniform("facing", facing);
        shader.setUniform("strength", strength);
    }

    public void triggerSlash(float screenX, float screenY, float viewportWidth, float viewportHeight,
            float facing, float comboStrength) {
        trigger(screenX, screenY, viewportWidth, viewportHeight, Mode.SLASH, 0.36f,
                facing, 0.82f + Math.min(0.38f, Math.max(0, comboStrength) * 0.06f));
    }

    public void triggerDamage(float screenX, float screenY, float viewportWidth, float viewportHeight) {
        trigger(screenX, screenY, viewportWidth, viewportHeight, Mode.DAMAGE, 0.48f, 1, 1);
    }

    public void triggerBoost(float screenX, float screenY, float viewportWidth, float viewportHeight) {
        trigger(screenX, screenY, viewportWidth, viewportHeight, Mode.BOOST, 0.58f, 1, 1);
    }

    public void update(float delta) {
        if (!isEnabled()) {
            return;
        }
        progress += Math.max(0, delta) / duration;
        if (progress >= 1) {
            progress = 1;
            setEnabled(false);
        }
    }

    public Mode mode() {
        return mode;
    }

    public float progress() {
        return progress;
    }

    private void trigger(float screenX, float screenY, float viewportWidth, float viewportHeight,
            Mode mode, float duration, float facing, float strength) {
        centerX = Math.clamp(screenX / Math.max(1, viewportWidth), 0, 1);
        centerY = Math.clamp(1 - screenY / Math.max(1, viewportHeight), 0, 1);
        this.mode = mode;
        this.duration = duration;
        this.facing = facing >= 0 ? 1 : -1;
        this.strength = Math.max(0, strength);
        progress = 0;
        setEnabled(true);
    }
}
