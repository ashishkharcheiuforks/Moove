package com.backdoor.moove.core.helper;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Helper class for working with SharedPreferences.
 */
public class SharedPrefs {
    private SharedPreferences prefs;
    private Context mContext;
    public static final String MOOVE_PREFS = "moove_prefs";
    public static final String CHANGES_PREFS = "changes_settings";
    private static int MODE = Context.MODE_PRIVATE;

    public SharedPrefs(Context context) {
        this.mContext = context;
    }

    /**
     * Save String preference.
     *
     * @param stringToSave key.
     * @param value        value.
     */
    public void savePrefs(String stringToSave, String value) {
        prefs = mContext.getSharedPreferences(MOOVE_PREFS, MODE);
        SharedPreferences.Editor uiEd = prefs.edit();
        uiEd.putString(stringToSave, value);
        uiEd.apply();
    }

    /**
     * Save Integer preference.
     *
     * @param stringToSave key.
     * @param value        value.
     */
    public void saveInt(String stringToSave, int value) {
        prefs = mContext.getSharedPreferences(MOOVE_PREFS, MODE);
        SharedPreferences.Editor uiEd = prefs.edit();
        uiEd.putInt(stringToSave, value);
        uiEd.apply();
    }

    /**
     * Get Integer preference.
     *
     * @param stringToLoad key.
     * @return
     */
    public int loadInt(String stringToLoad) {
        prefs = mContext.getSharedPreferences(MOOVE_PREFS, MODE);
        int x;
        try {
            x = prefs.getInt(stringToLoad, 0);
        } catch (ClassCastException e) {
            x = Integer.parseInt(prefs.getString(stringToLoad, "0"));
        }
        return x;
    }

    /**
     * Save Long preference.
     *
     * @param stringToSave key.
     * @param value        value.
     */
    public void saveLong(String stringToSave, long value) {
        prefs = mContext.getSharedPreferences(MOOVE_PREFS, MODE);
        SharedPreferences.Editor uiEd = prefs.edit();
        uiEd.putLong(stringToSave, value);
        uiEd.apply();
    }

    /**
     * Get Long preference.
     *
     * @param stringToLoad key.
     * @return
     */
    public long loadLong(String stringToLoad) {
        prefs = mContext.getSharedPreferences(MOOVE_PREFS, MODE);
        long x;
        try {
            x = prefs.getLong(stringToLoad, 1000);
        } catch (ClassCastException e) {
            x = Long.parseLong(prefs.getString(stringToLoad, "1000"));
        }
        return x;
    }

    /**
     * Get String preference.
     *
     * @param stringToLoad key.
     * @return
     */
    public String loadPrefs(String stringToLoad) {
        String res;
        try {
            prefs = mContext.getSharedPreferences(MOOVE_PREFS, MODE);
            res = prefs.getString(stringToLoad, "");
        } catch (NullPointerException e) {
            e.printStackTrace();
            res = "";
        }
        return res;
    }

    /**
     * Check if preference exist.
     *
     * @param checkString key.
     * @return
     */
    public boolean isString(String checkString) {
        prefs = mContext.getSharedPreferences(MOOVE_PREFS, MODE);
        return prefs.contains(checkString);
    }

    /**
     * Save Boolean preference.
     *
     * @param stringToSave key.
     * @param value        value.
     */
    public void saveBoolean(String stringToSave, boolean value) {
        prefs = mContext.getSharedPreferences(MOOVE_PREFS, MODE);
        SharedPreferences.Editor uiEd = prefs.edit();
        uiEd.putBoolean(stringToSave, value);
        uiEd.apply();
    }

    /**
     * Get Boolean preference.
     *
     * @param stringToLoad key.
     * @return
     */
    public boolean loadBoolean(String stringToLoad) {
        prefs = mContext.getSharedPreferences(MOOVE_PREFS, MODE);
        boolean res;
        try {
            res = prefs.getBoolean(stringToLoad, false);
        } catch (ClassCastException e) {
            res = Boolean.parseBoolean(prefs.getString(stringToLoad, "false"));
        }
        return res;
    }

    public void saveVersionBoolean(String stringToSave) {
        prefs = mContext.getSharedPreferences(CHANGES_PREFS, MODE);
        SharedPreferences.Editor uiEd = prefs.edit();
        uiEd.putBoolean(stringToSave, true);
        uiEd.apply();
    }

    public boolean loadVersionBoolean(String stringToLoad) {
        prefs = mContext.getSharedPreferences(CHANGES_PREFS, MODE);
        boolean res;
        try {
            res = prefs.getBoolean(stringToLoad, false);
        } catch (ClassCastException e) {
            res = Boolean.parseBoolean(prefs.getString(stringToLoad, "false"));
        }
        return res;
    }
}