package tower_climber;

import de.kostari.cloud.core.components.Particles;
import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.utils.Colors;
import de.kostari.cloud.core.window.Time;

public final class DashBurst extends GameObject {

    private float lifetime = 0.7f;

    public DashBurst(float x, float y, float direction) {
        transform.position.set(x, y);

        Particles particles = new Particles();
        particles.main.looping = false;
        particles.main.playOnAwake = false;
        particles.main.duration = 0.04f;
        particles.main.maxParticles = 18;
        particles.main.startLifetime.set(0.18f, 0.42f);
        particles.main.startSpeed.set(150, 330);
        particles.main.startSize.set(3, 8);
        particles.main.startRotation.set(-12, 12);
        particles.main.startColor.set(Colors.from255(132, 255, 235, 220));
        particles.main.gravity.set(0, 45);
        particles.main.damping = 2.2f;
        particles.emission.rateOverTime = 0;
        particles.shape.shape = Particles.ShapeType.CONE;
        particles.shape.direction.set(-Math.signum(direction), 0);
        particles.shape.angle = 38;
        particles.shape.radius = 8;
        particles.colorOverLifetime.enabled = true;
        particles.colorOverLifetime.color.set(
                Colors.from255(184, 255, 246, 230),
                Colors.from255(74, 122, 190, 0));
        particles.sizeOverLifetime.enabled = true;
        particles.sizeOverLifetime.multiplier = progress -> 1 - progress * 0.82f;
        particles.renderer.stretch(2.4f, 0.7f);
        addComponent(particles);
        particles.emit(18);
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
