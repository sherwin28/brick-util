package net.isger.brick.util.scanner;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import net.isger.brick.util.hitcher.Director;
import net.isger.brick.util.hitcher.Hitcher;
import net.isger.brick.util.scanner.scan.FileScan;
import net.isger.brick.util.scanner.scan.JarScan;
import net.isger.brick.util.scanner.scan.ScanFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Scanner extends Director {

    private static final String KEY_SCANS = "brick.util.scanner.scans";

    private static final String SCAN_PATH = "net/isger/brick/util/scanner/scan";

    private static final Logger LOG;

    private static final Scanner SCANNER;

    private Vector<Scan> scans;

    static {
        LOG = LoggerFactory.getLogger(Scanner.class);
        SCANNER = new Scanner();
    }

    private Scanner() {
        scans = new Vector<Scan>();
        add(new FileScan());
        add(new JarScan());
    }

    protected String directHitchPath() {
        return this.directHitchPath(KEY_SCANS, SCAN_PATH);
    }

    protected void directSanity() {

    }

    public static Scanner getScanner() {
        return canonicalize(SCANNER);
    }

    public void add(Scan scan) {
        if (LOG.isDebugEnabled()) {
            LOG.info("Binding scan {}", scan.getClass().getName());
        }
        scans.add(scan);
    }

    public static String[] scan(String path, ScanFilter filter) {
        List<String> result = new ArrayList<String>();
        Set<URL> urls = findURLs(path);
        for (URL url : urls) {
            result.addAll(Arrays.asList(scan(url, filter)));
        }
        return result.toArray(new String[result.size()]);
    }

    public static String[] scan(URL url, ScanFilter filter) {
        List<String> result = new ArrayList<String>();
        Scanner scanner = getScanner();
        String[] names;
        for (Scan scan : scanner.scans) {
            names = scan.scan(url, filter);
            if (names != null && names.length > 0) {
                result.addAll(Arrays.asList(names));
            }
        }
        return result.toArray(new String[result.size()]);
    }

    private static Set<URL> findURLs(String path) {
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
            LOG.warn("Error getting resources from path {}", path, e);
        }
        return urls;
    }

}
