package com.example.schedulemanager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.schedulemanager.database.ScheduleContract.ScheduleEntry;
import com.example.schedulemanager.models.Priority;
import com.example.schedulemanager.models.Schedule;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "schedule_manager.db";
    private static final int DATABASE_VERSION = 2;

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_SCHEDULES_TABLE = "CREATE TABLE " + ScheduleEntry.TABLE_NAME + " ("
                + ScheduleEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ScheduleEntry.COLUMN_TITLE + " TEXT NOT NULL, "
                + ScheduleEntry.COLUMN_DESCRIPTION + " TEXT, "
                + ScheduleEntry.COLUMN_DATE + " TEXT NOT NULL, "
                + ScheduleEntry.COLUMN_START_TIME + " TEXT NOT NULL, "
                + ScheduleEntry.COLUMN_END_TIME + " TEXT NOT NULL, "
                + ScheduleEntry.COLUMN_PRIORITY + " INTEGER NOT NULL DEFAULT 0, "
                + ScheduleEntry.COLUMN_CATEGORY + " INTEGER NOT NULL DEFAULT 0, "
                + ScheduleEntry.COLUMN_RECURRENCE + " INTEGER NOT NULL DEFAULT 0, "
                + ScheduleEntry.COLUMN_IS_COMPLETED + " INTEGER NOT NULL DEFAULT 0, "
                + ScheduleEntry.COLUMN_REMINDER_MINUTES + " INTEGER NOT NULL DEFAULT 0, "
                + ScheduleEntry.COLUMN_CREATED_AT + " TEXT);";

        db.execSQL(SQL_CREATE_SCHEDULES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ScheduleEntry.TABLE_NAME);
        onCreate(db);
    }

    // CRUD Methods

    public long insertSchedule(Schedule schedule) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = scheduleToContentValues(schedule);
        return db.insert(ScheduleEntry.TABLE_NAME, null, values);
    }

    public long addSchedule(Schedule schedule) {
        return insertSchedule(schedule);
    }

    public int updateSchedule(Schedule schedule) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = scheduleToContentValues(schedule);
        return db.update(ScheduleEntry.TABLE_NAME, values, ScheduleEntry._ID + " = ?",
                new String[]{String.valueOf(schedule.getId())});
    }

    public void deleteSchedule(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(ScheduleEntry.TABLE_NAME, ScheduleEntry._ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    public Schedule getScheduleById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(ScheduleEntry.TABLE_NAME, null,
                ScheduleEntry._ID + " = ?", new String[]{String.valueOf(id)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            Schedule schedule = cursorToSchedule(cursor);
            cursor.close();
            return schedule;
        }
        return null;
    }

    public List<Schedule> getAllSchedules() {
        return searchSchedules(null, -1, -1);
    }

    public List<Schedule> searchSchedules(String query, int categoryId, int status) {
        List<Schedule> schedules = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        StringBuilder selection = new StringBuilder();
        List<String> selectionArgs = new ArrayList<>();

        if (query != null && !query.isEmpty()) {
            selection.append("(").append(ScheduleEntry.COLUMN_TITLE).append(" LIKE ? OR ")
                    .append(ScheduleEntry.COLUMN_DESCRIPTION).append(" LIKE ?)");
            selectionArgs.add("%" + query + "%");
            selectionArgs.add("%" + query + "%");
        }

        if (categoryId != -1) {
            if (selection.length() > 0) selection.append(" AND ");
            selection.append(ScheduleEntry.COLUMN_CATEGORY).append(" = ?");
            selectionArgs.add(String.valueOf(categoryId));
        }

        if (status != -1) {
            if (selection.length() > 0) selection.append(" AND ");
            selection.append(ScheduleEntry.COLUMN_IS_COMPLETED).append(" = ?");
            selectionArgs.add(String.valueOf(status));
        }

        String selectionStr = selection.length() > 0 ? selection.toString() : null;
        String[] selectionArgsArr = selectionArgs.isEmpty() ? null : selectionArgs.toArray(new String[0]);

        try (Cursor cursor = db.query(ScheduleEntry.TABLE_NAME, null, selectionStr, selectionArgsArr, null, null,
                ScheduleEntry.COLUMN_DATE + " ASC, " + ScheduleEntry.COLUMN_START_TIME + " ASC")) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    schedules.add(cursorToSchedule(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return schedules;
    }

    public int getCompletedSchedulesCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + ScheduleEntry.TABLE_NAME + " WHERE " + ScheduleEntry.COLUMN_IS_COMPLETED + " = 1", null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public int getTotalSchedulesCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + ScheduleEntry.TABLE_NAME, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public List<Schedule> getSchedulesByDate(String date) {
        List<Schedule> schedules = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.query(ScheduleEntry.TABLE_NAME, null,
                ScheduleEntry.COLUMN_DATE + " = ?", new String[]{date},
                null, null, ScheduleEntry.COLUMN_START_TIME + " ASC")) {

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    schedules.add(cursorToSchedule(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return schedules;
    }

    private ContentValues scheduleToContentValues(Schedule schedule) {
        ContentValues values = new ContentValues();
        values.put(ScheduleEntry.COLUMN_TITLE, schedule.getTitle());
        values.put(ScheduleEntry.COLUMN_DESCRIPTION, schedule.getDescription());
        values.put(ScheduleEntry.COLUMN_DATE, schedule.getDate());
        values.put(ScheduleEntry.COLUMN_START_TIME, schedule.getStartTime());
        values.put(ScheduleEntry.COLUMN_END_TIME, schedule.getEndTime());
        values.put(ScheduleEntry.COLUMN_PRIORITY, schedule.getPriority().toInt());
        values.put(ScheduleEntry.COLUMN_CATEGORY, schedule.getCategory() != null ? schedule.getCategory().toInt() : 0);
        values.put(ScheduleEntry.COLUMN_RECURRENCE, schedule.getRecurrence() != null ? schedule.getRecurrence().toInt() : 0);
        values.put(ScheduleEntry.COLUMN_IS_COMPLETED, schedule.isCompleted() ? 1 : 0);
        values.put(ScheduleEntry.COLUMN_REMINDER_MINUTES, schedule.getReminderMinutes());
        values.put(ScheduleEntry.COLUMN_CREATED_AT, schedule.getCreatedAt());
        return values;
    }

    private Schedule cursorToSchedule(Cursor cursor) {
        return new Schedule.Builder()
                .id(cursor.getInt(cursor.getColumnIndexOrThrow(ScheduleEntry._ID)))
                .title(cursor.getString(cursor.getColumnIndexOrThrow(ScheduleEntry.COLUMN_TITLE)))
                .description(cursor.getString(cursor.getColumnIndexOrThrow(ScheduleEntry.COLUMN_DESCRIPTION)))
                .date(cursor.getString(cursor.getColumnIndexOrThrow(ScheduleEntry.COLUMN_DATE)))
                .startTime(cursor.getString(cursor.getColumnIndexOrThrow(ScheduleEntry.COLUMN_START_TIME)))
                .endTime(cursor.getString(cursor.getColumnIndexOrThrow(ScheduleEntry.COLUMN_END_TIME)))
                .priority(com.example.schedulemanager.models.Priority.fromInt(cursor.getInt(cursor.getColumnIndexOrThrow(ScheduleEntry.COLUMN_PRIORITY))))
                .category(com.example.schedulemanager.models.Category.fromInt(cursor.getInt(cursor.getColumnIndexOrThrow(ScheduleEntry.COLUMN_CATEGORY))))
                .recurrence(com.example.schedulemanager.models.Recurrence.fromInt(cursor.getInt(cursor.getColumnIndexOrThrow(ScheduleEntry.COLUMN_RECURRENCE))))
                .isCompleted(cursor.getInt(cursor.getColumnIndexOrThrow(ScheduleEntry.COLUMN_IS_COMPLETED)) == 1)
                .reminderMinutes(cursor.getInt(cursor.getColumnIndexOrThrow(ScheduleEntry.COLUMN_REMINDER_MINUTES)))
                .createdAt(cursor.getString(cursor.getColumnIndexOrThrow(ScheduleEntry.COLUMN_CREATED_AT)))
                .build();
    }
}
