package tower_climber;

import java.util.prefs.Preferences;

public final class HighScores {

    private static final String KEY = "tower_climber_high_score";
    private static int fallback;

    private HighScores() {
    }

    public static int load() {
        try {
            fallback = Math.max(fallback, Preferences.userNodeForPackage(HighScores.class).getInt(KEY, 0));
        } catch (SecurityException ignored) {
            // Sandboxed builds still retain the score for the lifetime of the process.
        }
        return fallback;
    }

    public static int submit(int score) {
        fallback = Math.max(load(), Math.max(0, score));
        try {
            Preferences.userNodeForPackage(HighScores.class).putInt(KEY, fallback);
        } catch (SecurityException ignored) {
            // Persistence is best-effort; gameplay never depends on platform preferences.
        }
        return fallback;
    }

    public static boolean isNewRecord(int score, int previousBest) {
        return score > Math.max(0, previousBest) && score > 0;
    }
}
