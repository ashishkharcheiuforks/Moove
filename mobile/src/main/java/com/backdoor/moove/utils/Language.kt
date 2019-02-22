package com.backdoor.moove.utils

import android.content.Context
import com.backdoor.moove.R
import java.util.*

class Language(private val prefs: Prefs){

    fun getLocale(isBirth: Boolean): Locale? {
        var res: Locale? = null
        val locale: String = prefs.ttsLocale
        when (locale) {
            ENGLISH -> res = Locale.ENGLISH
            FRENCH -> res = Locale.FRENCH
            GERMAN -> res = Locale.GERMAN
            JAPANESE -> res = Locale.JAPANESE
            ITALIAN -> res = Locale.ITALIAN
            KOREAN -> res = Locale.KOREAN
            POLISH -> res = Locale("pl", "")
            RUSSIAN -> res = Locale("ru", "")
            SPANISH -> res = Locale("es", "")
            UKRAINIAN -> res = Locale("uk", "")
            PORTUGUESE -> res = Locale("pt", "")
        }
        return res
    }

    fun getLocaleByPosition(position: Int): String {
        var locale = ENGLISH
        when (position) {
            0 -> locale = ENGLISH
            1 -> locale = FRENCH
            2 -> locale = GERMAN
            3 -> locale = ITALIAN
            4 -> locale = JAPANESE
            5 -> locale = KOREAN
            6 -> locale = POLISH
            7 -> locale = RUSSIAN
            8 -> locale = SPANISH
            9 -> locale = PORTUGUESE
            10 -> locale = UKRAINIAN
        }
        return locale
    }

    fun getLocalePosition(locale: String?): Int {
        if (locale == null) {
            return 0
        }
        var mItemSelect = 0
        when {
            locale.matches(ENGLISH.toRegex()) -> mItemSelect = 0
            locale.matches(FRENCH.toRegex()) -> mItemSelect = 1
            locale.matches(GERMAN.toRegex()) -> mItemSelect = 2
            locale.matches(ITALIAN.toRegex()) -> mItemSelect = 3
            locale.matches(JAPANESE.toRegex()) -> mItemSelect = 4
            locale.matches(KOREAN.toRegex()) -> mItemSelect = 5
            locale.matches(POLISH.toRegex()) -> mItemSelect = 6
            locale.matches(RUSSIAN.toRegex()) -> mItemSelect = 7
            locale.matches(SPANISH.toRegex()) -> mItemSelect = 8
            locale.matches(PORTUGUESE.toRegex()) -> mItemSelect = 9
            locale.matches(UKRAINIAN.toRegex()) -> mItemSelect = 10
        }
        return mItemSelect
    }

    fun getLocaleNames(context: Context): List<String> {
        val names = ArrayList<String>()
        names.add(context.getString(R.string.english))
        names.add(context.getString(R.string.french))
        names.add(context.getString(R.string.german))
        names.add(context.getString(R.string.italian))
        names.add(context.getString(R.string.japanese))
        names.add(context.getString(R.string.korean))
        names.add(context.getString(R.string.polish))
        names.add(context.getString(R.string.russian))
        names.add(context.getString(R.string.spanish))
        names.add(context.getString(R.string.portuguese))
        names.add(context.getString(R.string.ukrainian))
        return names
    }

    companion object {
        const val ENGLISH = "en"
        const val FRENCH = "fr"
        const val GERMAN = "de"
        const val ITALIAN = "it"
        const val JAPANESE = "ja"
        const val KOREAN = "ko"
        const val POLISH = "pl"
        const val RUSSIAN = "ru"
        const val SPANISH = "es"
        const val UKRAINIAN = "uk"
        const val PORTUGUESE = "pt"

        private const val EN = "en-US"
        private const val RU = "ru-RU"
        private const val UK = "uk-UA"
        private const val DE = "de-DE"
        private const val ES = "es-ES"
        private const val PT = "pt-PT"

        fun getScreenLanguage(code: Int): Locale {
            when (code) {
                0 -> return Locale.getDefault()
                1 -> return Locale.ENGLISH
                2 -> return Locale.GERMAN
                3 -> return Locale("es", "")
                4 -> return Locale.FRENCH
                5 -> return Locale.ITALIAN
                6 -> return Locale("pt", "")
                7 -> return Locale("pl", "")
                8 -> return Locale("cs", "")
                9 -> return Locale("ro", "")
                10 -> return Locale("tr", "")
                11 -> return Locale("uk", "")
                12 -> return Locale("ru", "")
                13 -> return Locale.JAPANESE
                14 -> return Locale.CHINESE
                15 -> return Locale("hi", "")
                else -> return Locale.getDefault()
            }
        }
    }
}
