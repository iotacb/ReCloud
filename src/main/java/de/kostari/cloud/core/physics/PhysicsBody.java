package de.kostari.cloud.core.physics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.kostari.cloud.core.components.Component;
import de.kostari.cloud.core.utils.math.Vector2;

/**
 * A lightweight axis-aligned physics body.
 *
 * Add it to a GameObject with {@code addComponent}; the current scene discovers
 * and simulates it automatically.
 */
public class PhysicsBody extends Component {

    public final Vector2 velocity = new Vector2();

    private final Vector2 force = new Vector2();
    private final Vector2 offset = new Vector2();
    private final List<Collision> collisions = new ArrayList<>();
    private final List<CollisionListener> collisionListeners = new ArrayList<>();

    private BodyType type;
    private float width;
    private float height;
    private float mass = 1f;
    private float gravityScale = 1f;
    private float bounce = 0f;
    private float friction = 0.6f;
    private float linearDamping = 0f;
    private int layer = 1;
    private int collisionMask = -1;
    private boolean sensor;
    private boolean enabled = true;
    private boolean grounded;
    private boolean oneWayPlatform;

    public PhysicsBody(float width, float height) {
        this(BodyType.DYNAMIC, width, height);
    }

    public PhysicsBody(BodyType type, float width, float height) {
        size(width, height);
        type(type);
    }

    public static PhysicsBody dynamic(float width, float height) {
        return new PhysicsBody(BodyType.DYNAMIC, width, height);
    }

    public static PhysicsBody fixed(float width, float height) {
        return new PhysicsBody(BodyType.STATIC, width, height);
    }

    public static PhysicsBody kinematic(float width, float height) {
        return new PhysicsBody(BodyType.KINEMATIC, width, height);
    }

    public PhysicsBody type(BodyType type) {
        if (type == null) {
            throw new IllegalArgumentException("Body type cannot be null");
        }
        this.type = type;
        return this;
    }

    public BodyType type() {
        return type;
    }

    public PhysicsBody size(float width, float height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Physics body width and height must be greater than zero");
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

    public PhysicsBody offset(float x, float y) {
        this.offset.set(x, y);
        return this;
    }

    public Vector2 offset() {
        return offset;
    }

    public PhysicsBody mass(float mass) {
        if (mass <= 0) {
            throw new IllegalArgumentException("Mass must be greater than zero");
        }
        this.mass = mass;
        return this;
    }

    public float mass() {
        return mass;
    }

    public PhysicsBody gravityScale(float gravityScale) {
        this.gravityScale = gravityScale;
        return this;
    }

    public float gravityScale() {
        return gravityScale;
    }

    public PhysicsBody bounce(float bounce) {
        this.bounce = clamp01(bounce);
        return this;
    }

    public float bounce() {
        return bounce;
    }

    public PhysicsBody friction(float friction) {
        this.friction = Math.max(0, friction);
        return this;
    }

    public float friction() {
        return friction;
    }

    public PhysicsBody linearDamping(float damping) {
        this.linearDamping = Math.max(0, damping);
        return this;
    }

    public float linearDamping() {
        return linearDamping;
    }

    public PhysicsBody velocity(float x, float y) {
        velocity.set(x, y);
        return this;
    }

    public PhysicsBody applyForce(float x, float y) {
        force.add(x, y);
        return this;
    }

    public PhysicsBody applyForce(Vector2 force) {
        return applyForce(force.x, force.y);
    }

    public PhysicsBody applyImpulse(float x, float y) {
        if (type == BodyType.DYNAMIC) {
            velocity.add(x / mass, y / mass);
        }
        return this;
    }

    public PhysicsBody applyImpulse(Vector2 impulse) {
        return applyImpulse(impulse.x, impulse.y);
    }

    public PhysicsBody sensor(boolean sensor) {
        this.sensor = sensor;
        return this;
    }

    public boolean isSensor() {
        return sensor;
    }

    /**
     * Makes this body collide only with dynamic bodies descending onto its top.
     * This is intended for platformer ledges that can be jumped through from
     * below without producing side or underside collisions.
     */
    public PhysicsBody oneWayPlatform(boolean oneWayPlatform) {
        this.oneWayPlatform = oneWayPlatform;
        return this;
    }

    public boolean isOneWayPlatform() {
        return oneWayPlatform;
    }

    public PhysicsBody layer(int layer) {
        this.layer = layer;
        return this;
    }

    public int layer() {
        return layer;
    }

    public PhysicsBody collisionMask(int collisionMask) {
        this.collisionMask = collisionMask;
        return this;
    }

    public int collisionMask() {
        return collisionMask;
    }

    public PhysicsBody enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isGrounded() {
        return grounded;
    }

    public AABB bounds() {
        if (gameObject == null) {
            throw new IllegalStateException("PhysicsBody must be added to a GameObject before reading its bounds");
        }
        return AABB.fromCenter(
                gameObject.transform.position.x + offset.x,
                gameObject.transform.position.y + offset.y,
                width,
                height);
    }

    public boolean overlaps(PhysicsBody other) {
        return bounds().overlaps(other.bounds());
    }

    public boolean contains(float x, float y) {
        return bounds().contains(x, y);
    }

    public List<Collision> collisions() {
        return Collections.unmodifiableList(collisions);
    }

    public boolean isTouching(PhysicsBody other) {
        for (Collision collision : collisions) {
            if (collision.other() == other) {
                return true;
            }
        }
        return false;
    }

    public PhysicsBody onCollision(CollisionListener listener) {
        if (listener != null) {
            collisionListeners.add(listener);
        }
        return this;
    }

    void beginStep() {
        collisions.clear();
        grounded = false;
    }

    void integrate(Vector2 gravity, float delta) {
        if (!enabled) {
            return;
        }

        if (type == BodyType.DYNAMIC) {
            velocity.x += (gravity.x * gravityScale + force.x / mass) * delta;
            velocity.y += (gravity.y * gravityScale + force.y / mass) * delta;

            float dampingFactor = 1f / (1f + linearDamping * delta);
            velocity.multiply(dampingFactor);
        }

        if (type != BodyType.STATIC) {
            gameObject.transform.position.add(velocity.x * delta, velocity.y * delta);
        }
    }

    void clearForce() {
        force.set(0, 0);
    }

    float inverseMass() {
        return type == BodyType.DYNAMIC ? 1f / mass : 0f;
    }

    void addCollision(PhysicsBody other, float normalX, float normalY, float penetration, boolean sensorContact) {
        for (Collision collision : collisions) {
            if (collision.other() == other) {
                return;
            }
        }

        collisions.add(new Collision(
                this,
                other,
                new Vector2(normalX, normalY),
                penetration,
                sensorContact));

        if (!sensorContact && normalY < -0.5f) {
            grounded = true;
        }
    }

    void notifyCollisionListeners() {
        if (collisionListeners.isEmpty()) {
            return;
        }
        for (Collision collision : collisions) {
            for (CollisionListener listener : new ArrayList<>(collisionListeners)) {
                listener.onCollision(collision);
            }
        }
    }

    private static float clamp01(float value) {
        return Math.max(0, Math.min(1, value));
    }
}
