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
        String reminder = sharedPreferences.getString(KEY_DEFAULT_REMINDER, "0");
        return Integer.parseInt(reminder);
    }

    public void setThemeMode(int mode) {
        sharedPreferences.edit().putInt(KEY_THEME_MODE, mode).apply();
    }

    public int getThemeMode() {
        String theme = sharedPreferences.getString(KEY_THEME_MODE, "-1");
        return Integer.parseInt(theme);
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
}
