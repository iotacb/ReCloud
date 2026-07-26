package particle_system_demo;

import java.util.Random;

import de.kostari.cloud.core.components.Particles;
import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.scene.Scene;
import de.kostari.cloud.core.ui.Canvas;
import de.kostari.cloud.core.ui.Flex;
import de.kostari.cloud.core.ui.FlexDirection;
import de.kostari.cloud.core.ui.Panel;
import de.kostari.cloud.core.ui.Text;
import de.kostari.cloud.core.utils.Colors;
import de.kostari.cloud.core.utils.input.Keys;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.types.Color4f;
import de.kostari.cloud.core.window.Input;
import de.kostari.cloud.core.window.Time;
import de.kostari.cloud.core.window.Window;

public class ParticleSystemScene extends Scene {

    private static final Color4f BACKGROUND = Colors.from255(4, 7, 18, 255);
    private static final Color4f GRID = Colors.from255(56, 73, 112, 35);
    private static final Color4f PLATFORM = Colors.from255(38, 48, 75, 255);
    private static final Color4f PLATFORM_EDGE = Colors.from255(103, 232, 249, 150);

    private final Random random = new Random(8142);

    private GameObject fireEmitter;
    private GameObject explosionEmitter;
    private Particles fire;
    private Particles fountain;
    private Particles snow;
    private Particles explosions;

    private Text stats;
    private Text state;
    private boolean paused;
    private float fireworkTimer = 0.8f;

    @Override
    public void init() {
        Window.get().setClearColor(BACKGROUND.r, BACKGROUND.g, BACKGROUND.b, BACKGROUND.a);
        Render.postProcessing().enableBloom(0.62f, 0.48f, 2.2f);
        Render.postProcessing().enableVignette(0.3f, 0.65f, 0.25f);

        fire = createFire();
        fountain = createFountain();
        snow = createSnow();
        explosions = createExplosions();

        prewarm(fire, 1.5f);
        prewarm(fountain, 1.8f);
        prewarm(snow, 7);
        triggerExplosion(960, 300, 150);

        createHud();
        super.init();
    }

    @Override
    public void update() {
        if (Input.keyPressed(Keys.KEY_SPACE)) {
            togglePause();
        }
        if (Input.keyPressed(Keys.KEY_R)) {
            restartSystems();
        }
        if (!paused && Input.mouseButtonPressed(0)) {
            triggerExplosion(Input.getWorldMouseX(), Input.getWorldMouseY(), 180);
            fireworkTimer = 2.2f;
        }

        if (!paused) {
            fireEmitter.transform.rotation = (float) Math.sin(Time.timePassed * 1.4f) * 4;

            fireworkTimer -= Time.delta;
            if (fireworkTimer <= 0) {
                float x = 760 + random.nextFloat() * 430;
                float y = 150 + random.nextFloat() * 260;
                triggerExplosion(x, y, 130);
                fireworkTimer = 1.8f + random.nextFloat() * 1.1f;
            }
        }

        int alive = fire.getAliveParticleCount()
                + fountain.getAliveParticleCount()
                + snow.getAliveParticleCount()
                + explosions.getAliveParticleCount();
        stats.text("Live particles: " + alive + "   |   FPS: " + Math.round(Window.get().getFPS()));
        state.text(paused ? "PAUSED" : "RUNNING");
        Window.get().setTitle("Particle System Demo | " + alive + " particles | "
                + Math.round(Window.get().getFPS()) + " fps");

        super.update();
    }

    @Override
    public void draw() {
        drawBackdrop();
        super.draw();
    }

    private Particles createFire() {
        fireEmitter = new GameObject();
        fireEmitter.transform.position.set(190, 590);

        Particles particles = new Particles();
        particles.setRandomSeed(11);
        particles.main.maxParticles = 300;
        particles.main.startLifetime.set(0.7f, 1.35f);
        particles.main.startSpeed.set(55, 120);
        particles.main.startSize.set(10, 24);
        particles.main.startRotation.set(0, 360);
        particles.main.damping = 0.45f;
        particles.main.startColor.set(
                Colors.from255(255, 225, 80, 255),
                Colors.from255(255, 105, 25, 255));

        particles.emission.rateOverTime = 85;
        particles.emission.addBurst(0, 28);
        particles.shape.shape = Particles.ShapeType.CONE;
        particles.shape.angle = 28;
        particles.shape.radius = 12;
        particles.shape.direction.set(0, -1);

        particles.velocityOverLifetime.enabled = true;
        particles.velocityOverLifetime.linear.set(15, -20);
        particles.velocityOverLifetime.multiplier = time -> time;

        particles.colorOverLifetime.enabled = true;
        particles.colorOverLifetime.color.set(
                new Color4f(1, 1, 1, 1),
                new Color4f(0.85f, 0.08f, 0.01f, 0));

        particles.sizeOverLifetime.enabled = true;
        particles.sizeOverLifetime.multiplier = time -> 1 - time * 0.82f;

        particles.rotationOverLifetime.enabled = true;
        particles.rotationOverLifetime.angularVelocity.set(-120, 120);

        fireEmitter.addComponent(particles);
        return particles;
    }

