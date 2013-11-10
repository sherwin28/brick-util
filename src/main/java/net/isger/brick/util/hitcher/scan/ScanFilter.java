package net.isger.brick.util.hitcher.scan;

public interface ScanFilter {

    public boolean isDeep();

    public boolean accept(String name);

}
