package net.isger.brick.util.reflect.type;

import java.util.HashMap;
import java.util.Map;

import net.isger.brick.util.Reflects;
import net.isger.brick.util.reflect.Conversion;

public class MapAdapter {

    public boolean isSupport(Class<?> type) {
        return Map.class.isAssignableFrom(type);
    }

    @SuppressWarnings("rawtypes")
    public Object convert(Class<?> type, Object value) {
        Object result = null;
        // 转换为集合
        if (Map.class.isAssignableFrom(type)) {
            Map map = null;
            // 是否支持HashMap
            if (Reflects.isAbstract(type)
                    && type.isAssignableFrom(HashMap.class)) {
                map = new HashMap();
            } else {
                // map = type.newInstance();
            }

        }
        return result;
    }
}
