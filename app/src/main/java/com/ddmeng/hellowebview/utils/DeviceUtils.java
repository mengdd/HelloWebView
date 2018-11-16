package com.ddmeng.hellowebview.utils;

import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;

public class DeviceUtils {
    public static int getScreenWidth(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.widthPixels;
    }

    public static int getScreenHeight(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.heightPixels;
    }

    public static boolean isScreenLand(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.heightPixels < metrics.widthPixels;
    }

    public synchronized static int getSDKVersion() {
        return Build.VERSION.SDK_INT;
    }
}
