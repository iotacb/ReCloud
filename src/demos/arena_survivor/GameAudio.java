package arena_survivor;

import de.kostari.cloud.core.utils.audio.Audio;

final class GameAudio {
    private static final String ROOT = "./demo_assets/arena_survivor/audio/";

    private final Audio playerShot = load("player-shot.ogg", 0.22f);
    private final Audio enemyShot = load("enemy-shot.ogg", 0.18f);
    private final Audio playerHurt = load("player-hurt.ogg", 0.48f);
    private final Audio enemyHit = load("enemy-hit.ogg", 0.20f);
    private final Audio waveClear = load("wave-clear.ogg", 0.55f);
    private final Audio shopBuy = load("shop-buy.ogg", 0.45f);

    void playerShot() {
        playerShot.play(true);
    }

    void enemyShot() {
        enemyShot.play(true);
    }

    void playerHurt() {
        playerHurt.play(true);
    }

    void enemyHit() {
        enemyHit.play(true);
    }

    void waveClear() {
        waveClear.play();
    }

    void shopBuy() {
        shopBuy.play(true);
    }

    void dispose() {
        playerShot.cleanUp();
        enemyShot.cleanUp();
        playerHurt.cleanUp();
        enemyHit.cleanUp();
        waveClear.cleanUp();
        shopBuy.cleanUp();
    }

    private static Audio load(String file, float gain) {
        Audio audio = new Audio(ROOT + file).load();
        audio.setGain(gain);
        return audio;
    }
}
