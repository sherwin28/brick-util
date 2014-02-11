package net.isger.brick.util.reflect.conversion;

import net.isger.brick.util.reflect.Conversion;

public class ClassConversion implements Conversion {

    public static final ClassConversion CONVERSION = new ClassConversion();

    private ClassConversion() {
    }

    public boolean isSupport(Class<?> type) {
        return type.equals(Class.class);
    }

    public Object convert(Class<?> type, Object value) {
        if (value instanceof String) {
            try {
                return Class.forName((String) value);
            } catch (ClassNotFoundException e) {
            }
        }
        throw new IllegalStateException("Unexpected class conversion for "
                + value);
    }

}
