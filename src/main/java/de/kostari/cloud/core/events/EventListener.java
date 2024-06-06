package de.kostari.cloud.core.events;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class EventListener {

    private Method method;
    private Object listener;

    public EventListener(Object listener, Method method) {
        this.listener = listener;
        this.method = method;
    }

    public void invoke(Object... args) {
        if (method.getParameterTypes().length != args.length) {
            throw new IllegalArgumentException(
                    "Event called with '" + args.length + "' arguments, but method '" +
                            method.getName()
                            + "' expected '" + method.getParameterTypes().length + "' arguments!");
        }
        try {
            method.invoke(listener, args);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public Method getMethod() {
        return method;
    }
}
