package com.example.schedulemanager.database;

import android.net.Uri;
import android.provider.BaseColumns;

public final class ScheduleContract {

    public static final String CONTENT_AUTHORITY = "com.example.schedulemanager.provider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_SCHEDULES = "schedules";

    private ScheduleContract() {}

    public static final class ScheduleEntry implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_SCHEDULES);

        public static final String TABLE_NAME = "schedules";

        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_START_TIME = "start_time";
        public static final String COLUMN_END_TIME = "end_time";
        public static final String COLUMN_PRIORITY = "priority";
        public static final String COLUMN_CATEGORY = "category";
        public static final String COLUMN_RECURRENCE = "recurrence";
        public static final String COLUMN_IS_COMPLETED = "is_completed";
        public static final String COLUMN_REMINDER_MINUTES = "reminder_minutes";
        public static final String COLUMN_CREATED_AT = "created_at";
    }
}
