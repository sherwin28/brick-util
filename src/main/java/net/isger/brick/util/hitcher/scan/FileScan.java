package net.isger.brick.util.hitcher.scan;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FileScan extends AbstractScan {

    public String[] scan(URL url, final ScanFilter filter) {
        if (url.getProtocol() != "file") {
            return null;
        }
        String[] result = null;
        File path = new File(url.getPath());
        if (path.isDirectory()) {
            result = scan(path, path, filter).toArray(new String[0]);
        }
        return result;
    }

    private List<String> scan(File basePath, File targetPath, ScanFilter filter) {
        List<String> result = new ArrayList<String>();
        boolean isDeep = filter.isDeep();
        for (File file : targetPath.listFiles()) {
            if (file.isFile()) {
                if (filter.accept(file.getName())) {
                    result.add(getName(basePath, file));
                } else if (isDeep) {
                    result.addAll(scan(basePath, file, filter));
                }
            }
        }
        return result;
    }
}
