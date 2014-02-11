package net.isger.brick.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.isger.brick.util.anno.Ignore;
import net.isger.brick.util.anno.Ignore.Mode;
import net.isger.brick.util.reflect.BoundField;
import net.isger.brick.util.reflect.BoundMethod;
import net.isger.brick.util.reflect.Constructor;
import net.isger.brick.util.reflect.Excluder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 反射工具
 * 
 * @author issing
 * 
 */
public class Reflects {

    private static final String KEY_CLASS = "class";

    private static final Logger LOG;

    static {
        LOG = LoggerFactory.getLogger(Reflects.class);
    }

    private Reflects() {
    }

    /**
     * 获取绑定字段信息
     * 
     * @param clazz
     * @return
     */
    public static BoundField[] getBoundFields(Class<?> clazz) {
        Map<String, BoundField> result = new LinkedHashMap<String, BoundField>();
        // 跳过接口以及原始数据类型
        if (!clazz.isInterface() || Object.class.isAssignableFrom(clazz)) {
            Ignore ignore;
            Mode ignoreMode;
            Field[] fields;
            BoundField boundField;
            while (clazz != Object.class) {
                // 忽略指定类
                ignore = clazz.getAnnotation(Ignore.class);
                ignoreMode = ignore != null ? ignore.mode() : Mode.INCLUDE;
                // 导入声明字段
                fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    if (!Excluder.exclude(field)
                            && (boundField = createBoundField(field, ignoreMode)) != null) {
                        boundField = result.put(boundField.getName(),
                                boundField);
                        if (boundField != null) {
                            throw new IllegalArgumentException(clazz.getName()
                                    + " declares multiple fields named "
                                    + boundField.getName());
                        }
                    }
                }
                clazz = clazz.getSuperclass();
            }
        }
        return result.values().toArray(new BoundField[result.size()]);
    }

    /**
     * 获取绑定字段信息
     * 
     * @param clazz
     * @param fieldName
     * @return
     */
    public static BoundField getBoundField(Class<?> clazz, String fieldName) {
        for (BoundField field : getBoundFields(clazz)) {
            if (field.match(fieldName)) {
                return field;
            }
        }
        return null;
    }

    /**
     * 创建绑定字段信息
     * 
     * @param field
     * @param classMode
     * @return
     */
    private static BoundField createBoundField(Field field, Mode classMode) {
        Ignore ignore = field.getAnnotation(Ignore.class);
        Mode fieldMode = ignore != null ? ignore.mode() : classMode;
        if (fieldMode.equals(Mode.EXCLUDE)) {
            return null;
        }
        return new BoundField(field);
    }

    /**
     * 获取绑定方法信息
     * 
     * @param clazz
     * @return
     */
    public static BoundMethod[] getBoundMethods(Class<?> clazz) {
        Map<String, BoundMethod> result = new LinkedHashMap<String, BoundMethod>();
        // 跳过接口以及原始数据类型
        if (Object.class.isAssignableFrom(clazz)) {
            Ignore ignore;
            Mode classMode;
            Method[] methods;
            BoundMethod boundMethod;
            while (clazz != Object.class) {
                // 忽略指定类
                ignore = clazz.getAnnotation(Ignore.class);
                classMode = ignore != null ? ignore.mode() : Mode.INCLUDE;
                // 导入声明字段
                methods = clazz.getDeclaredMethods();
                for (Method method : methods) {
                    if (!Excluder.exclude(method)
                            && (boundMethod = createBoundMethod(method,
                                    classMode)) != null) {
                        boundMethod = result.put(boundMethod.getName(),
                                boundMethod);
                        if (boundMethod != null) {
                            throw new IllegalArgumentException(clazz.getName()
                                    + " declares multiple methods named "
                                    + boundMethod.getName());
                        }
                    }
                }
                clazz = clazz.getSuperclass();
            }
        }
        return result.values().toArray(new BoundMethod[result.size()]);
    }

    /**
     * 创建绑定方法信息
     * 
     * @param method
     * @param classMode
     * @return
     */
    private static BoundMethod createBoundMethod(Method method, Mode classMode) {
        Ignore ignore = method.getAnnotation(Ignore.class);
        Mode methodMode = ignore != null ? ignore.mode() : classMode;
        if (methodMode.equals(Mode.EXCLUDE)) {
            return null;
        }
        return new BoundMethod(method);
    }

    /**
     * 集合填充生成指定类型对象
     * 
     * @param name
     * @param values
     * @return
     */
    public static Object newInstance(Map<String, Object> values) {
        String typeName = (String) values.get(KEY_CLASS);
        if (typeName == null) {
            return values;
        }
        values.remove(KEY_CLASS);
        Class<?> clazz = getClass(typeName);
        if (clazz == null) {
            throw new IllegalStateException("Not found class by " + typeName);
        }
        return newInstance(clazz, values);
    }

    /**
     * 集合填充生成指定类型对象
     * 
     * @param name
     * @param values
     * @return
     */
    public static Object newInstance(String name, Map<String, Object> values) {
        Object instance = newInstance(name);
        toInstance(instance, values);
        return instance;
    }

    /**
     * 集合填充生成指定类型对象
     * 
     * @param clazz
     * @param values
     * @return
     */
    public static <T> T newInstance(Class<T> clazz, Map<String, Object> values) {
        T instance = newInstance(clazz);
        if (values != null && values.size() > 0) {
            toInstance(instance, values);
        }
        return instance;
    }

    /**
     * 生成实例
     * 
     * @param name
     * @return
     */
    public static Object newInstance(String name) {
        Class<?> clazz = getClass(name);
        if (clazz == null) {
            throw new IllegalStateException("Not found class by " + name);
        }
        return newInstance(clazz);
    }

    /**
     * 生成实例
     * 
     * @param clazz
     * @return
     */
    public static <T> T newInstance(Class<? extends T> clazz) {
        if (isAbstract(clazz)) {
            // return new Standin(clazz).getSource();
            throw new IllegalStateException(
                    "Unsupport abstract class or interface " + clazz.getName());
        }
        return Constructor.construct(clazz);
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
     * @param name
     * @return
     */
    public static Class<?> getClass(String name) {
        Class<?> result = null;
        try {
            result = Class.forName(name);
        } catch (Exception e) {
        }
        return result;
    }

    /**
     * 设置实例所有属性值（通过集合数据）
     * 
     * @param type
     * @param values
     * @return
     */
    @SuppressWarnings("unchecked")
    public static void toInstance(Object instance, Map<String, Object> values) {
        if (instance instanceof Map) {
            ((Map<String, Object>) instance).putAll(values);
            return;
        }
        values = canonicalize(values);
        String fieldName = null;
        try {
            for (BoundField field : getBoundFields(instance.getClass())) {
                fieldName = field.getName();
                if (values.containsKey(fieldName)) {
                    field.setValue(instance, values.get(fieldName));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> canonicalize(Map<String, Object> values) {
        Map<String, Object> result = new HashMap<String, Object>();
        int index;
        String key;
        String subKey;
        Object value;
        Map<String, Object> container;
        for (Entry<String, Object> entry : values.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            key = entry.getKey();
            index = key.indexOf(".");
            if (index == -1) {
                subKey = null;
            } else {
                subKey = key.substring(index + 1);
                key = key.substring(0, index);
            }
            value = result.get(key);
            if (value == null) {
                if (subKey == null) {
                    result.put(key, entry.getValue());
                    continue;
                }
                container = new HashMap<String, Object>();
                result.put(key, container);
            } else if (value instanceof Map) {
                container = (Map<String, Object>) value;
                if (subKey == null) {
                    throw new IllegalStateException();
                }
            } else {
                throw new IllegalStateException();
            }
            container.put(subKey, entry.getValue());
        }
        return result;
    }

    /**
     * 设置实例属性值
     * 
     * @param instance
     * @param name
     * @param value
     */
    public static void toField(Object instance, String name, Object value) {
        BoundField field = getBoundField(instance.getClass(), name);
        if (field == null) {
            throw new IllegalStateException("Not found field by " + name);
        }
        field.setValue(instance, value);
    }

    /**
     * 提取字段值转换为集合
     * 
     * @param instance
     * @return
     */
    public static Map<String, Object> toMap(Object instance) {
        Class<?> clazz = instance.getClass();
        Map<String, Object> values = new HashMap<String, Object>();
        for (BoundField field : getBoundFields(clazz)) {
            try {
                values.put(field.getName(), field.getValue(instance));
            } catch (Exception e) {
                LOG.warn("Failure getting field [{}] value.", field.getName(),
                        e);
            }
        }
        return values;
    }

    public static <T> List<T> toList(Class<T> clazz, Object[] gridModel) {
        List<T> result = new ArrayList<T>();
        String[] columns = (String[]) gridModel[0];
        Object[][] rows = (Object[][]) gridModel[1];
        int size = columns.length;
        int count = rows.length;
        T bean;
        BoundField field;
        for (int i = 0; i < count; i++) {
            bean = Reflects.newInstance(clazz);
            for (int j = 0; j < size; j++) {
                field = getBoundField(clazz, columns[j]);
                if (field != null) {
                    field.setValue(bean, rows[i][j]);
                }
            }
            result.add(bean);
        }
        return result;
    }

    public static List<Map<String, Object>> toListMap(Object[] gridModel) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        String[] columns = (String[]) gridModel[0];
        Object[][] rows = (Object[][]) gridModel[1];
        int size = columns.length;
        int count = rows.length;
        Map<String, Object> bean;
        for (int i = 0; i < count; i++) {
            bean = new HashMap<String, Object>();
            for (int j = 0; j < size; j++) {
                bean.put(columns[j], rows[i][j]);
            }
            result.add(bean);
        }
        return result;
    }

    public static List<Object> toList(Object[] gridModel, String column) {
        List<Object> result = new ArrayList<Object>();
        String[] columns = (String[]) gridModel[0];
        int size = columns.length;
        int index;
        for (index = 0; index < size; index++) {
            if (column.equals(columns[index])) {
                break;
            }
        }
        if (index < size) {
            Object[][] rows = (Object[][]) gridModel[1];
            for (Object[] row : rows) {
                result.add(row[index]);
            }
        }
        return result;
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
