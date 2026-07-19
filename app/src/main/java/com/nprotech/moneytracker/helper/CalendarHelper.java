package com.nprotech.moneytracker.helper;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.models.CalendarDayModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarHelper {

    public static Date getCalendarDay(Date date, int i) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(5, i);
        return calendar.getTime();
    }

    public static Date getInitialDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(5, 1);
        return calendar.getTime();
    }

    public static String getBudgetFormattedDate(Context context, Date date, int type) {
        if (type != 0) {
            if (type != 1) {
                if (type == 2) {
                    return getFormattedQuarterlyDate(date);
                }
                return getFormattedYearlyDate(date);
            }
            return getFormattedMonthlyDate(date);
        }
        return getFormattedWeeklyDate(date);
    }

    public static String getPieFormattedDate(Context context, Date date, int type) {
        if (type != 0) {
            if (type != 1) {
                if (type != 2) {
                    if (type != 3) {
                        if (type == 4) {
                            return getFormattedYearlyDate(date);
                        }
                        return context.getResources().getString(R.string.all_transaction);
                    }
                    return getFormattedQuarterlyDate(date);
                }
                return getFormattedMonthlyDate(date);
            }
            return getFormattedWeeklyDate(date);
        }
        return getFormattedDailyDate(date);
    }

    public static String getTrendFormattedDate(Context context, Date date, int type) {
        if (type != 0) {
            if (type != 1) {
                if (type != 2) {
                    if (type != 3) {
                        if (type == 4) {
                            return getFormattedYearlyDate(date);
                        }
                        return context.getResources().getString(R.string.all_transaction);
                    }
                    return getFormattedQuarterlyDate(date);
                }
                return getFormattedMonthlyDate(date);
            }
            return getFormattedWeeklyDate(date);
        }
        return getFormattedDailyDate(date);
    }

    public static String getWeeklySpendingFormattedDate(Date date) {
        return getFormattedWeeklyDate(date);
    }

    public static String getFormattedDailyDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return new SimpleDateFormat(DateFormat.getBestDateTimePattern(Locale.getDefault(), "dd MMMM yyyy"), Locale.getDefault()).format(calendar.getTime());
    }

    public static String getFormattedWeeklyDate(Date date) {
        int firstDayOfWeek = PreferenceManager.INSTANCE.getWeekStartOn();
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(1);
        calendar.setTime(date);
        int i = calendar.get(7);
        if (firstDayOfWeek > i) {
            calendar.add(3, -1);
        }
        calendar.set(7, firstDayOfWeek);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setFirstDayOfWeek(1);
        calendar2.setTime(date);
        if (firstDayOfWeek > i) {
            calendar2.add(3, -1);
        }
        calendar2.set(7, firstDayOfWeek);
        calendar2.add(7, 6);
        String str = DateHelper.isNotSameYear(calendar.getTime()) ? "dd MMM yyyy" : "dd MMM";
        String str2 = DateHelper.isNotSameYear(calendar2.getTime()) ? "dd MMM yyyy" : "dd MMM";
        String bestDateTimePattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), str);
        String bestDateTimePattern2 = DateFormat.getBestDateTimePattern(Locale.getDefault(), str2);
        String format = new SimpleDateFormat(bestDateTimePattern, Locale.getDefault()).format(calendar.getTime());
        return format + " - " + new SimpleDateFormat(bestDateTimePattern2, Locale.getDefault()).format(calendar2.getTime());
    }

    public static String getFormattedMonthlyDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return new SimpleDateFormat(DateFormat.getBestDateTimePattern(Locale.getDefault(), "MMMM yyyy"), Locale.getDefault()).format(calendar.getTime());
    }

    public static String getFormattedQuarterlyDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        calendar.setTime(date);
        calendar2.setTime(date);
        String bestDateTimePattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), "MMMM yyyy");
        switch (calendar.get(2)) {
            case 0:
            case 1:
            case 2:
                calendar.set(2, 0);
                calendar2.set(2, 2);
                return new SimpleDateFormat(bestDateTimePattern, Locale.getDefault()).format(calendar.getTime()) + " - " + new SimpleDateFormat(bestDateTimePattern, Locale.getDefault()).format(calendar2.getTime());
            case 3:
            case 4:
            case 5:
                calendar.set(2, 3);
                calendar2.set(2, 5);
                return new SimpleDateFormat(bestDateTimePattern, Locale.getDefault()).format(calendar.getTime()) + " - " + new SimpleDateFormat(bestDateTimePattern, Locale.getDefault()).format(calendar2.getTime());
            case 6:
            case 7:
            case 8:
                calendar.set(2, 6);
                calendar2.set(2, 8);
                return new SimpleDateFormat(bestDateTimePattern, Locale.getDefault()).format(calendar.getTime()) + " - " + new SimpleDateFormat(bestDateTimePattern, Locale.getDefault()).format(calendar2.getTime());
            default:
                calendar.set(2, 9);
                calendar2.set(2, 11);
                return new SimpleDateFormat(bestDateTimePattern, Locale.getDefault()).format(calendar.getTime()) + " - " + new SimpleDateFormat(bestDateTimePattern, Locale.getDefault()).format(calendar2.getTime());
        }
    }

    public static String getFormattedYearlyDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return new SimpleDateFormat(DateFormat.getBestDateTimePattern(Locale.getDefault(), "yyyy"), Locale.getDefault()).format(calendar.getTime());
    }

    public static Date incrementDay(Date date, int i) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(5, i);
        return calendar.getTime();
    }

    public static Date incrementWeek(Date date, int i) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(3, i);
        return calendar.getTime();
    }

    public static Date incrementMonth(Date date, int i) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(2, i);
        return calendar.getTime();
    }

    public static Date incrementQuarter(Date date, int i) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(2, i * 3);
        return calendar.getTime();
    }

    public static Date incrementYear(Date date, int i) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(1, i);
        return calendar.getTime();
    }

    public static int getDayFromDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(5);
    }

    public static int getMonthFromDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(2);
    }

    public static Date getDateFromPicker(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(1, year);
        calendar.set(2, month);
        calendar.set(5, day);
        calendar.set(11, 0);
        calendar.set(12, 0);
        calendar.set(13, 0);
        return calendar.getTime();
    }

    public static int getYearFromDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(1);
    }

    public static int getDayOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.getActualMaximum(5);
    }

    public static int getDayOfWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(5, 1);
        return calendar.get(7);
    }

    public static long getDailyStartDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(11, 0);
        calendar.set(12, 0);
        calendar.set(13, 0);
        calendar.set(14, 0);
        return calendar.getTime().getTime();
    }

    public static long getDailyEndDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(11, 0);
        calendar.set(12, 0);
        calendar.set(13, 0);
        calendar.set(14, 0);
        calendar.add(5, 1);
        return calendar.getTime().getTime();
    }

