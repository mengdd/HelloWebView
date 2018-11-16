package com.ddmeng.hellowebview.utils;

import android.content.Intent;
import android.webkit.WebSettings;

import java.lang.reflect.Method;

public class BrowserUtils {

    public static final String IS_FROM_SELF = "com.mengdd.hellobrowser.IS_FROM_SELF";

    /**
     * set page cache
     *
     * @param settings
     * @param capacity
     */
    public static void setWebViewPageCache(WebSettings settings, int capacity) {
        try {
            Method setPageCache = settings.getClass().getMethod(
                    "setPageCacheCapacity", new Class[]{int.class});
            setPageCache.invoke(settings, new Object[]{capacity});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Intent newIntentFromSelf(String action) {
        return newIntentFromSelf().setAction(action);
    }

    public static Intent newIntentFromSelf() {
        Intent intent = new Intent();
        intent.putExtra(IS_FROM_SELF, true);
        return intent;
    }

}
