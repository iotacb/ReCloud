package custom_shader;

import de.kostari.cloud.core.utils.render.post.PostProcessingContext;
import de.kostari.cloud.core.utils.render.post.ShaderPostEffect;
import de.kostari.cloud.core.window.Input;

public class InteractiveShaderEffect extends ShaderPostEffect {

    private static final String[] MODE_NAMES = {
            "Ripple lens",
            "Pixel field",
            "Chromatic split"
    };

    private int mode;
    private float strength = 0.65f;

    @Override
    protected String fragmentShaderPath() {
        return "demo_assets/custom_shader/demo_custom_fragment.glsl";
    }

    @Override
    protected void createUniforms() {
        shader.createUniforms(
                "resolution",
                "mouse",
                "time",
                "strength",
                "mode",
                "mouseDown");
    }

    @Override
    protected void updateUniforms(PostProcessingContext context) {
        shader.setUniform("resolution", (float) context.getWidth(), (float) context.getHeight());
        shader.setUniform("mouse", (float) Input.getMouseX(), (float) Input.getMouseY());
        shader.setUniform("time", context.getTime());
        shader.setUniform("strength", strength);
        shader.setUniform("mode", mode);
        shader.setUniform("mouseDown", Input.mouseButtonDown(0) ? 1 : 0);
    }

    public void nextMode() {
        mode((mode + 1) % MODE_NAMES.length);
    }

    public void mode(int mode) {
        this.mode = Math.max(0, Math.min(MODE_NAMES.length - 1, mode));
    }

    public int getMode() {
        return mode;
    }

    public String getModeName() {
        return MODE_NAMES[mode];
    }

    public void strength(float strength) {
        this.strength = Math.max(0.0f, Math.min(1.0f, strength));
    }

    public float getStrength() {
        return strength;
    }

    public void reset() {
        mode = 0;
        strength = 0.65f;
        setEnabled(true);
    }
}
