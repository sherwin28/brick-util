package net.isger.brick.util.scanner.scan;

public interface ScanFilter {

    public boolean isDeep();

    public boolean accept(String name);

}
