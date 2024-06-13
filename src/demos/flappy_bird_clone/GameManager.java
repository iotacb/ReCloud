package flappy_bird_clone;

import de.kostari.cloud.core.events.Event;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.types.Color4f;

public class GameManager {

    public static final Color4f DEBUG_COLOR = new Color4f(.2f, .4f, 1f, .6f);

    public static final Event restartGameEvent = new Event();
    public static final Event spawnNewPipeEvent = new Event();
    public static final Event gameStartedEvent = new Event();
    public static final Event playerScoreEvent = new Event();

    public static boolean debugging = false;
    public static boolean gameRunning = false;

    public static float pipeSpeedFactor = 1f;

    public static int score = 0;

    public static void drawDebugRect(float x, float y, float width, float height, boolean center) {
        if (debugging) {
            Render.drawRect(x, y, width, height, center, DEBUG_COLOR);
        }
    }

}
