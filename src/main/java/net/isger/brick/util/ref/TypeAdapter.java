package net.isger.brick.util.ref;

public interface TypeAdapter {

    public boolean isSupport(Class<?> type);

    public Object adapte(Class<?> type, Object value);

}
