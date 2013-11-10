package net.isger.brick.util.reflect;

import java.lang.reflect.Field;

public interface Exclusion {

    public boolean exclude(Field field);

    public boolean exclude(Class<?> clazz);

}
