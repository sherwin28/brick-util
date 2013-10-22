package net.isger.brick.util.ref;

import java.util.Collection;
import java.util.Map;

import net.isger.brick.util.Reflects;

import com.sun.org.apache.bcel.internal.generic.Type;

public class DefaultAdapter implements TypeAdapter {

    /**
     * 支持适配类型
     * 
     * @param type
     * @return
     */
    public boolean isSupport(Class<?> type) {
        // 不支持数组、集合、列表、定义类型转换
        return !(type.isArray() || Map.class.isAssignableFrom(type)
                || Collection.class.isAssignableFrom(type) || Type.class
                    .isAssignableFrom(type));
    }

    @SuppressWarnings("unchecked")
    public Object adapte(Class<?> type, Object value) {
        Object result = null;
        Class<?> valueType = value.getClass();
        // 所属实例直接赋值
        if (type.isInstance(valueType)) {
            result = value;
        }
        // 转换为字符串
        else if (String.class.isAssignableFrom(type)) {
            result = value instanceof String ? value : String.valueOf(value);
        }
        // 转换为类对象
        else if (Class.class.isAssignableFrom(type)) {
            try {
                result = value instanceof Class ? value
                        : value instanceof String ? Class
                                .forName((String) value) : null;
            } catch (ClassNotFoundException e) {
            }
        }
        // 转换为对象（值为集合）
        else if (Map.class.isAssignableFrom(valueType)) {
            result = Reflects.toInstance(type, (Map<String, Object>) value);
        }
        return result;
    }

    public boolean equals(Object instance) {
        return this.getClass() == instance.getClass();
    }
}
