package com.example.schedulemanager.utils;

import android.content.Context;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class DateTimeUtils {

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm";

    private DateTimeUtils() {}

    public static String getCurrentDate(Context context) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        sdf.setTimeZone(getTimeZone(context));
        return sdf.format(new Date());
    }

    public static String getCurrentDateTime(Context context) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT, Locale.getDefault());
        sdf.setTimeZone(getTimeZone(context));
        return sdf.format(new Date());
    }

    private static java.util.TimeZone getTimeZone(Context context) {
        String tzId = new PreferenceManager(context).getTimezone();
        return java.util.TimeZone.getTimeZone(tzId);
    }

    public static String formatDisplayDate(String dateStr) {
        try {
            Date date = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).parse(dateStr);
            if (date != null) {
                return new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateStr;
    }

    public static Calendar getCalendar(String date, String time, Context context) {
        Calendar calendar = Calendar.getInstance(getTimeZone(context));
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT, Locale.getDefault());
            sdf.setTimeZone(getTimeZone(context));
            Date d = sdf.parse(date + " " + time);
            if (d != null) {
                calendar.setTime(d);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return calendar;
    }

    public static boolean isTimeBefore(String time1, String time2) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
            Date d1 = sdf.parse(time1);
            Date d2 = sdf.parse(time2);
            return d1 != null && d2 != null && d1.before(d2);
        } catch (ParseException e) {
            return false;
        }
    }

    public static boolean isDateInPast(String date, String time, Context context) {
        Calendar target = getCalendar(date, time, context);
        return target.getTimeInMillis() < System.currentTimeMillis();
    }
}
