package com.example.schedulemanager.fragments;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.example.schedulemanager.R;
import com.example.schedulemanager.activities.PinLockActivity;
import com.example.schedulemanager.utils.PreferenceManager;

import java.util.Locale;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        setupTimePreference("dnd_start_time");
        setupTimePreference("dnd_end_time");

        Preference changePinPref = findPreference("change_pin");
        if (changePinPref != null) {
            changePinPref.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(getActivity(), PinLockActivity.class);
                intent.putExtra(PinLockActivity.EXTRA_MODE, PinLockActivity.MODE_SETUP);
                if (getActivity() != null) {
                    getActivity().startActivity(intent);
                }
                return true;
            });
        }

        ListPreference timezonePref = findPreference("timezone");
        if (timezonePref != null) {
            String[] ids = java.util.TimeZone.getAvailableIDs();
            timezonePref.setEntries(ids);
            timezonePref.setEntryValues(ids);
            if (timezonePref.getValue() == null) {
                timezonePref.setValue(java.util.TimeZone.getDefault().getID());
            }
        }

        Preference manageCategoriesPref = findPreference("manage_categories");
        if (manageCategoriesPref != null) {
            manageCategoriesPref.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(getActivity(), com.example.schedulemanager.activities.CategoryManagementActivity.class);
                if (getActivity() != null) {
                    getActivity().startActivity(intent);
                }
                return true;
            });
        }
    }

    private void setupTimePreference(String key) {
        Preference preference = findPreference(key);
        if (preference != null) {
            SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
            String currentTime = prefs.getString(key, key.contains("start") ? "22:00" : "07:00");
            preference.setSummary(currentTime);

            preference.setOnPreferenceClickListener(pref -> {
                String time = prefs.getString(key, key.contains("start") ? "22:00" : "07:00");
                String[] parts = time.split(":");
                int hour = Integer.parseInt(parts[0]);
                int minute = Integer.parseInt(parts[1]);

                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (view, hourOfDay, minuteOfHour) -> {
                    String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
                    prefs.edit().putString(key, selectedTime).apply();
                    preference.setSummary(selectedTime);
                }, hour, minute, true);
                timePickerDialog.show();
                return true;
            });
        }
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
            String languageCode = sharedPreferences.getString(key, "en");
            
            new PreferenceManager(requireContext()).setLanguage(languageCode);

            androidx.core.os.LocaleListCompat appLocales = androidx.core.os.LocaleListCompat.forLanguageTags(languageCode);
            androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(appLocales);
            
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), com.example.schedulemanager.activities.MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                getActivity().finish();
            }
        } else if ("pin_lock_enabled".equals(key)) {
            boolean enabled = sharedPreferences.getBoolean(key, false);
            if (enabled) {
                String savedPin = new PreferenceManager(requireContext()).getAppPin();
                if (savedPin.isEmpty()) {
                    // Force setup PIN if enabling for the first time
                    Intent intent = new Intent(getActivity(), PinLockActivity.class);
                    intent.putExtra(PinLockActivity.EXTRA_MODE, PinLockActivity.MODE_SETUP);
                    startActivity(intent);
                }
            }
        }
    }
}
