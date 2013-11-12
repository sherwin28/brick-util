package net.isger.brick.util.reflect;

public interface Conversion {

    /**
     * TODO 支持类型不能只是目标类型，要求包含隶属类型
     * 
     * @param clazz
     * @return
     */
    public boolean isSupport(Class<?> clazz);

    public Object convert(Object value);

}
