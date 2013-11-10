package net.isger.brick.util.hitcher;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.StringTokenizer;

import net.isger.brick.util.Hitchers;

public abstract class Director {

    protected static final int UNDIRECTED = 0;

    protected static final int DIRECTING = 1;

    protected static final int DIRESUCCESS = 2;

    protected static final int DIREFAILURE = 3;

    /** 分隔标记 */
    private static final String TOKEN_SEPARETOR = ":";

    protected int directed;

    protected Director() {
        directed = UNDIRECTED;
    }

    /**
     * 引导
     * 
     */
    protected void direct(Object source) {
        synchronized (this.getClass()) {
            if (directed == UNDIRECTED) {
                directed = DIRECTING;
            } else {
                return;
            }
        }
        if (source == null) {
            source = this;
        }
        StringTokenizer directHitchPath = new StringTokenizer(
                directHitchPath(), TOKEN_SEPARETOR);
        while (directHitchPath.hasMoreElements() && directed != DIREFAILURE) {
            directHitch(directHitchPath.nextElement().toString(), source);
        }
        directSanity();
        directed = DIRESUCCESS;
    }

    /**
     * 路径
     * 
     * @return
     */
    protected abstract String directHitchPath();

    /**
     * 路径
     * 
     * @param key
     * @param def
     * @return
     */
    protected String directHitchPath(final String key, final String def) {
        String hitchPath = AccessController
                .doPrivileged(new PrivilegedAction<String>() {
                    public String run() {
                        return System.getProperty(key, def);
                    }
                });
        if (hitchPath.indexOf(def) == -1) {
            hitchPath += TOKEN_SEPARETOR + def;
        }
        return hitchPath;
    }

    /**
     * 搭载
     * 
     * @param path
     */
    private void directHitch(String path, Object source) {
        Hitcher hitcher = Hitchers.getHitcher(path);
        if (hitcher.getHitchCount() == 0 || hitcher.hitch(this)) {
            directAttach(path);
            return;
        }
        directed = DIREFAILURE;
    }

    /**
     * 附加
     * 
     * @param path
     */
    protected void directAttach(String path) {
    }

    /**
     * 合理
     * 
     */
    protected void directSanity() {
    }

    /**
     * 规范
     * 
     * @param director
     * @return
     */
    public static <T extends Director> T canonicalize(T director) {
        return canonicalize(director, director);
    }

    /**
     * 规范
     * 
     * @param director
     * @param source
     * @return
     */
    public static <T extends Director> T canonicalize(T director, Object source) {
        if (director.directed == UNDIRECTED) {
            director.direct(source);
        } else if (director.directed == DIREFAILURE) {
            throw new IllegalStateException(director.getClass().getName()
                    + " could not be successfully initialized.");
        }
        return director;
    }

}
