package net.isger.brick.util.hicher;

import java.io.File;
import java.net.URL;

public abstract class Scaner {

    public static final String HITCH_SUFFIX = "Hitch.class";

    public abstract String[] scan(URL url);

    protected boolean match(File path, String source) {
        boolean isMatch;
        File sourceFile = new File(source);
        File parentPath = sourceFile.getParentFile();
        toMatchPath: {
            if (parentPath == null) {
                isMatch = path == null;
                break toMatchPath;
            }
            isMatch = parentPath.equals(path);
        }
        return isMatch && matchName(sourceFile.getName());
    }

    protected boolean matchName(String name) {
        return name.endsWith(HITCH_SUFFIX);
    }

}
