package tower_climber;

import de.kostari.cloud.core.components.Particles;
import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.utils.Colors;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.types.Color4f;
import de.kostari.cloud.core.window.Time;

public final class RecordCelebration extends GameObject {

    private static final float DURATION = 2.5f;

    private float age;

    public RecordCelebration(float x, float y) {
        transform.position.set(x, y);

        Particles confetti = new Particles();
        confetti.main.looping = false;
        confetti.main.playOnAwake = false;
        confetti.main.duration = 0.1f;
        confetti.main.maxParticles = 84;
        confetti.main.startLifetime.set(1.15f, 2.15f);
        confetti.main.startSpeed.set(125, 330);
        confetti.main.startSize.set(3, 10);
        confetti.main.startRotation.set(0, 360);
        confetti.main.startColor.set(Colors.from255(255, 222, 112, 245));
        confetti.main.gravity.set(0, 180);
        confetti.main.damping = 0.75f;
        confetti.emission.rateOverTime = 0;
        confetti.shape.shape = Particles.ShapeType.CIRCLE;
        confetti.shape.radius = 28;
        confetti.shape.emitFromEdge = true;
        confetti.colorOverLifetime.enabled = true;
        confetti.colorOverLifetime.color.set(
                Colors.from255(255, 235, 145, 245),
                Colors.from255(190, 119, 255, 0));
        confetti.sizeOverLifetime.enabled = true;
        confetti.sizeOverLifetime.multiplier = progress -> 1 - progress * 0.72f;
        confetti.rotationOverLifetime.enabled = true;
        confetti.rotationOverLifetime.angularVelocity.set(-320, 320);
        confetti.renderer.stretch(1.9f, 0.65f);
        addComponent(confetti);
        confetti.emit(84);
    }

    @Override
    public void update() {
        age += Math.min(Time.delta, 0.05f);
        if (age >= DURATION) {
            destroy();
        }
        super.update();
    }

    @Override
    public void draw() {
        float progress = Math.min(1, age / DURATION);
        float intro = Math.min(1, progress * 8);
        float fade = Math.max(0, 1 - progress * 1.15f);
        float pulse = 0.5f + 0.5f * (float) Math.sin(age * 8.5f);
        float size = 52 + intro * 210 + pulse * 12;
        Render.drawRotatedRect(transform.position.x, transform.position.y,
                size, size, true,
                new Color4f(1f, 0.82f, 0.28f, fade * 0.055f), 45 + age * 28);
        Render.drawRotatedRect(transform.position.x, transform.position.y,
                size * 0.67f, size * 0.67f, true,
                new Color4f(0.47f, 1f, 0.88f, fade * 0.05f), 45 - age * 36);
        super.draw();
    }
}
