package demo_ui;

import de.kostari.cloud.core.ui.Button;
import de.kostari.cloud.core.ui.ButtonSkin;
import de.kostari.cloud.core.ui.Panel;
import de.kostari.cloud.core.ui.Text;
import de.kostari.cloud.core.ui.UIMaterial;
import de.kostari.cloud.core.utils.Colors;
import de.kostari.cloud.core.utils.types.Color4f;

/** Shared visual language for the engine demos. */
public final class DemoUI {

    public static final Color4f TEXT = Colors.hex("#eef9ff");
    public static final Color4f MUTED = Colors.hex("#9cb3c3");
    public static final Color4f CYAN = Colors.hex("#56e7ff");
    public static final Color4f VIOLET = Colors.hex("#aa8cff");
    public static final Color4f GREEN = Colors.hex("#66f2ae");
    public static final Color4f GOLD = Colors.hex("#ffc66d");
    public static final Color4f RED = Colors.hex("#ff7189");

    private DemoUI() {
    }

    public static Panel card(Color4f accent) {
        Panel panel = new Panel().background(surface(accent, 12));
        panel.layout().padding(14, 16);
        return panel;
    }

    public static Panel badge(Color4f accent) {
        Panel panel = new Panel().background(new UIMaterial()
                .gradient(tint(accent, 0.18f), Colors.hex("#07111ee8"))
                .border(1, tint(accent, 0.58f))
                .radius(9)
                .glow(accent, 8, 0.18f));
        panel.layout().padding(8, 11);
        return panel;
    }

    public static UIMaterial surface(Color4f accent, float radius) {
        return new UIMaterial()
                .gradient(Colors.hex("#111d30ef"), Colors.hex("#070d18f4"))
                .border(1, tint(accent, 0.42f))
                .radius(radius)
                .glow(accent, 10, 0.13f);
    }

    public static Button button(String label, Color4f accent, Runnable action) {
        return new Button(label)
                .skin(buttonSkin(accent))
                .fontScale(0.72f)
                .onClick(action);
    }

    public static ButtonSkin buttonSkin(Color4f accent) {
        UIMaterial normal = buttonMaterial(accent, 0.13f, 0.48f);
        UIMaterial hovered = buttonMaterial(accent, 0.24f, 0.85f)
                .sheen(Colors.hex("#ffffffbb"), 0.18f, 0.42f, 0.75f);
        UIMaterial pressed = buttonMaterial(accent, 0.08f, 0.95f);
        UIMaterial disabled = new UIMaterial()
                .fill(Colors.hex("#151b25cc"))
                .border(1, Colors.hex("#75808d33"))
                .radius(8);
        return new ButtonSkin(normal, hovered, pressed, hovered, disabled,
                TEXT, TEXT, TEXT, MUTED);
    }

    public static Text heading(String value, Color4f accent) {
        return new Text(value).fontScale(1.05f).color(accent).shadow(2);
    }

    public static Text label(String value) {
        return new Text(value).fontScale(0.72f).color(TEXT).lineHeight(1.22f).shadow(1);
    }

    public static Text muted(String value) {
        return new Text(value).fontScale(0.65f).color(MUTED).lineHeight(1.22f);
    }

    public static UIMaterial meterTrack(Color4f accent) {
        return new UIMaterial()
                .fill(Colors.hex("#050b14e8"))
                .border(1, tint(accent, 0.32f))
                .radius(5);
    }

    public static UIMaterial meterFill(Color4f accent) {
        return new UIMaterial()
                .gradient(tint(accent, 0.98f), tint(accent, 0.65f))
                .radius(4)
                .glow(accent, 7, 0.5f);
    }

    public static Color4f tint(Color4f color, float alpha) {
        return new Color4f(color.r, color.g, color.b, alpha);
    }

    private static UIMaterial buttonMaterial(Color4f accent, float fillAlpha, float borderAlpha) {
        return new UIMaterial()
                .gradient(tint(accent, fillAlpha + 0.08f),
                        new Color4f(accent.r * 0.16f, accent.g * 0.16f, accent.b * 0.16f, 0.96f))
                .border(1, tint(accent, borderAlpha))
                .radius(8)
                .glow(accent, 8, fillAlpha);
    }
}