    private Particles createFountain() {
        GameObject emitter = new GameObject();
        emitter.transform.position.set(500, 585);

        Particles particles = new Particles();
        particles.setRandomSeed(22);
        particles.main.maxParticles = 260;
        particles.main.simulationSpace = Particles.SimulationSpace.WORLD;
        particles.main.startLifetime.set(1.5f, 2.2f);
        particles.main.startSpeed.set(235, 330);
        particles.main.startSize.set(5, 11);
        particles.main.startRotation.set(0, 360);
        particles.main.gravity.set(0, 330);
        particles.main.startColor.set(
                Colors.from255(175, 245, 255, 255),
                Colors.from255(30, 150, 255, 255));

        particles.emission.rateOverTime = 58;
        particles.shape.shape = Particles.ShapeType.CONE;
        particles.shape.angle = 18;
        particles.shape.radius = 8;
        particles.shape.direction.set(0, -1);

        particles.colorOverLifetime.enabled = true;
        particles.colorOverLifetime.color.set(
                new Color4f(1, 1, 1, 0.95f),
                new Color4f(0.2f, 0.7f, 1, 0));

        particles.sizeOverLifetime.enabled = true;
        particles.sizeOverLifetime.multiplier = time -> 1 - time * 0.55f;

        particles.rotationOverLifetime.enabled = true;
        particles.rotationOverLifetime.angularVelocity.set(-220, 220);

        emitter.addComponent(particles);
        return particles;
    }

    private Particles createSnow() {
        GameObject emitter = new GameObject();
        emitter.transform.position.set(640, -15);

        Particles particles = new Particles();
        particles.setRandomSeed(33);
        particles.main.maxParticles = 650;
        particles.main.simulationSpace = Particles.SimulationSpace.WORLD;
        particles.main.startLifetime.set(8, 12);
        particles.main.startSpeed.set(28, 65);
        particles.main.startSize.set(3, 8);
        particles.main.startRotation.set(0, 360);
        particles.main.gravity.set(0, 5);
        particles.main.startColor.set(
                Colors.from255(255, 255, 255, 150),
                Colors.from255(160, 225, 255, 230));

        particles.emission.rateOverTime = 46;
        particles.shape.shape = Particles.ShapeType.BOX;
        particles.shape.boxSize.set(1240, 20);
        particles.shape.direction.set(0, 1);

        particles.velocityOverLifetime.enabled = true;
        particles.velocityOverLifetime.linear.set(26, 0);
        particles.velocityOverLifetime.multiplier = time ->
                (float) Math.sin(time * Math.PI * 5);

        particles.rotationOverLifetime.enabled = true;
        particles.rotationOverLifetime.angularVelocity.set(-55, 55);

        emitter.addComponent(particles);
        return particles;
    }

    private Particles createExplosions() {
        explosionEmitter = new GameObject();
        explosionEmitter.transform.position.set(960, 300);

        Particles particles = new Particles();
        particles.setRandomSeed(44);
        particles.main.playOnAwake = false;
        particles.main.maxParticles = 700;
        particles.main.simulationSpace = Particles.SimulationSpace.WORLD;
        particles.main.startLifetime.set(0.45f, 1.15f);
        particles.main.startSpeed.set(90, 310);
        particles.main.startSize.set(5, 15);
        particles.main.startRotation.set(0, 360);
        particles.main.gravity.set(0, 135);
        particles.main.damping = 0.35f;
        particles.main.startColor.set(
                Colors.from255(255, 245, 95, 255),
                Colors.from255(255, 40, 190, 255));

        particles.emission.enabled = false;
        particles.shape.shape = Particles.ShapeType.CIRCLE;
        particles.shape.radius = 8;

        particles.colorOverLifetime.enabled = true;
        particles.colorOverLifetime.color.set(
                new Color4f(1, 1, 1, 1),
                new Color4f(0.3f, 0.05f, 0.4f, 0));

        particles.sizeOverLifetime.enabled = true;
        particles.sizeOverLifetime.multiplier = time -> {
            float flashIn = Math.min(1, time * 12);
            return flashIn * (1 - time);
        };

        particles.rotationOverLifetime.enabled = true;
        particles.rotationOverLifetime.angularVelocity.set(-360, 360);

        explosionEmitter.addComponent(particles);
        return particles;
    }

