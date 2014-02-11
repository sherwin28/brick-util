package net.isger.brick.util.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import net.isger.brick.util.hitcher.Director;

public class Excluder extends Director {

    // private static final Logger LOG;

    private static final Excluder EXCLUDER = new Excluder();

    private static final String EXCLUSION_PATH = "net/isger/brick/util/reflect/exclusion";

    // static {
    // LOG = LoggerFactory.getLogger(Excluder.class);
    // }

    private Excluder() {
    }

    protected String directHitchPath() {
        return EXCLUSION_PATH;
    }

    public void addExclusion(Exclusion exclusion) {

    }

    protected void directSanity() {
    }

    public static boolean exclude(Class<?> clazz) {
        return false;
    }

    public static boolean exclude(Field field) {
        int mod = field.getModifiers();
        return Modifier.isStatic(mod) || Modifier.isFinal(mod)
                || Modifier.isTransient(mod);
    }

    public static boolean exclude(Method method) {
        return false;
    }

    public static Excluder getExcluder() {
        return canonicalize(EXCLUDER);
    }

}
