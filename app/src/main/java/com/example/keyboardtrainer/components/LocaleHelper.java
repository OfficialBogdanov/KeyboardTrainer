package com.example.keyboardtrainer.components;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import java.util.Locale;

public class LocaleHelper {
    // Constant
    private static final String SELECTED_LANGUAGE = "Locale.Helper.Selected.Language";

    // Context
    public static Context onAttach(Context context) {
        String lang = getPersistedData(context, Locale.getDefault().getLanguage());
        return setLocale(context, lang, false);
    }

    // Language
    public static void setLocale(Context context, String language) {
        setLocale(context, language, true);
    }

    public static Context setLocale(Context context, String language, boolean forceUpdate) {
        Locale current = getCurrentLocale(context);
        Locale newLocale = new Locale(language);

        if (!current.equals(newLocale) || forceUpdate) {
            persist(context, language);
            return updateResources(context, language);
        }
        return context;
    }

    // Locale
    private static Locale getCurrentLocale(Context context) {
        return context.getResources().getConfiguration().getLocales().get(0);
    }

    public static String getLanguage(Context context) {
        return getPersistedData(context, Locale.getDefault().getLanguage());
    }

    /** @noinspection deprecation*/ // Preferences
    private static String getPersistedData(Context context, String defaultLanguage) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(SELECTED_LANGUAGE, defaultLanguage);
    }

    /** @noinspection deprecation*/
    private static void persist(Context context, String language) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SELECTED_LANGUAGE, language);
        editor.apply();
    }

    // Resources
    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();

        configuration.setLocale(locale);

        context = context.createConfigurationContext(configuration);

        return context;
    }
}