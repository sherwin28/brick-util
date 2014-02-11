package net.isger.brick.util.scanner;

import java.net.URL;

import net.isger.brick.util.scanner.scan.ScanFilter;

public interface Scan {

    public String[] scan(URL url, ScanFilter filter);

}
