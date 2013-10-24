package net.isger.brick.util.hicher;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;

public class FileScaner extends Scaner {

    public String[] scan(URL url) {
        String[] result = null;
        File path = new File(url.getPath());
        if (path.isDirectory()) {
            result = path.list(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return matchName(name);
                }
            });
        }
        return result;
    }

}
