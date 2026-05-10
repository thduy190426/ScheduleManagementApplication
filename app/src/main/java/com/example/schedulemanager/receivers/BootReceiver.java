package com.example.schedulemanager.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import com.example.schedulemanager.services.RescheduleWorker;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Device booted. Enqueuing work to reschedule alarms!");
            
            OneTimeWorkRequest rescheduleRequest = new OneTimeWorkRequest.Builder(RescheduleWorker.class)
                    .build();
            
            WorkManager.getInstance(context).enqueue(rescheduleRequest);
        }
    }
}
