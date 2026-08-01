package tower_climber;

import de.kostari.cloud.core.lighting.Light2D;
import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.utils.types.Color4f;
import de.kostari.cloud.core.window.Time;

/** Persistent, early-registered lights for high-priority tower events. */
public final class TowerLightingRig {

    private final LightNode storm = new LightNode(820, true, 30, 0.72f);
    private final LightNode combat = new LightNode(260, true, 12, 0.62f);
    private final LightNode nova = new LightNode(420, true, 24, 0.5f);

    public void updateStorm(float x, float y, float flash, int zone) {
        Color4f color = switch (Math.floorMod(zone, 4)) {
            case 1 -> new Color4f(0.48f, 1f, 0.72f, 1);
            case 2 -> new Color4f(0.76f, 0.45f, 1f, 1);
            case 3 -> new Color4f(0.56f, 0.82f, 1f, 1);
            default -> new Color4f(0.7f, 0.86f, 1f, 1);
        };
        storm.steady(x, y, color, 820, flash * flash * 3.8f);
    }

    public void flashCombat(float x, float y, Color4f color,
            float radius, float intensity, float duration) {
        combat.flash(x, y, color, radius, intensity, duration);
    }

    public void updateNovaCharge(float x, float y, float progress, boolean active) {
        if (!active) {
            nova.steady(x, y, new Color4f(0.72f, 0.42f, 1f, 1), 300, 0);
            return;
        }
        float pulse = 0.88f + 0.12f * (float) Math.sin(Time.timePassed * (12 + progress * 24));
        nova.steady(x, y,
                new Color4f(0.72f + progress * 0.18f, 0.38f + progress * 0.42f, 1f, 1),
                230 + progress * 290,
                (0.75f + progress * 2.7f) * pulse);
    }

    public void releaseNova(float x, float y) {
        nova.flash(x, y, new Color4f(0.78f, 0.62f, 1f, 1),
                920, 5.2f, 0.78f);
    }

    private static final class LightNode extends GameObject {

        private final Light2D light;
        private float flashRemaining;
        private float flashDuration;
        private float flashIntensity;

        private LightNode(float radius, boolean shadows, float softness, float shadowStrength) {
            light = addComponent(new Light2D(radius, new Color4f(1, 1, 1, 1))
                    .intensity(0)
                    .falloff(1.65f)
                    .castsShadows(shadows)
                    .softness(softness)
                    .shadowStrength(shadowStrength));
        }

        @Override
        public void update() {
            if (flashRemaining > 0) {
                flashRemaining = Math.max(0, flashRemaining - Math.min(Time.delta, 0.05f));
                float progress = flashRemaining / Math.max(0.001f, flashDuration);
                light.intensity(flashIntensity * progress * progress);
                if (flashRemaining <= 0) {
                    light.enabled(false);
                }
            }
            super.update();
        }

        private void steady(float x, float y, Color4f color, float radius, float intensity) {
            transform.position.set(x, y);
            flashRemaining = 0;
            light.color(color).radius(radius).intensity(intensity).enabled(intensity > 0.001f);
        }

        private void flash(float x, float y, Color4f color,
                float radius, float intensity, float duration) {
            transform.position.set(x, y);
            flashDuration = Math.max(0.01f, duration);
            flashRemaining = flashDuration;
            flashIntensity = Math.max(0, intensity);
            light.color(color).radius(radius).intensity(flashIntensity).enabled(true);
        }
    }
}
