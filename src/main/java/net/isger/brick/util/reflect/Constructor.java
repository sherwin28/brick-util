package net.isger.brick.util.reflect;

import java.util.Vector;

import net.isger.brick.util.hitcher.Director;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constructor extends Director {

    private static final Logger LOG;

    private static Constructor CONSTRUCTOR = new Constructor();

    private static final String KEY_CONSTRUCTIONS = "brick.util.reflect.constructions";

    private static final String CONSTRUCTION_PATH = "net/isger/brick/util/reflect/construction";

    private Vector<Construction> constructions;

    static {
        LOG = LoggerFactory.getLogger(Constructor.class);
    }

    private Constructor() {
        constructions = new Vector<Construction>();
    }

    protected String directHitchPath() {
        return directHitchPath(KEY_CONSTRUCTIONS, CONSTRUCTION_PATH);
    }

    protected void directSanity() {

    }

    public static Constructor getConstructor() {
        return canonicalize(CONSTRUCTOR);
    }

    public void add(Construction construction) {
        LOG.info("Constructor add construction {}", construction);
        constructions.add(construction);
    }

    /**
     * 类型检测
     * 
     * @param clazz
     * @return
     */
    public static boolean isSupport(Class<?> clazz) {
        Constructor constructor = getConstructor();
        for (Construction construction : constructor.constructions) {
            if (construction.isSupport(clazz)) {
                return true;
            }
        }
        return false;
    }

    public static Object construct(Class<?> clazz, Object... args) {
        Constructor constructor = getConstructor();
        for (Construction construction : constructor.constructions) {
            if (construction.isSupport(clazz)) {
                try {
                    return construction.construct(args);
                } catch (Exception e) {
                }
            }
        }
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new IllegalStateException("Unsupported construct type " + clazz);
    }

}
