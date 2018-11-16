package com.ddmeng.hellowebview.download;

import java.io.File;

public interface OnDownloadChangedListener {

    public void onAlreadyStarted(final File file);

    public void onDownloadStarted(final File file);

    public void onDownloadFinished(final File file);

    public void onDownloadFailed(final File file);

}
