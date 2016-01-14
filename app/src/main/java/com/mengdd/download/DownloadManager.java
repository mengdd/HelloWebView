package com.mengdd.download;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.mengdd.hellowebview.utils.FileUtils;
import com.mengdd.hellowebview.utils.LogUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DownloadManager {
    private static final String LOG_TAG = "DownloadManager";

    private static final String DOWNLOAD_DIRECTORY = "/Download/HelloWebView";

    private Map<String, Downloader> mDownloadMap = new HashMap<String, Downloader>();

    // Singleton
    private DownloadManager() {
    }

    // Thread may keep a private copy of object for better performance.
    // Key word volatile stops this kind of action.
    // Threads have to read this object from shared memory every time when they
    // use it.
    private static volatile DownloadManager mInstance = null;

    public static DownloadManager getInstance() {
        if (null == mInstance) {
            synchronized (DownloadManager.class) {
                if (null == mInstance) {// double check here
                    mInstance = new DownloadManager();
                }
            }
        }
        return mInstance;
    }

    public boolean addDownloadTask(final Context context, final String downloadUrl,
                                   final OnDownloadChangedListener onDownloadChangedListener) {

        if (TextUtils.isEmpty(downloadUrl)) {
            LogUtil.i(LOG_TAG, "download url in web is empty!");
            return false;
        }

        Downloader downloader = mDownloadMap.get(downloadUrl);
        if (null == downloader) {
            downloader = new Downloader(downloadUrl);
            mDownloadMap.put(downloadUrl, downloader);
        }
        downloader.addDownloadChangedListener(onDownloadChangedListener);
        downloader.download();

        return true;
    }

    public static File getDownloadFile(String downloadURL) {
        String filename = FileUtils.getFileName(downloadURL);

        if (TextUtils.isEmpty(filename)) {
            return null;
        }

        File dir = getDownloadDirectory();
        // 如果目录不存在则创建目录,如果目录存在则返回
        FileUtils.createDirForcely(dir.toString());

        // 注意并没有创建这个文件，所以文件可能不存在
        File file = new File(dir, filename);

        return file;

    }

    /**
     * 得到下载目录
     *
     * @return
     */
    private static File getDownloadDirectory() {

        // Saving to external storage (SD card).
        String root = Environment.getExternalStorageDirectory().getPath();
        // if (!new File(root).exists()) {
        // root = Environment.getLegacyExternalStorageDirectory().getPath();
        // }

        File file = new File(root + DOWNLOAD_DIRECTORY);
        // 文件名不能有下划线，否则会崩在有些手机的native层

        return file;
    }
}
