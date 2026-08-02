package de.kostari.cloud.core.ui;

import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;

import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.types.Color4f;
import de.kostari.cloud.core.window.Time;

public class TextBox extends Control {

    private final Panel panel = new Panel();
    private final Text valueText = new Text("").verticalAlign(AlignItems.CENTER).wrap(false);
    private final Text placeholderText = new Text("").verticalAlign(AlignItems.CENTER).wrap(false);
    private TextBoxSkin skin = TextBoxSkin.defaultSkin();
    private final StringBuilder value = new StringBuilder();
    private int caret;
    private int maxLength = Integer.MAX_VALUE;
    private Consumer<String> onChange;
    private Consumer<String> onSubmit;

    public TextBox() {
        this("");
    }

    public TextBox(String placeholder) {
        layout().minSize(140, 44);
        panel.layout().padding(10, 12);
        panel.clipChildren(true);
        placeholderText.text(placeholder);
        panel.add(valueText, placeholderText);
        add(panel);
        updateContent();
        applySkin();
    }

    public TextBox text(String text) {
        value.setLength(0);
        if (text != null) {
            value.append(text, 0, Math.min(text.length(), maxLength));
        }
        caret = value.length();
        changed();
        return this;
    }

    public String text() {
        return value.toString();
    }

    public TextBox placeholder(String text) {
        placeholderText.text(text);
        updateContent();
        return this;
    }

    public TextBox maxLength(int value) {
        maxLength = Math.max(0, value);
        if (this.value.length() > maxLength) {
            this.value.setLength(maxLength);
            caret = Math.min(caret, maxLength);
            changed();
        }
        return this;
    }

    public TextBox onChange(Consumer<String> callback) {
        onChange = callback;
        return this;
    }

    public TextBox onSubmit(Consumer<String> callback) {
        onSubmit = callback;
        return this;
    }

    public TextBox skin(TextBoxSkin value) {
        skin = value == null ? TextBoxSkin.defaultSkin() : value;
        applySkin();
        return this;
    }

    public Panel panel() {
        return panel;
    }

    public Text textElement() {
        return valueText;
    }

    public Text placeholderElement() {
        return placeholderText;
    }

    @Override
    protected void onPointerDown(float x, float y) {
        super.onPointerDown(x, y);
        caret = caretAt(x);
        invalidatePaint();
    }

    @Override
    protected void onKeyPressed(int key) {
        switch (key) {
            case GLFW.GLFW_KEY_BACKSPACE -> {
                if (caret > 0) {
                    value.deleteCharAt(--caret);
                    changed();
                }
            }
            case GLFW.GLFW_KEY_DELETE -> {
                if (caret < value.length()) {
                    value.deleteCharAt(caret);
                    changed();
                }
            }
            case GLFW.GLFW_KEY_LEFT -> {
                caret = Math.max(0, caret - 1);
                invalidatePaint();
            }
            case GLFW.GLFW_KEY_RIGHT -> {
                caret = Math.min(value.length(), caret + 1);
                invalidatePaint();
            }
            case GLFW.GLFW_KEY_HOME -> {
                caret = 0;
                invalidatePaint();
            }
            case GLFW.GLFW_KEY_END -> {
                caret = value.length();
                invalidatePaint();
            }
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
                if (onSubmit != null) {
                    onSubmit.accept(value.toString());
                }
            }
            default -> {
            }
        }
    }

    @Override
    protected void onTextInput(int codepoint) {
        if (codepoint >= 32 && codepoint != 127 && value.length() < maxLength) {
            String inserted = new String(Character.toChars(codepoint));
            value.insert(caret, inserted);
            caret += inserted.length();
            changed();
        }
    }

    @Override
    protected void onFocusChanged(boolean focused) {
        super.onFocusChanged(focused);
        updateContent();
    }

    @Override
    protected void onStateChanged(UIState previous, UIState current) {
        applySkin();
    }

    @Override
    protected void drawOverlay() {
        if (!isFocused() || !isEnabled() || (int) (Time.timePassed * 2) % 2 != 0) {
            return;
        }
        Insets padding = panel.layout().padding();
        float x = renderBounds().x + padding.left()
                + Render.getTextWidth(valueText.font(), value.substring(0, caret), valueText.fontScale());
        float height = Render.getTextHeight(valueText.font()) * valueText.fontScale();
        float y = renderBounds().y + Math.max(padding.top(), (renderBounds().height - height) * 0.5f);
        Color4f caretColor = Drawables.alpha(skin.caret(), renderOpacity());
        Render.drawRect(x, y, 1.5f, height, false, caretColor);
    }

    private int caretAt(float pointerX) {
        float x = renderBounds().x + panel.layout().padding().left();
        for (int i = 0; i < value.length(); i++) {
            float width = Render.getTextWidth(valueText.font(), value.substring(i, i + 1), valueText.fontScale());
            if (pointerX < x + width * 0.5f) {
                return i;
            }
            x += width;
        }
        return value.length();
    }

    private void changed() {
        updateContent();
        if (onChange != null) {
            onChange.accept(value.toString());
        }
    }

    private void updateContent() {
        valueText.text(value.toString());
        placeholderText.visible(value.isEmpty() && !isFocused());
    }

    private void applySkin() {
        panel.background(switch (state()) {
            case HOVERED -> skin.hovered();
            case FOCUSED, PRESSED -> skin.focused();
            case DISABLED -> skin.disabled();
            default -> skin.normal();
        });
        valueText.color(skin.text());
        placeholderText.color(skin.placeholder());
    }
}
