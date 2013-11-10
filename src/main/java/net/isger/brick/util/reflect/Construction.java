package net.isger.brick.util.reflect;

public interface Construction {

    public boolean isSupport(Class<?> clazz);

    public Object construct(Object... args);

}
