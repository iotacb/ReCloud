package arena_survivor;

import de.kostari.cloud.core.utils.render.post.PostProcessingContext;
import de.kostari.cloud.core.utils.render.post.ShaderPostEffect;

final class ArenaAtmosphereEffect extends ShaderPostEffect {
    private float effectTime;
    private float playerX = 0.5f;
    private float playerY = 0.5f;
    private float danger;
    private float bossEnergy;
    private float combat;

    @Override
    protected String fragmentShaderPath() {
        return "./demo_assets/arena_survivor/shaders/aether-atmosphere.glsl";
    }

    @Override
    protected void createUniforms() {
        shader.createUniforms("resolution", "effectTime", "playerCenter",
                "danger", "bossEnergy", "combat");
    }

    @Override
    protected void updateUniforms(PostProcessingContext context) {
        shader.setUniform("resolution", (float) context.getWidth(), (float) context.getHeight());
        shader.setUniform("effectTime", effectTime);
        shader.setUniform("playerCenter", playerX, playerY);
        shader.setUniform("danger", danger);
        shader.setUniform("bossEnergy", bossEnergy);
        shader.setUniform("combat", combat);
    }

    void update(float delta, float screenX, float screenY, float viewportWidth, float viewportHeight,
            float danger, float bossEnergy, float combat) {
        effectTime += Math.clamp(delta, 0, 0.05f);
        playerX = Math.clamp(screenX / Math.max(1, viewportWidth), 0, 1);
        playerY = Math.clamp(1 - screenY / Math.max(1, viewportHeight), 0, 1);
        this.danger = Math.clamp(danger, 0, 1);
        this.bossEnergy = Math.clamp(bossEnergy, 0, 1);
        this.combat = Math.clamp(combat, 0, 1);
    }
}
