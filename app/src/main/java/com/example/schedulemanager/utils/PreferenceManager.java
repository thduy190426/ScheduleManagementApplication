package com.example.schedulemanager.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_DEFAULT_REMINDER = "default_reminder";
    private static final String KEY_THEME_MODE = "theme_mode";
    private static final String KEY_IS_FIRST_LAUNCH = "is_first_launch";
    private static final String KEY_LAST_OPENED_DATE = "last_opened_date";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_REMINDER_SOUND = "reminder_sound";
    private static final String KEY_DND_ENABLED = "dnd_enabled";
    private static final String KEY_DND_START_TIME = "dnd_start_time";
    private static final String KEY_DND_END_TIME = "dnd_end_time";
    private static final String KEY_PIN_LOCK_ENABLED = "pin_lock_enabled";
    private static final String KEY_APP_PIN = "app_pin";
    private static final String KEY_DEFAULT_SORT = "default_sort_order";

    private final SharedPreferences sharedPreferences;

    public PreferenceManager(Context context) {
        sharedPreferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void setLanguage(String lang) {
        sharedPreferences.edit().putString(KEY_LANGUAGE, lang).apply();
    }

    public String getLanguage() {
        return sharedPreferences.getString(KEY_LANGUAGE, "en");
    }

    public void setUserName(String name) {
        sharedPreferences.edit().putString(KEY_USER_NAME, name).apply();
    }

    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, "");
    }

    public void setDefaultReminder(int minutes) {
        sharedPreferences.edit().putInt(KEY_DEFAULT_REMINDER, minutes).apply();
    }

    public int getDefaultReminder() {
        Object val = sharedPreferences.getAll().get(KEY_DEFAULT_REMINDER);
        if (val instanceof Integer) return (Integer) val;
        if (val instanceof String) return Integer.parseInt((String) val);
        return 0;
    }

    public void setThemeMode(int mode) {
        sharedPreferences.edit().putInt(KEY_THEME_MODE, mode).apply();
    }

    public int getThemeMode() {
        Object val = sharedPreferences.getAll().get(KEY_THEME_MODE);
        if (val instanceof Integer) return (Integer) val;
        if (val instanceof String) return Integer.parseInt((String) val);
        return -1;
    }

    public void setFirstLaunch(boolean isFirst) {
        sharedPreferences.edit().putBoolean(KEY_IS_FIRST_LAUNCH, isFirst).apply();
    }

    public boolean isFirstLaunch() {
        return sharedPreferences.getBoolean(KEY_IS_FIRST_LAUNCH, true);
    }

    public void setLastOpenedDate(String date) {
        sharedPreferences.edit().putString(KEY_LAST_OPENED_DATE, date).apply();
    }

    public String getLastOpenedDate() {
        return sharedPreferences.getString(KEY_LAST_OPENED_DATE, "");
    }

    public boolean isReminderSoundEnabled() {
        return sharedPreferences.getBoolean(KEY_REMINDER_SOUND, true);
    }

    public void setReminderSoundEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_REMINDER_SOUND, enabled).apply();
    }

    public boolean isDndEnabled() {
        return sharedPreferences.getBoolean(KEY_DND_ENABLED, false);
    }

    public String getDndStartTime() {
        return sharedPreferences.getString(KEY_DND_START_TIME, "22:00");
    }

    public String getDndEndTime() {
        return sharedPreferences.getString(KEY_DND_END_TIME, "07:00");
    }

    public void setDndStartTime(String time) {
        sharedPreferences.edit().putString(KEY_DND_START_TIME, time).apply();
    }

    public void setDndEndTime(String time) {
        sharedPreferences.edit().putString(KEY_DND_END_TIME, time).apply();
    }

    public boolean isPinLockEnabled() {
        return sharedPreferences.getBoolean(KEY_PIN_LOCK_ENABLED, false);
    }

    public void setPinLockEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_PIN_LOCK_ENABLED, enabled).apply();
    }

    public String getAppPin() {
        return sharedPreferences.getString(KEY_APP_PIN, "");
    }

    public void setAppPin(String pin) {
        sharedPreferences.edit().putString(KEY_APP_PIN, pin).apply();
    }

    public void setTimezone(String timezoneId) {
        sharedPreferences.edit().putString("timezone", timezoneId).apply();
    }

    public String getTimezone() {
        return sharedPreferences.getString("timezone", java.util.TimeZone.getDefault().getID());
    }

    public void setDefaultSortOrder(String sortOrder) {
        sharedPreferences.edit().putString(KEY_DEFAULT_SORT, sortOrder).apply();
    }

    public String getDefaultSortOrder() {
        return sharedPreferences.getString(KEY_DEFAULT_SORT, "smart");
    }
}
