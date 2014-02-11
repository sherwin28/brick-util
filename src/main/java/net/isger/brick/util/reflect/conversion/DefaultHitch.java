package net.isger.brick.util.reflect.conversion;

import net.isger.brick.util.reflect.Converter;

public class DefaultHitch {

    public static void hitch(Object source) {
        if (!(source instanceof Converter)) {
            return;
        }
        Converter converter = (Converter) source;
        converter.add(ClassConversion.CONVERSION);
        converter.add(NumberConversion.CONVERSION);
        converter.add(DateConversion.CONVERSION);
    }

}
