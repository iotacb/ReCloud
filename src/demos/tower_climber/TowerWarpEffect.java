package tower_climber;

import de.kostari.cloud.core.utils.render.post.PostProcessingContext;
import de.kostari.cloud.core.utils.render.post.ShaderPostEffect;

public class TowerWarpEffect extends ShaderPostEffect {

    private float centerX = 0.5f;
    private float centerY = 0.5f;
    private float progress = 1;

    public TowerWarpEffect() {
        setEnabled(false);
    }

    @Override
    protected String fragmentShaderPath() {
        return "demo_assets/tower_climber/area_warp.glsl";
    }

    @Override
    protected void createUniforms() {
        shader.createUniforms("resolution", "center", "progress");
    }

    @Override
    protected void updateUniforms(PostProcessingContext context) {
        shader.setUniform("resolution", (float) context.getWidth(), (float) context.getHeight());
        shader.setUniform("center", centerX, centerY);
        shader.setUniform("progress", progress);
    }

    public void trigger(float screenX, float screenY, float viewportWidth, float viewportHeight) {
        centerX = Math.clamp(screenX / Math.max(1, viewportWidth), 0, 1);
        centerY = Math.clamp(1 - screenY / Math.max(1, viewportHeight), 0, 1);
        progress = 0;
        setEnabled(true);
    }

    public void update(float delta) {
        if (!isEnabled()) {
            return;
        }
        progress += Math.max(0, delta) / 1.05f;
        if (progress >= 1) {
            progress = 1;
            setEnabled(false);
        }
    }

    public float progress() {
        return progress;
    }
}
