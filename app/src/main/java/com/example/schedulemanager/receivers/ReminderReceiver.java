package com.example.schedulemanager.receivers;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.schedulemanager.R;
import com.example.schedulemanager.activities.DetailActivity;
import com.example.schedulemanager.database.DatabaseHelper;
import com.example.schedulemanager.models.Recurrence;
import com.example.schedulemanager.models.Schedule;
import com.example.schedulemanager.utils.DateTimeUtils;
import com.example.schedulemanager.utils.IntentKeys;
import com.example.schedulemanager.utils.NotificationHelper;
import com.example.schedulemanager.utils.PreferenceManager;
import com.example.schedulemanager.utils.ReminderScheduler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String TAG = "ReminderReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Schedule schedule = intent.getParcelableExtra(IntentKeys.EXTRA_SCHEDULE);
        
        if (IntentKeys.ACTION_MARK_COMPLETED.equals(action)) {
            handleMarkCompleted(context, schedule);
            return;
        } else if (IntentKeys.ACTION_SNOOZE.equals(action)) {
            handleSnooze(context, schedule);
            return;
        }

        if (schedule == null) {
            int scheduleId = intent.getIntExtra(IntentKeys.EXTRA_SCHEDULE_ID, -1);
            if (scheduleId != -1) {
                schedule = DatabaseHelper.getInstance(context).getScheduleById(scheduleId);
            }
        }
        
        if (schedule == null) return;

        Log.d(TAG, "Received reminder for: " + schedule.getTitle());

        PreferenceManager prefManager = new PreferenceManager(context);
        
        if (isDndActive(prefManager)) {
            Log.d(TAG, "DND is active. Skipping notification.");
            return;
        }

        Intent detailIntent = new Intent(context, DetailActivity.class);
        detailIntent.putExtra(IntentKeys.EXTRA_SCHEDULE, schedule);
        detailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                schedule.getId(),
                detailIntent,
                flags
        );

        boolean soundEnabled = prefManager.isReminderSoundEnabled();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_category)
                .setContentTitle(schedule.getTitle())
                .setContentText(context.getString(R.string.notification_reminder_msg, schedule.getReminderMinutes()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setNumber(1)
                .setContentIntent(pendingIntent);

        Intent snoozeIntent = new Intent(context, ReminderReceiver.class);
        snoozeIntent.setAction(IntentKeys.ACTION_SNOOZE);
        snoozeIntent.putExtra(IntentKeys.EXTRA_SCHEDULE, schedule);
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(context, schedule.getId() + 1000, snoozeIntent, flags);
        builder.addAction(R.drawable.ic_edit, context.getString(R.string.notification_action_snooze), snoozePendingIntent);

        Intent completeIntent = new Intent(context, ReminderReceiver.class);
        completeIntent.setAction(IntentKeys.ACTION_MARK_COMPLETED);
        completeIntent.putExtra(IntentKeys.EXTRA_SCHEDULE, schedule);
        PendingIntent completePendingIntent = PendingIntent.getBroadcast(context, schedule.getId() + 2000, completeIntent, flags);
        builder.addAction(R.drawable.ic_check, context.getString(R.string.notification_action_done), completePendingIntent);

        if (soundEnabled) {
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            builder.setSound(alarmSound);
            builder.setDefaults(NotificationCompat.DEFAULT_VIBRATE);
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.notify(schedule.getId(), builder.build());
        } catch (SecurityException e) {
            Log.e(TAG, "Notification permission not granted", e);
        }

        if (schedule.getRecurrence() != null && schedule.getRecurrence() != Recurrence.NONE) {
            handleRecurrence(context, schedule);
        }
    }

    private void handleMarkCompleted(Context context, Schedule schedule) {
        if (schedule == null) return;
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
        schedule.setCompleted(true);
        dbHelper.updateSchedule(schedule);
        
        NotificationManagerCompat.from(context).cancel(schedule.getId());
        Log.d(TAG, "Schedule marked as completed: " + schedule.getId());
        
        Intent updateWidgetIntent = new Intent("com.example.schedulemanager.widgets.ACTION_REFRESH");
        context.sendBroadcast(updateWidgetIntent);
    }

    private void handleSnooze(Context context, Schedule schedule) {
        if (schedule == null) return;
        
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 15);
        
        ReminderScheduler.scheduleCustomReminder(context, schedule, calendar.getTimeInMillis());
        
        NotificationManagerCompat.from(context).cancel(schedule.getId());
        Log.d(TAG, "Snoozed reminder for 15 minutes: " + schedule.getId());
    }

    private void handleRecurrence(Context context, Schedule schedule) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
        Calendar nextDate = DateTimeUtils.getCalendar(schedule.getDate(), "00:00", context);
        
        switch (schedule.getRecurrence()) {
            case DAILY:
                nextDate.add(Calendar.DAY_OF_YEAR, 1);
                break;
            case WEEKLY:
                nextDate.add(Calendar.WEEK_OF_YEAR, 1);
                break;
            case MONTHLY:
                nextDate.add(Calendar.MONTH, 1);
                break;
            default:
                return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String nextDateStr = sdf.format(nextDate.getTime());

        Schedule nextSchedule = new Schedule.Builder()
                .title(schedule.getTitle())
                .description(schedule.getDescription())
                .date(nextDateStr)
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .priority(schedule.getPriority())
                .category(schedule.getCategory())
                .customCategory(schedule.getCustomCategory())
                .recurrence(schedule.getRecurrence())
                .reminderMinutes(schedule.getReminderMinutes())
                .isCompleted(false)
                .createdAt(DateTimeUtils.getCurrentDateTime(context))
                .build();

        long id = dbHelper.insertSchedule(nextSchedule);
        nextSchedule.setId((int) id);
        ReminderScheduler.scheduleReminder(context, nextSchedule);
        
        Log.d(TAG, "Recurring schedule created for date: " + nextDateStr);
    }

    private boolean isDndActive(PreferenceManager prefManager) {
        if (!prefManager.isDndEnabled()) {
            return false;
        }

        String startTimeStr = prefManager.getDndStartTime();
        String endTimeStr = prefManager.getDndEndTime();

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        try {
            Date startTime = sdf.parse(startTimeStr);
            Date endTime = sdf.parse(endTimeStr);

            if (startTime == null || endTime == null) return false;

            Calendar now = Calendar.getInstance();
            Calendar start = Calendar.getInstance();
            start.setTime(startTime);
            start.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));

            Calendar end = Calendar.getInstance();
            end.setTime(endTime);
            end.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));

            long nowMs = now.getTimeInMillis();
            long startMs = start.getTimeInMillis();
            long endMs = end.getTimeInMillis();

            if (startMs <= endMs) {
                return nowMs >= startMs && nowMs <= endMs;
            } else {
                return nowMs >= startMs || nowMs <= endMs;
            }
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing DND times", e);
            return false;
        }
    }
}
