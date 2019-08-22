package com.backdoor.moove.utils

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.text.TextUtils
import com.backdoor.moove.R
import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {

    const val SECOND: Long = 1000
    const val MINUTE: Long = 60 * SECOND
    const val HOUR: Long = MINUTE * 60
    private const val HALF_DAY: Long = HOUR * 12
    const val DAY: Long = HALF_DAY * 2

    fun isCurrent(eventTime: String?): Boolean {
        return getDateTimeFromGmt(eventTime) > System.currentTimeMillis()
    }

    fun isCurrent(millis: Long): Boolean {
        return millis > System.currentTimeMillis()
    }

    private const val GMT = "GMT"
    private val gmtFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)

    val fullDateFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
    val fullDateTime24 = SimpleDateFormat("EEE, dd MMM yyyy HH:mm", Locale.getDefault())
    val fullDateTime12 = SimpleDateFormat("EEE, dd MMM yyyy K:mm a", Locale.getDefault())
    val time24 = SimpleDateFormat("HH:mm", Locale.getDefault())
    val time12 = SimpleDateFormat("K:mm a", Locale.getDefault())

    fun day(): SimpleDateFormat = SimpleDateFormat("dd", Locale.getDefault())

    fun month(): SimpleDateFormat = SimpleDateFormat("MMM", Locale.getDefault())

    fun year(): SimpleDateFormat = SimpleDateFormat("yyyy", Locale.getDefault())

    fun getGmtFromDateTime(date: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        gmtFormat.timeZone = TimeZone.getTimeZone(GMT)
        return try {
            gmtFormat.format(calendar.time)
        } catch (e: Exception) {
            ""
        }
    }

    fun getPlaceDateTimeFromGmt(dateTime: String?, lang: Int = 0): DMY {
        var date: Date

        try {
            gmtFormat.timeZone = TimeZone.getTimeZone(GMT)
            date = gmtFormat.parse(dateTime ?: "") ?: Date()
        } catch (e: Exception) {
            date = Date()
        }

        var day = ""
        var month = ""
        var year = ""

        try {
            day = day().format(date)
            month = month().format(date)
            year = year().format(date)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return DMY(day, month, year)
    }

    fun isSameDay(gmt: String?): Boolean {
        val gmt2 = gmtDateTime
        if (TextUtils.isEmpty(gmt) && TextUtils.isEmpty(gmt2)) return true
        else if (TextUtils.isEmpty(gmt) || TextUtils.isEmpty(gmt2)) return false
        return (gmtFormat.parse(gmt ?: "") ?: Date()).toCalendar().sameDayAs((gmtFormat.parse(gmt2) ?: Date()).toCalendar())
    }

    val gmtDateTime: String
        get() {
            gmtFormat.timeZone = TimeZone.getTimeZone(GMT)
            return try {
                gmtFormat.format(Date())
            } catch (e: Exception) {
                ""
            }
        }

    fun getDateTimeFromGmt(dateTime: String?): Long {
        if (TextUtils.isEmpty(dateTime)) {
            return 0
        }
        val calendar = Calendar.getInstance()
        try {
            gmtFormat.timeZone = TimeZone.getTimeZone(GMT)
            val date = gmtFormat.parse(dateTime ?: "")
            calendar.time = date ?: Date()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return calendar.timeInMillis
    }

    fun getFullDateTime(date: String?, is24: Boolean): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = getDateTimeFromGmt(date)
        return if (is24)
            fullDateTime24.format(calendar.time)
        else
            fullDateTime12.format(calendar.time)
    }

    fun getFullDateTime(date: Long, is24: Boolean): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        return if (is24)
            fullDateTime24.format(calendar.time)
        else
            fullDateTime12.format(calendar.time)
    }

    fun getDate(date: Date): String {
        return fullDateFormat.format(date)
    }

    fun getTime(date: Date, is24: Boolean): String {
        return if (is24)
            time24.format(date)
        else
            time12.format(date)
    }

    fun showTimePicker(context: Context, is24: Boolean,
                       hour: Int, minute: Int, listener: TimePickerDialog.OnTimeSetListener): TimePickerDialog {
        val dialog = TimePickerDialog(context, R.style.HomeDarkDialog, listener, hour, minute, is24)
        dialog.show()
        return dialog
    }

    fun showDatePicker(context: Context, year: Int, month: Int, dayOfMonth: Int, listener: DatePickerDialog.OnDateSetListener): DatePickerDialog {
        val dialog = DatePickerDialog(context, R.style.HomeDarkDialog, listener, year, month, dayOfMonth)
        dialog.show()
        return dialog
    }

    data class DMY(val day: String, val month: String, val year: String)

    data class HM(val hour: Int, val minute: Int)
}

