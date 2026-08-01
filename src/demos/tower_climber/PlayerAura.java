package tower_climber;

import de.kostari.cloud.core.components.Particles;
import de.kostari.cloud.core.components.Particles.SimulationSpace;
import de.kostari.cloud.core.lighting.Light2D;
import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.utils.Colors;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.types.Color4f;
import de.kostari.cloud.core.window.Time;

public final class PlayerAura extends GameObject {

    private final Player player;
    private final Particles motes;
    private final Light2D light;
    private int powerLevel = 1;
    private boolean active = true;

    public PlayerAura(Player player) {
        this.player = player;

        motes = new Particles();
        motes.setRandomSeed(0xA11A_C10DL);
        motes.main.looping = true;
        motes.main.playOnAwake = true;
        motes.main.duration = 1;
        motes.main.maxParticles = 90;
        motes.main.simulationSpace = SimulationSpace.WORLD;
        motes.main.startLifetime.set(0.35f, 0.72f);
        motes.main.startSpeed.set(16, 48);
        motes.main.startSize.set(2, 5);
        motes.main.startRotation.set(0, 360);
        motes.main.startColor.set(Colors.from255(255, 255, 255, 220));
        motes.main.gravity.set(0, -24);
        motes.main.damping = 2.4f;
        motes.emission.rateOverTime = 2;
        motes.shape.shape = Particles.ShapeType.CIRCLE;
        motes.shape.radius = 17;
        motes.shape.emitFromEdge = true;
        motes.colorOverLifetime.enabled = true;
        motes.colorOverLifetime.color.set(
                Colors.from255(111, 255, 226, 220),
                Colors.from255(190, 119, 255, 0));
        motes.sizeOverLifetime.enabled = true;
        motes.sizeOverLifetime.multiplier = time -> 1 - time * 0.85f;
        motes.rotationOverLifetime.enabled = true;
        motes.rotationOverLifetime.angularVelocity.set(-180, 180);
        addComponent(motes);
        light = addComponent(new Light2D(180, new Color4f(0.38f, 1f, 0.84f, 1))
                .intensity(0.42f)
                .falloff(1.7f)
                .softness(12)
                .shadowStrength(0.58f));
    }

    @Override
    public void update() {
        transform.position.set(player.transform.position);
        float movementBoost = Math.min(7, Math.abs(player.getBody().velocity.x) / 65f);
        motes.emission.rateOverTime = active ? 1 + Math.max(0, powerLevel - 1) * 2.6f + movementBoost : 0;
        float channelBoost = player.isChanneling() ? 1.35f : 0;
        float pulse = 0.93f + 0.07f * (float) Math.sin(Time.timePassed * 5.2f);
        light.intensity(active ? (0.32f + Math.min(0.72f, powerLevel * 0.07f) + channelBoost) * pulse : 0)
                .radius(165 + Math.min(80, powerLevel * 6) + (player.isChanneling() ? 115 : 0))
                .enabled(active);
        super.update();
    }

    @Override
    public void draw() {
        if (active && powerLevel > 1) {
            float pulse = 0.5f + 0.5f * (float) Math.sin(Time.timePassed * 5.2f);
            float size = 43 + Math.min(18, powerLevel * 1.8f) + pulse * 5;
            Color4f glow = new Color4f(0.38f, 1f, 0.87f,
                    0.025f + Math.min(0.055f, powerLevel * 0.006f));
            Render.drawRotatedRect(player.transform.position.x, player.transform.position.y,
                    size, size, true, glow, 45 + Time.timePassed * 12);
        }
        super.draw();
    }

    public void setPowerLevel(int powerLevel) {
        this.powerLevel = Math.max(1, powerLevel);
    }

    public void burst(int count) {
        if (active) {
            transform.position.set(player.transform.position);
            motes.emit(Math.max(0, count));
        }
    }

    public void setActive(boolean active) {
        this.active = active;
        if (!active) {
            motes.stop();
            light.enabled(false);
        }
    }
}