    private void triggerExplosion(float x, float y, int count) {
        explosionEmitter.transform.position.set(x, y);
        explosions.emit(count);
    }

    private void togglePause() {
        paused = !paused;
        if (paused) {
            fire.pause();
            fountain.pause();
            snow.pause();
            explosions.pause();
        } else {
            fire.play();
            fountain.play();
            snow.play();
            if (explosions.isPaused()) {
                explosions.play();
            }
        }
    }

    private void restartSystems() {
        paused = false;
        restart(fire);
        restart(fountain);
        restart(snow);
        explosions.stop(true);
        fireworkTimer = 0.25f;
    }

    private static void restart(Particles particles) {
        particles.stop(true);
        particles.play();
    }

    private static void prewarm(Particles particles, float seconds) {
        float elapsed = 0;
        while (elapsed < seconds) {
            float step = Math.min(1.0f / 30.0f, seconds - elapsed);
            particles.simulate(step);
            elapsed += step;
        }
    }

    private void createHud() {
        Canvas canvas = new Canvas();

        Flex header = new Flex(FlexDirection.COLUMN);
        header.style().css(
                "padding: 14px 16px; gap: 4px; background: #071122dd; border: 1px solid #38bdf855;");

        Text title = new Text("Particle System Showcase");
        title.style().css("font-scale: 1.45; color: #f8fafc; shadow-depth: 2px;");

        stats = new Text("");
        stats.style().css("color: #a5f3fc; shadow-depth: 1px;");
        header.add(title, stats);
        canvas.append(header, 22, 20, 430, Canvas.AUTO);

        state = new Text("RUNNING");
        state.style().css(
                "padding: 9px 12px; color: #bbf7d0; background: #052e2bcc; border: 1px solid #34d39977; shadow-depth: 1px;");
        canvas.append(state, 1148, 22, 108, Canvas.AUTO);

        canvas.append(label("FIRE", "cone · color · size"), 105, 625, 170, Canvas.AUTO);
        canvas.append(label("FOUNTAIN", "gravity · world space"), 410, 625, 200, Canvas.AUTO);
        canvas.append(label("FIREWORKS", "circle · manual emit · damping"), 840, 625, 240, Canvas.AUTO);

        Text controls = new Text("CLICK  explosion     SPACE  pause / resume     R  restart");
        controls.style().css(
                "padding: 9px 14px; color: #e0f2fe; background: #0f172acc; border: 1px solid #ffffff22; shadow-depth: 1px;");
        canvas.append(controls, 386, 676, 508, Canvas.AUTO);
    }

    private static Panel label(String title, String subtitle) {
        Panel panel = new Panel();
        panel.style().css(
                "padding: 8px 10px; background: #08101fcc; border: 1px solid #64748b55;");

        Flex content = new Flex(FlexDirection.COLUMN);
        content.style().css("gap: 2px;");

        Text heading = new Text(title);
        heading.style().css("color: #f8fafc; shadow-depth: 1px;");
        Text detail = new Text(subtitle);
        detail.style().css("font-scale: 0.78; color: #94a3b8;");
        content.add(heading, detail);
        panel.add(content);
        return panel;
    }

    private static void drawBackdrop() {
        for (int x = 0; x < Window.get().getWidth(); x += 64) {
            Render.drawRect(x, 0, 1, Window.get().getHeight(), false, GRID);
        }
        for (int y = 0; y < Window.get().getHeight(); y += 64) {
            Render.drawRect(0, y, Window.get().getWidth(), 1, false, GRID);
        }

        Render.drawRect(95, 590, 190, 12, false, PLATFORM);
        Render.drawRect(95, 590, 190, 2, false, PLATFORM_EDGE);
        Render.drawRect(405, 590, 190, 12, false, PLATFORM);
        Render.drawRect(405, 590, 190, 2, false, PLATFORM_EDGE);
        Render.drawRect(730, 110, 500, 500, false, Colors.from255(13, 20, 40, 80));
    }
}
