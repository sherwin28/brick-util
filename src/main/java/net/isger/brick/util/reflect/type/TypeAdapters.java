package net.isger.brick.util.reflect.type;

import java.util.Iterator;

import net.isger.brick.util.Hitchers;
import net.isger.brick.util.hicher.Hitcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TypeAdapters {

    private static final Logger LOG;

    private static final Object LOCKED = new Object();

    private static final TypeAdapters ADAPTERS = new TypeAdapters();

    private static final String HITCH_PATH = "net/isger/brick/util/reflect/type";

    private static final int UNINITIALIZED = 0;

    private static final int INITIALIZING = 1;

    private static final int INITSUCCESS = 2;

    private static final int INITFAILURE = 3;

    private int initial;

    static {
        LOG = LoggerFactory.getLogger(TypeAdapters.class);
    }

    private TypeAdapters() {
        initial = UNINITIALIZED;
    }

    /**
     * 适配容器初始化
     * 
     */
    private void initial() {
        synchronized (LOCKED) {
            if (ADAPTERS.initial == UNINITIALIZED) {
                ADAPTERS.initial = INITIALIZING;
            } else {
                LOG.warn("Exists the initialize thread.");
                return;
            }
        }
        hitch();
        // TODO 初始化成功未做检查合理性 （Sanity）
    }

    private void hitch() {
        Hitcher hitcher = Hitchers.getHitcher(HITCH_PATH);
        initial = hitcher.hitch() ? INITSUCCESS : INITFAILURE;
    }

    /**
     * 添加类型适配器
     * 
     * @param adapter
     */
    public static void addAdapter(TypeAdapter adapter) {
    }

    /**
     * 获取指定类型适配器
     * 
     * @param type
     * @return
     */
    public static Iterator<TypeAdapter> getAdapter(Class<?> type) {
        if (ADAPTERS.initial == UNINITIALIZED) {
            ADAPTERS.initial();
        } else if (ADAPTERS.initial == INITFAILURE) {
            throw new IllegalStateException(TypeAdapters.class.getName()
                    + " could not be successfully initialized.");
        }
        return null;
    }

}
