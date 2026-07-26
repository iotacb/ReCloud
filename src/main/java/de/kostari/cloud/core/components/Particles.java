package de.kostari.cloud.core.components;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import de.kostari.cloud.core.utils.math.Vector2;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.render.Texture;
import de.kostari.cloud.core.utils.types.Color4f;
import de.kostari.cloud.core.window.Time;

/**
 * A configurable, pooled 2D particle system.
 * <p>
 * The public modules intentionally mirror the way Unity groups particle system
 * settings. Configure the modules before adding the component to a
 * {@code GameObject}, or call {@link #play()} after changing start-time values.
 * Particle positions and sizes use the same units as the rest of the renderer.
 */
public class Particles extends Component {

    private static final float MIN_DURATION = 0.0001f;
    private static final float DEGREES_TO_RADIANS = (float) (Math.PI / 180.0);

    public final MainModule main = new MainModule();
    public final EmissionModule emission = new EmissionModule();
    public final ShapeModule shape = new ShapeModule();
    public final VelocityOverLifetimeModule velocityOverLifetime = new VelocityOverLifetimeModule();
    public final ColorOverLifetimeModule colorOverLifetime = new ColorOverLifetimeModule();
    public final SizeOverLifetimeModule sizeOverLifetime = new SizeOverLifetimeModule();
    public final RotationOverLifetimeModule rotationOverLifetime = new RotationOverLifetimeModule();
    public final RendererModule renderer = new RendererModule();

    private Particle[] particlePool = new Particle[0];
    private int aliveParticleCount;
    private boolean playing;
    private boolean paused;
    private boolean emitting;
    private float cycleTime;
    private float delayRemaining;
    private float emissionAccumulator;
    private int nextBurstIndex;
    private Random random = new Random();

    @Override
    public void init() {
        ensurePoolSize();
        if (main.playOnAwake) {
            play();
        }
    }

    @Override
    public void update() {
        simulate(Time.delta);
    }

    /**
     * Advances the system by an explicit amount of time. This is useful for
     * previews, fixed-step simulation, and tests.
     */
    public void simulate(float deltaSeconds) {
        if (deltaSeconds <= 0 || paused) {
            return;
        }

        ensurePoolSize();
        updateParticles(deltaSeconds);

        if (!playing || !emitting) {
            return;
        }

        float remaining = deltaSeconds;
        if (delayRemaining > 0) {
            float consumed = Math.min(delayRemaining, remaining);
            delayRemaining -= consumed;
            remaining -= consumed;
        }

        while (remaining > 0 && playing && emitting) {
            float duration = Math.max(MIN_DURATION, main.duration);
            float untilCycleEnd = duration - cycleTime;

            if (untilCycleEnd <= 0) {
                finishCycle();
                continue;
            }

            float step = Math.min(remaining, untilCycleEnd);
            cycleTime += step;
            remaining -= step;
            emitContinuous(step);
            emitPendingBursts();

            if (cycleTime >= duration) {
                finishCycle();
            }
        }
    }

    @Override
    public void draw() {
        for (int i = 0; i < aliveParticleCount; i++) {
            Particle particle = particlePool[i];
            float x = particle.position.x;
            float y = particle.position.y;
            float rotation = particle.rotation;

            if (main.simulationSpace == SimulationSpace.LOCAL) {
                float emitterRotation = gameObject.transform.rotation;
                float radians = emitterRotation * DEGREES_TO_RADIANS;
                float cosine = (float) Math.cos(radians);
                float sine = (float) Math.sin(radians);
                float rotatedX = x * cosine - y * sine;
                float rotatedY = x * sine + y * cosine;
                x = gameObject.transform.position.x + rotatedX;
                y = gameObject.transform.position.y + rotatedY;
                rotation += emitterRotation;
            }

            if (renderer.texture == null) {
                Render.drawRotatedRect(x, y, particle.size, particle.size, true,
                        particle.renderColor, rotation);
            } else {
                Render.drawRotatedTexture(renderer.texture, x, y, particle.size,
                        particle.size, true, rotation, particle.renderColor);
            }
        }
    }

    @Override
    public void dispose() {
        stop(true);
    }

