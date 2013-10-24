package net.isger.brick.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.isger.brick.util.anno.Ignore;
import net.isger.brick.util.reflect.type.TypeAdapter;
import net.isger.brick.util.reflect.type.TypeAdapters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 反射工具
 * 
 * @author issing
 * 
 */
public class Reflects {

    private static final Logger LOG = LoggerFactory.getLogger(Reflects.class);

    private Reflects() {
    }

    public static <T> T toInstance(Class<T> type, Object value) {
        T instance = null;

        return instance;
    }

    /**
     * 集合填充生成指定类型对象
     * 
     * @param type
     * @param values
     * @return
     */
    public static <T> T toInstance(Class<T> type, Map<String, Object> values) {
        T instance = null;
        try {
            instance = type.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        set(instance, values);
        return instance;
    }

    /**
     * 设置实例所有属性值（通过集合数据）
     * 
     * @param type
     * @param values
     * @return
     */
    public static void set(Object instance, Map<String, Object> values) {
        String fieldName = null;
        try {
            for (Field f : getFields(instance.getClass())) {
                fieldName = f.getName();
                if (values.containsKey(fieldName)) {
                    set(instance, f, values.get(fieldName));
                }
            }
            // BeanUtils.populate(instance, values);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 设置实例属性值
     * 
     * @param instance
     * @param field
     * @param value
     */
    public static void set(Object instance, Field field, Object value) {
        Class<?> type = field.getType();
        adapte: if (value != null && !type.isInstance(value)) {
            Object aval = null;
            Iterator<TypeAdapter> adapters = TypeAdapters.getAdapter(type);
            while (adapters.hasNext()) {
                aval = adapters.next().adapte(type, value);
                if (aval != null) {
                    value = aval;
                    break adapte;
                }
            }
        }
        try {
            field.set(instance, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 提取字段值转换为集合
     * 
     * @param instance
     * @return
     */
    public static Map<String, Object> toMap(Object instance) {
        Map<String, Object> values = new HashMap<String, Object>();
        for (Field f : getFields(instance.getClass())) {
            try {
                values.put(f.getName(), f.get(instance));
            } catch (Exception e) {
                LOG.warn("Get field [{}] value is fail.", f.getName(), e);
            }
        }
        return values;
    }

    /**
     * 获取字段
     * 
     * @param clazz
     * @return
     */
    public static Collection<Field> getFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<Field>();
        Ignore ignore = clazz.getAnnotation(Ignore.class);
        int iv = ignore == null ? Ignore.DEFAULT : ignore.value();
        int mod;
        Map<String, Field> fs = new HashMap<String, Field>();
        // 提取所有字段（声明方式不提取父类属性）
        fetch(fs, clazz, iv == Ignore.DECLARE, true);
        for (Field f : fs.values()) {
            mod = f.getModifiers();
            // 忽略静态属性（除完全方式）
            if (iv != Ignore.COMPLETE && Modifier.isStatic(mod)) {
                continue;
            }
            f.setAccessible(true);
            fields.add(f);
        }
        return fields;
    }

    /**
     * 提取字段
     * 
     * @param fields
     * @param clazz
     * @param isDeclare
     * @param isOrigin
     */
    private static void fetch(Map<String, Field> fields, Class<?> clazz,
            boolean isDeclare, boolean isOrigin) {
        Field[] fs = clazz.getDeclaredFields();
        int mod;
        if (isOrigin) {
            for (Field f : fs) {
                fields.put(f.getName(), f);
            }
            for (Field f : clazz.getFields()) {
                fields.put(f.getName(), f);
            }
        } else {
            for (Field f : fs) {
                mod = f.getModifiers();
                if (!Modifier.isStatic(mod)
                        && (Modifier.isProtected(mod) || Modifier.isPublic(mod))
                        && !fields.containsKey(f.getName())) {
                    fields.put(f.getName(), f);
                }
            }
        }
        clazz = clazz.getSuperclass();
        if (!(clazz == null || isDeclare)) {
            fetch(fields, clazz, isDeclare, false);
        }
    }

    /**
     * 生成实例
     * 
     * @param name
     * @return
     */
    public static Object newInstance(String name) {
        Object result = null;
        try {
            result = getClass(name).newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * 是否为抽象类
     * 
     * @param clazz
     * @return
     */
    public static boolean isAbstract(Class<?> clazz) {
        return Modifier.isAbstract(clazz.getModifiers());
    }

    /**
     * 是否为类
     * 
     * @param name
     * @return
     */
    public static boolean isClass(String name) {
        return getClass(name) != null;
    }

    /**
     * 获取类型
     * 
     * @param type
     * @return
     */
    public static Class<?> getClass(String type) {
        Class<?> result = null;
        try {
            result = Class.forName(type);
        } catch (Exception e) {
        }
        return result;
    }

    public static Object newInstance(String gate, Object load) {
        return null;
    }

    public static String invoke(Object instance, String methodName, Object args) {
        // TODO Auto-generated method stub
        return null;
    }

    // private static final String SET_METHOD = "set";
    //
    // private static final int SET_METHOD_LEN = SET_METHOD.length();
    //
    // private static Map<Class<?>, Map<String, Method>> sms;
    //
    // static {
    // sms = new HashMap<Class<?>, Map<String, Method>>();
    // }
    //
    // private Reflects() {
    // }
    //
    // private static Map<String, Method> getSM(Class<?> clazz) {
    // return getMethods(clazz, SET_METHOD, SET_METHOD_LEN, null, sms);
    // }
    //
    // private static Map<String, Method> getMethods(Class<?> clazz,
    // String prefix, int prefixLen, String filter,
    // Map<Class<?>, Map<String, Method>> allMethods) {
    // Map<String, Method> result = allMethods.get(clazz);
    // if (result == null) {
    // result = new HashMap<String, Method>();
    // Method[] cms = clazz.getMethods();
    // String methodName = null;
    // for (Method method : cms) {
    // if ((methodName = method.getName()).startsWith(prefix)
    // && (filter == null || !methodName.equals(filter))) {
    // method.setAccessible(true);
    // result.put(makeKey(methodName, prefixLen), method);
    // }
    // }
    // allMethods.put(clazz, result);
    // }
    // return result;
    // }
    //
    // private static String makeKey(String methodName, int len) {
    // StringBuffer fieldName = new StringBuffer(32);
    // char[] chs = methodName.substring(len).toCharArray();
    // chs[0] = Character.toLowerCase(chs[0]);
    // for (char ch : chs) {
    // if (Character.isUpperCase(ch)) {
    // fieldName.append('_');
    // }
    // fieldName.append(Character.toLowerCase(ch));
    // }
    // return fieldName.toString();
    // }
    //
    // public static boolean isClass(String res) {
    // return getClass(res) != null;
    // }
    //
    // @SuppressWarnings("unchecked")
    // public static Map<String, Object> toMap(Object instance)
    // throws RuntimeException {
    // return (Map<String, Object>) Mapl.toMaplist(instance);
    // }
    //
    // @SuppressWarnings("unchecked")
    // public static <T> T toInstance(Class<T> clazz, Map<String, Object>
    // values)
    // throws RuntimeException {
    // return (T) Mapl.maplistToObj(values, clazz);
    // }
    //
    // public static <T> List<T> toListBean(Class<T> clazz, Object[] values) {
    // List<T> result = new ArrayList<T>();
    // Map<String, Method> methods = getSM(clazz);
    // String[] columns = (String[]) values[0];
    // int size = columns.length;
    // Object[][] datas = (Object[][]) values[1];
    // int count = datas.length;
    // try {
    // Method method = null;
    // for (int i = 0; i < count; i++) {
    // T bean = clazz.newInstance();
    // for (int j = 0; j < size; j++) {
    // method = methods.get(columns[j]);
    // if (method != null) {
    // method.invoke(bean, datas[i][j]);
    // }
    // }
    // result.add(bean);
    // }
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // }
    // return result;
    // }
    //
    // public static List<Map<String, Object>> toListMap(Object[] values) {
    // List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
    // if (values != null && values.length > 1) {
    // String[] columns = (String[]) values[0];
    // Object[][] rows = (Object[][]) values[1];
    // for (int i = 0; i < rows.length; i++) {
    // Map<String, Object> row = new HashMap<String, Object>();
    // for (int j = 0; j < columns.length; j++) {
    // row.put(columns[j], rows[i][j]);
    // }
    // result.add(row);
    // }
    // }
    // return result;
    // }
    //
    // /**
    // * 获取类型
    // *
    // * @param type
    // * @return
    // */
    // public static Class<?> getClass(String type) {
    // Class<?> result = null;
    // try {
    // result = Class.forName(type);
    // } catch (Exception e) {
    // }
    // return result;
    // }
    //
    // /**
    // * 获取实例
    // *
    // * @param type
    // * @return
    // */
    // public static Object getInstance(String type) {
    // Object result = null;
    // try {
    // result = getClass(type).newInstance();
    // } catch (Exception e) {
    // }
    // return result;
    // }
    //
    // /**
    // * 获取字段
    // *
    // * @param clazz
    // * @param fieldName
    // * @return
    // */
    // public static Field getField(Class<?> clazz, String fieldName) {
    // Field field = null;
    // try {
    // field = clazz.getDeclaredField(fieldName);
    // } catch (Exception e) {
    // }
    // if (field == null && clazz != null) {
    // field = getField(clazz.getSuperclass(), fieldName);
    // }
    // return field;
    // }
    //
    // /**
    // * 获取字段值
    // *
    // * @param object
    // * @param fieldName
    // * @return
    // */
    // public static Object getProperty(Object instance, String fieldName) {
    // Object result = null;
    // Field field = getField(instance.getClass(), fieldName);
    // if (field != null) {
    // field.setAccessible(true);
    // try {
    // result = field.get(instance);
    // } catch (Exception e) {
    // }
    // }
    // return result;
    // }
    //
    // /**
    // * 获取实例
    // *
    // * @param data
    // * @return
    // */
    // public static Object getInstance(byte[] data) {
    // Object result = null;
    // ByteArrayInputStream buffer = new ByteArrayInputStream(data);
    // ObjectInputStream is = null;
    // try {
    // is = new ObjectInputStream(buffer);
    // result = is.readObject();
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // } finally {
    // close(is);
    // close(buffer);
    // }
    // return result;
    // }
    //
    // /**
    // * 获取字节码
    // *
    // * @param instance
    // * @return
    // */
    // public static byte[] getBytes(Object instance) {
    // byte[] result = null;
    // ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    // ObjectOutputStream os = null;
    // try {
    // os = new ObjectOutputStream(buffer);
    // os.writeObject(instance);
    // result = buffer.toByteArray();
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // } finally {
    // close(os);
    // close(buffer);
    // }
    // return result;
    // }
    //
    // private static void close(Closeable instance) {
    // if (instance != null) {
    // try {
    // instance.close();
    // } catch (IOException e) {
    // }
    // }
    // }
    //
    // public static Object getInstance(String type, Object... args) {
    // Object instance = null;
    // try {
    // instance = Mirror.me(Class.forName(type)).born(args);
    // } catch (ClassNotFoundException e) {
    // }
    // return instance;
    // }
    //
    // public static Object invoke(Object instance, String operate, Object...
    // args) {
    // return Mirror.me(instance.getClass()).invoke(instance, operate, args);
    // }
    //
    // public static Object cast(Map<String, Object> info, Type type) {
    // return Mapl.maplistToObj(info, type);
    // }

}
