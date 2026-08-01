package tower_climber;

import de.kostari.cloud.core.components.Particles;
import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.utils.types.Color4f;
import de.kostari.cloud.core.window.Time;

public class ImpactBurst extends GameObject {

    private final Particles particles;
    private float lifetime = 1.15f;

    public ImpactBurst(float x, float y, Color4f start, Color4f end, int count, float speed) {
        transform.position.set(x, y);
        particles = new Particles();
        particles.main.looping = false;
        particles.main.playOnAwake = false;
        particles.main.duration = 0.08f;
        particles.main.maxParticles = Math.max(1, count);
        particles.main.startLifetime.set(0.45f, 0.9f);
        particles.main.startSpeed.set(speed * 0.55f, speed);
        particles.main.startSize.set(3, 9);
        particles.main.startRotation.set(0, 360);
        particles.main.startColor.set(start);
        particles.main.gravity.set(0, 160);
        particles.main.damping = 1.8f;
        particles.emission.rateOverTime = 0;
        particles.shape.shape = Particles.ShapeType.CIRCLE;
        particles.shape.radius = 9;
        particles.shape.emitFromEdge = true;
        particles.colorOverLifetime.enabled = true;
        particles.colorOverLifetime.color.set(start, end);
        particles.sizeOverLifetime.enabled = true;
        particles.sizeOverLifetime.multiplier = time -> 1 - time;
        particles.rotationOverLifetime.enabled = true;
        particles.rotationOverLifetime.angularVelocity.set(-220, 220);
        addComponent(particles);
        particles.emit(count);
    }

    @Override
    public void update() {
        lifetime -= Math.min(Time.delta, 0.05f);
        if (lifetime <= 0) {
            destroy();
        }
        super.update();
    }
}
