package de.kostari.cloud.demo;

import de.kostari.cloud.core.Clogger;
import de.kostari.cloud.core.utils.input.Keys;
import de.kostari.cloud.core.window.Input;

public class GameController {

    public static GameStates gameState = GameStates.PLAYING;

    public static void updateGameStates() {
        if (gameState != GameStates.PLAYING && Input.keyPressed(Keys.KEY_F1)) {
            gameState = GameStates.PLAYING;
        }

        if (gameState != GameStates.EDITOR && Input.keyPressed(Keys.KEY_A)) {
            gameState = GameStates.EDITOR;
            Clogger.log("gameState");
        }

        if (Input.keyPressed(Keys.KEY_ESCAPE)) {
            if (gameState == GameStates.PLAYING) {
                gameState = GameStates.PAUSED;
            } else if (gameState == GameStates.PAUSED) {
                gameState = GameStates.PLAYING;
            }
        }
    }

}
