package com.zhilai.driver.config;

import android.content.Context;
import android.content.SharedPreferences;

public class SPHelper {
    private static SharedPreferences sp;

    public SPHelper(Context context, String name) {
        sp = context.getApplicationContext().getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public void put(String key, Object value) {

        SharedPreferences.Editor editor = sp.edit();

        if (value instanceof Boolean)
            editor.putBoolean(key, (Boolean) value);
        else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        } else if (value instanceof Long) {
            editor.putFloat(key, (Long) value);
        }

        editor.apply();
    }

    public Object get(String key, Object defValue) {

        Object object = null;

        if (defValue instanceof Boolean)
            object = sp.getBoolean(key, (Boolean) defValue);
        else if (defValue instanceof Integer) {
            object = sp.getInt(key, (Integer) defValue);
        } else if (defValue instanceof String) {
            object = sp.getString(key, (String) defValue);
        } else if (defValue instanceof Float) {
            object = sp.getFloat(key, (Float) defValue);
        } else if (defValue instanceof Long) {
            object = sp.getLong(key, (Long) defValue);
        }

        return object;
    }
}
