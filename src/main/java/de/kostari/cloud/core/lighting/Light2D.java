package de.kostari.cloud.core.lighting;

import de.kostari.cloud.core.components.Component;
import de.kostari.cloud.core.utils.math.Vector2;
import de.kostari.cloud.core.utils.types.Color4f;

/**
 * A radial 2D light rendered by a {@link LightingEffect}.
 *
 * The owning object's position is the light origin. The optional local offset
 * follows the object's rotation.
 */
public class Light2D extends Component {

    private final Vector2 offset = new Vector2();
    private final Color4f color = new Color4f(1, 1, 1, 1);

    private float radius = 250f;
    private float intensity = 1f;
    private float falloff = 2f;
    private float softness;
    private float shadowStrength = 1f;
    private boolean castsShadows = true;
    private boolean enabled = true;

    public Light2D() {
    }

    public Light2D(float radius, Color4f color) {
        if (radius <= 0) {
            throw new IllegalArgumentException("Light radius must be greater than zero");
        }
        if (color == null) {
            throw new IllegalArgumentException("Light color cannot be null");
        }
        this.radius = radius;
        this.color.r = color.r;
        this.color.g = color.g;
        this.color.b = color.b;
    }

    public Light2D radius(float radius) {
        if (radius <= 0) {
            throw new IllegalArgumentException("Light radius must be greater than zero");
        }
        this.radius = radius;
        return this;
    }

    public float radius() {
        return radius;
    }

    public Light2D intensity(float intensity) {
        this.intensity = Math.max(0, intensity);
        return this;
    }

    public float intensity() {
        return intensity;
    }

    /**
     * Sets the radial falloff exponent. Values below one spread light farther;
     * values above one make it concentrate around the source.
     */
    public Light2D falloff(float falloff) {
        this.falloff = Math.max(0.01f, falloff);
        return this;
    }

    public float falloff() {
        return falloff;
    }

    /**
     * Sets the radius of the area-light source in world units. Zero produces
     * hard shadows; positive values use spatially dithered rays for a soft
     * penumbra without multiplying the per-pixel tracing cost.
     */
    public Light2D softness(float softness) {
        this.softness = Math.max(0, softness);
        return this;
    }

    public float softness() {
        return softness;
    }

    public Light2D shadowStrength(float shadowStrength) {
        this.shadowStrength = clamp01(shadowStrength);
        return this;
    }

    public float shadowStrength() {
        return shadowStrength;
    }

    public Light2D castsShadows(boolean castsShadows) {
        this.castsShadows = castsShadows;
        return this;
    }

    public boolean castsShadows() {
        return castsShadows;
    }

    public Light2D color(float red, float green, float blue) {
        color.r = clamp01(red);
        color.g = clamp01(green);
        color.b = clamp01(blue);
        return this;
    }

    public Light2D color(Color4f color) {
        if (color == null) {
            throw new IllegalArgumentException("Light color cannot be null");
        }
        return color(color.r, color.g, color.b);
    }

    public Color4f color() {
        return color;
    }

    public Light2D offset(float x, float y) {
        offset.set(x, y);
        return this;
    }

    public Vector2 offset() {
        return offset;
    }

    public Light2D enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Vector2 worldPosition() {
        if (gameObject == null) {
            throw new IllegalStateException("Light2D must be added to a GameObject before reading its position");
        }

        float radians = (float) Math.toRadians(gameObject.transform.rotation);
        float cosine = (float) Math.cos(radians);
        float sine = (float) Math.sin(radians);
        float x = offset.x * gameObject.transform.scale.x;
        float y = offset.y * gameObject.transform.scale.y;
        return new Vector2(
                gameObject.transform.position.x + x * cosine - y * sine,
                gameObject.transform.position.y + x * sine + y * cosine);
    }

    private static float clamp01(float value) {
        return Math.max(0, Math.min(1, value));
    }
}
