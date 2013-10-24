package net.isger.brick.util.hicher;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;

import net.isger.brick.blue.ClassSeal;
import net.isger.brick.blue.Marks.ACCESS;
import net.isger.brick.blue.Marks.OPCODES;
import net.isger.brick.blue.Marks.TYPE;
import net.isger.brick.blue.Marks.VERSION;
import net.isger.brick.blue.MethodSeal;
import net.isger.brick.util.Hitchers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hitcher extends ClassLoader {

    private static final Logger LOG;

    private Class<?> stationClass;

    private int amount;

    static {
        LOG = LoggerFactory.getLogger(Hitcher.class);
    }

    public Hitcher(String path) {
        Set<URL> urls = findHitchURLs(path);
        ClassSeal cs = ClassSeal.create(VERSION.V0104.value,
                ACCESS.PUBLIC.value, "HitchStation", TYPE.OBJECT.name);
        MethodSeal ms = cs.makeMethod(
                ACCESS.PUBLIC.value | ACCESS.STATIC.value, "void", "main",
                "java.lang.String[]");
        String className = null;
        for (URL url : urls) {
            for (String name : Hitchers.scanHichNames(url)) {
                className = path.replaceAll("/", ".") + "."
                        + name.replaceFirst("\\.class$", "");
                if (ms.getOperateMark(className + ".hitch()") != null) {
                    LOG.warn("Multiple [{}] hitching.", className);
                } else {
                    // TODO 搭载过程未做异常处理
                    ms.markOperate(className + ".hitch()", className,
                            OPCODES.INVOKESTATIC.value, "void", "hitch");
                    ms.coding(null, className + ".hitch()");
                    amount++;
                }
            }
        }
        byte[] code = net.isger.brick.blue.Compiler.compile(cs);
        stationClass = this.defineClass("HitchStation", code, 0, code.length);
    }

    private static Set<URL> findHitchURLs(String path) {
        Set<URL> urls = new LinkedHashSet<URL>();
        try {
            ClassLoader loader = Hitchers.class.getClassLoader();
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

    public boolean hitch() {
        boolean isHitch = false;
        if (amount > 0) {
            try {
                stationClass.getMethods()[0].invoke(null,
                        new Object[] { new String[] {} });
                isHitch = true;
            } catch (Exception e) {
            }
        }
        return isHitch;
    }

}
