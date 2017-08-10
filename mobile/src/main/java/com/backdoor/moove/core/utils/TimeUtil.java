package com.backdoor.moove.core.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Some utils for time counting.
 */
public class TimeUtil {

    /**
     * Millisecond constants.
     */
    public final static long minute = 60 * 1000;
    public final static long hour = minute * 60;
    public final static long halfDay = hour * 12;
    public final static long day = halfDay * 2;

    public static final SimpleDateFormat fullDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());
    public static final SimpleDateFormat fullDateTime24 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm", Locale.getDefault());
    public static final SimpleDateFormat fullDateTime12 = new SimpleDateFormat("EEE, dd MMM yyyy K:mm a", Locale.getDefault());
    public static final SimpleDateFormat time24 = new SimpleDateFormat("HH:mm", Locale.getDefault());
    public static final SimpleDateFormat time12 = new SimpleDateFormat("K:mm a", Locale.getDefault());

    public TimeUtil() {
    }

    /**
     * Get date and time string from date.
     *
     * @param date date to convert.
     * @param is24 24H time format flag.
     * @return Date string
     */
    public static String getFullDateTime(long date, boolean is24) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        if (is24) return fullDateTime24.format(calendar.getTime());
        else return fullDateTime12.format(calendar.getTime());
    }

    /**
     * Get date and time string from date.
     *
     * @param date date to convert.
     * @return Date string
     */
    public static String getDate(Date date) {
        return fullDateFormat.format(date);
    }

    /**
     * Get time from date object.
     *
     * @param date date to convert.
     * @param is24 24H time format flag.
     * @return Time string
     */
    public static String getTime(Date date, boolean is24) {
        if (is24) return time24.format(date);
        else return time12.format(date);
    }

    public static boolean isCurrent(long time) {
        boolean res = false;
        Calendar cc = Calendar.getInstance();
        cc.setTimeInMillis(System.currentTimeMillis());
        long currentTime = cc.getTimeInMillis();
        if (time < currentTime) {
            res = true;
        }
        return res;
    }
}
