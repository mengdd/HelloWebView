package com.mengdd.download;

import android.content.Context;

public class DownloadManager {

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

    public boolean addDownloadTask(final Context context,
            final String downloadUrl,
            final OnDownloadChangedListener onDownloadChangedListener) {
        // TODO
        return true;
    }
}
