package tower_climber;

import de.kostari.cloud.core.components.Particles;
import de.kostari.cloud.core.components.Particles.SimulationSpace;
import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.scene.Camera;
import de.kostari.cloud.core.scene.SceneManager;
import de.kostari.cloud.core.utils.Colors;
import de.kostari.cloud.core.window.Window;

public class RainParticles extends GameObject {

    private static final float SPAWN_MARGIN = 90;

    private final Particles particles;
    private final float baseEmissionRate;
    private final float baseSpeed;

    public RainParticles(int maximumDrops, float fallSpeed) {
        int count = Math.max(32, maximumDrops);
        float speed = Math.max(240, fallSpeed);
        baseSpeed = speed;
        float viewportHeight = Window.get().getHeight();
        float travelTime = (viewportHeight + SPAWN_MARGIN * 2) / speed;

        particles = new Particles();
        particles.setRandomSeed(0xC10D_5EEDL);
        particles.main.looping = true;
        particles.main.playOnAwake = true;
        particles.main.duration = 1;
        particles.main.maxParticles = count;
        particles.main.simulationSpace = SimulationSpace.WORLD;
        particles.main.startLifetime.set(travelTime * 0.9f, travelTime * 1.15f);
        particles.main.startSpeed.set(speed * 0.9f, speed * 1.1f);
        particles.main.startSize.set(4, 7);
        particles.main.startRotation.set(-8, -8);
        particles.main.startColor.set(Colors.from255(160, 220, 235, 190));
        particles.main.gravity.set(0, 55);
        particles.main.damping = 0;

        baseEmissionRate = count / Math.max(0.1f, travelTime);
        particles.emission.rateOverTime = baseEmissionRate;
        particles.shape.shape = Particles.ShapeType.BOX;
        particles.shape.direction.set(0.14f, 1);
        particles.shape.emitFromEdge = false;

        particles.colorOverLifetime.enabled = true;
        particles.colorOverLifetime.color.set(
                Colors.from255(190, 236, 245, 220),
                Colors.from255(70, 125, 160, 35));
        particles.sizeOverLifetime.enabled = true;
        particles.sizeOverLifetime.multiplier = time -> 0.72f + time * 0.28f;
        particles.renderer.stretch(0.34f, 4.6f);

        addComponent(particles);
        prewarm(count);
    }

    @Override
    public void update() {
        positionEmitterAboveCamera();
        particles.shape.boxSize.set(Window.get().getWidth() + SPAWN_MARGIN * 2, 24);
        super.update();
    }

    public int activeDropCount() {
        return particles.getAliveParticleCount();
    }

    public void setIntensity(float intensity) {
        float scale = Math.max(0.35f, Math.min(1.8f, intensity));
        particles.emission.rateOverTime = baseEmissionRate * scale;
        particles.main.startSpeed.set(baseSpeed * (0.84f + scale * 0.08f),
                baseSpeed * (1.02f + scale * 0.1f));
    }

    private void prewarm(int count) {
        Camera camera = SceneManager.current().getCamera();
        float cameraX = camera == null ? 0 : camera.transform.position.x;
        float cameraY = camera == null ? 0 : camera.transform.position.y;
        transform.position.set(
                cameraX + Window.get().getWidth() * 0.5f,
                cameraY + Window.get().getHeight() * 0.5f);
        particles.shape.boxSize.set(
                Window.get().getWidth() + SPAWN_MARGIN * 2,
                Window.get().getHeight() + SPAWN_MARGIN * 2);
        particles.emit(count);
    }

    private void positionEmitterAboveCamera() {
        Camera camera = SceneManager.current().getCamera();
        float cameraX = camera == null ? 0 : camera.transform.position.x;
        float cameraY = camera == null ? 0 : camera.transform.position.y;
        transform.position.set(
                cameraX + Window.get().getWidth() * 0.5f,
                cameraY - SPAWN_MARGIN);
    }
}
