package com.backdoor.moove.utils

import android.content.Context
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
        const val MAP_TYPE = "map_type"
        const val MAP_STYLE = "map_type"
        const val LOCATION_RADIUS = "radius"
        const val TRACKING_NOTIFICATION = "tracking_notification"
        const val VIBRATION_STATUS = "vibration_status"
        const val SILENT_SOUND = "sound_status"
        const val INFINITE_SOUND = "infinite_sound"
        const val SILENT_CALL = "silent_call"
        const val LED_STATUS = "led_status"
        const val LED_COLOR = "led_color"
        const val MARKER_STYLE = "marker_style"
        const val INFINITE_VIBRATION = "infinite_vibration"
        const val WEAR_NOTIFICATION = "wear_notification"
        const val TRACK_TIME = "tracking_time"
        const val UNLOCK_DEVICE = "unlock_device"
        const val VOLUME = "reminder_volume"
        const val TTS = "tts_enabled"
        const val TTS_LOCALE = "tts_locale"
        const val CUSTOM_SOUND = "melody_file"
        const val REMINDER_IMAGE = "reminder_image"
        const val REMINDER_IMAGE_BLUR = "reminder_image_blur"
        const val IS_24_TIME_FORMAT = "24_hour"
        const val RATE_SHOW = "rate_shown"
        const val APP_RUNS_COUNT = "app_runs"
        const val PLACES_AUTO = "places_auto"
        const val INCREASE_LOUDNESS = "increase_loudness"
        const val RADIUS = "radius"
    }

    private var prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isIncreasingLoudnessEnabled: Boolean
        get() = getBoolean(INCREASE_LOUDNESS)
        set(value) {
            putBoolean(INCREASE_LOUDNESS, value)
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

    private fun getString(stringToLoad: String, def: String = ""): String {
        try {
            return prefs.getString(stringToLoad, def) ?: ""
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

    var melody: String
        get() = getString(CUSTOM_SOUND, "")
        set(value) {
            putString(CUSTOM_SOUND, value)
        }

    var ttsLocale: String
        get() = getString(TTS_LOCALE, "")
        set(value) {
            putString(TTS_LOCALE, value)
        }

    var reminderImage: String
        get() = getString(REMINDER_IMAGE, "")
        set(value) {
            putString(REMINDER_IMAGE, value)
        }

    var radius: Int
        get() = getInt(RADIUS, 25)
        set(value) {
            putInt(RADIUS, value)
        }

    var markerStyle: Int
        get() = getInt(MARKER_STYLE, 0)
        set(value) {
            putInt(MARKER_STYLE, value)
        }

    var mapType: Int
        get() = getInt(MAP_TYPE, GoogleMap.MAP_TYPE_NORMAL)
        set(value) {
            putInt(MAP_TYPE, value)
        }

    var mapStyle: Int
        get() = getInt(MAP_STYLE, 0)
        set(value) {
            putInt(MAP_STYLE, value)
        }

    var trackTime: Int
        get() = getInt(TRACK_TIME, 1)
        set(value) {
            putInt(TRACK_TIME, value)
        }

    var appRuns: Int
        get() = getInt(APP_RUNS_COUNT, 0)
        set(value) {
            putInt(APP_RUNS_COUNT, value)
        }

    var loudness: Int
        get() = getInt(VOLUME, 25)
        set(value) {
            putInt(VOLUME, value)
        }

    var ledColor: Int
        get() = getInt(LED_COLOR, 4)
        set(value) {
            putInt(LED_COLOR, value)
        }

    var rateShowed: Boolean
        get() = getBoolean(RATE_SHOW)
        set(value) {
            putBoolean(RATE_SHOW, value)
        }

    var blurImage: Boolean
        get() = getBoolean(REMINDER_IMAGE_BLUR)
        set(value) {
            putBoolean(REMINDER_IMAGE_BLUR, value)
        }

    var ttsEnabled: Boolean
        get() = getBoolean(TTS)
        set(value) {
            putBoolean(TTS, value)
        }

    var wearNotification: Boolean
        get() = getBoolean(WEAR_NOTIFICATION)
        set(value) {
            putBoolean(WEAR_NOTIFICATION, value)
        }

    var autoPlace: Boolean
        get() = getBoolean(PLACES_AUTO)
        set(value) {
            putBoolean(PLACES_AUTO, value)
        }

    var infiniteVibration: Boolean
        get() = getBoolean(INFINITE_VIBRATION)
        set(value) {
            putBoolean(INFINITE_VIBRATION, value)
        }

    var use24Hour: Boolean
        get() = getBoolean(IS_24_TIME_FORMAT)
        set(value) {
            putBoolean(IS_24_TIME_FORMAT, value)
        }

    var unlockScreen: Boolean
        get() = getBoolean(UNLOCK_DEVICE)
        set(value) {
            putBoolean(UNLOCK_DEVICE, value)
        }

    var ledEnabled: Boolean
        get() = getBoolean(LED_STATUS)
        set(value) {
            putBoolean(LED_STATUS, value)
        }

    var isDistanceNotificationEnabled: Boolean
        get() = getBoolean(TRACKING_NOTIFICATION)
        set(value) {
            putBoolean(TRACKING_NOTIFICATION, value)
        }

    var silentCall: Boolean
        get() = getBoolean(SILENT_CALL)
        set(value) {
            putBoolean(SILENT_CALL, value)
        }

    var vibrate: Boolean
        get() = getBoolean(VIBRATION_STATUS)
        set(value) {
            putBoolean(VIBRATION_STATUS, value)
        }

    var soundInSilent: Boolean
        get() = getBoolean(SILENT_SOUND)
        set(value) {
            putBoolean(SILENT_SOUND, value)
        }

    var repeatMelody: Boolean
        get() = getBoolean(INFINITE_SOUND)
        set(value) {
            putBoolean(INFINITE_SOUND, value)
        }

    fun initPrefs(context: Context) {
        val settingsUI = File("/data/data/" + context.packageName +
                "/shared_prefs/" + PREFS_NAME + ".xml")
        if (!settingsUI.exists()) {
            val editor = prefs.edit()
            editor.putInt(MAP_TYPE, GoogleMap.MAP_TYPE_NORMAL)
            editor.putInt(MAP_STYLE, 0)
            editor.putString(REMINDER_IMAGE, Module.DEFAULT)
            editor.putInt(LED_COLOR, 4)
            editor.putInt(LOCATION_RADIUS, 25)
            editor.putInt(TRACK_TIME, 1)
            editor.putInt(VOLUME, 25)
            editor.putInt(MARKER_STYLE, 11)
            editor.putInt(APP_RUNS_COUNT, 0)
            editor.putBoolean(TRACKING_NOTIFICATION, true)
            editor.putBoolean(RATE_SHOW, false)
            editor.putBoolean(INFINITE_VIBRATION, false)
            editor.putBoolean(IS_24_TIME_FORMAT, true)
            editor.putBoolean(UNLOCK_DEVICE, false)
            editor.putBoolean(TTS, false)
            editor.putBoolean(REMINDER_IMAGE_BLUR, true)
            editor.putString(CUSTOM_SOUND, Module.DEFAULT)
            editor.putBoolean(PLACES_AUTO, true)
            editor.apply()
        }
    }

    fun checkPrefs() {
        if (!hasKey(REMINDER_IMAGE)) {
            reminderImage = Module.DEFAULT
        }
        if (!hasKey(Prefs.TRACK_TIME)) {
            trackTime = 1
        }
        if (!hasKey(Prefs.APP_RUNS_COUNT)) {
            appRuns = 0
        }
        if (!hasKey(Prefs.VOLUME)) {
            loudness = 25
        }
        if (!hasKey(RATE_SHOW)) {
            rateShowed = false
        }
        if (!hasKey(REMINDER_IMAGE_BLUR)) {
            blurImage = false
        }
        if (!hasKey(TTS)) {
            ttsEnabled = false
        }
        if (!hasKey(WEAR_NOTIFICATION)) {
            wearNotification = false
        }
        if (!hasKey(PLACES_AUTO)) {
            autoPlace = true
        }
        if (!hasKey(INFINITE_VIBRATION)) {
            infiniteVibration = false
        }
        if (!hasKey(IS_24_TIME_FORMAT)) {
            use24Hour = true
        }
        if (!hasKey(UNLOCK_DEVICE)) {
            unlockScreen = false
        }
        if (!hasKey(LED_STATUS)) {
            ledEnabled = false
        }
        if (!hasKey(LED_COLOR)) {
            ledColor = 4
        }
    }
}