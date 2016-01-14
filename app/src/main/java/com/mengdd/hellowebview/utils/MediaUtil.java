package com.mengdd.hellowebview.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class MediaUtil {

    private final static String MIMETYPE_OCTET_STREAM = "application/octet-stream";

    public static boolean playMedia(Context context, String url, String mimeType) {
        if (!isVideoType(mimeType)) {
            return false;
        }
        // 直接播放失败, 调用选择框让用户选择
        return playMediaForUserSelect(context, url, mimeType);
    }

    public static boolean isVideoType(String mimeType) {
        int fileType = MediaFile.getFileTypeForMimeType(mimeType);
        if (MIMETYPE_OCTET_STREAM.equals(mimeType)) {

            return false;
        }

        // cctv'live format: application/vnd.apple.mpegurl
        return MediaFile.isVideoFileType(fileType)
                || "video/*".equalsIgnoreCase(mimeType)
                || "application/vnd.apple.mpegurl".equals(mimeType);
    }

    /**
     * Users choose a app to play media
     */
    private static boolean playMediaForUserSelect(Context c, String url,
                                                  String mimetype) {
        Intent intent = BrowserUtils.newIntentFromSelf(Intent.ACTION_VIEW);
        Uri name = Uri.parse(url);
        intent.setDataAndType(name, mimetype);
        try {
            c.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            return false;
        }
        return true;
    }

}
