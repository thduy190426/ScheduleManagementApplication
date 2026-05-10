package com.example.schedulemanager.services;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.schedulemanager.database.DatabaseHelper;
import com.example.schedulemanager.models.Schedule;
import com.example.schedulemanager.utils.DateTimeUtils;
import com.example.schedulemanager.utils.ReminderScheduler;

import java.util.List;

public class RescheduleWorker extends Worker {
    private static final String TAG = "RescheduleWorker";

    public RescheduleWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "doWork: Rescheduling all future alarms using WorkManager");
        
        try {
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(getApplicationContext());
            List<Schedule> allSchedules = dbHelper.getAllSchedules();
            String today = DateTimeUtils.getCurrentDate(getApplicationContext());

            for (Schedule schedule : allSchedules) {
                if (!schedule.isCompleted() && schedule.getDate().compareTo(today) >= 0) {
                    ReminderScheduler.scheduleReminder(getApplicationContext(), schedule);
                }
            }
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Error rescheduling alarms", e);
            return Result.retry();
        }
    }
}