    /**
     * Starts or resumes emission. Calling this after a completed or stopped
     * system starts a new emission cycle without clearing particles that are
     * still alive.
     */
    public void play() {
        ensurePoolSize();
        if (paused) {
            paused = false;
            return;
        }

        if (!playing) {
            resetEmissionClock();
        }
        playing = true;
        emitting = true;
    }

    /**
     * Freezes particle movement, lifetimes, and emission.
     */
    public void pause() {
        if (playing || aliveParticleCount > 0) {
            paused = true;
        }
    }

    /**
     * Stops emission. Existing particles keep simulating until they expire.
     */
    public void stop() {
        stop(false);
    }

    /**
     * Stops emission and optionally removes all live particles.
     */
    public void stop(boolean clearParticles) {
        playing = false;
        paused = false;
        emitting = false;
        if (clearParticles) {
            clear();
        }
    }

    /**
     * Removes every live particle while preserving the current playback state.
     */
    public void clear() {
        aliveParticleCount = 0;
    }

    /**
     * Immediately emits particles using the current modules.
     *
     * @return the number actually emitted; this can be less than requested when
     *         the max-particle limit has been reached
     */
    public int emit(int count) {
        if (count <= 0) {
            return 0;
        }

        ensurePoolSize();
        int emitted = Math.min(count, particlePool.length - aliveParticleCount);
        for (int i = 0; i < emitted; i++) {
            spawnParticle(particlePool[aliveParticleCount++]);
        }
        return emitted;
    }

