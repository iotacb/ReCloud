package de.kostari.cloud.core.ui;

import java.util.function.Consumer;

public class Slider extends Control {

    private final Panel track = new Panel();
    private final Panel fill = new Panel();
    private final Panel thumb = new Panel();
    private SliderSkin skin = SliderSkin.defaultSkin();
    private float minimum;
    private float maximum = 1;
    private float value;
    private float step;
    private float trackHeight = 6;
    private float thumbSize = 18;
    private Consumer<Float> onChange;

    public Slider() {
        layout().minSize(100, 24);
        add(track, fill, thumb);
        applySkin();
    }

    public Slider range(float minimum, float maximum) {
        if (maximum <= minimum) {
            throw new IllegalArgumentException("Slider maximum must be greater than its minimum");
        }
        this.minimum = minimum;
        this.maximum = maximum;
        return value(value);
    }

    public Slider value(float value) {
        float next = Math.clamp(value, minimum, maximum);
        if (step > 0) {
            next = minimum + Math.round((next - minimum) / step) * step;
            next = Math.clamp(next, minimum, maximum);
        }
        if (this.value != next) {
            this.value = next;
            invalidateLayout();
            if (onChange != null) {
                onChange.accept(this.value);
            }
        }
        return this;
    }

    public float value() {
        return value;
    }

    public Slider step(float value) {
        step = Math.max(0, value);
        return value(this.value);
    }

    public Slider onChange(Consumer<Float> callback) {
        onChange = callback;
        return this;
    }

    public Slider skin(SliderSkin value) {
        skin = value == null ? SliderSkin.defaultSkin() : value;
        applySkin();
        return this;
    }

    public Slider geometry(float trackHeight, float thumbSize) {
        this.trackHeight = Math.max(1, trackHeight);
        this.thumbSize = Math.max(this.trackHeight, thumbSize);
        invalidateLayout();
        return this;
    }

    public Panel track() {
        return track;
    }

    public Panel fill() {
        return fill;
    }

    public Panel thumb() {
        return thumb;
    }

    @Override
    protected UISize measureContent(UIConstraints constraints) {
        return new UISize(constraints.constrainWidth(160), constraints.constrainHeight(Math.max(24, thumbSize)));
    }

    @Override
    protected void arrangeChildren(UIRect area) {
        float centerY = area.y + area.height * 0.5f;
        float startX = area.x + thumbSize * 0.5f;
        float usableWidth = Math.max(0, area.width - thumbSize);
        float progress = (value - minimum) / (maximum - minimum);
        float thumbX = startX + usableWidth * progress;
        track.arrange(new UIRect(startX, centerY - trackHeight * 0.5f, usableWidth, trackHeight));
        fill.arrange(new UIRect(startX, centerY - trackHeight * 0.5f, usableWidth * progress, trackHeight));
        thumb.arrange(new UIRect(thumbX - thumbSize * 0.5f, centerY - thumbSize * 0.5f, thumbSize, thumbSize));
    }

    @Override
    protected void onPointerDown(float x, float y) {
        super.onPointerDown(x, y);
        updateFromPointer(x);
    }

    @Override
    protected void onPointerDrag(float x, float y) {
        if (isEnabled()) {
            updateFromPointer(x);
        }
    }

    @Override
    protected void onKeyPressed(int key) {
        if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT) {
            value(value - keyboardStep());
        } else if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT) {
            value(value + keyboardStep());
        }
    }

    @Override
    protected void onStateChanged(UIState previous, UIState current) {
        applySkin();
    }

    private void updateFromPointer(float x) {
        UIRect area = renderContentBounds();
        float usableWidth = Math.max(1, area.width - thumbSize);
        float progress = Math.clamp((x - area.x - thumbSize * 0.5f) / usableWidth, 0, 1);
        value(minimum + (maximum - minimum) * progress);
    }

    private float keyboardStep() {
        return step > 0 ? step : (maximum - minimum) / 100f;
    }

    private void applySkin() {
        track.background(skin.track());
        fill.background(skin.fill());
        thumb.background(switch (state()) {
            case HOVERED -> skin.hoveredThumb();
            case PRESSED -> skin.pressedThumb();
            case DISABLED -> skin.disabledThumb();
            default -> skin.thumb();
        });
    }
}
