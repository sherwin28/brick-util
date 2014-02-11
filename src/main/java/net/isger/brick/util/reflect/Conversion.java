package net.isger.brick.util.reflect;

public interface Conversion {

    /**
     * 支持类型
     * 
     * @param clazz
     * @return
     */
    public boolean isSupport(Class<?> clazz);

    public Object convert(Class<?> clazz, Object value);

}
