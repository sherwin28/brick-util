package net.isger.brick.util.reflect;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.Vector;

import net.isger.brick.util.Reflects;
import net.isger.brick.util.hitcher.Director;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Converter extends Director {

    private static final String KEY_CONVERSIONS = "brick.util.reflect.conversions";

    private static final String CONVERSION_PATH = "net/isger/brick/util/reflect/conversion";

    private static final Logger LOG;

    private static final Converter CONVERTER;

    private Vector<Conversion> conversions;

    static {
        LOG = LoggerFactory.getLogger(Converter.class);
        CONVERTER = new Converter();
    }

    private Converter() {
        conversions = new Vector<Conversion>();
    }

    protected String directHitchPath() {
        return directHitchPath(KEY_CONVERSIONS, CONVERSION_PATH);
    }

    protected void directSanity() {
    }

    public static Converter getConverter() {
        return canonicalize(CONVERTER);
    }

    public void add(Conversion conversion) {
        if (LOG.isDebugEnabled()) {
            LOG.info("Binding conversion {}", conversion.getClass().getName());
        }
        conversions.add(conversion);
    }

    /**
     * 类型检测
     * 
     * @param clazz
     * @return
     */
    public static boolean isSupport(Class<?> clazz) {
        Converter converter = getConverter();
        for (Conversion conversion : converter.conversions) {
            if (conversion.isSupport(clazz)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 转换
     * 
     * @param clazz
     * @param value
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Object convert(Class<?> clazz, Object value) {
        Converter converter = getConverter();
        for (Conversion conversion : converter.conversions) {
            if (conversion.isSupport(clazz)) {
                try {
                    return conversion.convert(clazz, value);
                } catch (Exception e) {
                }
            }
        }
        if (value == null) {
            return defaultValue(clazz);
        }
        Class<?> srcType = value.getClass();
        if (srcType == null || clazz.isAssignableFrom(srcType)) {
            return value;
        } else if (clazz.isArray() && srcType.isArray()) {

        } else if (srcType.isArray()) {
            if (Array.getLength(value) == 0) {
                return defaultValue(clazz);
            }
            return convert(clazz, Array.get(value, 0));
        } else if (clazz.isArray()) {
            if (clazz.getComponentType().isAssignableFrom(srcType)) {
                Object array = Array.newInstance(clazz.getComponentType(), 1);
                Array.set(array, 0, value);
                return array;
            }
        } else if (value instanceof String) {
            return Reflects.newInstance((String) value);
        } else if (value instanceof Map) {
            return Reflects.newInstance(clazz, (Map<String, Object>) value);
        }
        throw new IllegalStateException("Unsupported convert to "
                + clazz.getName() + " from " + srcType.getName());
    }

    public static Object defaultValue(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            return 0;
        }
        return null;
    }
}
