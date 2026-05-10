package com.example.schedulemanager.utils;

import android.content.Context;
import com.example.schedulemanager.database.DatabaseHelper;
import com.example.schedulemanager.models.Schedule;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class BackupManager {

    private static final String TAG = "BackupManager";
    private static final String BACKUP_FILE_NAME = "schedules_backup.json";

    private final Context context;
    private final DatabaseHelper dbHelper;
    private final Gson gson;

    public BackupManager(Context context) {
        this.context = context;
        this.dbHelper = DatabaseHelper.getInstance(context);
        this.gson = new Gson();
    }

    public String exportToJSON() throws IOException {
        List<Schedule> schedules = dbHelper.getAllSchedules();
        return gson.toJson(schedules);
    }

    public void importFromJSON(String json) throws IOException {
        Type listType = new TypeToken<List<Schedule>>() {}.getType();
        List<Schedule> schedules = gson.fromJson(json, listType);

        if (schedules != null) {
            for (Schedule schedule : schedules) {
                if (dbHelper.getScheduleById(schedule.getId()) != null) {
                    dbHelper.updateSchedule(schedule);
                } else {
                    long newId = dbHelper.insertSchedule(schedule);
                    schedule.setId((int) newId);
                }
                if (!schedule.isCompleted()) {
                    ReminderScheduler.scheduleReminder(context, schedule);
                }
            }
        }
    }
}
