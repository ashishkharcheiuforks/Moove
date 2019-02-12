package com.backdoor.moove.core.consts

import android.content.Context

import com.backdoor.moove.core.helper.SharedPrefs

import java.util.Locale

class Language {

    /**
     * Get locale for tts.
     *
     * @param context application context.
     * @return Locale
     */
    fun getLocale(context: Context): Locale? {
        var res: Locale? = null
        when (SharedPrefs.getInstance(context)!!.loadPrefs(Prefs.TTS_LOCALE)) {
            ENGLISH -> res = Locale.ENGLISH
            FRENCH -> res = Locale.FRENCH
            GERMAN -> res = Locale.GERMAN
            JAPANESE -> res = Locale.JAPANESE
            ITALIAN -> res = Locale.ITALIAN
            KOREAN -> res = Locale.KOREAN
            POLISH -> res = Locale("pl", "")
            RUSSIAN -> res = Locale("ru", "")
            SPANISH -> res = Locale("es", "")
        }
        return res
    }

    companion object {
        val ENGLISH = "en"
        val FRENCH = "fr"
        val GERMAN = "de"
        val ITALIAN = "it"
        val JAPANESE = "ja"
        val KOREAN = "ko"
        val POLISH = "pl"
        val RUSSIAN = "ru"
        val SPANISH = "es"
    }
}
