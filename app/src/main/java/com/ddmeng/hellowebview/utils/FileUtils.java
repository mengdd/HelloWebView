package com.ddmeng.hellowebview.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.webkit.URLUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;

public class FileUtils {

    private static final String LOG_TAG = "FileUtils";

    /**
     * 创建目录
     *
     * @param dirPath
     * @return
     */
    public static boolean createDir(String dirPath) {
        LogUtil.i(LOG_TAG, "create dir: " + dirPath);
        File f = new File(dirPath);
        if (f.exists()) {
            if (f.isDirectory()) {
                return true;
            }
            f.delete();
            return f.mkdir();
        }
        return f.mkdirs();
    }

    /**
     * 创建文件
     *
     * @param filename
     */
    public static void createFile(String filename) {

        File file = new File(filename);

        if (!file.exists()) {
            try {

                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 根据下载路径得到文件名
     *
     * @param downloadUrl
     * @return
     */
    public static String getFileName(String downloadUrl) {
        String filenameUndecoded = URLUtil.guessFileName(downloadUrl, null, null);
        try {
            filenameUndecoded = URLDecoder.decode(filenameUndecoded, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        LogUtil.i(LOG_TAG, "getFileName: " + filenameUndecoded);
        return filenameUndecoded;
    }

    /**
     * 删除目录中的文件
     *
     * @param dirPath
     */
    public static void cleanFilesInDir(String dirPath) {
        LogUtil.i(LOG_TAG, "cleanFilesInDir");
        File dir = new File(dirPath);
        if (dir.exists() && dir.isDirectory()) {
            LogUtil.i(LOG_TAG, "directory exists");
            File[] files = dir.listFiles();
            if (null != files) {
                LogUtil.i(LOG_TAG, "files length: " + files.length);
                for (File f : files) {
                    boolean ret = f.delete();// 删除目录中的文件和空目录，非空目录无法删除
                    LogUtil.i(LOG_TAG, "ret: " + ret);
                }
            }
        }
    }

    /**
     * 存储Bitmap到指定路径
     *
     * @param bitmap
     * @param dirName
     * @param fileName
     * @return
     */
    public static boolean saveBitmap(Bitmap bitmap, String dirName, String fileName) {

        File file = new File(dirName, fileName);
        FileUtils.createFile(file.toString());
        LogUtil.i(LOG_TAG, "saveBitmap: " + file.toString());
        try {
            BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(
                    file.toString()));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.flush();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        LogUtil.i(LOG_TAG, "save finished! " + file.toString() + ", length: " + file.length());
        return true;
    }

    public static Bitmap loadBitmap(String dir, String fileName) {
        Bitmap bitmap = null;
        File path = new File(dir, fileName);
        if (path.exists() && path.isFile()) {
            bitmap = BitmapFactory.decodeFile(path.toString());
        }
        return bitmap;
    }

    private static final String TEMP_FILE_PREFIX = "tmp";

    /**
     * 创建临时文件
     *
     * @param dirFile
     * @return
     */
    public static File createTempFile(File dirFile) {
        File tmpFile = null;
        try {
            tmpFile = File.createTempFile(TEMP_FILE_PREFIX, null, dirFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tmpFile;
    }

    public static boolean createSingleDir(String path) {
        LogUtil.i(LOG_TAG, "create sub dir: " + path);
        if (null == path) {
            return false;
        }
        File f = new File(path);
        if (f.exists()) {
            if (f.isDirectory()) {
                return true;
            } else {
                // exist but is a file
                f.delete();
            }

        }

        // if file not exists
        return f.mkdir();
    }

    public static boolean createDirForcely(String dirPath) {
        LogUtil.i(LOG_TAG, "create dir forcely: " + dirPath);
        if (null == dirPath) {
            return false;
        }
        File file = new File(dirPath);

        File parent = null;
        parent = file.getParentFile();

        if (null != parent) {
            // Recursion
            createDirForcely(parent.toString());
        }
        return createSingleDir(file.toString());

    }
}
