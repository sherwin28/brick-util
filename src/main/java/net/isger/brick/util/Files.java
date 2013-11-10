package net.isger.brick.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

/**
 * 文件工具
 * 
 * @author issing
 * 
 */
public class Files {

    private Files() {
    }

    public static boolean isJar(String path) {
        JarInputStream jis = null;
        try {
            jis = openJarIS(path);
            return jis.getNextJarEntry() != null;
        } catch (IOException e) {
            return false;
        } finally {
            close(jis);
        }
    }

    /**
     * 打开Jar输出流
     * 
     * @param path
     * @return
     */
    public static JarOutputStream openJarOS(String path) throws IOException {
        JarOutputStream jos = null;
        try {
            jos = new JarOutputStream(new FileOutputStream(path));
        } catch (IOException e) {
            jos = new JarOutputStream(new URL(path).openConnection()
                    .getOutputStream());
        }
        return jos;
    }

    /**
     * 打开Jar输入流
     * 
     * @param path
     * @return
     * @throws IOException
     */
    public static JarInputStream openJarIS(String path) throws IOException {
        JarInputStream jis = null;
        try {
            jis = new JarInputStream(new FileInputStream(path));
        } catch (IOException e) {
            jis = new JarInputStream(new URL(path).openConnection()
                    .getInputStream());
        }
        return jis;
    }

    /**
     * 搜索文件
     * 
     * @param jis
     * @param name
     * @return
     * @throws IOException
     */
    public static JarEntry search(JarInputStream jis, String name)
            throws IOException {
        JarEntry je = null;
        while ((je = jis.getNextJarEntry()) != null) {
            if (!je.isDirectory() && je.getName().equals(name)) {
                break;
            }
        }
        return je;
    }

    /**
     * 获取文件（规范性过滤）
     * 
     * @param name
     * @return
     */
    public static File getFile(String name) {
        File file = new File(name);
        if (!isCanonical(file)) {
            file = null;
        }
        return file;
    }

    /**
     * 获取文件（规范性过滤）
     * 
     * @param path
     * @param name
     * @return
     */
    public static File getFile(String path, String name) {
        File file = new File(path, name);
        if (!isCanonical(file)) {
            file = null;
        }
        return file;
    }

    /**
     * 判断文件规范性
     * 
     * @param file
     * @return
     */
    private static boolean isCanonical(File file) {
        boolean result = true;
        try {
            file.getCanonicalPath();
        } catch (IOException e) {
            result = false;
        }
        return result;
    }

    /**
     * 拷贝文件
     * 
     * @param is
     * @param dest
     * @return
     * @throws IOException
     */
    public static File copy(InputStream is, String dest) throws IOException {
        File file = getFile(dest);
        OutputStream os = null;
        try {
            File pf = file.getParentFile();
            if (pf != null && !pf.exists()) {
                pf.mkdirs();
            }
            file.createNewFile();
            os = new FileOutputStream(file);
            byte[] bs = new byte[1024];
            int len = 0;
            while ((len = is.read(bs)) != -1) {
                os.write(bs, 0, len);
                os.flush();
            }
        } finally {
            close(os);
        }
        return file;
    }

    /**
     * 关闭连接
     * 
     * @param closeable
     */
    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
            }
        }
    }
}
