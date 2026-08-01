package de.kostari.cloud.core.lighting;

import de.kostari.cloud.core.components.Component;
import de.kostari.cloud.core.utils.math.Vector2;

/**
 * A rectangular object that blocks rays cast by {@link Light2D} instances.
 * Position, rotation, and scale are inherited from the owning game object.
 */
public class LightOccluder2D extends Component {

    private final Vector2 offset = new Vector2();

    private float width;
    private float height;
    private float opacity = 1f;
    private boolean enabled = true;

    public LightOccluder2D(float width, float height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Occluder width and height must be greater than zero");
        }
        this.width = width;
        this.height = height;
    }

    public LightOccluder2D size(float width, float height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Occluder width and height must be greater than zero");
        }
        this.width = width;
        this.height = height;
        return this;
    }

    public float width() {
        return width;
    }

    public float height() {
        return height;
    }

    public LightOccluder2D offset(float x, float y) {
        offset.set(x, y);
        return this;
    }

    public Vector2 offset() {
        return offset;
    }

    /**
     * Controls how much light is blocked, from zero (transparent) to one
     * (opaque).
     */
    public LightOccluder2D opacity(float opacity) {
        this.opacity = Math.max(0, Math.min(1, opacity));
        return this;
    }

    public float opacity() {
        return opacity;
    }

    public LightOccluder2D enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Vector2 worldCenter() {
        if (gameObject == null) {
            throw new IllegalStateException(
                    "LightOccluder2D must be added to a GameObject before reading its center");
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

    public float worldWidth() {
        return gameObject == null ? width : width * Math.abs(gameObject.transform.scale.x);
    }

    public float worldHeight() {
        return gameObject == null ? height : height * Math.abs(gameObject.transform.scale.y);
    }
}
