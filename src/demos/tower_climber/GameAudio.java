package tower_climber;

import de.kostari.cloud.core.utils.audio.Audio;

public final class GameAudio {

    private static final String ROOT = "./demo_assets/tower_climber/";

    private final Audio music = load("music-tower.ogg", 0.24f);
    private final Audio jump = load("sfx-jump.ogg", 0.35f);
    private final Audio stomp = load("sfx-stomp.ogg", 0.62f);
    private final Audio comboChime = load("sfx-xp.ogg", 0.18f, 1.08f);
    private final Audio hurt = load("sfx-hurt.ogg", 0.55f);
    private final Audio xp = load("sfx-xp.ogg", 0.28f);
    private final Audio boost = load("sfx-jump.ogg", 0.58f, 0.72f);
    private final Audio boostSpark = load("sfx-xp.ogg", 0.24f, 1.28f);
    private final Audio slash = load("sfx-jump.ogg", 0.3f, 1.42f);
    private final Audio slashImpact = load("sfx-stomp.ogg", 0.48f, 1.24f);
    private final Audio dash = load("sfx-jump.ogg", 0.42f, 1.62f);
    private final Audio dashSpark = load("sfx-xp.ogg", 0.16f, 1.46f);
    private final Audio bow = load("sfx-jump.ogg", 0.32f, 1.18f);
    private final Audio shuriken = load("sfx-jump.ogg", 0.24f, 1.72f);
    private final Audio unlock = load("sfx-xp.ogg", 0.42f, 0.78f);
    private final Audio novaCharge = load("sfx-level-up-energy.ogg", 0.46f, 0.66f);
    private final Audio novaImpact = load("sfx-level-up.ogg", 0.82f, 0.61f);
    private final Audio novaTail = load("sfx-level-up-energy.ogg", 0.48f, 0.72f);
    private final Audio levelUpEnergy = load("sfx-level-up-energy.ogg", 0.52f, 0.82f);
    private final Audio levelUpImpact = load("sfx-level-up.ogg", 0.72f, 0.76f);
    private final Audio gameOver = load("sfx-game-over.ogg", 0.55f);

    public GameAudio() {
        music.setLooping(true);
        music.play();
    }

    public void jump() {
        jump.play(true);
    }

    public void stomp(int combo) {
        stomp.setPitch(0.94f + Math.min(0.24f, Math.max(0, combo - 1) * 0.035f));
        stomp.play();
        if (combo >= 2) {
            comboChime.setPitch(1.04f + Math.min(0.38f, combo * 0.055f));
            comboChime.play();
        }
    }

    public void hurt() {
        hurt.play(true);
    }

    public void collectXp() {
        xp.play(true);
    }

    public void boost() {
        boost.play();
        boostSpark.play();
    }

    public void slash(boolean hit) {
        slash.play(true);
        if (hit) {
            slashImpact.play(true);
        }
    }

    public void dash() {
        dash.play(true);
        dashSpark.play(true);
    }

    public void bow() {
        bow.play(true);
    }

    public void shuriken() {
        shuriken.play(true);
    }

    public void unlock() {
        unlock.play(true);
    }

    public void novaCharge() {
        novaCharge.play();
    }

    public void novaImpact() {
        novaImpact.play();
        novaTail.play();
    }

    public void levelUp() {
        levelUpEnergy.play();
        levelUpImpact.play();
    }

    public void gameOver(boolean newRecord) {
        music.stop();
        if (newRecord) {
            levelUpEnergy.setPitch(0.94f);
            levelUpImpact.setPitch(0.88f);
            comboChime.setPitch(1.35f);
            levelUpEnergy.play();
            levelUpImpact.play();
            comboChime.play();
        } else {
            gameOver.play();
        }
    }

    public void setPaused(boolean paused) {
        music.setGain(paused ? 0.09f : 0.24f);
    }

    public void dispose() {
        music.cleanUp();
        jump.cleanUp();
        stomp.cleanUp();
        comboChime.cleanUp();
        hurt.cleanUp();
        xp.cleanUp();
        boost.cleanUp();
        boostSpark.cleanUp();
        slash.cleanUp();
        slashImpact.cleanUp();
        dash.cleanUp();
        dashSpark.cleanUp();
        bow.cleanUp();
        shuriken.cleanUp();
        unlock.cleanUp();
        novaCharge.cleanUp();
        novaImpact.cleanUp();
        novaTail.cleanUp();
        levelUpEnergy.cleanUp();
        levelUpImpact.cleanUp();
        gameOver.cleanUp();
    }

    private Audio load(String file, float gain) {
        return load(file, gain, 1);
    }

    private Audio load(String file, float gain, float pitch) {
        Audio audio = new Audio(ROOT + file).load();
        audio.setGain(gain);
        audio.setPitch(pitch);
        return audio;
    }
}
