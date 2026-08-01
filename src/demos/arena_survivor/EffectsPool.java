package arena_survivor;

import de.kostari.cloud.core.components.Particles;
import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.utils.types.Color4f;

final class EffectsPool {
    private static final int POOL_SIZE = 10;

    private final FxEmitter[] emitters = new FxEmitter[POOL_SIZE];
    private int next;

    EffectsPool() {
        for (int i = 0; i < emitters.length; i++) {
            emitters[i] = new FxEmitter(i * 91L + 7);
        }
    }

    void burst(float x, float y, Color4f color, int count, float speed) {
        FxEmitter emitter = emitters[next++ % emitters.length];
        emitter.emit(x, y, color, count, speed);
    }

    private static final class FxEmitter extends GameObject {
        private final Particles particles;

        private FxEmitter(long seed) {
            particles = new Particles();
            particles.main.playOnAwake = false;
            particles.main.looping = false;
            particles.main.duration = 0.08f;
            particles.main.maxParticles = 160;
            particles.main.simulationSpace = Particles.SimulationSpace.WORLD;
            particles.main.startLifetime.set(0.28f, 0.7f);
            particles.main.startSpeed.set(80, 250);
            particles.main.startSize.set(3, 9);
            particles.main.startRotation.set(0, 360);
            particles.main.damping = 2.4f;
            particles.emission.enabled = false;
            particles.emission.rateOverTime = 0;
            particles.shape.shape = Particles.ShapeType.CIRCLE;
            particles.shape.radius = 8;
            particles.shape.emitFromEdge = true;
            particles.colorOverLifetime.enabled = true;
            particles.sizeOverLifetime.enabled = true;
            particles.sizeOverLifetime.multiplier = age -> (1 - age) * (1 - age);
            particles.rotationOverLifetime.enabled = true;
            particles.rotationOverLifetime.angularVelocity.set(-220, 220);
            particles.setRandomSeed(seed);
            addComponent(particles);
        }

        private void emit(float x, float y, Color4f color, int count, float speed) {
            transform.position.set(x, y);
            Color4f transparent = new Color4f(color.r, color.g, color.b, 0);
            particles.main.startSpeed.set(speed * 0.45f, speed);
            particles.main.startColor.set(color);
            particles.colorOverLifetime.color.set(color, transparent);
            particles.emit(Math.clamp(count, 1, 150));
        }
    }
}
