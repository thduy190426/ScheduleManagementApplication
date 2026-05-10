package com.example.schedulemanager;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.schedulemanager.utils.NotificationHelper;
import com.example.schedulemanager.utils.PreferenceManager;

import android.content.Context;
import com.example.schedulemanager.utils.LocaleHelper;
import com.jakewharton.threetenabp.AndroidThreeTen;

public class ScheduleManagerApp extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidThreeTen.init(this);
        NotificationHelper.createNotificationChannel(this);
        applyTheme();
    }

    private void applyTheme() {
        PreferenceManager prefManager = new PreferenceManager(this);
        int themeMode = prefManager.getThemeMode();
        if (themeMode != -1) {
            AppCompatDelegate.setDefaultNightMode(themeMode);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }
}
