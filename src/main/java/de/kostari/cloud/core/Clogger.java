package de.kostari.cloud.core;

public class Clogger {

    public static boolean DEBUG = false;

    public static void log(Object msg) {
        if (DEBUG) {
            StackTraceElement trace = Thread.currentThread().getStackTrace()[2];
            System.out.println(msg + " at: " + trace.getClassName() + ":" + trace.getLineNumber());
            return;
        }
        System.out.println(msg);
    }

}
