package net.isger.brick.util.scanner.scan;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.isger.brick.util.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JarScan extends AbstractScan {

    private static final Logger LOG;

    static {
        LOG = LoggerFactory.getLogger(JarScan.class);
    }

    public String[] scan(URL url, ScanFilter filter) {
        if (!"jar".equalsIgnoreCase(url.getProtocol())) {
            return null;
        }
        File workPath = null;
        String path = url.getPath();
        int index = path.lastIndexOf("jar!/");
        if (index != -1) {
            workPath = new File(path.substring(index + 5));
            path = path.substring(0, index + 3);
        } else if (!path.endsWith(".jar")) {
            throw new IllegalStateException("Have the ability to give the url "
                    + url + " of jar");
        }

        List<String> result = new ArrayList<String>();
        ZipInputStream zis = null;
        try {
            zis = getZipInputStream(path);
            ZipEntry entry = null;
            while ((entry = zis.getNextEntry()) != null) {
                if (match(workPath, entry, filter)) {
                    result.add(getName(workPath, new File(entry.getName())));
                }
            }
        } catch (IOException e) {
            LOG.warn("Error scanning hicher from path {}", url, e);
        } finally {
            Files.close(zis);
        }
        return result.toArray(new String[result.size()]);
    }

    private ZipInputStream getZipInputStream(String path) throws IOException {
        ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(new FileInputStream(path));
        } catch (IOException e) {
            zis = new ZipInputStream(new URL(path).openStream());
        }
        return zis;
    }

    protected boolean match(File path, ZipEntry entry, ScanFilter filter) {
        boolean isMatch = false;
        if (!entry.isDirectory()) {
            File sourceFile = new File(entry.getName());
            File parentPath = sourceFile.getParentFile();
            toMatchPath: {
                if (parentPath == null) {
                    isMatch = path == null;
                    break toMatchPath;
                }
                isMatch = filter.isDeep() ? parentPath.getAbsolutePath()
                        .startsWith(path.getAbsolutePath()) : parentPath
                        .equals(path);
            }
            return isMatch && filter.accept(sourceFile.getName());
        }
        return isMatch;
    }

}
