package net.isger.brick.util.reflect.type;

import net.isger.brick.util.reflect.Converter;

public class DefaultHitch {

    public static void hitch(Object source) {
        if (!(source instanceof Converter)) {
            return;
        }
        System.out.println("this is DefaultHitch.");
        Converter converter = (Converter) source;
        // converter.add(new ArrayAdapter());
        // converter.add(new CollectionAdapter());
        // converter.add(new MapAdapter());
        // converter.add(new OrdinaryAdapter());
    }

}
