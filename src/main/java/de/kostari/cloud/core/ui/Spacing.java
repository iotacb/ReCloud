package de.kostari.cloud.core.ui;

final class Spacing {

    float top;
    float right;
    float bottom;
    float left;

    Spacing set(float all) {
        top = all;
        right = all;
        bottom = all;
        left = all;
        return this;
    }

    Spacing set(float vertical, float horizontal) {
        top = vertical;
        right = horizontal;
        bottom = vertical;
        left = horizontal;
        return this;
    }

    Spacing set(float top, float right, float bottom, float left) {
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.left = left;
        return this;
    }

    float horizontal() {
        return left + right;
    }

    float vertical() {
        return top + bottom;
    }
}
