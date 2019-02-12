package com.backdoor.moove.core.helper;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.Nullable;

/**
 * Helper class for working with SharedPreferences.
 */
public class SharedPrefs {

    public static final String MOOVE_PREFS = "moove_prefs";
    private static final String CHANGES_PREFS = "changes_settings";

    private SharedPreferences mPrefs;
    private SharedPreferences mChangesPrefs;
    @Nullable
    private static SharedPrefs instance;

    private SharedPrefs(Context context) {
        this.mPrefs = context.getSharedPreferences(MOOVE_PREFS, Context.MODE_PRIVATE);
        this.mChangesPrefs = context.getSharedPreferences(CHANGES_PREFS, Context.MODE_PRIVATE);
    }

    @Nullable
    public static SharedPrefs getInstance(@Nullable Context context) {
        if (instance == null && context != null) {
            synchronized (SharedPrefs.class) {
                if (instance == null) {
                    instance = new SharedPrefs(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    /**
     * Save String preference.
     *
     * @param stringToSave key.
     * @param value        value.
     */
    public void savePrefs(String stringToSave, String value) {
        mPrefs.edit().putString(stringToSave, value).apply();
    }

    /**
     * Save Integer preference.
     *
     * @param stringToSave key.
     * @param value        value.
     */
    public void saveInt(String stringToSave, int value) {
        mPrefs.edit().putInt(stringToSave, value).apply();
    }

    /**
     * Get Integer preference.
     *
     * @param stringToLoad key.
     * @return
     */
    public int loadInt(String stringToLoad) {
        try {
            return mPrefs.getInt(stringToLoad, 0);
        } catch (ClassCastException e) {
            return Integer.parseInt(mPrefs.getString(stringToLoad, "0"));
        }
    }

    /**
     * Save Long preference.
     *
     * @param stringToSave key.
     * @param value        value.
     */
    public void saveLong(String stringToSave, long value) {
        mPrefs.edit().putLong(stringToSave, value).apply();
    }

    /**
     * Get Long preference.
     *
     * @param stringToLoad key.
     * @return
     */
    public long loadLong(String stringToLoad) {
        try {
            return mPrefs.getLong(stringToLoad, 1000);
        } catch (ClassCastException e) {
            return Long.parseLong(mPrefs.getString(stringToLoad, "1000"));
        }
    }

    /**
     * Get String preference.
     *
     * @param stringToLoad key.
     * @return
     */
    public String loadPrefs(String stringToLoad) {
        try {
            return mPrefs.getString(stringToLoad, "");
        } catch (NullPointerException e) {
            return "";
        }
    }

    /**
     * Check if preference exist.
     *
     * @param checkString key.
     * @return
     */
    public boolean isString(String checkString) {
        return mPrefs.contains(checkString);
    }

    /**
     * Save Boolean preference.
     *
     * @param stringToSave key.
     * @param value        value.
     */
    public void saveBoolean(String stringToSave, boolean value) {
        mPrefs.edit().putBoolean(stringToSave, value).apply();
    }

    /**
     * Get Boolean preference.
     *
     * @param stringToLoad key.
     * @return
     */
    public boolean loadBoolean(String stringToLoad) {
        try {
            return mPrefs.getBoolean(stringToLoad, false);
        } catch (ClassCastException e) {
            return Boolean.parseBoolean(mPrefs.getString(stringToLoad, "false"));
        }
    }

    public void saveVersionBoolean(String stringToSave) {
        mChangesPrefs.edit().putBoolean(stringToSave, true).apply();
    }

    public boolean loadVersionBoolean(String stringToLoad) {
        try {
            return mChangesPrefs.getBoolean(stringToLoad, false);
        } catch (ClassCastException e) {
            return Boolean.parseBoolean(mChangesPrefs.getString(stringToLoad, "false"));
        }
    }
}