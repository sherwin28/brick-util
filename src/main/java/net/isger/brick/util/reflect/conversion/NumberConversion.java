package net.isger.brick.util.reflect.conversion;

import net.isger.brick.util.reflect.Conversion;
import net.isger.brick.util.reflect.Converter;

public class NumberConversion implements Conversion {

    public static final NumberConversion CONVERSION = new NumberConversion();

    private NumberConversion() {
    }

    public boolean isSupport(Class<?> type) {
        return type.equals(Number.class) || Number.class.isAssignableFrom(type);
    }

    public Number convert(Class<?> type, Object value) {
        String source;
        if (value == null || (source = value.toString()).length() == 0) {
            return (Number) Converter.defaultValue(type);
        }
        if (Integer.class.isAssignableFrom(type)) {
            return Integer.parseInt(source);
        }
        if (Long.class.isAssignableFrom(type)) {
            return Long.parseLong(source);
        }
        if (Float.class.isAssignableFrom(type)) {
            return Float.parseFloat(source);
        }
        if (Double.class.isAssignableFrom(type)) {
            return Double.parseDouble(source);
        }
        if (source.indexOf(".") > 0) {
            return Double.parseDouble(source);
        } else {
            return Long.parseLong(source);
        }
    }
}
