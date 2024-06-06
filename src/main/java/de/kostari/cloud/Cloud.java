package de.kostari.cloud;

import java.text.SimpleDateFormat;

import de.kostari.cloud.core.utils.LogLevel;

public class Cloud {

    /**
     * Determines whether the log level should be shown or not.
     */
    public static boolean SHOW_LOG_LEVEL = true;
    private static String LOG_LEVEL_TEMPLATE = "[%s]: ";

    /**
     * Determines whether the timestamp should be shown or not.
     */
    public static boolean SHOW_TIMESTAMPS = true;
    private static String TIMESTAMP_TEMPLATE = "[%s]: ";

    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");

    /**
     * Determines the engine's orientation.
     * -1 Means that the 0,0 coordinate is in the top left corner of the window.
     * 1 Means that the 0,0 coordinate is in the bottom left corner of the window.
     */
    public static int ENGINE_UP = -1;

    public static final String PATH_DELIMETER = "\\|";

    /**
     * Prints a log message to the console with the specified log level and
     * arguments.
     * 
     * @param level the log level of the message
     * @param args  the arguments to include in the message
     */
    public static void print(LogLevel level, Object... args) {
        StringBuilder message = new StringBuilder();

        if (SHOW_LOG_LEVEL) {
            String logLevel = String.format(LOG_LEVEL_TEMPLATE, level.name());
            message.append(logLevel);
        }

        if (SHOW_TIMESTAMPS) {
            String timestamp = String.format(TIMESTAMP_TEMPLATE, DATE_FORMAT.format(System.currentTimeMillis()));
            message.append(timestamp);
        }

        for (Object arg : args) {
            message.append(arg);
        }

        System.out.println(message.toString());
    }

    /**
     * Prints a log message to the console with the specified arguments.
     * 
     * @param args the arguments to include in the message
     */
    public static void print(Object... args) {
        print(LogLevel.INFO, args);
    }

    /**
     * Prints a log message to the console with the specified log level and
     * arguments.
     * Also prints where the print method was called from.
     * 
     * @param level the log level of the message
     * @param args  the arguments to include in the message
     */
    public static void printDebug(LogLevel level, Object... args) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StringBuilder message = new StringBuilder();

        for (Object arg : args) {
            message.append(arg);
        }

        if (stackTrace.length > 2) {
            StackTraceElement caller = stackTrace[3];
            String className = caller.getClassName();
            String methodName = caller.getMethodName();
            int lineNumber = caller.getLineNumber();

            print(level, "print call in: ", className, " at ", methodName, "() at line ", lineNumber, ": ",
                    message.toString());
        } else {
            print(level, message.toString());
        }
    }

    /**
     * Prints a log message to the console with the specified arguments.
     * Also prints where the print method was called from.
     * 
     * @param args the arguments to include in the message
     */
    public static void printDebug(Object... args) {
        printDebug(LogLevel.DEBUG, args);
    }
}
