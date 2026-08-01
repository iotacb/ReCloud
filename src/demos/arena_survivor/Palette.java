package arena_survivor;

import de.kostari.cloud.core.utils.Colors;
import de.kostari.cloud.core.utils.types.Color4f;

final class Palette {
    static final Color4f VOID = Colors.from255(5, 8, 18, 255);
    static final Color4f FLOOR_A = Colors.from255(15, 24, 42, 255);
    static final Color4f FLOOR_B = Colors.from255(18, 31, 50, 255);
    static final Color4f GRID = Colors.from255(54, 78, 104, 55);
    static final Color4f BORDER = Colors.from255(70, 241, 203, 210);
    static final Color4f CYAN = Colors.from255(72, 244, 218, 255);
    static final Color4f BLUE = Colors.from255(83, 166, 255, 255);
    static final Color4f VIOLET = Colors.from255(192, 105, 255, 255);
    static final Color4f GOLD = Colors.from255(255, 199, 78, 255);
    static final Color4f RED = Colors.from255(255, 82, 104, 255);
    static final Color4f GREEN = Colors.from255(105, 235, 132, 255);
    static final Color4f WHITE = Colors.from255(236, 248, 255, 255);
    static final Color4f SHADOW = Colors.from255(1, 4, 12, 130);

    private Palette() {
    }

    static Color4f alpha(Color4f color, float alpha) {
        return new Color4f(color.r, color.g, color.b, alpha);
    }
}
