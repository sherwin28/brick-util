package net.isger.brick.util.hitcher.scan;

import java.io.File;

import net.isger.brick.util.hitcher.Scan;

public abstract class AbstractScan implements Scan {

    private static final String HITCH_SUFFIX = "Hitch.class";

    protected String getName(File basePath, File target) {
        return target.getAbsolutePath().substring(
                basePath.getAbsolutePath().length());
    }

    protected boolean matchName(String name) {
        return name.endsWith(HITCH_SUFFIX);
    }

}
