package com.example.omarf.weather;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by omarf on 1/26/2017.
 */

public class KeyPreferences {
    private static final String KEY_TAG = "key";

    public static String getKey(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_TAG,   "1bdb6580e4c12e06");
    }

    public static void setKey(Context context, String key) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(KEY_TAG, key)
                .apply();

    }

}
