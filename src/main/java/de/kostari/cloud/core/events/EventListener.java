package de.kostari.cloud.core.events;

import java.lang.reflect.InvocationTargetException;

public class EventListener {

    private EventInfo info;
    private Object listener;

    public EventListener(Object listener, EventInfo info) {
        this.listener = listener;
        this.info = info;
    }

    public void invoke(Object... args) {
        Object[] arguments = args;
        if (info.getMethod().getParameterTypes().length != arguments.length) {
            if (info.getMethod().getParameterTypes().length == info.getArgs().length) {
                arguments = info.getArgs();
            } else {
                throw new IllegalArgumentException(
                        "Event called with '" + args.length + "' arguments, but method '" +
                                info.getMethodName()
                                + "' expected '" + info.getMethod().getParameterTypes().length + "' arguments!");
            }
        }
        try {
            info.getMethod().setAccessible(true);
            info.getMethod().invoke(listener, arguments);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public EventInfo getInfo() {
        return info;
    }
}
