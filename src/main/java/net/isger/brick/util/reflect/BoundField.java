package net.isger.brick.util.reflect;

import java.lang.reflect.Field;

import net.isger.brick.util.anno.Alias;

public class BoundField {

    private Field field;

    private String name;

    public BoundField(Field field) {
        this.field = field;
        this.field.setAccessible(true);
        Alias alias = field.getAnnotation(Alias.class);
        if (alias == null || (this.name = alias.value().trim()).length() == 0) {
            this.name = field.getName();
        }
    }

    public Field getField() {
        return field;
    }

    public String getName() {
        return name;
    }

    public void setValue(Object instance, Object value) {
        Class<?> type = field.getType();
        if (value != null && !type.isInstance(value)) {
            value = Converter.convert(type, value);
        }
        try {
            field.set(instance, value);
        } catch (Exception e) {
            throw new IllegalStateException("Failure to setting field "
                    + getName(), e);
        }
    }

    public Object getValue(Object instance) {
        try {
            return field.get(instance);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Can not to access field "
                    + getName());
        }
    }

}
