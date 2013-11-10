package net.isger.brick.util.hitcher;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;

import net.isger.brick.blue.ClassSeal;
import net.isger.brick.blue.Marks.ACCESS;
import net.isger.brick.blue.Marks.MISC;
import net.isger.brick.blue.Marks.OPCODES;
import net.isger.brick.blue.Marks.TYPE;
import net.isger.brick.blue.Marks.VERSION;
import net.isger.brick.blue.MethodSeal;
import net.isger.brick.util.hitcher.scan.ScanFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hitcher extends ClassLoader {

    private static final String HITCHER = "Hitcher";

    private static final String HITCH_SUFFIX = "Hitch.class";

    private static final Logger LOG;

    private static final ScanFilter FILTER;

    private Hitcher target;

    private int amount;

    static {
        LOG = LoggerFactory.getLogger(Hitcher.class);
        FILTER = new ScanFilter() {
            public boolean isDeep() {
                return false;
            }

            public boolean accept(String name) {
                return name.endsWith(HITCH_SUFFIX);
            }
        };
    }

    protected Hitcher() {
    }

    public Hitcher(String path) {
        Set<URL> urls = findHitchURLs(path);
        // public class HitchStation extends Hitcher {
        ClassSeal cs = ClassSeal.create(VERSION.V0104.value,
                ACCESS.PUBLIC.value, HITCHER, Hitcher.class.getName());
        // public HitchStation() {}
        MethodSeal ms = cs.makeMethod(ACCESS.PUBLIC.value, "void", "<init>");
        ms.coding("this", "super()");
        // public boolean hitch(Object source) {
        ms = cs.makeMethod(ACCESS.PUBLIC.value, TYPE.BOOLEAN.name, "hitch",
                TYPE.OBJECT.name);
        String className = null;
        String hitchOperate = null;
        for (URL url : urls) {
            for (String name : Scanner.scan(url, FILTER)) {
                className = (path + name.replaceFirst("[.]class$", ""))
                        .replaceAll("/", ".");
                hitchOperate = className + ".hitch(obj)";
                if (ms.getOperateMark(hitchOperate) != null) {
                    LOG.warn("Multiple to hitching {}", className);
                } else {
                    // TODO 搭载过程未做异常处理
                    ms.markOperate(hitchOperate, className,
                            OPCODES.INVOKESTATIC.value, TYPE.VOID.name,
                            "hitch", TYPE.OBJECT.name);
                    ms.coding(null, hitchOperate, MISC.arg(0));
                    amount++;
                }
            }
        }
        // return true;
        ms.markConst("true", true);
        ms.coding("return", null, "true");
        // }
        byte[] code = net.isger.brick.blue.Compiler.compile(cs);
        try {
            target = (Hitcher) this.defineClass(HITCHER, code, 0, code.length)
                    .newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Failure create hitch station for "
                    + path);
        }
    }

    private static Set<URL> findHitchURLs(String path) {
        Set<URL> urls = new LinkedHashSet<URL>();
        try {
            ClassLoader loader = Hitcher.class.getClassLoader();
            Enumeration<URL> urlEnum;
            if (loader == null) {
                urlEnum = ClassLoader.getSystemResources(path);
            } else {
                urlEnum = loader.getResources(path);
            }
            while (urlEnum.hasMoreElements()) {
                urls.add((URL) urlEnum.nextElement());
            }
        } catch (IOException e) {
            LOG.warn("Error getting resources from path.", e);
        }
        return urls;
    }

    public int getHitchCount() {
        return amount;
    }

    public boolean hitch(Object source) {
        boolean isHitch = false;
        if (amount > 0) {
            try {
                target.hitch(source);
                isHitch = true;
            } catch (Exception e) {
            }
        }
        return isHitch;
    }

}
