package net.isger.brick.util.reflect.type;

public class DefaultHitch {

    public static void hitch() {
        TypeAdapters.addAdapter(new ArrayAdapter());
        TypeAdapters.addAdapter(new CollectionAdapter());
        TypeAdapters.addAdapter(new MapAdapter());
        TypeAdapters.addAdapter(new OrdinaryAdapter());
    }

}
