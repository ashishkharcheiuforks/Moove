package com.backdoor.moove.core.helper

import android.content.Context
import android.content.SharedPreferences

/**
 * Helper class for working with SharedPreferences.
 */
class SharedPrefs private constructor(context: Context) {

    private val mPrefs: SharedPreferences
    private val mChangesPrefs: SharedPreferences

    init {
        this.mPrefs = context.getSharedPreferences(MOOVE_PREFS, Context.MODE_PRIVATE)
        this.mChangesPrefs = context.getSharedPreferences(CHANGES_PREFS, Context.MODE_PRIVATE)
    }

    /**
     * Save String preference.
     *
     * @param stringToSave key.
     * @param value        value.
     */
    fun savePrefs(stringToSave: String, value: String) {
        mPrefs.edit().putString(stringToSave, value).apply()
    }

    /**
     * Save Integer preference.
     *
     * @param stringToSave key.
     * @param value        value.
     */
    fun saveInt(stringToSave: String, value: Int) {
        mPrefs.edit().putInt(stringToSave, value).apply()
    }

    /**
     * Get Integer preference.
     *
     * @param stringToLoad key.
     * @return
     */
    fun loadInt(stringToLoad: String): Int {
        try {
            return mPrefs.getInt(stringToLoad, 0)
        } catch (e: ClassCastException) {
            return Integer.parseInt(mPrefs.getString(stringToLoad, "0")!!)
        }

    }

    /**
     * Save Long preference.
     *
     * @param stringToSave key.
     * @param value        value.
     */
    fun saveLong(stringToSave: String, value: Long) {
        mPrefs.edit().putLong(stringToSave, value).apply()
    }

    /**
     * Get Long preference.
     *
     * @param stringToLoad key.
     * @return
     */
    fun loadLong(stringToLoad: String): Long {
        try {
            return mPrefs.getLong(stringToLoad, 1000)
        } catch (e: ClassCastException) {
            return java.lang.Long.parseLong(mPrefs.getString(stringToLoad, "1000")!!)
        }

    }

    /**
     * Get String preference.
     *
     * @param stringToLoad key.
     * @return
     */
    fun loadPrefs(stringToLoad: String): String? {
        try {
            return mPrefs.getString(stringToLoad, "")
        } catch (e: NullPointerException) {
            return ""
        }

    }

    /**
     * Check if preference exist.
     *
     * @param checkString key.
     * @return
     */
    fun isString(checkString: String): Boolean {
        return mPrefs.contains(checkString)
    }

    /**
     * Save Boolean preference.
     *
     * @param stringToSave key.
     * @param value        value.
     */
    fun saveBoolean(stringToSave: String, value: Boolean) {
        mPrefs.edit().putBoolean(stringToSave, value).apply()
    }

    /**
     * Get Boolean preference.
     *
     * @param stringToLoad key.
     * @return
     */
    fun loadBoolean(stringToLoad: String): Boolean {
        try {
            return mPrefs.getBoolean(stringToLoad, false)
        } catch (e: ClassCastException) {
            return java.lang.Boolean.parseBoolean(mPrefs.getString(stringToLoad, "false"))
        }

    }

    fun saveVersionBoolean(stringToSave: String) {
        mChangesPrefs.edit().putBoolean(stringToSave, true).apply()
    }

    fun loadVersionBoolean(stringToLoad: String): Boolean {
        try {
            return mChangesPrefs.getBoolean(stringToLoad, false)
        } catch (e: ClassCastException) {
            return java.lang.Boolean.parseBoolean(mChangesPrefs.getString(stringToLoad, "false"))
        }

    }

    companion object {

        val MOOVE_PREFS = "moove_prefs"
        private val CHANGES_PREFS = "changes_settings"
        private var instance: SharedPrefs? = null

        fun getInstance(context: Context?): SharedPrefs? {
            if (instance == null && context != null) {
                synchronized(SharedPrefs::class.java) {
                    if (instance == null) {
                        instance = SharedPrefs(context.applicationContext)
                    }
                }
            }
            return instance
        }
    }
}