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
import net.isger.brick.util.scanner.Scanner;
import net.isger.brick.util.scanner.scan.ScanFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hitcher extends ClassLoader {

    private static final String HITCH_STATION = "HitchStation";

    private static final String HITCH_METHOD = "hitch";

    private static final String HITCH_SUFFIX = "Hitch.class";

    private static final Logger LOG;

    private static final ScanFilter FILTER;

    private Class<?> station;

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

    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return this.getClass().getClassLoader().loadClass(name);
    }

    public Hitcher(String path) {
        Set<URL> urls = findHitchURLs(path);
        // public class HitchStation {
        ClassSeal cs = ClassSeal.create(VERSION.V0104.value,
                ACCESS.PUBLIC.value, HITCH_STATION, TYPE.OBJECT.name);
        // public HitchStation() {}
        MethodSeal ms = cs.makeMethod(ACCESS.PUBLIC.value, "void", "<init>");
        ms.coding("this", "super()");
        // public static boolean hitch(Object source) {
        ms = cs.makeMethod(ACCESS.PUBLIC.value | ACCESS.STATIC.value,
                TYPE.BOOLEAN.name, HITCH_METHOD, TYPE.OBJECT.name);
        String className = null;
        String hitchOperate = null;
        for (URL url : urls) {
            for (String name : Scanner.scan(url, FILTER)) {
                className = (path + name.replaceFirst("[.]class$", ""))
                        .replaceAll("[\\\\/]", ".");
                hitchOperate = className + ".hitch(obj)";
                if (ms.getOperateMark(hitchOperate) != null) {
                    LOG.warn("Multiple to hitching {}", className);
                } else {
                    // TODO 搭载过程未做异常处理
                    ms.markOperate(hitchOperate, className,
                            OPCODES.INVOKESTATIC.value, TYPE.VOID.name,
                            HITCH_METHOD, TYPE.OBJECT.name);
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
            station = this.defineClass(HITCH_STATION, code, 0, code.length);
        } catch (Throwable e) {
            throw new IllegalStateException("Failure create hitch station for "
                    + path, e);
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
                station.getMethod(HITCH_METHOD, Object.class).invoke(station,
                        source);
                isHitch = true;
            } catch (Exception e) {
                LOG.warn("Failure to hitch resource", e);
            }
        }
        return isHitch;
    }
}
