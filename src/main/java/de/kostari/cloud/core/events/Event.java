package de.kostari.cloud.core.events;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class Event {

    private ArrayList<EventListener> listeners = new ArrayList<>();

    /**
     * Calls all method attached to this event.
     * 
     * @param args The arguments to pass to the methods
     */
    public void call(Object... args) {
        listeners.forEach(listener -> listener.invoke(args));
    }

    /**
     * Adds a method to this event.
     * 
     * The method must be public and have the same amount of arguments as the
     * event invoker.
     * 
     * @param listener The class instance to call the method on
     * @param name     The name of the method
     */
    public void join(Object listener, String name) {
        Class<?> c = listener.getClass();

        try {
            for (Method method : c.getMethods()) {
                if (method.getName().equals(name)) {
                    EventListener e = new EventListener(listener, method);
                    if (!listeners.contains(e)) {
                        listeners.add(e);
                    } else {
                        throw new RuntimeException("Method " + name + " already added to event " + this);
                    }
                }
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Removes a method from this event.
     * 
     * @param name The name of the method
     */
    public void leave(String name) {
        listeners.removeIf(eventListener -> eventListener.getMethod().getName().equals(name));
    }
}