    public boolean isPlaying() {
        return playing && !paused;
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean isEmitting() {
        return emitting && playing && !paused;
    }

    public boolean isAlive() {
        return aliveParticleCount > 0 || emitting;
    }

    public int getAliveParticleCount() {
        return aliveParticleCount;
    }

    public float getCycleTime() {
        return cycleTime;
    }

    /**
     * Returns a read-only view of a live particle. Active particles occupy
     * indices {@code 0..getAliveParticleCount()-1}; the ordering is not stable
     * when particles expire.
     */
    public Particle getParticle(int index) {
        if (index < 0 || index >= aliveParticleCount) {
            throw new IndexOutOfBoundsException(index);
        }
        return particlePool[index];
    }

    /**
     * Makes randomized particle values deterministic from the next emitted
     * particle onward.
     */
    public Particles setRandomSeed(long seed) {
        random = new Random(seed);
        return this;
    }

    private void resetEmissionClock() {
        cycleTime = 0;
        emissionAccumulator = 0;
        nextBurstIndex = 0;
        delayRemaining = Math.max(0, main.startDelay.evaluate(random));
        emission.sortBursts();
    }

    private void finishCycle() {
        if (main.looping) {
            cycleTime = 0;
            nextBurstIndex = 0;
        } else {
            cycleTime = Math.max(MIN_DURATION, main.duration);
            emitting = false;
            playing = false;
        }
    }

    private void emitContinuous(float deltaSeconds) {
        if (!emission.enabled || emission.rateOverTime <= 0) {
            return;
        }

        emissionAccumulator += emission.rateOverTime * deltaSeconds;
        int count = (int) emissionAccumulator;
        if (count > 0) {
            emit(count);
            emissionAccumulator -= count;
        }
    }

    private void emitPendingBursts() {
        if (!emission.enabled) {
            return;
        }

        List<Burst> bursts = emission.bursts;
        while (nextBurstIndex < bursts.size()) {
            Burst burst = bursts.get(nextBurstIndex);
            if (burst.time > cycleTime) {
                break;
            }
            emit(burst.count.evaluateInt(random));
            nextBurstIndex++;
        }
    }

    private void updateParticles(float deltaSeconds) {
        int index = 0;
        while (index < aliveParticleCount) {
            Particle particle = particlePool[index];
            particle.age += deltaSeconds;
            if (particle.age >= particle.lifetime) {
                removeParticle(index);
                continue;
            }

            float normalizedAge = particle.age / particle.lifetime;
            particle.velocity.x += main.gravity.x * deltaSeconds;
            particle.velocity.y += main.gravity.y * deltaSeconds;

            if (main.damping > 0) {
                float damping = 1.0f / (1.0f + main.damping * deltaSeconds);
                particle.velocity.multiply(damping);
            }

            float extraVelocityX = 0;
            float extraVelocityY = 0;
            if (velocityOverLifetime.enabled) {
                float multiplier = velocityOverLifetime.multiplier.evaluate(normalizedAge);
                extraVelocityX = velocityOverLifetime.linear.x * multiplier;
                extraVelocityY = velocityOverLifetime.linear.y * multiplier;
            }

            particle.position.x += (particle.velocity.x + extraVelocityX) * deltaSeconds;
            particle.position.y += (particle.velocity.y + extraVelocityY) * deltaSeconds;

            if (rotationOverLifetime.enabled) {
                particle.rotation += particle.angularVelocity * deltaSeconds;
            }

            particle.size = particle.startSize;
            if (sizeOverLifetime.enabled) {
                particle.size *= Math.max(0, sizeOverLifetime.multiplier.evaluate(normalizedAge));
            }

            updateParticleColor(particle, normalizedAge);
            index++;
        }
    }

    private void updateParticleColor(Particle particle, float normalizedAge) {
        Color4f output = particle.renderColor;
        if (!colorOverLifetime.enabled) {
            output.r = particle.startColor.r;
            output.g = particle.startColor.g;
            output.b = particle.startColor.b;
            output.a = particle.startColor.a;
            return;
        }

        colorOverLifetime.color.evaluate(normalizedAge, output);
        output.r *= particle.startColor.r;
        output.g *= particle.startColor.g;
        output.b *= particle.startColor.b;
        output.a *= particle.startColor.a;
    }

    private void removeParticle(int index) {
        aliveParticleCount--;
        if (index != aliveParticleCount) {
            Particle dead = particlePool[index];
            particlePool[index] = particlePool[aliveParticleCount];
            particlePool[aliveParticleCount] = dead;
        }
    }

    private void spawnParticle(Particle particle) {
        particle.age = 0;
        particle.lifetime = Math.max(MIN_DURATION, main.startLifetime.evaluate(random));
        particle.startSize = Math.max(0, main.startSize.evaluate(random));
        particle.size = particle.startSize;
        particle.rotation = main.startRotation.evaluate(random);
        particle.angularVelocity = rotationOverLifetime.angularVelocity.evaluate(random);

        main.startColor.evaluate(random, particle.startColor);
        updateParticleColor(particle, 0);

        sampleShape(particle.position, particle.direction);
        if (main.simulationSpace == SimulationSpace.WORLD) {
            rotate(particle.position, gameObject.transform.rotation);
            rotate(particle.direction, gameObject.transform.rotation);
            particle.rotation += gameObject.transform.rotation;
        }
        float speed = main.startSpeed.evaluate(random);
        particle.velocity.set(particle.direction).multiply(speed);

        if (main.simulationSpace == SimulationSpace.WORLD) {
            particle.position.add(gameObject.transform.position);
        }
    }

    private void sampleShape(Vector2 position, Vector2 direction) {
        position.set(0, 0);
        direction.set(shape.direction);
        if (direction.lengthSquared() == 0) {
            direction.set(0, -1);
        } else {
            direction.normalize();
        }

        if (!shape.enabled || shape.shape == ShapeType.POINT) {
            return;
        }

        switch (shape.shape) {
            case CIRCLE -> {
                float angle = randomRange(0, (float) (Math.PI * 2));
                float distance = shape.emitFromEdge
                        ? Math.max(0, shape.radius)
                        : (float) Math.sqrt(random.nextFloat()) * Math.max(0, shape.radius);
                float x = (float) Math.cos(angle);
                float y = (float) Math.sin(angle);
                position.set(x * distance, y * distance);
                direction.set(x, y);
            }
            case BOX -> {
                float halfWidth = Math.abs(shape.boxSize.x) * 0.5f;
                float halfHeight = Math.abs(shape.boxSize.y) * 0.5f;
                position.set(randomRange(-halfWidth, halfWidth), randomRange(-halfHeight, halfHeight));
            }
            case CONE -> {
                float baseAngle = (float) Math.atan2(direction.y, direction.x);
                float spread = Math.max(0, shape.angle) * DEGREES_TO_RADIANS;
                float angle = baseAngle + randomRange(-spread * 0.5f, spread * 0.5f);
                direction.set((float) Math.cos(angle), (float) Math.sin(angle));

                float distance = shape.emitFromEdge
                        ? Math.max(0, shape.radius)
                        : random.nextFloat() * Math.max(0, shape.radius);
                position.set(direction).multiply(distance);
            }
            default -> {
            }
        }
    }

    private float randomRange(float min, float max) {
        return min + random.nextFloat() * (max - min);
    }

    private static void rotate(Vector2 vector, float angleDegrees) {
        if (angleDegrees == 0) {
            return;
        }
        float radians = angleDegrees * DEGREES_TO_RADIANS;
        float cosine = (float) Math.cos(radians);
        float sine = (float) Math.sin(radians);
        float x = vector.x;
        vector.x = x * cosine - vector.y * sine;
        vector.y = x * sine + vector.y * cosine;
    }

    private void ensurePoolSize() {
        int requestedSize = Math.max(0, main.maxParticles);
        if (particlePool.length == requestedSize) {
            return;
        }

        Particle[] resized = new Particle[requestedSize];
        int retained = Math.min(aliveParticleCount, requestedSize);
        System.arraycopy(particlePool, 0, resized, 0, retained);
        for (int i = retained; i < requestedSize; i++) {
            resized[i] = new Particle();
        }
        particlePool = resized;
        aliveParticleCount = retained;
    }

    public enum SimulationSpace {
        LOCAL,
        WORLD
    }

    public enum ShapeType {
        POINT,
        CIRCLE,
        BOX,
        CONE
    }

    /**
     * A constant or uniformly randomized float value.
     */
    public static final class MinMaxFloat {
        public float min;
        public float max;

        public MinMaxFloat(float value) {
            this(value, value);
        }

        public MinMaxFloat(float min, float max) {
            this.min = min;
            this.max = max;
        }

        public MinMaxFloat set(float value) {
            min = value;
            max = value;
            return this;
        }

        public MinMaxFloat set(float min, float max) {
            this.min = min;
            this.max = max;
            return this;
        }

        public float evaluate(Random random) {
            float low = Math.min(min, max);
            float high = Math.max(min, max);
            return low == high ? low : low + random.nextFloat() * (high - low);
        }

        private int evaluateInt(Random random) {
            int low = Math.max(0, Math.round(Math.min(min, max)));
            int high = Math.max(low, Math.round(Math.max(min, max)));
            return low == high ? low : low + random.nextInt(high - low + 1);
        }
    }

    /**
     * A constant color or a random color between two endpoints.
     */
    public static final class MinMaxColor {
        public Color4f min;
        public Color4f max;

        public MinMaxColor(Color4f color) {
            this(color, color);
        }

        public MinMaxColor(Color4f min, Color4f max) {
            this.min = new Color4f(min);
            this.max = new Color4f(max);
        }

        public MinMaxColor set(Color4f color) {
            return set(color, color);
        }

        public MinMaxColor set(Color4f min, Color4f max) {
            this.min = new Color4f(min);
            this.max = new Color4f(max);
            return this;
        }

        private void evaluate(Random random, Color4f output) {
            float t = random.nextFloat();
            output.r = lerp(min.r, max.r, t);
            output.g = lerp(min.g, max.g, t);
            output.b = lerp(min.b, max.b, t);
            output.a = lerp(min.a, max.a, t);
        }
    }

    /**
     * A normalized scalar curve. The input is clamped to {@code 0..1}.
     */
    @FunctionalInterface
    public interface Curve {
        Curve CONSTANT_ONE = time -> 1;
        Curve LINEAR = time -> time;
        Curve EASE_IN = time -> time * time;
        Curve EASE_OUT = time -> 1 - (1 - time) * (1 - time);

        float sample(float normalizedTime);

        default float evaluate(float normalizedTime) {
            return sample(Math.clamp(normalizedTime, 0, 1));
        }
    }

    /**
     * A two-color gradient evaluated over normalized particle lifetime.
     */
    public static final class Gradient {
        public Color4f start;
        public Color4f end;

        public Gradient(Color4f start, Color4f end) {
            this.start = new Color4f(start);
            this.end = new Color4f(end);
        }

        public Gradient set(Color4f start, Color4f end) {
            this.start = new Color4f(start);
            this.end = new Color4f(end);
            return this;
        }

        private void evaluate(float time, Color4f output) {
            float t = Math.clamp(time, 0, 1);
            output.r = lerp(start.r, end.r, t);
            output.g = lerp(start.g, end.g, t);
            output.b = lerp(start.b, end.b, t);
            output.a = lerp(start.a, end.a, t);
        }
    }

    public static final class MainModule {
        public float duration = 5;
        public boolean looping = true;
        public boolean playOnAwake = true;
        public int maxParticles = 1000;
        public float damping;
        public SimulationSpace simulationSpace = SimulationSpace.LOCAL;
        public final Vector2 gravity = new Vector2();
        public final MinMaxFloat startDelay = new MinMaxFloat(0);
        public final MinMaxFloat startLifetime = new MinMaxFloat(1);
        public final MinMaxFloat startSpeed = new MinMaxFloat(50);
        public final MinMaxFloat startSize = new MinMaxFloat(8);
        public final MinMaxFloat startRotation = new MinMaxFloat(0);
        public final MinMaxColor startColor = new MinMaxColor(new Color4f(1, 1, 1, 1));
    }

    public static final class EmissionModule {
        public boolean enabled = true;
        public float rateOverTime = 10;
        private final List<Burst> bursts = new ArrayList<>();

        public Burst addBurst(float time, int count) {
            return addBurst(time, count, count);
        }

        public Burst addBurst(float time, int minCount, int maxCount) {
            Burst burst = new Burst(time, minCount, maxCount);
            bursts.add(burst);
            sortBursts();
            return burst;
        }

        public void clearBursts() {
            bursts.clear();
        }

        public List<Burst> getBursts() {
            return List.copyOf(bursts);
        }

        private void sortBursts() {
            bursts.sort(Comparator.comparingDouble(burst -> burst.time));
        }
    }

    public static final class Burst {
        public float time;
        public final MinMaxFloat count;

        private Burst(float time, int minCount, int maxCount) {
            this.time = Math.max(0, time);
            this.count = new MinMaxFloat(Math.max(0, minCount), Math.max(0, maxCount));
        }
    }

    public static final class ShapeModule {
        public boolean enabled = true;
        public ShapeType shape = ShapeType.POINT;
        public float radius = 20;
        public float angle = 25;
        public boolean emitFromEdge;
        public final Vector2 boxSize = new Vector2(50, 50);
        public final Vector2 direction = new Vector2(0, -1);
    }

    public static final class VelocityOverLifetimeModule {
        public boolean enabled;
        public final Vector2 linear = new Vector2();
        public Curve multiplier = Curve.CONSTANT_ONE;
    }

    public static final class ColorOverLifetimeModule {
        public boolean enabled;
        public final Gradient color = new Gradient(
                new Color4f(1, 1, 1, 1),
                new Color4f(1, 1, 1, 0));
    }

    public static final class SizeOverLifetimeModule {
        public boolean enabled;
        public Curve multiplier = Curve.CONSTANT_ONE;
    }

    public static final class RotationOverLifetimeModule {
        public boolean enabled;
        public final MinMaxFloat angularVelocity = new MinMaxFloat(0);
    }

    public static final class RendererModule {
        public Texture texture;
    }

    /**
     * Read-only live particle data returned by {@link #getParticle(int)}.
     */
    public static final class Particle {
        private final Vector2 position = new Vector2();
        private final Vector2 velocity = new Vector2();
        private final Vector2 direction = new Vector2();
        private final Color4f startColor = new Color4f(1, 1, 1, 1);
        private final Color4f renderColor = new Color4f(1, 1, 1, 1);
        private float age;
        private float lifetime;
        private float startSize;
        private float size;
        private float rotation;
        private float angularVelocity;

        public Vector2 getPosition() {
            return new Vector2(position);
        }

        public Vector2 getVelocity() {
            return new Vector2(velocity);
        }

        public Color4f getColor() {
            return new Color4f(renderColor);
        }

        public float getAge() {
            return age;
        }

        public float getLifetime() {
            return lifetime;
        }

        public float getNormalizedAge() {
            return lifetime <= 0 ? 1 : Math.clamp(age / lifetime, 0, 1);
        }

        public float getSize() {
            return size;
        }

        public float getRotation() {
            return rotation;
        }
    }

    private static float lerp(float from, float to, float time) {
        return from + (to - from) * time;
    }
}
