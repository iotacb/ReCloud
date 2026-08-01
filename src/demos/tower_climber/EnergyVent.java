package tower_climber;

import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.lighting.Light2D;
import de.kostari.cloud.core.physics.AABB;
import de.kostari.cloud.core.utils.Colors;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.types.Color4f;
import de.kostari.cloud.core.window.Time;

public final class EnergyVent extends GameObject {

    private static final float BEAM_WIDTH = 30;
    private static final float BEAM_HEIGHT = 112;
    private static final float WARNING_DURATION = 0.72f;
    private static final float ACTIVE_DURATION = 0.5f;

    private final TowerPlatform platform;
    private final int zone;
    private final float period;
    private float clock;
    private boolean wasActive;
    private boolean debugCollider;
    private final Light2D light;

    public EnergyVent(TowerPlatform platform, float x, int zone, int difficulty, float phase) {
        this.platform = platform;
        this.zone = Math.floorMod(zone, 4);
        period = Math.max(2.05f, 2.95f - Math.max(0, difficulty - 1) * 0.055f);
        clock = Math.floorMod((int) (phase * 10_000), 10_000) / 10_000f * period;
        transform.position.set(x, platform.top());
        Color4f lightColor = accentColor(255);
        light = addComponent(new Light2D(145, lightColor)
                .intensity(0.28f)
                .falloff(1.9f)
                .softness(8)
                .shadowStrength(0.5f));
    }

    @Override
    public void update() {
        clock += Math.min(Time.delta, 0.05f);
        if (clock >= period) {
            clock %= period;
        }
        boolean active = isActive();
        float warning = isWarning() ? 0.85f + warningProgress() * 0.75f : 0;
        light.intensity(active ? 2.1f * activeEnvelope() : Math.max(0.24f, warning))
                .radius(active ? 245 : isWarning() ? 185 : 120);
        if (active && !wasActive) {
            Color4f accent = accentColor(220);
            new ImpactBurst(transform.position.x, transform.position.y - 4,
                    accent, new Color4f(accent.r, accent.g, accent.b, 0), 8, 115);
        }
        wasActive = active;
        super.update();
    }

    @Override
    public void draw() {
        float pulse = 0.5f + 0.5f * (float) Math.sin(clock * 22);
        Color4f accent = accentColor(255);
        Color4f base = baseColor();
        Render.drawRect(transform.position.x, transform.position.y - 3,
                44, 9, true, base);
        Render.drawRect(transform.position.x, transform.position.y - 9,
                22, 4, true, new Color4f(accent.r, accent.g, accent.b, 0.35f + pulse * 0.25f));
        Render.drawRotatedRect(transform.position.x, transform.position.y - 13,
                8 + pulse * 2, 8 + pulse * 2, true,
                new Color4f(accent.r, accent.g, accent.b, 0.26f + pulse * 0.26f), 45);
        if (isWarning() || isActive()) {
            Color4f warningLight = Colors.from255(255, 102, 66,
                    Math.round(145 + pulse * 105));
            Render.drawRect(transform.position.x - 15, transform.position.y - 8,
                    5, 5, true, warningLight);
            Render.drawRect(transform.position.x + 15, transform.position.y - 8,
                    5, 5, true, warningLight);
        }

        if (isWarning()) {
            float charge = warningProgress();
            Color4f preview = new Color4f(accent.r, accent.g, accent.b,
                    0.09f + pulse * 0.08f + charge * 0.1f);
            for (int segment = 0; segment < 5; segment++) {
                Render.drawRect(transform.position.x,
                        transform.position.y - 25 - segment * 21,
                        6 + charge * 5, 11, true, preview);
            }
        } else if (isActive()) {
            float flare = activeEnvelope();
            float centerY = transform.position.y - BEAM_HEIGHT * 0.5f;
            Render.drawRect(transform.position.x, centerY,
                    BEAM_WIDTH + pulse * 8, BEAM_HEIGHT, true,
                    new Color4f(accent.r, accent.g, accent.b, 0.2f * flare));
            Render.drawRect(transform.position.x, centerY,
                    7 + pulse * 3, BEAM_HEIGHT, true,
                    new Color4f(0.9f, 1f, 1f, 0.74f * flare));
            for (int segment = 0; segment < 5; segment++) {
                float y = transform.position.y - 18 - segment * 23;
                Render.drawRotatedRect(transform.position.x, y,
                        14 + pulse * 5, 14 + pulse * 5, true,
                        new Color4f(accent.r, accent.g, accent.b, 0.38f * flare), 45);
            }
        }

        if (debugCollider && isActive()) {
            drawBounds(bounds());
        }
        super.draw();
    }

    public boolean overlaps(Player player) {
        return isActive() && bounds().overlaps(player.getBody().bounds());
    }

    public boolean isWarning() {
        float safeDuration = period - WARNING_DURATION - ACTIVE_DURATION;
        return clock >= safeDuration && clock < safeDuration + WARNING_DURATION;
    }

    public boolean isActive() {
        return clock >= period - ACTIVE_DURATION;
    }

    public AABB bounds() {
        return AABB.fromCenter(transform.position.x,
                transform.position.y - BEAM_HEIGHT * 0.5f, BEAM_WIDTH, BEAM_HEIGHT);
    }

    public TowerPlatform platform() {
        return platform;
    }

    public void setDebugCollider(boolean debugCollider) {
        this.debugCollider = debugCollider;
    }

    private float warningProgress() {
        float safeDuration = period - WARNING_DURATION - ACTIVE_DURATION;
        return Math.max(0, Math.min(1, (clock - safeDuration) / WARNING_DURATION));
    }

    private float activeEnvelope() {
        float progress = Math.max(0, Math.min(1,
                (clock - (period - ACTIVE_DURATION)) / ACTIVE_DURATION));
        return 0.55f + 0.45f * (float) Math.sin(progress * Math.PI);
    }

    private Color4f accentColor(int alpha) {
        return switch (zone) {
            case 1 -> Colors.from255(112, 255, 170, alpha);
            case 2 -> Colors.from255(216, 132, 255, alpha);
            case 3 -> Colors.from255(132, 220, 255, alpha);
            default -> Colors.from255(255, 194, 91, alpha);
        };
    }

    private Color4f baseColor() {
        return switch (zone) {
            case 1 -> Colors.from255(31, 73, 58, 255);
            case 2 -> Colors.from255(65, 38, 80, 255);
            case 3 -> Colors.from255(35, 57, 84, 255);
            default -> Colors.from255(79, 58, 35, 255);
        };
    }

    private void drawBounds(AABB box) {
        Color4f debug = Colors.from255(255, 90, 90, 230);
        Render.drawRect(box.left(), box.top(), box.width(), 1, debug);
        Render.drawRect(box.left(), box.bottom() - 1, box.width(), 1, debug);
        Render.drawRect(box.left(), box.top(), 1, box.height(), debug);
        Render.drawRect(box.right() - 1, box.top(), 1, box.height(), debug);
    }
}
