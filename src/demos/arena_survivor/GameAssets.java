package arena_survivor;

import de.kostari.cloud.core.ui.UI;
import de.kostari.cloud.core.utils.render.Texture;
import de.kostari.cloud.core.utils.render.TextureSheet;
import de.kostari.cloud.core.utils.render.font.Font;

final class GameAssets {
    private static final String ROOT = "./demo_assets/arena_survivor/";

    private final Texture[] tiles;

    final Texture floor;
    final Texture player;
    final Texture slime;
    final Texture demon;
    final Texture goblin;
    final Texture ghost;
    final Texture brute;
    final Texture rat;
    final Texture sword;
    final Texture dagger;
    final Texture axe;
    final Texture hammer;
    final Texture wand;
    final Texture orb;
    final Texture pickup;

    GameAssets() {
        UI.setDefaultFont(new Font(ROOT + "fonts/PressStart2P-Regular.ttf", 28).load());
        tiles = new TextureSheet(ROOT + "sprites/tiny-dungeon.png", 16, 16).getCells();

        floor = tile(12);
        player = tile(87);
        slime = tile(108);
        demon = tile(110);
        goblin = tile(112);
        ghost = tile(121);
        brute = tile(122);
        rat = tile(123);
        sword = tile(103);
        dagger = tile(105);
        axe = tile(118);
        hammer = tile(117);
        wand = tile(130);
        orb = tile(114);
        pickup = tile(101);
    }

    Texture tile(int index) {
        return tiles[Math.clamp(index, 0, tiles.length - 1)];
    }
}
