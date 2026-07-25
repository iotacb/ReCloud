package de.kostari.cloud.core.utils.tween;

@FunctionalInterface
public interface EaseFunction {

    float apply(float t);
}
