package com.example.keyboardtrainer.dialog;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.keyboardtrainer.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ThemeSettingsDialog extends BottomSheetDialogFragment {
    // UI
    private LinearLayout lightThemeOption, darkThemeOption, systemThemeOption;

    // Constant
    private static final String PREFS_NAME = "KeyboardTrainerPrefs";
    private static final String THEME_MODE_KEY = "themeMode";

    // Lifecycle
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_theme_settings, container, false);

        initializeViews(view);
        setupThemeOptions();

        return view;
    }

    // Initialization
    private void initializeViews(View view) {
        lightThemeOption = view.findViewById(R.id.lightThemeOption);
        darkThemeOption = view.findViewById(R.id.darkThemeOption);
        systemThemeOption = view.findViewById(R.id.systemThemeOption);
    }

    private void setupThemeOptions() {
        int currentTheme = getCurrentTheme();
        updateSelection(currentTheme);
        setupClickListeners();
    }

    // Preferences
    private int getCurrentTheme() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getInt(THEME_MODE_KEY, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    // Listeners
    private void setupClickListeners() {
        lightThemeOption.setOnClickListener(v -> {
            applyTheme(AppCompatDelegate.MODE_NIGHT_NO);
            updateSelection(AppCompatDelegate.MODE_NIGHT_NO);
        });

        darkThemeOption.setOnClickListener(v -> {
            applyTheme(AppCompatDelegate.MODE_NIGHT_YES);
            updateSelection(AppCompatDelegate.MODE_NIGHT_YES);
        });

        systemThemeOption.setOnClickListener(v -> {
            applyTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            updateSelection(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        });
    }

    // Theme
    private void applyTheme(int themeMode) {
        saveThemePreference(themeMode);
        setAppTheme(themeMode);
        dismiss();
    }

    private void saveThemePreference(int themeMode) {
        SharedPreferences.Editor editor = requireActivity().getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putInt(THEME_MODE_KEY, themeMode);
        editor.apply();
    }

    private void setAppTheme(int themeMode) {
        AppCompatDelegate.setDefaultNightMode(themeMode);
    }

    // Feedback
    private void updateSelection(int selectedTheme) {
        resetAllBackgrounds();
        highlightSelectedTheme(selectedTheme);
    }

    private void resetAllBackgrounds() {
        lightThemeOption.setBackgroundResource(R.drawable.section_background);
        darkThemeOption.setBackgroundResource(R.drawable.section_background);
        systemThemeOption.setBackgroundResource(R.drawable.section_background);
    }

    private void highlightSelectedTheme(int selectedTheme) {
        switch (selectedTheme) {
            case AppCompatDelegate.MODE_NIGHT_NO:
                lightThemeOption.setBackgroundResource(R.drawable.selected_background);
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                darkThemeOption.setBackgroundResource(R.drawable.selected_background);
                break;
            default:
                systemThemeOption.setBackgroundResource(R.drawable.selected_background);
        }
    }
}