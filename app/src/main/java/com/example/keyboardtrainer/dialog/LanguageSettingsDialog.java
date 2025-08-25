package com.example.keyboardtrainer.dialog;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.keyboardtrainer.components.LocaleHelper;
import com.example.keyboardtrainer.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class LanguageSettingsDialog extends BottomSheetDialogFragment {
    // UI
    private LinearLayout russianOption, englishOption;

    // Constants
    private static final String PREFS_NAME = "KeyboardTrainerPrefs";
    private static final String LANGUAGE_KEY = "appLanguage";

    // Lifecycle
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_language_settings, container, false);

        initializeViews(view);
        setupLanguageOptions();

        return view;
    }

    // Initialization
    private void initializeViews(View view) {
        russianOption = view.findViewById(R.id.russianLanguageOption);
        englishOption = view.findViewById(R.id.englishLanguageOption);
    }

    private void setupLanguageOptions() {
        String currentLanguage = LocaleHelper.getLanguage(requireContext());
        updateSelection(currentLanguage);
        setupClickListeners();
    }

    // Listeners
    private void setupClickListeners() {
        russianOption.setOnClickListener(v -> {
            setLanguage("ru");
            updateSelection("ru");
        });

        englishOption.setOnClickListener(v -> {
            setLanguage("en");
            updateSelection("en");
        });
    }

    // Language
    private void setLanguage(String language) {
        saveLanguagePreference(language);
        applyLocaleChange(language);
        dismiss();
        restartActivity();
    }

    private void saveLanguagePreference(String language) {
        SharedPreferences.Editor editor = requireActivity().getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(LANGUAGE_KEY, language);
        editor.apply();
    }

    private void applyLocaleChange(String language) {
        LocaleHelper.setLocale(requireContext(), language);
    }

    private void restartActivity() {
        requireActivity().recreate();
    }

    // Feedback
    private void updateSelection(String selectedLanguage) {
        resetAllBackgrounds();
        highlightSelectedLanguage(selectedLanguage);
    }

    private void resetAllBackgrounds() {
        russianOption.setBackgroundResource(R.drawable.section_background);
        englishOption.setBackgroundResource(R.drawable.section_background);
    }

    private void highlightSelectedLanguage(String selectedLanguage) {
        if ("ru".equals(selectedLanguage)) {
            russianOption.setBackgroundResource(R.drawable.selected_background);
        } else {
            englishOption.setBackgroundResource(R.drawable.selected_background);
        }
    }
}