package com.example.schedulemanager.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.schedulemanager.R;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if ("theme_mode".equals(key)) {
            String themeValue = sharedPreferences.getString(key, "-1");
            int mode = Integer.parseInt(themeValue);
            AppCompatDelegate.setDefaultNightMode(mode);
        } else if ("language".equals(key)) {
            String languageCode = sharedPreferences.getString(key, "vi");
            LocaleListCompat appLocales = LocaleListCompat.forLanguageTags(languageCode);
            AppCompatDelegate.setApplicationLocales(appLocales);
        }
    }
}
