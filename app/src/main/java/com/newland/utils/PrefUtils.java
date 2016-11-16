package com.newland.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * 保存配置
 */
public class PrefUtils {

    private static PrefUtils instance;
    private SharedPreferences sp;

    private PrefUtils(Context context) {
        if (sp == null) {
            sp = PreferenceManager.getDefaultSharedPreferences(context);
        }
    }

    public static PrefUtils getInstance(Context context) {
        if (instance == null) {
            instance = new PrefUtils(context);
        }
        return instance;
    }

    public boolean putBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(key, value);
        return editor.commit();
    }

    public boolean getBoolean(String key) {
        return sp.getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defValue) {
        return sp.getBoolean(key, defValue);
    }

    public boolean putLong(String key, long value) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(key, value);
        return editor.commit();
    }

    public long getLong(String key) {
        return sp.getLong(key, 0);
    }

    public boolean putString(String key, String value) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        return editor.commit();
    }

    public String getString(String key) {
        return sp.getString(key, null);
    }
}
