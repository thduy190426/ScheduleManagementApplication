package com.example.schedulemanager.receivers;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.schedulemanager.R;
import com.example.schedulemanager.activities.DetailActivity;
import com.example.schedulemanager.models.Schedule;
import com.example.schedulemanager.utils.IntentKeys;
import com.example.schedulemanager.utils.NotificationHelper;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String TAG = "ReminderReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Schedule schedule = intent.getParcelableExtra(IntentKeys.EXTRA_SCHEDULE);
        if (schedule == null) return;

        Log.d(TAG, "Received reminder for: " + schedule.getTitle());

        Intent detailIntent = new Intent(context, DetailActivity.class);
        detailIntent.putExtra(IntentKeys.EXTRA_SCHEDULE, schedule);
        detailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                schedule.getId(),
                detailIntent,
                flags
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(schedule.getTitle())
                .setContentText("Reminder: Starts in " + schedule.getReminderMinutes() + " minutes")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        // Check for permission is required on API 33+
        try {
            notificationManager.notify(schedule.getId(), builder.build());
        } catch (SecurityException e) {
            Log.e(TAG, "Notification permission not granted", e);
        }
    }
}
