package de.kostari.cloud.core;

public class Clogger {

    /**
     * Determines whether the logger should print the line number and class name of
     * the caller.
     */
    public static boolean DEBUG = false;

    /**
     * Prints a log message to the console
     * When Clogger.DEBUG is set to true, it will also print the line number and
     * class name of the caller.
     * 
     * @param msg
     */
    public static void log(Object msg) {
        if (DEBUG) {
            StackTraceElement trace = Thread.currentThread().getStackTrace()[2];
            System.out.println(msg + " at: " + trace.getClassName() + ":" + trace.getLineNumber());
            return;
        }
        System.out.println(msg);
    }

}
