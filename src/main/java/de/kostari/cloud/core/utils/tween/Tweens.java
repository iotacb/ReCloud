package de.kostari.cloud.core.utils.tween;

import java.util.ArrayList;
import java.util.List;

import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.ui.UIElement;
import de.kostari.cloud.core.ui.UIRect;
import de.kostari.cloud.core.utils.math.Vector2;
import de.kostari.cloud.core.utils.types.Color4f;
import de.kostari.cloud.core.window.Time;

public final class Tweens {

    private static final List<Tween> activeTweens = new ArrayList<>();
    private static final List<Tween> pendingTweens = new ArrayList<>();
    private static boolean updating;

    private Tweens() {
    }

    public static Tween custom(float duration, TweenUpdater updater) {
        if (updater == null) {
            throw new IllegalArgumentException("Tween updater cannot be null.");
        }
        return add(new Tween(duration, updater));
    }

    public static Tween value(float from, float to, float duration, FloatSetter setter) {
        if (setter == null) {
            throw new IllegalArgumentException("Tween setter cannot be null.");
        }
        return custom(duration, progress -> setter.set(lerp(from, to, progress)));
    }

    public static Tween value(FloatGetter getter, float to, float duration, FloatSetter setter) {
        if (getter == null) {
            throw new IllegalArgumentException("Tween getter cannot be null.");
        }
        return value(getter.get(), to, duration, setter);
    }

    public static Tween vector(Vector2 target, float toX, float toY, float duration) {
        if (target == null) {
            throw new IllegalArgumentException("Tween target cannot be null.");
        }
        return vector(target, new Vector2(toX, toY), duration).target(target);
    }

    public static Tween vector(Vector2 target, Vector2 to, float duration) {
        if (target == null || to == null) {
            throw new IllegalArgumentException("Tween vectors cannot be null.");
        }
        Vector2 from = target.clone();
        Vector2 end = to.clone();
        return custom(duration, progress -> target.set(
                lerp(from.x, end.x, progress),
                lerp(from.y, end.y, progress))).target(target);
    }

    public static Tween vector(Vector2 from, Vector2 to, float duration, VectorSetter setter) {
        if (from == null || to == null || setter == null) {
            throw new IllegalArgumentException("Tween vectors and setter cannot be null.");
        }
        Vector2 start = from.clone();
        Vector2 end = to.clone();
        return custom(duration, progress -> setter.set(
                lerp(start.x, end.x, progress),
                lerp(start.y, end.y, progress)));
    }

    public static Tween color(Color4f target, Color4f to, float duration) {
        if (target == null || to == null) {
            throw new IllegalArgumentException("Tween colors cannot be null.");
        }
        Color4f from = new Color4f(target);
        Color4f end = new Color4f(to);
        return custom(duration, progress -> {
            target.r = lerp(from.r, end.r, progress);
            target.g = lerp(from.g, end.g, progress);
            target.b = lerp(from.b, end.b, progress);
            target.a = lerp(from.a, end.a, progress);
        }).target(target);
    }

    public static Tween color(Color4f from, Color4f to, float duration, ColorSetter setter) {
        if (from == null || to == null || setter == null) {
            throw new IllegalArgumentException("Tween colors and setter cannot be null.");
        }
        Color4f start = new Color4f(from);
        Color4f end = new Color4f(to);
        return custom(duration, progress -> setter.set(new Color4f(
                lerp(start.r, end.r, progress),
                lerp(start.g, end.g, progress),
                lerp(start.b, end.b, progress),
                lerp(start.a, end.a, progress))));
    }

    public static Tween move(GameObject gameObject, float toX, float toY, float duration) {
        if (gameObject == null) {
            throw new IllegalArgumentException("Tween gameObject cannot be null.");
        }
        return vector(gameObject.transform.position, toX, toY, duration).target(gameObject);
    }

    public static Tween localMove(GameObject gameObject, float toX, float toY, float duration) {
        if (gameObject == null) {
            throw new IllegalArgumentException("Tween gameObject cannot be null.");
        }
        return vector(gameObject.transform.localPosition, toX, toY, duration).target(gameObject);
    }

    public static Tween scale(GameObject gameObject, float toX, float toY, float duration) {
        if (gameObject == null) {
            throw new IllegalArgumentException("Tween gameObject cannot be null.");
        }
        return vector(gameObject.transform.scale, toX, toY, duration).target(gameObject);
    }

