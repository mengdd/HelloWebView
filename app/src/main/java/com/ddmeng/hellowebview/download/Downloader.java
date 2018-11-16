package com.ddmeng.hellowebview.download;


import com.ddmeng.hellowebview.utils.LogUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Downloader {

    private static final String LOG_TAG = "Downloader";
    private static final int CONNECT_TIMEOUT = 1000 * 10;
    private static final int READ_TIMEOUT = 1000 * 30;

    private URL mDownloadUrl = null;
    private File mFile = null;

    public Downloader(String url) {

        try {
            mDownloadUrl = new URL(url);
            mFile = DownloadManager.getDownloadFile(url);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }

    private boolean isDownloading = false;

    public boolean download() {
        if (isDownloading) {

            LogUtil.i(LOG_TAG, "download running already!");
            onAlreadyStarted();

            return false;

        }

        isDownloading = true;

        new DownloadThread().start();
        return true;

    }

    private class DownloadThread extends Thread {

        @Override
        public void run() {

            HttpURLConnection connection = null;
            RandomAccessFile threadfile = null;
            InputStream inputStream = null;
            try {
                onStarted();
                // 使用Get方式下载
                connection = (HttpURLConnection) mDownloadUrl.openConnection();
                connection.setConnectTimeout(CONNECT_TIMEOUT);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Referer", mDownloadUrl.toString());
                connection.setRequestProperty("Charset", "UTF-8");
                connection.setRequestProperty("Connection", "Keep-Alive");

                // 断点续传
                long downloadedLength = mFile.length() - 1;
                if (downloadedLength < 0) {
                    downloadedLength = 0;
                }
                connection.setRequestProperty("Range", "bytes=" + downloadedLength + "-");// 设置获取实体数据的范围

                // 如果文件已经完整存在,参数传mFile.length(), 会受到响应值416
                connection.connect();

                LogUtil.i(LOG_TAG, "Range" + "bytes=" + mFile.length() + "-");
                if (HttpURLConnection.HTTP_OK == connection.getResponseCode()
                        || HttpURLConnection.HTTP_PARTIAL == connection.getResponseCode()) {
                    LogUtil.i(LOG_TAG, "response code: " + connection.getResponseCode());
                    LogUtil.i(LOG_TAG, "response content length: " + connection.getContentLength());
                    inputStream = connection.getInputStream();
                    byte[] buffer = new byte[1024];
                    int byteCount = 0;

                    threadfile = new RandomAccessFile(mFile, "rwd");

                    LogUtil.i(LOG_TAG, "random file length: " + threadfile.length());
                    threadfile.seek(downloadedLength);
                    while ((byteCount = inputStream.read(buffer, 0, 1024)) != -1) {
                        threadfile.write(buffer, 0, byteCount);
                    }
                    onFinished();

                } else {
                    LogUtil.e(LOG_TAG, "response code: " + connection.getResponseCode());
                    onFailed();
                }
            } catch (Exception e) {
                LogUtil.e(LOG_TAG, "exception in download");
                e.printStackTrace();
                onFailed();

            } finally {

                isDownloading = false;
                try {
                    if (null != inputStream) {
                        inputStream.close();
                    }

                    if (null != threadfile) {
                        threadfile.close();
                    }

                    if (null != connection) {
                        connection.disconnect();
                    }
                } catch (IOException exception) {
                    exception.printStackTrace();
                }

            }

        }

    }

    // status methods
    private void onStarted() {

        LogUtil.i(LOG_TAG, " ================= on Started ================= ");
        if (null != mDownloadChangedListeners && null != mFile) {
            for (OnDownloadChangedListener listener : mDownloadChangedListeners) {
                listener.onDownloadStarted(mFile);
            }
        }

    }

    private void onFinished() {
        LogUtil.i(LOG_TAG, " ================= on Finished ================= ");
        if (null != mDownloadChangedListeners && null != mFile) {
            for (OnDownloadChangedListener listener : mDownloadChangedListeners) {
                listener.onDownloadFinished(mFile);
            }
        }

    }

    private void onAlreadyStarted() {
        LogUtil.i(LOG_TAG, " ================= on Already Started ================= ");
        if (null != mDownloadChangedListeners && null != mFile) {
            for (OnDownloadChangedListener listener : mDownloadChangedListeners) {
                listener.onAlreadyStarted(mFile);
            }
        }

    }

    private void onFailed() {
        LogUtil.i(LOG_TAG, " ================= on Failed ================= ");
        if (null != mDownloadChangedListeners && null != mFile) {
            for (OnDownloadChangedListener listener : mDownloadChangedListeners) {
                listener.onDownloadFailed(mFile);
            }
        }

    }

    private List<OnDownloadChangedListener> mDownloadChangedListeners = null;

    public void addDownloadChangedListener(OnDownloadChangedListener listener) {
        if (null == mDownloadChangedListeners) {
            mDownloadChangedListeners = new ArrayList<OnDownloadChangedListener>();
        }

        if (null != listener) {
            mDownloadChangedListeners.add(listener);
        }
    }

}
