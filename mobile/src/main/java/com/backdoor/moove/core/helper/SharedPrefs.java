package com.backdoor.moove.core.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

/**
 * Helper class for working with SharedPreferences.
 */
public class SharedPrefs {
    private SharedPreferences prefs;
    private Context mContext;
    public static final String APP_UI_PREFERENCES = "ui_settings";
    public static final String APP_CHANGES_PREFERENCES = "changes_settings";
    private static int MODE = Context.MODE_PRIVATE;
    public SharedPrefs(Context context){
        this.mContext = context;
    }

    /**
     * Save String preference.
     * @param stringToSave key.
     * @param value value.
     */
    public void savePrefs(String stringToSave, String value){
        prefs = mContext.getSharedPreferences(APP_UI_PREFERENCES, MODE);
        SharedPreferences.Editor uiEd = prefs.edit();
        uiEd.putString(stringToSave, value);
        uiEd.commit();
    }

    /**
     * Save Integer preference.
     * @param stringToSave key.
     * @param value value.
     */
    public void saveInt(String stringToSave, int value){
        prefs = mContext.getSharedPreferences(APP_UI_PREFERENCES, MODE);
        SharedPreferences.Editor uiEd = prefs.edit();
        uiEd.putInt(stringToSave, value);
        uiEd.commit();
    }

    /**
     * Get Integer preference.
     * @param stringToLoad key.
     * @return
     */
    public int loadInt(String stringToLoad){
        prefs = mContext.getSharedPreferences(APP_UI_PREFERENCES, MODE);
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
     * @param stringToSave key.
     * @param value value.
     */
    public void saveLong(String stringToSave, long value){
        prefs = mContext.getSharedPreferences(APP_UI_PREFERENCES, MODE);
        SharedPreferences.Editor uiEd = prefs.edit();
        uiEd.putLong(stringToSave, value);
        uiEd.commit();
    }

    /**
     * Get Long preference.
     * @param stringToLoad key.
     * @return
     */
    public long loadLong(String stringToLoad){
        prefs = mContext.getSharedPreferences(APP_UI_PREFERENCES, MODE);
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
     * @param stringToLoad key.
     * @return
     */
    public String loadPrefs(String stringToLoad){
        String res;
        try {
            prefs = mContext.getSharedPreferences(APP_UI_PREFERENCES, MODE);
            res = prefs.getString(stringToLoad, "");
        } catch (NullPointerException e) {
            e.printStackTrace();
            res = "";
        }
        return res;
    }

    /**
     * Check if preference exist.
     * @param checkString key.
     * @return
     */
    public boolean isString(String checkString){
        prefs = mContext.getSharedPreferences(APP_UI_PREFERENCES, MODE);
        return prefs.contains(checkString);
    }

    /**
     * Save Boolean preference.
     * @param stringToSave key.
     * @param value value.
     */
    public void saveBoolean(String stringToSave, boolean value){
        prefs = mContext.getSharedPreferences(APP_UI_PREFERENCES, MODE);
        SharedPreferences.Editor uiEd = prefs.edit();
        uiEd.putBoolean(stringToSave, value);
        uiEd.commit();
    }

    /**
     * Get Boolean preference.
     * @param stringToLoad key.
     * @return
     */
    public boolean loadBoolean(String stringToLoad){
        prefs = mContext.getSharedPreferences(APP_UI_PREFERENCES, MODE);
        boolean res;
        try {
            res = prefs.getBoolean(stringToLoad, false);
        } catch (ClassCastException e){
            res = Boolean.parseBoolean(prefs.getString(stringToLoad, "false"));
        }
        return res;
    }

    public void saveVersionBoolean(String stringToSave){
        prefs = mContext.getSharedPreferences(APP_CHANGES_PREFERENCES, MODE);
        SharedPreferences.Editor uiEd = prefs.edit();
        uiEd.putBoolean(stringToSave, true);
        uiEd.commit();
    }

    public boolean loadVersionBoolean(String stringToLoad){
        prefs = mContext.getSharedPreferences(APP_CHANGES_PREFERENCES, MODE);
        boolean res;
        try {
            res = prefs.getBoolean(stringToLoad, false);
        } catch (ClassCastException e){
            res = Boolean.parseBoolean(prefs.getString(stringToLoad, "false"));
        }
        return res;
    }
}