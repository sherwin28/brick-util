package net.isger.brick.util.reflect;

import java.lang.reflect.Method;

import net.isger.brick.blue.Marks.TYPE;
import net.isger.brick.util.anno.Alias;

public class BoundMethod {

    private Method method;

    private String name;

    public BoundMethod(Method method) {
        this.method = method;
        this.method.setAccessible(true);
        Alias alias = method.getAnnotation(Alias.class);
        if (alias == null || (this.name = alias.value().trim()).length() == 0) {
            this.name = method.getName()
                    + TYPE.getMethDesc(method.getReturnType(),
                            method.getParameterTypes());
        }
    }

    public Method getMethod() {
        return method;
    }

    public String getName() {
        return name;
    }

    public Object invoke(Object instance, Object... args) {

        try {
            return method.invoke(instance, args);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Can not to access method "
                    + getName());
        } catch (Exception e) {
            throw new IllegalStateException("Failure to invoke method "
                    + getName(), e);
        }
    }

}
