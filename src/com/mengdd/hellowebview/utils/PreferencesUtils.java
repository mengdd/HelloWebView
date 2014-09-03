package com.mengdd.hellowebview.utils;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesUtils {

    private static SharedPreferences.Editor mEditer = null;

    private final static String PREFERENCE_NAME = "hello_webview_preferences";

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    /**
     * 加载boolean类型值
     *
     * @param key
     *            待加载值的主键
     * @return 指定key的值, 不存在返回false
     */
    public static boolean loadBoolean(Context context, String key) {
        return loadBoolean(context, key, false);
    }

    /**
     * 加载boolean类型值
     *
     * @param key
     *            待加载值的主键
     * @param defValue
     *            如果key不存在默认值
     * @return 指定key的boolean值
     */
    public static boolean loadBoolean(Context context, String key, boolean defValue) {
        return getSharedPreferences(context).getBoolean(key, defValue);
    }

    /**
     * 加载int类型值
     *
     * @param key
     *            待加载值的主键
     * @return 指定key的值, 不存在返回0
     */
    public static int loadInt(Context context, String key) {
        return loadInt(context, key, 0);
    }

    /**
     * 加载int类型值
     *
     * @param key
     *            待加载值的主键
     * @param defValue
     *            如果key不存在默认值
     * @return 指定key的int值
     */
    public static int loadInt(Context context, String key, int defValue) {
        return getSharedPreferences(context).getInt(key, defValue);
    }

    /**
     * 加载float类型值
     *
     * @param key
     *            待加载值的主键
     * @return 指定key的值, 不存在返回0.0f
     */
    public static float loadFloat(Context context, String key) {
        return loadFloat(context, key, 0.0f);
    }

    /**
     * 加载float类型值
     *
     * @param key
     *            待加载值的主键
     * @param defValue
     *            如果key不存在默认值
     * @return 指定key的float值
     */
    public static float loadFloat(Context context, String key, float defValue) {
        return getSharedPreferences(context).getFloat(key, defValue);
    }

    /**
     * 加载long类型值
     *
     * @param key
     *            待加载值的主键
     * @return 指定key的值, 不存在返回0L
     */
    public static long loadLong(Context context, String key) {
        return loadLong(context, key, 0L);
    }

    /**
     * 加载类型值
     *
     * @param key
     *            待加载值的主键
     * @param defValue
     *            如果key不存在默认值
     * @return 指定key的值
     */
    public static long loadLong(Context context, String key, long defValue) {
        return getSharedPreferences(context).getLong(key, defValue);
    }

    /**
     * 加载String类型值
     *
     * @param key
     *            待加载值的主键
     * @return 指定key的值, 不存在返回null
     */
    public static String loadString(Context context, String key) {
        return loadString(context, key, null);
    }

    /**
     * 加载String类型值
     *
     * @param key
     *            待加载值的主键
     * @param defValue
     *            如果key不存在默认值
     * @return 指定key的String值
     */
    public static String loadString(Context context, String key, String defValue) {
        return getSharedPreferences(context).getString(key, defValue);
    }

    /**
     * 保存指定key的boolean类型值
     *
     * @param key
     *            待保存值的主键
     * @param value
     *            待保存的boolean值
     */
    public static void saveBoolean(Context context, String key, boolean value) {
        if (mEditer == null) {
            mEditer = getSharedPreferences(context).edit();
        }
        mEditer.putBoolean(key, value);
        mEditer.commit();
    }

    /**
     * 保存指定key的int类型值
     *
     * @param key
     *            待保存值的主键
     * @param value
     *            待保存的int值
     */
    public static void saveInt(Context context, String key, int value) {
        if (mEditer == null) {
            mEditer = getSharedPreferences(context).edit();
        }
        mEditer.putInt(key, value);
        mEditer.commit();
    }

    /**
     * 保存指定key的float类型值
     *
     * @param key
     *            待保存值的主键
     * @param value
     *            待保存的float值
     */
    public static void saveFloat(Context context, String key, float value) {
        if (mEditer == null) {
            mEditer = getSharedPreferences(context).edit();
        }
        mEditer.putFloat(key, value);
        mEditer.commit();
    }

    /**
     * 保存指定key的long类型值
     *
     * @param key
     *            待保存值的主键
     * @param value
     *            待保存的long值
     */
    public static void saveLong(Context context, String key, long value) {
        if (mEditer == null) {
            mEditer = getSharedPreferences(context).edit();
        }
        mEditer.putLong(key, value);
        mEditer.commit();
    }

    /**
     * 保存指定key的String类型值
     *
     * @param key
     *            待保存值的主键
     * @param value
     *            待保存的String值
     */
    public static void saveString(Context context, String key, String value) {
        if (mEditer == null) {
            mEditer = getSharedPreferences(context).edit();
        }
        mEditer.putString(key, value);
        mEditer.commit();
    }

    /**
     * 保存指定key的String类型值的集合
     *
     * @param string
     *            待保存值的数据集合
     */
    public static void saveStrings(Context context, Map<String, String> strings) {
        if (strings == null || strings.isEmpty()) {
            return;
        }
        if (mEditer == null) {
            mEditer = getSharedPreferences(context).edit();
        }
        Set<Entry<String, String>> datas = strings.entrySet();
        for (Entry<String, String> entry : datas) {
            mEditer.putString(entry.getKey(), entry.getValue());
        }
        mEditer.commit();
    }

}
