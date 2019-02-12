package com.backdoor.moove.core.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Some utils for time counting.
 */
class TimeUtil {
    companion object {

        /**
         * Millisecond constants.
         */
        val minute = (60 * 1000).toLong()
        val hour = minute * 60
        val halfDay = hour * 12
        val day = halfDay * 2

        val fullDateFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
        val fullDateTime24 = SimpleDateFormat("EEE, dd MMM yyyy HH:mm", Locale.getDefault())
        val fullDateTime12 = SimpleDateFormat("EEE, dd MMM yyyy K:mm a", Locale.getDefault())
        val time24 = SimpleDateFormat("HH:mm", Locale.getDefault())
        val time12 = SimpleDateFormat("K:mm a", Locale.getDefault())

        /**
         * Get date and time string from date.
         *
         * @param date date to convert.
         * @param is24 24H time format flag.
         * @return Date string
         */
        fun getFullDateTime(date: Long, is24: Boolean): String {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = date
            return if (is24)
                fullDateTime24.format(calendar.time)
            else
                fullDateTime12.format(calendar.time)
        }

        /**
         * Get date and time string from date.
         *
         * @param date date to convert.
         * @return Date string
         */
        fun getDate(date: Date): String {
            return fullDateFormat.format(date)
        }

        /**
         * Get time from date object.
         *
         * @param date date to convert.
         * @param is24 24H time format flag.
         * @return Time string
         */
        fun getTime(date: Date, is24: Boolean): String {
            return if (is24)
                time24.format(date)
            else
                time12.format(date)
        }

        fun isCurrent(time: Long): Boolean {
            var res = false
            val cc = Calendar.getInstance()
            cc.timeInMillis = System.currentTimeMillis()
            val currentTime = cc.timeInMillis
            if (time < currentTime) {
                res = true
            }
            return res
        }
    }
}
