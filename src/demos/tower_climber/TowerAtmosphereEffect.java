package tower_climber;

import de.kostari.cloud.core.utils.render.post.PostProcessingContext;
import de.kostari.cloud.core.utils.render.post.ShaderPostEffect;

public final class TowerAtmosphereEffect extends ShaderPostEffect {

    private float effectTime;
    private float previousZone;
    private float currentZone;
    private float zoneBlend = 1;
    private float danger;
    private float storm;
    private float lightning;

    @Override
    protected String fragmentShaderPath() {
        return "demo_assets/tower_climber/tower_atmosphere.glsl";
    }

    @Override
    protected void createUniforms() {
        shader.createUniforms("resolution", "effectTime", "previousZone", "currentZone",
                "zoneBlend", "danger", "storm", "lightning");
    }

    @Override
    protected void updateUniforms(PostProcessingContext context) {
        shader.setUniform("resolution", (float) context.getWidth(), (float) context.getHeight());
        shader.setUniform("effectTime", effectTime);
        shader.setUniform("previousZone", previousZone);
        shader.setUniform("currentZone", currentZone);
        shader.setUniform("zoneBlend", zoneBlend);
        shader.setUniform("danger", danger);
        shader.setUniform("storm", storm);
        shader.setUniform("lightning", lightning);
    }

    public void update(float delta, int previousZone, int currentZone, float zoneBlend,
            float danger, float storm, float lightning) {
        effectTime += Math.max(0, Math.min(delta, 0.05f));
        this.previousZone = Math.floorMod(previousZone, 4);
        this.currentZone = Math.floorMod(currentZone, 4);
        this.zoneBlend = clamp01(zoneBlend);
        this.danger = clamp01(danger);
        this.storm = clamp01(storm);
        this.lightning = clamp01(lightning);
    }

    public float effectTime() {
        return effectTime;
    }

    public float danger() {
        return danger;
    }

    private float clamp01(float value) {
        return Math.max(0, Math.min(1, value));
    }
}
