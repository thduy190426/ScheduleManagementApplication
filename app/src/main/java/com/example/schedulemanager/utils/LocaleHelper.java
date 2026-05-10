package com.example.schedulemanager.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import java.util.Locale;

public class LocaleHelper {

    public static Context onAttach(Context context) {
        String lang = getPersistedData(context);
        return setLocale(context, lang);
    }

    public static Context setLocale(Context context, String language) {
        persist(context, language);
        return updateResources(context, language);
    }

    private static String getPersistedData(Context context) {
        PreferenceManager prefManager = new PreferenceManager(context);
        return prefManager.getLanguage();
    }

    private static void persist(Context context, String language) {
        PreferenceManager prefManager = new PreferenceManager(context);
        prefManager.setLanguage(language);
    }

    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
            LocaleList localeList = new LocaleList(locale);
            LocaleList.setDefault(localeList);
            config.setLocales(localeList);
        } else {
            config.setLocale(locale);
        }

        res.updateConfiguration(config, res.getDisplayMetrics());

        return context.createConfigurationContext(config);
    }
}
