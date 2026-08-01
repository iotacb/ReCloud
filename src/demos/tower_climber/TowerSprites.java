package tower_climber;

import de.kostari.cloud.core.utils.render.Texture;
import de.kostari.cloud.core.utils.render.TextureSheet;

public final class TowerSprites {

    private static final String ROOT = "./demo_assets/tower_climber/";

    private static Texture basicEnemy;
    private static Texture flyingEnemy;
    private static Texture evaderEnemy;
    private static Texture xpCrystal;
    private static Texture[] playerIdle;
    private static Texture[] playerRun;
    private static Texture[] playerJump;
    private static Texture[] playerFall;

    private TowerSprites() {
    }

    public static void load() {
        if (basicEnemy != null && playerIdle != null) {
            return;
        }
        basicEnemy = new Texture(ROOT + "enemy-basic.png").load();
        flyingEnemy = new Texture(ROOT + "enemy-flying.png").load();
        evaderEnemy = new Texture(ROOT + "enemy-evader.png").load();
        xpCrystal = new Texture(ROOT + "xp-crystal.png").load();
        playerIdle = new TextureSheet(ROOT + "idle.png", 128, 128).getRow(0);
        playerRun = new TextureSheet(ROOT + "run.png", 128, 128).getRow(0);
        playerJump = new TextureSheet(ROOT + "jump.png", 128, 128).getRow(0);
        playerFall = new TextureSheet(ROOT + "fall.png", 128, 128).getRow(0);
    }

    public static Texture enemy(Enemy.Type type) {
        load();
        return switch (type) {
            case BASIC -> basicEnemy;
            case FLYING -> flyingEnemy;
            case EVADER -> evaderEnemy;
        };
    }

    public static Texture xpCrystal() {
        load();
        return xpCrystal;
    }

    public static Texture[] playerIdle() {
        load();
        return playerIdle.clone();
    }

    public static Texture[] playerRun() {
        load();
        return playerRun.clone();
    }

    public static Texture[] playerJump() {
        load();
        return playerJump.clone();
    }

    public static Texture[] playerFall() {
        load();
        return playerFall.clone();
    }
}
