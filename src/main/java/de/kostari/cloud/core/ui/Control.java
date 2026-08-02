package de.kostari.cloud.core.ui;

public abstract class Control extends UIElement {

    private boolean enabled = true;
    private boolean hovered;
    private boolean pressed;
    private boolean focused;
    private UIState state = UIState.NORMAL;

    protected Control() {
        pointerEvents(true);
    }

    public Control enabled(boolean value) {
        if (enabled != value) {
            enabled = value;
            if (!enabled) {
                hovered = false;
                pressed = false;
            }
            updateState();
        }
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isHovered() {
        return hovered;
    }

    public boolean isPressed() {
        return pressed;
    }

    public boolean isFocused() {
        return focused;
    }

    public UIState state() {
        return state;
    }

    @Override
    protected boolean isFocusable() {
        return enabled;
    }

    @Override
    protected void onPointerEnter() {
        if (enabled) {
            hovered = true;
            updateState();
        }
    }

    @Override
    protected void onPointerExit() {
        hovered = false;
        updateState();
    }

    @Override
    protected void onPointerDown(float x, float y) {
        if (enabled) {
            pressed = true;
            updateState();
        }
    }

    @Override
    protected void onPointerUp(float x, float y, boolean inside) {
        pressed = false;
        updateState();
    }

    @Override
    protected void onFocusChanged(boolean value) {
        focused = value;
        updateState();
    }

    protected void onStateChanged(UIState previous, UIState current) {
    }

    private void updateState() {
        UIState next;
        if (!enabled) {
            next = UIState.DISABLED;
        } else if (pressed) {
            next = UIState.PRESSED;
        } else if (hovered) {
            next = UIState.HOVERED;
        } else if (focused) {
            next = UIState.FOCUSED;
        } else {
            next = UIState.NORMAL;
        }
        if (state != next) {
            UIState previous = state;
            state = next;
            onStateChanged(previous, next);
            invalidatePaint();
        }
    }
}
