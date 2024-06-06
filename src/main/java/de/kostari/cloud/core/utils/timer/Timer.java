package de.kostari.cloud.core.utils.timer;

public class Timer {

    private long lastTime;

    /**
     * Returns true if the give amount of time has passed
     * 
     * @param time The time in milliseconds
     * @return true if the time has passed
     */
    public boolean timePassed(float time) {
        if (System.currentTimeMillis() - lastTime > time) {
            lastTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    /**
     * Resets the timer
     */
    public void reset() {
        lastTime = System.currentTimeMillis();
    }

}
