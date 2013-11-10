package net.isger.brick.util.hitcher;

import java.net.URL;

import net.isger.brick.util.hitcher.scan.ScanFilter;

public interface Scan {

    public String[] scan(URL url, ScanFilter filter);

}
