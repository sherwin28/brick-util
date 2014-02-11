package net.isger.brick.util.reflect.conversion;

import java.lang.reflect.Method;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.isger.brick.util.reflect.Conversion;

public class DateConversion implements Conversion {

    public static final DateConversion CONVERSION = new DateConversion();

    private static final String[] PATTERNS = new String[] {
            "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM-dd",
            "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM/dd",
            "yyyyMMddHHmmss", "yyyyMMddHHmm", "yyyyMMdd", "HH:mm:ss", "HH:mm" };

    private DateConversion() {
    }

    public boolean isSupport(Class<?> type) {
        return type.equals(Date.class) || Date.class.isAssignableFrom(type);
    }

    public Date convert(Class<?> type, Object value) {
        String source;
        if (value == null || (source = value.toString()).length() == 0) {
            return null;
        }
        try {
            Method method = type.getDeclaredMethod("valueOf");
            return (Date) method.invoke(type, source);
        } catch (Exception e) {
        }
        ParsePosition pos = new ParsePosition(0);
        SimpleDateFormat parser = new SimpleDateFormat();
        parser.setLenient(true);
        Date date;
        for (String pattern : PATTERNS) {
            parser.applyPattern(pattern);
            pos.setIndex(0);
            date = parser.parse(source, pos);
            if (date != null && pos.getIndex() == source.length()) {
                return date;
            }
        }
        throw new IllegalArgumentException(source);
    }
}
