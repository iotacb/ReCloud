package de.kostari.cloud.core.lighting;

import java.util.Arrays;

import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.scene.Camera;
import de.kostari.cloud.core.scene.Scene;
import de.kostari.cloud.core.scene.SceneManager;
import de.kostari.cloud.core.utils.math.Vector2;
import de.kostari.cloud.core.utils.render.Shader;
import de.kostari.cloud.core.utils.render.post.PostEffect;
import de.kostari.cloud.core.utils.render.post.PostProcessingContext;
import de.kostari.cloud.core.utils.types.Color4f;
import de.kostari.cloud.core.window.Window;

/**
 * Screen-space 2D lighting with radial falloff and analytically traced shadows.
 *
 * Each shaded pixel traces a segment toward every affecting light and tests it
 * against the scene's rectangular {@link LightOccluder2D} components. The
 * bounded light and occluder counts keep the GLSL 3.3 shader portable.
 */
public class LightingEffect implements PostEffect {

    public static final int MAX_LIGHTS = 16;
    public static final int MAX_OCCLUDERS = 32;

    private static final int VALUES_PER_VECTOR = 4;

    private final Color4f ambientColor = new Color4f(1, 1, 1, 1);
    private final float[] lightData = new float[MAX_LIGHTS * VALUES_PER_VECTOR];
    private final float[] lightColors = new float[MAX_LIGHTS * VALUES_PER_VECTOR];
    private final float[] lightShadows = new float[MAX_LIGHTS * VALUES_PER_VECTOR];
    private final float[] occluderBounds = new float[MAX_OCCLUDERS * VALUES_PER_VECTOR];
    private final float[] occluderTransforms = new float[MAX_OCCLUDERS * VALUES_PER_VECTOR];

    private Shader shader;
    private float ambientIntensity = 0.15f;
    private boolean enabled = true;
    private boolean initialized;
    private int activeLightCount;
    private int activeOccluderCount;

    @Override
    public void init() {
        if (initialized) {
            return;
        }

        shader = new Shader();
        shader.attachShaderFromFile(org.lwjgl.opengl.GL20.GL_VERTEX_SHADER, "../../shader/post_vertex.glsl");
        shader.attachShaderFromFile(org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER,
                "../../shader/post_lighting_fragment.glsl");
        shader.link();
        shader.bind();
        shader.createUniforms(
                "inputTexture",
                "viewportPosition",
                "viewportSize",
                "ambientColor",
                "ambientIntensity",
                "lightCount",
                "occluderCount",
                "lightData[0]",
                "lightColors[0]",
                "lightShadows[0]",
                "occluderBounds[0]",
                "occluderTransforms[0]");
        shader.setUniform("inputTexture", 0);
        shader.unbind();
        initialized = true;
    }

    @Override
    public void apply(PostProcessingContext context, int sourceTexture) {
        if (!enabled) {
            return;
        }

        collectSceneData();

        Camera camera = SceneManager.hasScene() ? SceneManager.current().getCamera() : null;
        float viewportX = camera == null ? 0 : camera.transform.position.x;
        float viewportY = camera == null ? 0 : camera.transform.position.y;
        float viewportWidth = camera == null ? Window.get().getWidth() : camera.getViewportWorldSize().x;
        float viewportHeight = camera == null ? Window.get().getHeight() : camera.getViewportWorldSize().y;

        shader.bind();
        shader.setUniform("viewportPosition", viewportX, viewportY);
        shader.setUniform("viewportSize", viewportWidth, viewportHeight);
        shader.setUniform("ambientColor", ambientColor.r, ambientColor.g, ambientColor.b);
        shader.setUniform("ambientIntensity", ambientIntensity);
        shader.setUniform("lightCount", activeLightCount);
        shader.setUniform("occluderCount", activeOccluderCount);
        shader.setUniformVec4Array("lightData[0]", lightData);
        shader.setUniformVec4Array("lightColors[0]", lightColors);
        shader.setUniformVec4Array("lightShadows[0]", lightShadows);
        shader.setUniformVec4Array("occluderBounds[0]", occluderBounds);
        shader.setUniformVec4Array("occluderTransforms[0]", occluderTransforms);
        context.drawFullscreen(sourceTexture, shader);
        shader.unbind();
    }

