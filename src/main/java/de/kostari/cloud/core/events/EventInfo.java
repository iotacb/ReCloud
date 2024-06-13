package de.kostari.cloud.core.events;

import java.lang.reflect.Method;

public class EventInfo {

    private String methodName;
    private Object[] args;

    private Method method;

    public EventInfo(String methodName, Object... args) {
        this.methodName = methodName;
        this.args = args;
    }

    public String getMethodName() {
        return methodName;
    }

    public Object[] getArgs() {
        return args;
    }

    protected Method getMethod() {
        return method;
    }

    protected void setMethod(Method method) {
        this.method = method;
    }

}
