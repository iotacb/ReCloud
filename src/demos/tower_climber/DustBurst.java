package tower_climber;

import de.kostari.cloud.core.components.Particles;
import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.utils.Colors;
import de.kostari.cloud.core.window.Time;

public final class DustBurst extends GameObject {

    private float lifetime = 0.9f;

    public DustBurst(float x, float y, int count, float speed, boolean launchBurst) {
        transform.position.set(x, y);

        Particles particles = new Particles();
        particles.main.looping = false;
        particles.main.playOnAwake = false;
        particles.main.duration = 0.05f;
        particles.main.maxParticles = Math.max(1, count);
        particles.main.startLifetime.set(0.28f, 0.62f);
        particles.main.startSpeed.set(speed * 0.55f, speed);
        particles.main.startSize.set(3, 8);
        particles.main.startRotation.set(-24, 24);
        particles.main.startColor.set(Colors.from255(115, 216, 210, 185));
        particles.main.gravity.set(0, launchBurst ? 90 : 220);
        particles.main.damping = 3.2f;
        particles.emission.rateOverTime = 0;
        particles.shape.shape = Particles.ShapeType.CONE;
        particles.shape.direction.set(0, launchBurst ? 1 : -1);
        particles.shape.angle = launchBurst ? 105 : 135;
        particles.shape.radius = 5;
        particles.colorOverLifetime.enabled = true;
        particles.colorOverLifetime.color.set(
                Colors.from255(176, 255, 235, 210),
                Colors.from255(52, 111, 124, 0));
        particles.sizeOverLifetime.enabled = true;
        particles.sizeOverLifetime.multiplier = time -> 1 - time * 0.75f;
        particles.renderer.stretch(1.6f, 0.55f);
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