//    public static long getWeeklyStartDate(Context context, Date date) {
//        int firstDayOfWeek = SharePreferenceHelper.getFirstDayOfWeek(context);
//        Calendar calendar = Calendar.getInstance();
//        calendar.setFirstDayOfWeek(1);
//        calendar.setTime(date);
//        if (firstDayOfWeek > calendar.get(7)) {
//            calendar.add(3, -1);
//        }
//        calendar.set(11, 0);
//        calendar.set(12, 0);
//        calendar.set(13, 0);
//        calendar.set(14, 0);
//        calendar.set(7, firstDayOfWeek);
//        return calendar.getTime().getTime();
//    }

//    public static long getWeeklyEndDate(Context context, Date date) {
//        int firstDayOfWeek = SharePreferenceHelper.getFirstDayOfWeek(context);
//        Calendar calendar = Calendar.getInstance();
//        calendar.setFirstDayOfWeek(1);
//        calendar.setTime(date);
//        if (firstDayOfWeek > calendar.get(7)) {
//            calendar.add(3, -1);
//        }
//        calendar.set(11, 0);
//        calendar.set(12, 0);
//        calendar.set(13, 0);
//        calendar.set(14, 0);
//        calendar.set(7, firstDayOfWeek);
//        calendar.add(3, 1);
//        return calendar.getTime().getTime();
//    }

    public static long getMonthlyStartDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(5, 1);
        calendar.set(11, 0);
        calendar.set(12, 0);
        calendar.set(13, 0);
        calendar.set(14, 0);
        return calendar.getTime().getTime();
    }

    public static long getMonthlyEndDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(5, 1);
        calendar.set(11, 0);
        calendar.set(12, 0);
        calendar.set(13, 0);
        calendar.set(14, 0);
        calendar.add(2, 1);
        return calendar.getTime().getTime();
    }

    public static long getQuarterlyStartDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(5, 1);
        calendar.set(11, 0);
        calendar.set(12, 0);
        calendar.set(13, 0);
        calendar.set(14, 0);
        switch (calendar.get(2)) {
            case 0:
            case 1:
            case 2:
                calendar.set(2, 0);
                break;
            case 3:
            case 4:
            case 5:
                calendar.set(2, 3);
                break;
            case 6:
            case 7:
            case 8:
                calendar.set(2, 6);
                break;
            default:
                calendar.set(2, 9);
                break;
        }
        return calendar.getTime().getTime();
    }

    public static long getQuarterlyEndDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(5, 1);
        calendar.set(11, 0);
        calendar.set(12, 0);
        calendar.set(13, 0);
        calendar.set(14, 0);
        switch (calendar.get(2)) {
            case 0:
            case 1:
            case 2:
                calendar.set(2, 0);
                break;
            case 3:
            case 4:
            case 5:
                calendar.set(2, 3);
                break;
            case 6:
            case 7:
            case 8:
                calendar.set(2, 6);
                break;
            default:
                calendar.set(2, 9);
                break;
        }
        calendar.add(2, 3);
        return calendar.getTime().getTime();
    }

    public static long getYearlyStartDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(2, 0);
        calendar.set(5, 1);
        calendar.set(11, 0);
        calendar.set(12, 0);
        calendar.set(13, 0);
        calendar.set(14, 0);
        return calendar.getTime().getTime();
    }

    public static long getYearlyEndDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(2, 0);
        calendar.set(5, 1);
        calendar.set(11, 0);
        calendar.set(12, 0);
        calendar.set(13, 0);
        calendar.set(14, 0);
        calendar.add(1, 1);
        return calendar.getTime().getTime();
    }

    public static Date getCustomInitialStartDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(5, 1);
        calendar.set(11, 0);
        calendar.set(12, 0);
        calendar.set(13, 0);
        calendar.set(14, 0);
        return calendar.getTime();
    }

    public static Date getCustomInitialEndDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(5, 1);
        calendar.set(11, 0);
        calendar.set(12, 0);
        calendar.set(13, 0);
        calendar.set(14, 0);
        calendar.add(2, 1);
        calendar.add(5, -1);
        return calendar.getTime();
    }

    public static long getCustomStartDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(11, 0);
        calendar.set(12, 0);
        calendar.set(13, 0);
        calendar.set(14, 0);
        return calendar.getTime().getTime();
    }

    public static long getCustomEndDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(11, 0);
        calendar.set(12, 0);
        calendar.set(13, 0);
        calendar.set(14, 0);
        calendar.add(5, 1);
        Log.d("asd", String.valueOf(calendar.getTime()));
        return calendar.getTime().getTime();
    }

    public static String getFormattedCustomDate(Date startDate, Date endDate) {
        if (DateHelper.isSameDay(startDate.getTime(), endDate.getTime())) {
            return DateHelper.getFormattedDate(startDate);
        }
        return DateHelper.getFormattedDate(startDate) + " - " + DateHelper.getFormattedDate(endDate);
    }

    public static int isSameMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date);
        if (calendar.get(1) == calendar2.get(1) && calendar.get(2) == calendar2.get(2)) {
            return calendar.get(5);
        }
        return -1;
    }

    public static Date getDateFromLong(long timeStamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStamp);
        return calendar.getTime();
    }

    public static List<CalendarDayModel> getMonthCells(Date date, int weekStartOn) {

        List<CalendarDayModel> list = new ArrayList<>();

        Calendar month = Calendar.getInstance();
        month.setTime(date);
        month.set(Calendar.DAY_OF_MONTH, 1);

        Calendar today = Calendar.getInstance();

        Calendar selectedMonth = Calendar.getInstance();
        selectedMonth.setTime(date);
        int firstDayOfMonth = month.get(Calendar.DAY_OF_WEEK);
        int offset = (firstDayOfMonth - weekStartOn + 7) % 7;

        month.add(Calendar.DAY_OF_MONTH, -offset);

        for (int i = 0; i < 42; i++) {

            Calendar current = (Calendar) month.clone();

            list.add(new CalendarDayModel(
                    current.getTime(),
                    current.get(Calendar.DAY_OF_MONTH),
                    current.get(Calendar.MONTH) == selectedMonth.get(Calendar.MONTH),
                    current.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                            && current.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR),
                    false
            ));

            month.add(Calendar.DAY_OF_MONTH, 1);
        }

        return list;
    }

    public static List<String> getShortWeekDays(Context context, int weekStartOn) {
        String[] days = context.getResources().getStringArray(R.array.week_days_short);
        List<String> list = new ArrayList<>();
        // Calendar constants are 1-7, array index is 0-6
        int startIndex = weekStartOn - 1;
        for (int i = 0; i < 7; i++) {
            list.add(days[(startIndex + i) % 7]);
        }
        return list;
    }

    public static long getStartOfDay(long timeMillis) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillis);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }
}