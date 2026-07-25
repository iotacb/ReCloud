package de.kostari.cloud.core.utils.tween;

public final class Tween {

    private final float duration;
    private final Tweens.TweenUpdater updater;

    private Object target;
    private EaseFunction ease = Ease.LINEAR;
    private Tweens.FloatCallback updateCallback;
    private Runnable startCallback;
    private Runnable completeCallback;
    private Runnable cancelCallback;

    private float delay;
    private float remainingDelay;
    private float elapsed;
    private int repeats;
    private int completedRepeats;
    private boolean infinite;
    private boolean yoyo;
    private boolean reversed;
    private boolean started;
    private boolean paused;
    private boolean cancelled;
    private boolean finished;

    Tween(float duration, Tweens.TweenUpdater updater) {
        this.duration = Math.max(0.0f, duration);
        this.updater = updater;
    }

    public Tween target(Object target) {
        this.target = target;
        return this;
    }

    public Object target() {
        return target;
    }

    public Tween ease(EaseFunction ease) {
        this.ease = ease == null ? Ease.LINEAR : ease;
        return this;
    }

    public Tween delay(float seconds) {
        this.delay = Math.max(0.0f, seconds);
        this.remainingDelay = delay;
        return this;
    }

    public Tween repeat(int times) {
        this.repeats = Math.max(0, times);
        this.infinite = false;
        return this;
    }

    public Tween loop() {
        this.infinite = true;
        return this;
    }

    public Tween yoyo() {
        this.yoyo = true;
        return this;
    }

    public Tween onStart(Runnable callback) {
        this.startCallback = callback;
        return this;
    }

    public Tween onUpdate(Tweens.FloatCallback callback) {
        this.updateCallback = callback;
        return this;
    }

    public Tween onComplete(Runnable callback) {
        this.completeCallback = callback;
        return this;
    }

    public Tween onCancel(Runnable callback) {
        this.cancelCallback = callback;
        return this;
    }

    public Tween pause() {
        this.paused = true;
        return this;
    }

    public Tween resume() {
        this.paused = false;
        return this;
    }

    public Tween restart() {
        remainingDelay = delay;
        elapsed = 0.0f;
        completedRepeats = 0;
        reversed = false;
        started = false;
        paused = false;
        cancelled = false;
        finished = false;
        Tweens.add(this);
        return this;
    }

    public void cancel() {
        if (finished || cancelled) {
            return;
        }

        cancelled = true;
        if (cancelCallback != null) {
            cancelCallback.run();
        }
    }

    public void complete() {
        if (finished || cancelled) {
            return;
        }

        if (!started) {
            start();
        }
        apply(1.0f);
        finish();
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    boolean tick(float delta) {
        if (finished || cancelled) {
            return true;
        }
        if (paused) {
            return false;
        }

        float remainingDelta = Math.max(0.0f, delta);
        if (remainingDelay > 0.0f) {
            remainingDelay -= remainingDelta;
            if (remainingDelay > 0.0f) {
                return false;
            }
            remainingDelta = -remainingDelay;
            remainingDelay = 0.0f;
        }

        if (!started) {
            start();
        }

        elapsed = duration <= 0.0f ? duration : elapsed + remainingDelta;
        float progress = duration <= 0.0f ? 1.0f : clamp(elapsed / duration);
        apply(progress);

        if (progress < 1.0f) {
            return false;
        }

        if (shouldRepeat()) {
            completedRepeats++;
            elapsed = 0.0f;
            if (yoyo) {
                reversed = !reversed;
            }
            return false;
        }

        finish();
        return true;
    }

    private void start() {
        started = true;
        if (startCallback != null) {
            startCallback.run();
        }
    }

    private void apply(float progress) {
        float directedProgress = reversed ? 1.0f - progress : progress;
        float easedProgress = ease.apply(clamp(directedProgress));
        updater.update(easedProgress);
        if (updateCallback != null) {
            updateCallback.apply(easedProgress);
        }
    }

    private boolean shouldRepeat() {
        return infinite || completedRepeats < repeats;
    }

    private void finish() {
        finished = true;
        if (completeCallback != null) {
            completeCallback.run();
        }
    }

    private float clamp(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }
}
