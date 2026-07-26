package de.kostari.cloud.core.physics;

import java.util.ArrayList;
import java.util.List;

import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.utils.math.Vector2;

/**
 * A small, discrete AABB physics world with gravity and impulse resolution.
 */
public class PhysicsWorld {

    private final Vector2 gravity = new Vector2(0, 980);
    private int substeps = 2;
    private float maximumDelta = 0.05f;
    private boolean enabled = true;

    public Vector2 gravity() {
        return gravity;
    }

    public PhysicsWorld gravity(float x, float y) {
        gravity.set(x, y);
        return this;
    }

    public PhysicsWorld substeps(int substeps) {
        if (substeps < 1) {
            throw new IllegalArgumentException("Physics substeps must be at least one");
        }
        this.substeps = substeps;
        return this;
    }

    public int substeps() {
        return substeps;
    }

    public PhysicsWorld maximumDelta(float maximumDelta) {
        if (maximumDelta <= 0) {
            throw new IllegalArgumentException("Maximum physics delta must be greater than zero");
        }
        this.maximumDelta = maximumDelta;
        return this;
    }

    public float maximumDelta() {
        return maximumDelta;
    }

    public PhysicsWorld enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Advances all PhysicsBody components found on the supplied game objects.
     */
    public void step(List<GameObject> gameObjects, float delta) {
        List<PhysicsBody> bodies = collectBodies(gameObjects);
        for (PhysicsBody body : bodies) {
            body.beginStep();
        }

        if (!enabled || bodies.isEmpty() || delta <= 0) {
            return;
        }

        float frameDelta = Math.min(delta, maximumDelta);
        float stepDelta = frameDelta / substeps;

        for (int step = 0; step < substeps; step++) {
            for (PhysicsBody body : bodies) {
                body.integrate(gravity, stepDelta);
            }
            detectAndResolve(bodies);
        }

        for (PhysicsBody body : bodies) {
            body.clearForce();
            body.notifyCollisionListeners();
        }
    }

    private List<PhysicsBody> collectBodies(List<GameObject> gameObjects) {
        List<PhysicsBody> bodies = new ArrayList<>();
        for (GameObject gameObject : gameObjects) {
            PhysicsBody body = gameObject.getComponent(PhysicsBody.class);
            if (body != null) {
                bodies.add(body);
            }
        }
        return bodies;
    }

    private void detectAndResolve(List<PhysicsBody> bodies) {
        for (int i = 0; i < bodies.size(); i++) {
            PhysicsBody a = bodies.get(i);
            for (int j = i + 1; j < bodies.size(); j++) {
                PhysicsBody b = bodies.get(j);
                if (!canCollide(a, b)) {
                    continue;
                }
                resolvePair(a, b);
            }
        }
    }

    private boolean canCollide(PhysicsBody a, PhysicsBody b) {
        if (!a.isEnabled() || !b.isEnabled()) {
            return false;
        }

        boolean layersMatch = (a.collisionMask() & b.layer()) != 0
                && (b.collisionMask() & a.layer()) != 0;
        if (!layersMatch) {
            return false;
        }

        boolean bothStatic = a.type() == BodyType.STATIC && b.type() == BodyType.STATIC;
        return !bothStatic || a.isSensor() || b.isSensor();
    }

    private void resolvePair(PhysicsBody a, PhysicsBody b) {
        AABB aBounds = a.bounds();
        AABB bBounds = b.bounds();
        if (!aBounds.overlaps(bBounds)) {
            return;
        }

        float overlapX = Math.min(aBounds.right(), bBounds.right())
                - Math.max(aBounds.left(), bBounds.left());
        float overlapY = Math.min(aBounds.bottom(), bBounds.bottom())
                - Math.max(aBounds.top(), bBounds.top());

        float normalX = 0;
        float normalY = 0;
        float penetration;

        if (overlapX < overlapY) {
            normalX = aBounds.centerX() < bBounds.centerX() ? 1 : -1;
            penetration = overlapX;
        } else {
            normalY = aBounds.centerY() < bBounds.centerY() ? 1 : -1;
            penetration = overlapY;
        }

        boolean sensorContact = a.isSensor() || b.isSensor();
        a.addCollision(b, -normalX, -normalY, penetration, sensorContact);
        b.addCollision(a, normalX, normalY, penetration, sensorContact);

        if (sensorContact) {
            return;
        }

        float inverseMassA = a.inverseMass();
        float inverseMassB = b.inverseMass();
        float inverseMassSum = inverseMassA + inverseMassB;
        if (inverseMassSum == 0) {
            return;
        }

        float correctionX = normalX * penetration / inverseMassSum;
        float correctionY = normalY * penetration / inverseMassSum;
        a.gameObject.transform.position.add(-correctionX * inverseMassA, -correctionY * inverseMassA);
        b.gameObject.transform.position.add(correctionX * inverseMassB, correctionY * inverseMassB);

        float relativeVelocityX = b.velocity.x - a.velocity.x;
        float relativeVelocityY = b.velocity.y - a.velocity.y;
        float velocityAlongNormal = relativeVelocityX * normalX + relativeVelocityY * normalY;
        if (velocityAlongNormal > 0) {
            return;
        }

        float restitution = Math.max(a.bounce(), b.bounce());
        float impulseMagnitude = -(1 + restitution) * velocityAlongNormal / inverseMassSum;
        float impulseX = normalX * impulseMagnitude;
        float impulseY = normalY * impulseMagnitude;

        a.velocity.add(-impulseX * inverseMassA, -impulseY * inverseMassA);
        b.velocity.add(impulseX * inverseMassB, impulseY * inverseMassB);

        applyFriction(a, b, normalX, normalY, impulseMagnitude, inverseMassA, inverseMassB, inverseMassSum);
    }

    private void applyFriction(
            PhysicsBody a,
            PhysicsBody b,
            float normalX,
            float normalY,
            float normalImpulse,
            float inverseMassA,
            float inverseMassB,
            float inverseMassSum) {
        float relativeVelocityX = b.velocity.x - a.velocity.x;
        float relativeVelocityY = b.velocity.y - a.velocity.y;
        float velocityAlongNormal = relativeVelocityX * normalX + relativeVelocityY * normalY;

        float tangentX = relativeVelocityX - velocityAlongNormal * normalX;
        float tangentY = relativeVelocityY - velocityAlongNormal * normalY;
        float tangentLength = (float) Math.sqrt(tangentX * tangentX + tangentY * tangentY);
        if (tangentLength < 0.00001f) {
            return;
        }

        tangentX /= tangentLength;
        tangentY /= tangentLength;

        float frictionImpulse = -(relativeVelocityX * tangentX + relativeVelocityY * tangentY) / inverseMassSum;
        float friction = (float) Math.sqrt(a.friction() * b.friction());
        float maximumFriction = Math.abs(normalImpulse) * friction;
        frictionImpulse = Math.max(-maximumFriction, Math.min(maximumFriction, frictionImpulse));

        float impulseX = tangentX * frictionImpulse;
        float impulseY = tangentY * frictionImpulse;
        a.velocity.add(-impulseX * inverseMassA, -impulseY * inverseMassA);
        b.velocity.add(impulseX * inverseMassB, impulseY * inverseMassB);
    }
}
