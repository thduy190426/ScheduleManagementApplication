package com.example.schedulemanager.utils;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.schedulemanager.models.Schedule;
import com.example.schedulemanager.receivers.ReminderReceiver;

import java.util.Calendar;

public class ReminderScheduler {

    private static final String TAG = "ReminderScheduler";

    @SuppressLint("ScheduleExactAlarm")
    public static void scheduleReminder(Context context, Schedule schedule) {
        if (schedule.getReminderMinutes() == 0) {
            cancelReminder(context, schedule.getId());
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Calendar calendar = DateTimeUtils.getCalendar(schedule.getDate(), schedule.getStartTime());
        calendar.add(Calendar.MINUTE, -schedule.getReminderMinutes());

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            return;
        }

        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra(IntentKeys.EXTRA_SCHEDULE_ID, schedule.getId());
        intent.putExtra(IntentKeys.EXTRA_SCHEDULE, schedule);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                schedule.getId(),
                intent,
                flags
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }

        Log.d(TAG, "Scheduled reminder for ID " + schedule.getId() + " at " + calendar.getTime());
    }

    public static void cancelReminder(Context context, int scheduleId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, ReminderReceiver.class);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                scheduleId,
                intent,
                flags
        );

        alarmManager.cancel(pendingIntent);
        Log.d(TAG, "Cancelled reminder for ID " + scheduleId);
    }
}