    public static Tween rotate(GameObject gameObject, float toDegrees, float duration) {
        if (gameObject == null) {
            throw new IllegalArgumentException("Tween gameObject cannot be null.");
        }
        return value(gameObject.transform.rotation, toDegrees, duration,
                value -> gameObject.transform.rotation = value).target(gameObject);
    }

    public static Tween width(UIElement element, float toWidth, float duration) {
        if (element == null) {
            throw new IllegalArgumentException("Tween UI element cannot be null.");
        }
        return value(element.bounds().width, toWidth, duration, value -> element.layout().width(value)).target(element);
    }

    public static Tween width(UIElement element, float fromWidth, float toWidth, float duration) {
        if (element == null) {
            throw new IllegalArgumentException("Tween UI element cannot be null.");
        }
        return value(fromWidth, toWidth, duration, value -> element.layout().width(value)).target(element);
    }

    public static Tween height(UIElement element, float toHeight, float duration) {
        if (element == null) {
            throw new IllegalArgumentException("Tween UI element cannot be null.");
        }
        return value(element.bounds().height, toHeight, duration, value -> element.layout().height(value))
                .target(element);
    }

    public static Tween height(UIElement element, float fromHeight, float toHeight, float duration) {
        if (element == null) {
            throw new IllegalArgumentException("Tween UI element cannot be null.");
        }
        return value(fromHeight, toHeight, duration, value -> element.layout().height(value)).target(element);
    }

    public static Tween size(UIElement element, float toWidth, float toHeight, float duration) {
        if (element == null) {
            throw new IllegalArgumentException("Tween UI element cannot be null.");
        }
        UIRect bounds = element.bounds();
        return size(element, bounds.width, bounds.height, toWidth, toHeight, duration);
    }

    public static Tween size(UIElement element, float fromWidth, float fromHeight,
            float toWidth, float toHeight, float duration) {
        if (element == null) {
            throw new IllegalArgumentException("Tween UI element cannot be null.");
        }
        return custom(duration, progress -> element.layout().size(
                lerp(fromWidth, toWidth, progress),
                lerp(fromHeight, toHeight, progress))).target(element);
    }

    public static Tween delay(float duration, Runnable callback) {
        return custom(duration, progress -> {
        }).onComplete(callback);
    }

    public static void update() {
        update(Time.delta);
    }

    public static void update(float delta) {
        float safeDelta = Math.max(0.0f, delta);
        updating = true;
        for (int i = activeTweens.size() - 1; i >= 0; i--) {
            Tween tween = activeTweens.get(i);
            if (tween.tick(safeDelta)) {
                activeTweens.remove(i);
            }
        }
        updating = false;

        if (!pendingTweens.isEmpty()) {
            activeTweens.addAll(pendingTweens);
            pendingTweens.clear();
        }
    }

    public static void cancel(Object target) {
        for (Tween tween : activeTweens) {
            if (tween.target() == target) {
                tween.cancel();
            }
        }
        for (Tween tween : pendingTweens) {
            if (tween.target() == target) {
                tween.cancel();
            }
        }
    }

    public static void cancelAll() {
        for (Tween tween : activeTweens) {
            tween.cancel();
        }
        for (Tween tween : pendingTweens) {
            tween.cancel();
        }
    }

    public static int activeCount() {
        return activeTweens.size() + pendingTweens.size();
    }

    static Tween add(Tween tween) {
        if (tween == null || activeTweens.contains(tween) || pendingTweens.contains(tween)) {
            return tween;
        }
        if (updating) {
            pendingTweens.add(tween);
        } else {
            activeTweens.add(tween);
        }
        return tween;
    }

    private static float lerp(float from, float to, float t) {
        return from + (to - from) * t;
    }

    @FunctionalInterface
    public interface FloatGetter {
        float get();
    }

    @FunctionalInterface
    public interface FloatSetter {
        void set(float value);
    }

    @FunctionalInterface
    public interface VectorSetter {
        void set(float x, float y);
    }

    @FunctionalInterface
    public interface ColorSetter {
        void set(Color4f color);
    }

    @FunctionalInterface
    public interface TweenUpdater {
        void update(float progress);
    }

    @FunctionalInterface
    public interface FloatCallback {
        void apply(float value);
    }
}
