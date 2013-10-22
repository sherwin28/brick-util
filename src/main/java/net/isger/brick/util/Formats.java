package net.isger.brick.util;

/**
 * 格式化工具
 * 
 * @author issing
 * 
 */
public class Formats {

    private Formats() {
    }

    public static String toPath(String value) {
        return value.replaceAll("[.]|\\\\", "/");
    }

    public static String toPath(String value, String name) {
        return toPath(value) + '/' + toPath(name);
    }

    public static String toCap(String value) {
        value = value.trim();
        int len = value.length();
        if (len > 0) {
            char[] cs = value.toCharArray();
            cs[0] = Character.toUpperCase(cs[0]);
            value = new String(cs);
        }
        return value;
    }
}
