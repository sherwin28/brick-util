package net.isger.brick.util;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.isger.brick.util.hicher.FileScaner;
import net.isger.brick.util.hicher.Hitcher;
import net.isger.brick.util.hicher.JarScaner;
import net.isger.brick.util.hicher.Scaner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 便车工具类
 * 
 * @author issing
 * 
 */
public class Hitchers extends ClassLoader {

    private static final Logger LOG;

    private static final Map<String, Scaner> SCANERS;

    private static final Map<String, Hitcher> HITCHERS;

    private static final String SCANER_TYPE_JAR = "jar";

    private static final String SCANER_TYPE_FILE = "file";

    static {
        LOG = LoggerFactory.getLogger(Hitchers.class);
        SCANERS = new HashMap<String, Scaner>() {
            private static final long serialVersionUID = -1973320412763851681L;
            {
                this.put(SCANER_TYPE_JAR, new JarScaner());
                this.put(SCANER_TYPE_FILE, new FileScaner());
            }
        };
        HITCHERS = new HashMap<String, Hitcher>();
    }

    private Hitchers() {
    }

    public static Hitcher getHitcher(String path) {
        Hitcher hitcher = HITCHERS.get(path);
        if (hitcher == null) {
            hitcher = new Hitcher(path);
        }
        return hitcher;
    }

    public static String[] scanHichNames(URL url) {
        String protocol = url.getProtocol();
        Scaner scaner = SCANERS.get(protocol);
        if (scaner == null) {
            LOG.warn("Not found the specified type [{}] of scaner.", protocol);
            return null;
        }
        return scaner.scan(url);
    }
}
