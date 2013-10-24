package net.isger.brick.util.reflect.type;

public interface TypeAdapter {

    public boolean isSupport(Class<?> type);

    public Object adapte(Class<?> type, Object value);

}
