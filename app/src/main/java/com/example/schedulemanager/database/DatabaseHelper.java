package com.example.schedulemanager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.schedulemanager.database.ScheduleContract.ScheduleEntry;
import com.example.schedulemanager.models.Priority;
import com.example.schedulemanager.models.Schedule;
import com.example.schedulemanager.models.SubTask;
import com.example.schedulemanager.models.CustomCategory;
import com.example.schedulemanager.models.Category;
import com.example.schedulemanager.models.Recurrence;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "schedule_manager.db";
    private static final int DATABASE_VERSION = 7;

    private static final String TABLE_CUSTOM_CATEGORIES = "custom_categories";
    private static final String COLUMN_CAT_ID = "_id";
    private static final String COLUMN_CAT_NAME = "name";
    private static final String COLUMN_CAT_ICON = "icon";
    private static final String COLUMN_CAT_COLOR = "color";

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
                + ScheduleEntry.COLUMN_CREATED_AT + " TEXT, "
                + ScheduleEntry.COLUMN_CUSTOM_CATEGORY + " TEXT, "
                + ScheduleEntry.COLUMN_ATTACHMENT_PATH + " TEXT, "
                + ScheduleEntry.COLUMN_IS_DELETED + " INTEGER NOT NULL DEFAULT 0, "
                + ScheduleEntry.COLUMN_DELETED_AT + " TEXT);";

        db.execSQL(SQL_CREATE_SCHEDULES_TABLE);

        String SQL_CREATE_CUSTOM_CATEGORIES = "CREATE TABLE " + TABLE_CUSTOM_CATEGORIES + " ("
                + COLUMN_CAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_CAT_NAME + " TEXT NOT NULL UNIQUE, "
                + COLUMN_CAT_ICON + " TEXT, "
                + COLUMN_CAT_COLOR + " INTEGER DEFAULT 0);";
        db.execSQL(SQL_CREATE_CUSTOM_CATEGORIES);

        String SQL_CREATE_SUBTASKS_TABLE = "CREATE TABLE " + ScheduleContract.SubTaskEntry.TABLE_NAME + " ("
                + ScheduleContract.SubTaskEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ScheduleContract.SubTaskEntry.COLUMN_SCHEDULE_ID + " INTEGER NOT NULL, "
                + ScheduleContract.SubTaskEntry.COLUMN_TITLE + " TEXT NOT NULL, "
                + ScheduleContract.SubTaskEntry.COLUMN_IS_COMPLETED + " INTEGER NOT NULL DEFAULT 0, "
                + "FOREIGN KEY (" + ScheduleContract.SubTaskEntry.COLUMN_SCHEDULE_ID + ") REFERENCES "
                + ScheduleContract.ScheduleEntry.TABLE_NAME + " (" + ScheduleContract.ScheduleEntry._ID + ") ON DELETE CASCADE);";
        db.execSQL(SQL_CREATE_SUBTASKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + ScheduleEntry.TABLE_NAME + " ADD COLUMN " + ScheduleEntry.COLUMN_CUSTOM_CATEGORY + " TEXT");
        }
        if (oldVersion < 4) {
            String SQL_CREATE_CUSTOM_CATEGORIES = "CREATE TABLE " + TABLE_CUSTOM_CATEGORIES + " ("
                    + COLUMN_CAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_CAT_NAME + " TEXT NOT NULL UNIQUE, "
                    + COLUMN_CAT_ICON + " TEXT, "
                    + COLUMN_CAT_COLOR + " INTEGER DEFAULT 0);";
            db.execSQL(SQL_CREATE_CUSTOM_CATEGORIES);
        }
        if (oldVersion < 5) {
            db.execSQL("ALTER TABLE " + ScheduleEntry.TABLE_NAME + " ADD COLUMN " + ScheduleEntry.COLUMN_IS_DELETED + " INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE " + ScheduleEntry.TABLE_NAME + " ADD COLUMN " + ScheduleEntry.COLUMN_DELETED_AT + " TEXT");
        }
        if (oldVersion < 6) {
            String SQL_CREATE_SUBTASKS_TABLE = "CREATE TABLE " + ScheduleContract.SubTaskEntry.TABLE_NAME + " ("
                    + ScheduleContract.SubTaskEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + ScheduleContract.SubTaskEntry.COLUMN_SCHEDULE_ID + " INTEGER NOT NULL, "
                    + ScheduleContract.SubTaskEntry.COLUMN_TITLE + " TEXT NOT NULL, "
                    + ScheduleContract.SubTaskEntry.COLUMN_IS_COMPLETED + " INTEGER NOT NULL DEFAULT 0, "
                    + "FOREIGN KEY (" + ScheduleContract.SubTaskEntry.COLUMN_SCHEDULE_ID + ") REFERENCES "
                    + ScheduleContract.ScheduleEntry.TABLE_NAME + " (" + ScheduleContract.ScheduleEntry._ID + ") ON DELETE CASCADE);";
            db.execSQL(SQL_CREATE_SUBTASKS_TABLE);
        }
        if (oldVersion < 7) {
            db.execSQL("ALTER TABLE " + ScheduleEntry.TABLE_NAME + " ADD COLUMN " + ScheduleEntry.COLUMN_ATTACHMENT_PATH + " TEXT");
        }
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

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
        softDeleteSchedule(id);
    }

    public void softDeleteSchedule(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ScheduleEntry.COLUMN_IS_DELETED, 1);
        values.put(ScheduleEntry.COLUMN_DELETED_AT, String.valueOf(System.currentTimeMillis()));
        db.update(ScheduleEntry.TABLE_NAME, values, ScheduleEntry._ID + " = ?", new String[]{String.valueOf(id)});
    }

    public void restoreSchedule(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ScheduleEntry.COLUMN_IS_DELETED, 0);
        values.put(ScheduleEntry.COLUMN_DELETED_AT, (String) null);
        db.update(ScheduleEntry.TABLE_NAME, values, ScheduleEntry._ID + " = ?", new String[]{String.valueOf(id)});
    }

    public List<Schedule> getTrashSchedules() {
        List<Schedule> schedules = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = ScheduleEntry.COLUMN_IS_DELETED + " = 1";
        String orderBy = ScheduleEntry.COLUMN_DELETED_AT + " DESC";
        
        try (Cursor cursor = db.query(ScheduleEntry.TABLE_NAME, null, selection, null, null, null, orderBy)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    schedules.add(cursorToSchedule(cursor));
                } while (cursor.moveToNext());
            }
        }
        return schedules;
    }

    public void deleteForever(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(ScheduleEntry.TABLE_NAME, ScheduleEntry._ID + " = ?", new String[]{String.valueOf(id)});
    }

    public void clearOldTrash() {
        SQLiteDatabase db = this.getWritableDatabase();
        long sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L);
        db.delete(ScheduleEntry.TABLE_NAME, 
                ScheduleEntry.COLUMN_IS_DELETED + " = 1 AND " + ScheduleEntry.COLUMN_DELETED_AT + " < ?", 
                new String[]{String.valueOf(sevenDaysAgo)});
    }

    public Schedule getScheduleById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.query(ScheduleEntry.TABLE_NAME, null, ScheduleEntry._ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursorToSchedule(cursor);
            }
        }
        return null;
    }

    public List<Schedule> getAllSchedules() {
        return searchSchedules(null, -1, -1, -1, "time", null);
    }

    public int getCompletedSchedulesCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + ScheduleEntry.TABLE_NAME + 
                      " WHERE " + ScheduleEntry.COLUMN_IS_COMPLETED + " = 1 AND " + ScheduleEntry.COLUMN_IS_DELETED + " = 0";
        Cursor cursor = db.rawQuery(query, null);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    public int getTotalSchedulesCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + ScheduleEntry.TABLE_NAME + 
                      " WHERE " + ScheduleEntry.COLUMN_IS_DELETED + " = 0";
        Cursor cursor = db.rawQuery(query, null);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    public java.util.Map<String, Integer> getCategoryStatistics() {
        java.util.Map<String, Integer> stats = new java.util.HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + ScheduleEntry.COLUMN_CATEGORY + ", " + ScheduleEntry.COLUMN_CUSTOM_CATEGORY + ", COUNT(*) " +
                       "FROM " + ScheduleEntry.TABLE_NAME + 
                       " WHERE " + ScheduleEntry.COLUMN_IS_DELETED + " = 0 " +
                       "GROUP BY " + ScheduleEntry.COLUMN_CATEGORY + ", " + ScheduleEntry.COLUMN_CUSTOM_CATEGORY;
        
        try (Cursor cursor = db.rawQuery(query, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int catOrdinal = cursor.getInt(0);
                    String customName = cursor.getString(1);
                    int count = cursor.getInt(2);
                    
                    String label;
                    if (catOrdinal == Category.OTHER.ordinal() && customName != null) {
                        label = customName;
                    } else {
                        label = Category.values()[catOrdinal].name();
                    }
                    stats.put(label, count);
                } while (cursor.moveToNext());
            }
        }
        return stats;
    }

    public java.util.Map<String, Integer> getWeeklyTrends() {
        java.util.Map<String, Integer> stats = new java.util.LinkedHashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        long now = System.currentTimeMillis();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
        
        for (int i = 6; i >= 0; i--) {
            String date = sdf.format(new java.util.Date(now - (i * 24 * 60 * 60 * 1000L)));
            String query = "SELECT COUNT(*) FROM " + ScheduleEntry.TABLE_NAME + 
                           " WHERE " + ScheduleEntry.COLUMN_DATE + " = ? AND " + ScheduleEntry.COLUMN_IS_DELETED + " = 0";
            try (Cursor cursor = db.rawQuery(query, new String[]{date})) {
                if (cursor != null && cursor.moveToFirst()) {
                    stats.put(date, cursor.getInt(0));
                } else {
                    stats.put(date, 0);
                }
            }
        }
        return stats;
    }

    public List<Schedule> searchSchedules(String query, int categoryId, int status, String sortBy) {
        return searchSchedules(query, categoryId, status, -1, sortBy, null);
    }

    public List<Schedule> searchSchedules(String query, int categoryId, int status, int priority, String sortBy, String date) {
        List<Schedule> schedules = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        StringBuilder selection = new StringBuilder();
        List<String> selectionArgs = new ArrayList<>();

        selection.append(ScheduleEntry.COLUMN_IS_DELETED).append(" = 0");

        if (query != null && !query.trim().isEmpty()) {
            String trimmedQuery = query.trim().toLowerCase();
            
            if (trimmedQuery.startsWith("p:") || trimmedQuery.startsWith("priority:")) {
                String[] parts = trimmedQuery.split(":", 2);
                if (parts.length > 1) {
                    String pValue = parts[1].trim();
                    int pInt = -1;
                    if (pValue.contains("cao") || pValue.contains("high")) pInt = 2;
                    else if (pValue.contains("trung") || pValue.contains("medium")) pInt = 1;
                    else if (pValue.contains("thấp") || pValue.contains("low")) pInt = 0;
                    
                    if (pInt != -1) {
                        selection.append(" AND ").append(ScheduleEntry.COLUMN_PRIORITY).append(" = ?");
                        selectionArgs.add(String.valueOf(pInt));
                    }
                }
            } else if (trimmedQuery.startsWith("d:") || trimmedQuery.startsWith("date:")) {
                String[] parts = trimmedQuery.split(":", 2);
                if (parts.length > 1) {
                    String dValue = parts[1].trim();
                    selection.append(" AND ").append(ScheduleEntry.COLUMN_DATE).append(" LIKE ?");
                    selectionArgs.add("%" + dValue + "%");
                }
            } else {
                selection.append(" AND (").append(ScheduleEntry.COLUMN_TITLE).append(" LIKE ? OR ")
                        .append(ScheduleEntry.COLUMN_DESCRIPTION).append(" LIKE ?)");
                selectionArgs.add("%" + query + "%");
                selectionArgs.add("%" + query + "%");
            }
        }

        if (date != null && !date.isEmpty() && !selection.toString().contains(ScheduleEntry.COLUMN_DATE)) {
            selection.append(" AND ").append(ScheduleEntry.COLUMN_DATE).append(" = ?");
            selectionArgs.add(date);
        }

        if (categoryId != -1) {
            selection.append(" AND ").append(ScheduleEntry.COLUMN_CATEGORY).append(" = ?");
            selectionArgs.add(String.valueOf(categoryId));
        }

        if (status != -1) {
            selection.append(" AND ").append(ScheduleEntry.COLUMN_IS_COMPLETED).append(" = ?");
            selectionArgs.add(String.valueOf(status));
        }

        if (priority != -1) {
            selection.append(" AND ").append(ScheduleEntry.COLUMN_PRIORITY).append(" = ?");
            selectionArgs.add(String.valueOf(priority));
        }

        String selectionStr = selection.toString();
        String[] selectionArgsArr = selectionArgs.toArray(new String[0]);

        if (sortBy == null) sortBy = "time";
        String orderBy;
        switch (sortBy) {
            case "priority":
                orderBy = ScheduleEntry.COLUMN_PRIORITY + " DESC, " + ScheduleEntry.COLUMN_DATE + " ASC";
                break;
            case "smart":
                orderBy = ScheduleEntry.COLUMN_DATE + " ASC, " + ScheduleEntry.COLUMN_PRIORITY + " DESC, " + ScheduleEntry.COLUMN_START_TIME + " ASC";
                break;
            case "title":
                orderBy = ScheduleEntry.COLUMN_TITLE + " COLLATE NOCASE ASC";
                break;
            case "time":
            default:
                orderBy = ScheduleEntry.COLUMN_DATE + " ASC, " + ScheduleEntry.COLUMN_START_TIME + " ASC";
                break;
        }

        try (Cursor cursor = db.query(ScheduleEntry.TABLE_NAME, null, selectionStr, selectionArgsArr, null, null, orderBy)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Schedule s = cursorToSchedule(cursor);
                    if (s != null) {
                        schedules.add(s);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "Search error: " + e.getMessage());
        }
        return schedules;
    }

    public List<Schedule> getSchedulesByDate(String date) {
        return searchSchedules(null, -1, -1, -1, "time", date);
    }

    public List<String> getDatesWithSchedules() {
        List<String> dates = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(true, ScheduleEntry.TABLE_NAME, 
                new String[]{ScheduleEntry.COLUMN_DATE},
                ScheduleEntry.COLUMN_IS_DELETED + " = 0", 
                null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                dates.add(cursor.getString(0));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return dates;
    }

    public long insertCustomCategory(CustomCategory category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CAT_NAME, category.getName());
        values.put(COLUMN_CAT_ICON, category.getIconName());
        values.put(COLUMN_CAT_COLOR, category.getColor());
        return db.insert(TABLE_CUSTOM_CATEGORIES, null, values);
    }

    public void deleteCustomCategory(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CUSTOM_CATEGORIES, COLUMN_CAT_NAME + " = ?", new String[]{name});
    }

    public CustomCategory getCustomCategoryByName(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.query(TABLE_CUSTOM_CATEGORIES, null, COLUMN_CAT_NAME + " = ?",
                new String[]{name}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                CustomCategory cat = new CustomCategory();
                cat.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CAT_ID)));
                cat.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAT_NAME)));
                cat.setIconName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAT_ICON)));
                cat.setColor(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CAT_COLOR)));
                return cat;
            }
        }
        return null;
    }

    public List<CustomCategory> getAllCustomCategories() {
        List<CustomCategory> categories = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.query(TABLE_CUSTOM_CATEGORIES, null, null, null, null, null, COLUMN_CAT_NAME + " ASC")) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    CustomCategory cat = new CustomCategory();
                    cat.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CAT_ID)));
                    cat.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAT_NAME)));
                    cat.setIconName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAT_ICON)));
                    cat.setColor(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CAT_COLOR)));
                    categories.add(cat);
                } while (cursor.moveToNext());
            }
        }
        return categories;
    }

    public long insertSubTask(SubTask subTask) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ScheduleContract.SubTaskEntry.COLUMN_SCHEDULE_ID, subTask.getScheduleId());
        values.put(ScheduleContract.SubTaskEntry.COLUMN_TITLE, subTask.getTitle());
        values.put(ScheduleContract.SubTaskEntry.COLUMN_IS_COMPLETED, subTask.isCompleted() ? 1 : 0);
        return db.insert(ScheduleContract.SubTaskEntry.TABLE_NAME, null, values);
    }

    public int updateSubTask(SubTask subTask) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ScheduleContract.SubTaskEntry.COLUMN_TITLE, subTask.getTitle());
        values.put(ScheduleContract.SubTaskEntry.COLUMN_IS_COMPLETED, subTask.isCompleted() ? 1 : 0);
        return db.update(ScheduleContract.SubTaskEntry.TABLE_NAME, values, ScheduleContract.SubTaskEntry._ID + " = ?",
                new String[]{String.valueOf(subTask.getId())});
    }

    public void deleteSubTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(ScheduleContract.SubTaskEntry.TABLE_NAME, ScheduleContract.SubTaskEntry._ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    public List<SubTask> getSubTasksForSchedule(int scheduleId) {
        List<SubTask> subTasks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.query(ScheduleContract.SubTaskEntry.TABLE_NAME, null,
                ScheduleContract.SubTaskEntry.COLUMN_SCHEDULE_ID + " = ?",
                new String[]{String.valueOf(scheduleId)}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    SubTask st = new SubTask();
                    st.setId(cursor.getInt(cursor.getColumnIndexOrThrow(ScheduleContract.SubTaskEntry._ID)));
                    st.setScheduleId(cursor.getInt(cursor.getColumnIndexOrThrow(ScheduleContract.SubTaskEntry.COLUMN_SCHEDULE_ID)));
                    st.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(ScheduleContract.SubTaskEntry.COLUMN_TITLE)));
                    st.setCompleted(cursor.getInt(cursor.getColumnIndexOrThrow(ScheduleContract.SubTaskEntry.COLUMN_IS_COMPLETED)) == 1);
                    subTasks.add(st);
                } while (cursor.moveToNext());
            }
        }
        return subTasks;
    }

    private ContentValues scheduleToContentValues(Schedule schedule) {
        ContentValues values = new ContentValues();
        values.put(ScheduleEntry.COLUMN_TITLE, schedule.getTitle());
        values.put(ScheduleEntry.COLUMN_DESCRIPTION, schedule.getDescription());
        values.put(ScheduleEntry.COLUMN_DATE, schedule.getDate());
        values.put(ScheduleEntry.COLUMN_START_TIME, schedule.getStartTime());
        values.put(ScheduleEntry.COLUMN_END_TIME, schedule.getEndTime());
        values.put(ScheduleEntry.COLUMN_PRIORITY, schedule.getPriority().ordinal());
        values.put(ScheduleEntry.COLUMN_CATEGORY, schedule.getCategory().ordinal());
        values.put(ScheduleEntry.COLUMN_RECURRENCE, schedule.getRecurrence().ordinal()); 
        values.put(ScheduleEntry.COLUMN_IS_COMPLETED, schedule.isCompleted() ? 1 : 0);
        values.put(ScheduleEntry.COLUMN_REMINDER_MINUTES, schedule.getReminderMinutes());
        values.put(ScheduleEntry.COLUMN_CREATED_AT, schedule.getCreatedAt());
        values.put(ScheduleEntry.COLUMN_CUSTOM_CATEGORY, schedule.getCustomCategory());
        values.put(ScheduleEntry.COLUMN_ATTACHMENT_PATH, schedule.getAttachmentPath());
        return values;
    }

    private Schedule cursorToSchedule(Cursor cursor) {
        Schedule schedule = new Schedule();
        schedule.setId(cursor.getInt(cursor.getColumnIndexOrThrow(ScheduleEntry._ID)));
        schedule.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(ScheduleEntry.COLUMN_TITLE)));
        schedule.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(ScheduleEntry.COLUMN_DESCRIPTION)));
        schedule.setDate(cursor.getString(cursor.getColumnIndexOrThrow(ScheduleEntry.COLUMN_DATE)));
        schedule.setStartTime(cursor.getString(cursor.getColumnIndexOrThrow(ScheduleEntry.COLUMN_START_TIME)));
        schedule.setEndTime(cursor.getString(cursor.getColumnIndexOrThrow(ScheduleEntry.COLUMN_END_TIME)));
        schedule.setPriority(Priority.values()[cursor.getInt(cursor.getColumnIndexOrThrow(ScheduleEntry.COLUMN_PRIORITY))]);
        schedule.setCategory(Category.fromInt(cursor.getInt(cursor.getColumnIndexOrThrow(ScheduleEntry.COLUMN_CATEGORY))));
        schedule.setRecurrence(Recurrence.values()[cursor.getInt(cursor.getColumnIndexOrThrow(ScheduleEntry.COLUMN_RECURRENCE))]);
        schedule.setCompleted(cursor.getInt(cursor.getColumnIndexOrThrow(ScheduleEntry.COLUMN_IS_COMPLETED)) == 1);
        schedule.setReminderMinutes(cursor.getInt(cursor.getColumnIndexOrThrow(ScheduleEntry.COLUMN_REMINDER_MINUTES)));
        schedule.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(ScheduleEntry.COLUMN_CREATED_AT)));
        schedule.setCustomCategory(cursor.getString(cursor.getColumnIndexOrThrow(ScheduleEntry.COLUMN_CUSTOM_CATEGORY)));
        schedule.setAttachmentPath(cursor.getString(cursor.getColumnIndexOrThrow(ScheduleEntry.COLUMN_ATTACHMENT_PATH)));
        return schedule;
    }
}
