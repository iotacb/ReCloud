package tower_climber;

import de.kostari.cloud.core.utils.render.post.PostProcessingContext;
import de.kostari.cloud.core.utils.render.post.ShaderPostEffect;

public final class TowerPixelationEffect extends ShaderPostEffect {

    private float virtualHeight = 360;
    private float strength = 0.78f;

    @Override
    protected String fragmentShaderPath() {
        return "demo_assets/tower_climber/pixelation.glsl";
    }

    @Override
    protected void createUniforms() {
        shader.createUniforms("resolution", "virtualHeight", "strength");
    }

    @Override
    protected void updateUniforms(PostProcessingContext context) {
        shader.setUniform("resolution", (float) context.getWidth(), (float) context.getHeight());
        shader.setUniform("virtualHeight", virtualHeight);
        shader.setUniform("strength", strength);
    }

    public TowerPixelationEffect virtualHeight(float virtualHeight) {
        this.virtualHeight = Math.max(1, virtualHeight);
        return this;
    }

    public TowerPixelationEffect strength(float strength) {
        this.strength = Math.max(0, Math.min(1, strength));
        return this;
    }

    public float virtualHeight() {
        return virtualHeight;
    }

    public float strength() {
        return strength;
    }
}
