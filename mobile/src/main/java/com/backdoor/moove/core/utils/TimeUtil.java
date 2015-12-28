package com.backdoor.moove.core.utils;

import java.text.ParseException;
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
    public final static long hour = minute  * 60;
    public final static long halfDay = hour * 12;
    public final static long day = halfDay * 2;

    public static final SimpleDateFormat format24 = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
    public static final SimpleDateFormat format12 = new SimpleDateFormat("dd MMM yyyy, K:mm a", Locale.getDefault());
    public static final SimpleDateFormat fullDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());
    public static final SimpleDateFormat fullDateTime24 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm", Locale.getDefault());
    public static final SimpleDateFormat fullDateTime12 = new SimpleDateFormat("EEE, dd MMM yyyy K:mm a", Locale.getDefault());
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    public static final SimpleDateFormat time24 = new SimpleDateFormat("HH:mm", Locale.getDefault());
    public static final SimpleDateFormat time12 = new SimpleDateFormat("K:mm a", Locale.getDefault());
    public static final SimpleDateFormat simpleDate = new SimpleDateFormat("d MMMM", Locale.getDefault());

    public TimeUtil(){}

    /**
     * Get date and time string from date.
     * @param date date to convert.
     * @param is24 24H time format flag.
     * @return Date string
     */
    public static String getFullDateTime(long date, boolean is24){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        if (is24) return fullDateTime24.format(calendar.getTime());
        else return fullDateTime12.format(calendar.getTime());
    }

    /**
     * Get date and time string from date.
     * @param date date to convert.
     * @return Date string
     */
    public static String getSimpleDate(long date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        return simpleDate.format(calendar.getTime());
    }

    /**
     * Get date and time string from date.
     * @param date date to convert.
     * @return Date string
     */
    public static String getDate(Date date){
        return fullDateFormat.format(date);
    }

    /**
     * Get time from string.
     * @param date date string.
     * @return Date
     */
    public static Date getDate(String date){
        try {
            return time24.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get user age from birth date string.
     * @param dateOfBirth date of birth.
     * @return Integer
     */
    public static int getYears(String dateOfBirth){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        int years;
        Date date = null;
        try {
            date = format.parse(dateOfBirth);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        if (date != null) calendar.setTime(date);
        int yearOfBirth = calendar.get(Calendar.YEAR);

        Calendar calendar1 = Calendar.getInstance();
        calendar1.getTimeInMillis();
        int currentYear = calendar1.get(Calendar.YEAR);
        years = currentYear - yearOfBirth;
        return years;
    }

    /**
     * Get date and time string from date.
     * @param date date to convert.
     * @param is24 24H time format flag.
     * @return Date string
     */
    public static String getDateTime(Date date, boolean is24){
        if (is24) return format24.format(date);
        else return format12.format(date);
    }

    /**
     * Get time from date object.
     * @param date date to convert.
     * @param is24 24H time format flag.
     * @return Time string
     */
    public static String getTime(Date date, boolean is24){
        if (is24) return time24.format(date);
        else return time12.format(date);
    }

    /**
     * Get age from year of birth.
     * @param year year.
     * @return Integer
     */
    public static int getAge(int year){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int mYear = calendar.get(Calendar.YEAR);
        return mYear - year;
    }

    /**
     * Get Date object.
     * @param year year.
     * @param month month.
     * @param day day.
     * @return Date
     */
    public static Date getDate(int year, int month, int day) {
        Calendar cal1 = Calendar.getInstance();
        cal1.set(Calendar.YEAR, year);
        cal1.set(Calendar.MONTH, month);
        cal1.set(Calendar.DAY_OF_MONTH, day);
        cal1.set(Calendar.HOUR_OF_DAY, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);
        cal1.set(Calendar.MILLISECOND, 0);
        return cal1.getTime();
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