    @Override
    public void cleanup() {
        if (shader != null) {
            shader.cleanup();
            shader = null;
        }
        initialized = false;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LightingEffect enabled(boolean enabled) {
        setEnabled(enabled);
        return this;
    }

    public LightingEffect ambientColor(float red, float green, float blue) {
        ambientColor.r = clamp01(red);
        ambientColor.g = clamp01(green);
        ambientColor.b = clamp01(blue);
        return this;
    }

    public LightingEffect ambientColor(Color4f color) {
        if (color == null) {
            throw new IllegalArgumentException("Ambient light color cannot be null");
        }
        return ambientColor(color.r, color.g, color.b);
    }

    public Color4f ambientColor() {
        return ambientColor;
    }

    public LightingEffect ambientIntensity(float ambientIntensity) {
        this.ambientIntensity = Math.max(0, ambientIntensity);
        return this;
    }

    public float ambientIntensity() {
        return ambientIntensity;
    }

    public int activeLightCount() {
        return activeLightCount;
    }

    public int activeOccluderCount() {
        return activeOccluderCount;
    }

    private void collectSceneData() {
        Arrays.fill(lightData, 0);
        Arrays.fill(lightColors, 0);
        Arrays.fill(lightShadows, 0);
        Arrays.fill(occluderBounds, 0);
        Arrays.fill(occluderTransforms, 0);
        activeLightCount = 0;
        activeOccluderCount = 0;

        if (!SceneManager.hasScene()) {
            return;
        }

        Scene scene = SceneManager.current();
        for (GameObject gameObject : scene.getGameObjects()) {
            if (activeLightCount < MAX_LIGHTS) {
                Light2D light = gameObject.getComponent(Light2D.class);
                if (light != null && light.isEnabled() && light.intensity() > 0) {
                    writeLight(light, activeLightCount++);
                }
            }

            if (activeOccluderCount < MAX_OCCLUDERS) {
                LightOccluder2D occluder = gameObject.getComponent(LightOccluder2D.class);
                if (occluder != null && occluder.isEnabled() && occluder.opacity() > 0) {
                    writeOccluder(occluder, activeOccluderCount++);
                }
            }

            if (activeLightCount == MAX_LIGHTS && activeOccluderCount == MAX_OCCLUDERS) {
                break;
            }
        }
    }

    private void writeLight(Light2D light, int index) {
        int offset = index * VALUES_PER_VECTOR;
        Vector2 position = light.worldPosition();

        lightData[offset] = position.x;
        lightData[offset + 1] = position.y;
        lightData[offset + 2] = light.radius();
        lightData[offset + 3] = light.intensity();

        lightColors[offset] = light.color().r;
        lightColors[offset + 1] = light.color().g;
        lightColors[offset + 2] = light.color().b;
        lightColors[offset + 3] = light.falloff();

        lightShadows[offset] = light.castsShadows() ? 1 : 0;
        lightShadows[offset + 1] = light.softness();
        lightShadows[offset + 2] = light.shadowStrength();
    }

    private void writeOccluder(LightOccluder2D occluder, int index) {
        int offset = index * VALUES_PER_VECTOR;
        Vector2 center = occluder.worldCenter();
        float radians = (float) Math.toRadians(occluder.gameObject.transform.rotation);

        occluderBounds[offset] = center.x;
        occluderBounds[offset + 1] = center.y;
        occluderBounds[offset + 2] = occluder.worldWidth() * 0.5f;
        occluderBounds[offset + 3] = occluder.worldHeight() * 0.5f;

        occluderTransforms[offset] = (float) Math.cos(radians);
        occluderTransforms[offset + 1] = (float) Math.sin(radians);
        occluderTransforms[offset + 2] = occluder.opacity();
    }

    private static float clamp01(float value) {
        return Math.max(0, Math.min(1, value));
    }
}
