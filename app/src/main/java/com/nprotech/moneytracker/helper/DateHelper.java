package com.nprotech.moneytracker.helper;

import android.content.Context;
import android.text.format.DateFormat;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateHelper {

    public static Date getCurrentDateTime() {
        return Calendar.getInstance().getTime();
    }

    public static Date getInitialExportStartDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(11, 0);
        calendar.set(12, 0);
        calendar.set(13, 0);
        calendar.set(5, 1);
        return calendar.getTime();
    }

    public static Date getInitialExportEndDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(11, 0);
        calendar.set(12, 0);
        calendar.set(13, 0);
        calendar.set(5, 1);
        calendar.add(2, 1);
        calendar.add(5, -1);
        return calendar.getTime();
    }

    public static Date getGoalDateTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(11, 0);
        calendar.set(12, 0);
        calendar.set(13, 0);
        calendar.add(2, 1);
        return calendar.getTime();
    }

    public static String getCreditDate(int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(11, 0);
        calendar.set(12, 0);
        calendar.set(13, 0);
        calendar.set(14, 0);
        calendar.set(5, day);
        return new SimpleDateFormat(DateFormat.getBestDateTimePattern(Locale.getDefault(), "dd MMM yyyy"), Locale.getDefault()).format(calendar.getTime());
    }

    public static String getTransFormattedDate(Date date) {
        String bestDateTimePattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), "dd MMM");
        return new SimpleDateFormat("EEEE", Locale.getDefault()).format(date) + ", " + new SimpleDateFormat(bestDateTimePattern, Locale.getDefault()).format(date);
    }

    public static String getBackUpDate(long time, Context context) {
        Date date = new Date();
        date.setTime(time);
        String bestDateTimePattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), DateFormat.is24HourFormat(context) ? "kk:mm" : "hh:mm aa");
        return new SimpleDateFormat(DateFormat.getBestDateTimePattern(Locale.getDefault(), "yyyy MMM dd") + StringUtils.SPACE + bestDateTimePattern, Locale.getDefault()).format(date.getTime());
    }

    public static String getFormattedDateTime(Context context, Date date) {
        String formattedDate = getFormattedDate(date);
        String bestDateTimePattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), DateFormat.is24HourFormat(context) ? "kk:mm" : "hh:mm aa");
        return formattedDate + StringUtils.SPACE + new SimpleDateFormat(bestDateTimePattern, Locale.getDefault()).format(date.getTime());
    }

    public static String getFormattedDate(Date date) {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date);
    }

    public static long getTodayMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(11, 0);
        calendar.set(13, 0);
        calendar.set(12, 0);
        return calendar.getTime().getTime();
    }

    public static String getFormattedTime(Context context, Date date) {
        return new SimpleDateFormat(DateFormat.getBestDateTimePattern(Locale.getDefault(), DateFormat.is24HourFormat(context) ? "kk:mm" : "hh:mm aa"), Locale.getDefault()).format(date.getTime());
    }

    public static String getDateFromPicker(Context context, int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        return getFormattedDate(calendar.getTime());
    }

    public static String getTimeFromPicker(Context context, int hour, int minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(11, hour);
        calendar.set(12, minutes);
        return getFormattedTime(context, calendar.getTime());
    }

    public static long getTransactionStartDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(11, 0);
        calendar.set(12, 0);
        calendar.set(13, 0);
        calendar.set(14, 0);
        return calendar.getTime().getTime();
    }

    public static long getTransactionEndDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(11, 0);
        calendar.set(12, 0);
        calendar.set(13, 0);
        calendar.set(14, 0);
        calendar.add(5, 1);
        return calendar.getTime().getTime();
    }

    public static boolean isNotSameYear(Date date) {
        Calendar calendar = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(1) != calendar2.get(1);
    }

    public static boolean isBeforeDate(Date date1, Date date2) {
        Calendar calendar = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        calendar.setTime(date1);
        calendar2.setTime(date2);
        if (date2.before(date1)) {
            return true;
        }
        return calendar.get(6) == calendar2.get(6) && calendar.get(1) == calendar2.get(1);
    }

    public static boolean isSameDay(long date1, long date2) {
        Calendar calendar = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        calendar.setTimeInMillis(date1);
        calendar2.setTimeInMillis(date2);
        return calendar.get(1) == calendar2.get(1) && calendar.get(6) == calendar2.get(6);
    }

    public static boolean isDatePast(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(5, 1);
        calendar.set(14, 0);
        calendar.set(13, 0);
        calendar.set(12, 0);
        calendar.set(11, 0);
        long timeInMillis = calendar.getTimeInMillis();
        calendar.setTime(date);
        return timeInMillis > calendar.getTimeInMillis();
    }
}