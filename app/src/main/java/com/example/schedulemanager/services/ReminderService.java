package com.example.schedulemanager.services;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.example.schedulemanager.database.DatabaseHelper;
import com.example.schedulemanager.models.Schedule;
import com.example.schedulemanager.utils.DateTimeUtils;
import com.example.schedulemanager.utils.ReminderScheduler;

import java.util.List;

public class ReminderService extends JobIntentService {

    private static final String TAG = "ReminderService";
    private static final int JOB_ID = 1000;

    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, ReminderService.class, JOB_ID, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Log.d(TAG, "onHandleWork: Rescheduling all future alarms");
        
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(getApplicationContext());
        List<Schedule> allSchedules = dbHelper.getAllSchedules();
        String today = DateTimeUtils.getCurrentDate();

        for (Schedule schedule : allSchedules) {
            // Only reschedule incomplete future/today's schedules
            if (!schedule.isCompleted() && schedule.getDate().compareTo(today) >= 0) {
                ReminderScheduler.scheduleReminder(getApplicationContext(), schedule);
            }
        }
    }
}
