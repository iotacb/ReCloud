package de.kostari.cloud.core.ui;

import de.kostari.cloud.core.utils.types.Color4f;

public class Button extends Control {

    private final Panel panel = new Panel();
    private final Text text;
    private ButtonSkin skin = ButtonSkin.defaultSkin();
    private Runnable onClick;

    public Button(String label) {
        text = new Text(label)
                .align(TextAlign.CENTER)
                .verticalAlign(AlignItems.CENTER)
                .wrap(false);
        panel.layout().padding(10, 14);
        panel.add(text);
        add(panel);
        applySkin();
    }

    public Button label(String value) {
        text.text(value);
        return this;
    }

    public String label() {
        return text.text();
    }

    public Button onClick(Runnable callback) {
        onClick = callback;
        return this;
    }

    public Button skin(ButtonSkin value) {
        skin = value == null ? ButtonSkin.defaultSkin() : value;
        applySkin();
        return this;
    }

    public Button fontScale(float value) {
        text.fontScale(value);
        return this;
    }

    public Button textColor(Color4f value) {
        text.color(value);
        return this;
    }

    public Panel panel() {
        return panel;
    }

    public Text textElement() {
        return text;
    }

    @Override
    public Button enabled(boolean value) {
        super.enabled(value);
        return this;
    }

    @Override
    protected void onPointerUp(float x, float y, boolean inside) {
        boolean activate = isEnabled() && isPressed() && inside;
        super.onPointerUp(x, y, inside);
        if (activate && onClick != null) {
            onClick.run();
        }
    }

    @Override
    protected void onKeyPressed(int key) {
        if (isEnabled() && (key == org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER
                || key == org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE) && onClick != null) {
            onClick.run();
        }
    }

    @Override
    protected void onStateChanged(UIState previous, UIState current) {
        applySkin();
    }

    private void applySkin() {
        panel.background(skin.drawable(state()));
        text.color(skin.textColor(state()));
    }
}
