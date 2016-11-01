package com.newland.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * 保存配置
 */
public class MyPref {

    private static MyPref instance;
    private SharedPreferences sp;

    private MyPref(Context context) {
        if (sp == null) {
            sp = PreferenceManager.getDefaultSharedPreferences(context);
        }
    }

    public static MyPref getInstance(Context context) {
        if (instance == null) {
            instance = new MyPref(context);
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

    public boolean putInt(String key, int value) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(key, value);
        return editor.commit();
    }

    public int getInt(String key) {
        return sp.getInt(key, 0);
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
