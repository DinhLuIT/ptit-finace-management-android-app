package com.ptithcm.finacemanager.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class for date operations.
 */
public class DateUtils {

    /**
     * Get today's date in DB format (yyyy-MM-dd).
     */
    public static String getTodayDB() {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_DB, Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Get current datetime in DB format (yyyy-MM-dd HH:mm:ss).
     */
    public static String getNowDB() {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATETIME_FORMAT_DB, Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Format a DB date string to display format.
     * Example: "2026-05-10" → "10/05/2026"
     */
    public static String formatForDisplay(String dbDate) {
        try {
            SimpleDateFormat dbFormat = new SimpleDateFormat(Constants.DATE_FORMAT_DB, Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat(Constants.DATE_FORMAT_DISPLAY, Locale.getDefault());
            Date date = dbFormat.parse(dbDate);
            return date != null ? displayFormat.format(date) : dbDate;
        } catch (ParseException e) {
            return dbDate;
        }
    }

    /**
     * Format date to full display format.
     * Example: "2026-05-10" → "10 May 2026"
     */
    public static String formatFull(String dbDate) {
        try {
            SimpleDateFormat dbFormat = new SimpleDateFormat(Constants.DATE_FORMAT_DB, Locale.getDefault());
            SimpleDateFormat fullFormat = new SimpleDateFormat(Constants.DATE_FORMAT_FULL, Locale.getDefault());
            Date date = dbFormat.parse(dbDate);
            return date != null ? fullFormat.format(date) : dbDate;
        } catch (ParseException e) {
            return dbDate;
        }
    }

    /**
     * Get the current month (1-12).
     */
    public static int getCurrentMonth() {
        return Calendar.getInstance().get(Calendar.MONTH) + 1;
    }

    /**
     * Get the current year.
     */
    public static int getCurrentYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    /**
     * Get first day of current month in DB format.
     */
    public static String getFirstDayOfMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_DB, Locale.getDefault());
        return sdf.format(cal.getTime());
    }

    /**
     * Get last day of current month in DB format.
     */
    public static String getLastDayOfMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_DB, Locale.getDefault());
        return sdf.format(cal.getTime());
    }

    /**
     * Check if a date string is today.
     */
    public static boolean isToday(String dbDate) {
        return getTodayDB().equals(dbDate);
    }

    /**
     * Check if a date string is yesterday.
     */
    public static boolean isYesterday(String dbDate) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_DB, Locale.getDefault());
        return sdf.format(cal.getTime()).equals(dbDate);
    }

    private DateUtils() {
        // Prevent instantiation
    }
}
