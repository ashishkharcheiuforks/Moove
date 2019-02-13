package com.backdoor.moove.utils

import android.content.Context
import com.backdoor.moove.core.consts.Constants
import com.backdoor.moove.core.consts.Language
import com.google.android.gms.maps.GoogleMap
import java.io.File

/**
 * Copyright 2019 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
open class Prefs(context: Context) {

    companion object {

        private const val PREFS_NAME = "moove_prefs"
        val MAP_TYPE = "map_type"
        val LOCATION_RADIUS = "radius"
        val TRACKING_NOTIFICATION = "tracking_notification"
        val VIBRATION_STATUS = "vibration_status"
        val SILENT_SOUND = "sound_status"
        val WAKE_STATUS = "wake_status"
        val INFINITE_SOUND = "infinite_sound"
        val SILENT_SMS = "silent_sms"
        val SILENT_CALL = "silent_call"
        val LED_STATUS = "led_status"
        val LED_COLOR = "led_color"
        val MARKER_STYLE = "marker_style"
        val INFINITE_VIBRATION = "infinite_vibration"
        val WEAR_NOTIFICATION = "wear_notification"
        val TRACK_TIME = "tracking_time"
        val TRACK_DISTANCE = "tracking_distance"
        val UNLOCK_DEVICE = "unlock_device"
        val VOLUME = "reminder_volume"
        val TTS = "tts_enabled"
        val TTS_LOCALE = "tts_locale"
        val CUSTOM_SOUND = "custom_sound"
        val CUSTOM_SOUND_FILE = "sound_file"
        val REMINDER_IMAGE = "reminder_image"
        val REMINDER_IMAGE_BLUR = "reminder_image_blur"
        val IS_24_TIME_FORMAT = "24_hour"
        val LAST_USED_REMINDER = "last_reminder"
        val RATE_SHOW = "rate_shown"
        val APP_RUNS_COUNT = "app_runs"
        val FIRST_LOAD = "first_loading"
        val WEAR_SERVICE = "wear_service"
        val PLACES_AUTO = "places_auto"
        val INCREASE_LOUDNESS = "increase_loudness"
    }

    private var prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isIncreasingLoudnessEnabled: Boolean
        get() = getBoolean(INCREASE_LOUDNESS)
        set(value) {
            putBoolean(INCREASE_LOUDNESS, value)
        }

    private fun getStringList(key: String): Set<String> {
        return prefs.getStringSet(key, setOf())
    }

    private fun putStringList(key: String, set: Set<String>) {
        prefs.edit().putStringSet(key, set).apply()
    }

    private fun putString(stringToSave: String, value: String) {
        prefs.edit().putString(stringToSave, value).apply()
    }

    private fun putInt(stringToSave: String, value: Int) {
        prefs.edit().putInt(stringToSave, value).apply()
    }

    private fun getInt(stringToLoad: String, def: Int): Int {
        return try {
            prefs.getInt(stringToLoad, def)
        } catch (e: ClassCastException) {
            try {
                Integer.parseInt(prefs.getString(stringToLoad, "$def"))
            } catch (e1: ClassCastException) {
                0
            }
        }
    }

    private fun putLong(stringToSave: String, value: Long) {
        prefs.edit().putLong(stringToSave, value).apply()
    }

    private fun getLong(stringToLoad: String): Long {
        return try {
            prefs.getLong(stringToLoad, 1000)
        } catch (e: ClassCastException) {
            java.lang.Long.parseLong(prefs.getString(stringToLoad, "1000"))
        }
    }

    private fun getString(stringToLoad: String, def: String = ""): String {
        try {
            return prefs.getString(stringToLoad, def)
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
        return def
    }

    private fun hasKey(checkString: String): Boolean = prefs.contains(checkString)

    private fun putBoolean(stringToSave: String, value: Boolean) {
        prefs.edit().putBoolean(stringToSave, value).apply()
    }

    private fun getBoolean(stringToLoad: String): Boolean {
        return try {
            prefs.getBoolean(stringToLoad, false)
        } catch (e: ClassCastException) {
            java.lang.Boolean.parseBoolean(prefs.getString(stringToLoad, "false"))
        }
    }

    fun saveVersionBoolean(stringToSave: String) {
        prefs.edit().putBoolean(stringToSave, true).apply()
    }

    fun loadVersionBoolean(stringToLoad: String): Boolean {
        return try {
            prefs.getBoolean(stringToLoad, false)
        } catch (e: ClassCastException) {
            java.lang.Boolean.parseBoolean(prefs.getString(stringToLoad, "false"))
        }

    }

    fun initPrefs(context: Context) {
        val settingsUI = File("/data/data/" + context.packageName +
                "/shared_prefs/" + SharedPrefs.MOOVE_PREFS + ".xml")
        if (!settingsUI.exists()) {
            val editor = prefs.edit()
            editor.putInt(MAP_TYPE, GoogleMap.MAP_TYPE_NORMAL)
            editor.putString(REMINDER_IMAGE, Constants.DEFAULT)
            editor.putInt(LED_COLOR, 4)
            editor.putInt(LOCATION_RADIUS, 25)
            editor.putInt(TRACK_DISTANCE, 1)
            editor.putInt(TRACK_TIME, 1)
            editor.putInt(VOLUME, 25)
            editor.putInt(MARKER_STYLE, 11)

            editor.putString(TTS_LOCALE, Language.ENGLISH)

            editor.putInt(APP_RUNS_COUNT, 0)
            editor.putBoolean(TRACKING_NOTIFICATION, true)
            editor.putBoolean(RATE_SHOW, false)
            editor.putBoolean(INFINITE_VIBRATION, false)
            editor.putBoolean(IS_24_TIME_FORMAT, true)
            editor.putBoolean(UNLOCK_DEVICE, false)
            editor.putBoolean(TTS, false)
            editor.putBoolean(REMINDER_IMAGE_BLUR, true)
            editor.putBoolean(CUSTOM_SOUND, false)
            editor.putBoolean(PLACES_AUTO, true)
            editor.apply()
        }
    }

    fun checkPrefs() {
        val sPrefs = prefs
        if (!sPrefs.contains(Prefs.TTS_LOCALE)) {
            sPrefs.savePrefs(Prefs.TTS_LOCALE, Language.ENGLISH)
        }
        if (!sPrefs.contains(Prefs.REMINDER_IMAGE)) {
            sPrefs.savePrefs(Prefs.REMINDER_IMAGE, Constants.DEFAULT)
        }
        if (!sPrefs.contains(Prefs.TRACK_DISTANCE)) {
            sPrefs.saveInt(Prefs.TRACK_DISTANCE, 1)
        }
        if (!sPrefs.contains(Prefs.TRACK_TIME)) {
            sPrefs.saveInt(Prefs.TRACK_TIME, 1)
        }
        if (!sPrefs.contains(Prefs.APP_RUNS_COUNT)) {
            sPrefs.saveInt(Prefs.APP_RUNS_COUNT, 0)
        }
        if (!sPrefs.contains(Prefs.VOLUME)) {
            sPrefs.saveInt(Prefs.VOLUME, 25)
        }
        if (!sPrefs.contains(Prefs.RATE_SHOW)) {
            sPrefs.saveBoolean(Prefs.RATE_SHOW, false)
        }
        if (!sPrefs.contains(Prefs.REMINDER_IMAGE_BLUR)) {
            sPrefs.saveBoolean(Prefs.REMINDER_IMAGE_BLUR, false)
        }
        if (!sPrefs.contains(Prefs.TTS)) {
            sPrefs.saveBoolean(Prefs.TTS, false)
        }
        if (!sPrefs.contains(Prefs.SILENT_SMS)) {
            sPrefs.saveBoolean(Prefs.SILENT_SMS, false)
        }
        if (!sPrefs.contains(Prefs.WEAR_NOTIFICATION)) {
            sPrefs.saveBoolean(Prefs.WEAR_NOTIFICATION, false)
        }
        if (!sPrefs.contains(Prefs.PLACES_AUTO)) {
            sPrefs.saveBoolean(Prefs.PLACES_AUTO, true)
        }
        if (!sPrefs.contains(Prefs.INFINITE_VIBRATION)) {
            sPrefs.saveBoolean(Prefs.INFINITE_VIBRATION, false)
        }
        if (!sPrefs.contains(Prefs.IS_24_TIME_FORMAT)) {
            sPrefs.saveBoolean(Prefs.IS_24_TIME_FORMAT, true)
        }
        if (!sPrefs.contains(Prefs.UNLOCK_DEVICE)) {
            sPrefs.saveBoolean(Prefs.UNLOCK_DEVICE, false)
        }

        if (!sPrefs.contains(Prefs.LED_STATUS)) {
            sPrefs.saveBoolean(Prefs.LED_STATUS, false)
        }
        if (!sPrefs.contains(Prefs.LED_COLOR)) {
            sPrefs.saveInt(Prefs.LED_COLOR, 4)
        }
    }
}