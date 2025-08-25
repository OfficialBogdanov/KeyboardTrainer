package com.example.keyboardtrainer.dialog;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.keyboardtrainer.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class TimerSettingsDialog extends BottomSheetDialogFragment {
    // UI
    private LinearLayout digitalTimerOption, progressTimerOption, combinedTimerOption;

    // Constants
    private static final String PREFS_NAME = "KeyboardTrainerPrefs";
    private static final String TIMER_STYLE_KEY = "timerStyle";

    // Lifecycle
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_timer_settings, container, false);

        // Initialization
        digitalTimerOption = view.findViewById(R.id.digitalTimerOption);
        progressTimerOption = view.findViewById(R.id.progressTimerOption);
        combinedTimerOption = view.findViewById(R.id.combinedTimerOption);

        // Settings
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int currentTimerStyle = prefs.getInt(TIMER_STYLE_KEY, 0);
        updateSelection(currentTimerStyle);

        // Listeners
        digitalTimerOption.setOnClickListener(v -> {
            selectTimerStyle(0);
            updateSelection(0);
        });

        progressTimerOption.setOnClickListener(v -> {
            selectTimerStyle(1);
            updateSelection(1);
        });

        combinedTimerOption.setOnClickListener(v -> {
            selectTimerStyle(2);
            updateSelection(2);
        });

        return view;
    }

    // Preference
    private void selectTimerStyle(int timerStyle) {
        SharedPreferences.Editor editor = requireActivity().getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putInt(TIMER_STYLE_KEY, timerStyle);
        editor.apply();
        dismiss();
    }

    // Feedback
    private void updateSelection(int selectedTimerStyle) {
        digitalTimerOption.setBackgroundResource(R.drawable.section_background);
        progressTimerOption.setBackgroundResource(R.drawable.section_background);
        combinedTimerOption.setBackgroundResource(R.drawable.section_background);

        switch (selectedTimerStyle) {
            case 1:
                progressTimerOption.setBackgroundResource(R.drawable.selected_background);
                break;
            case 2:
                combinedTimerOption.setBackgroundResource(R.drawable.selected_background);
                break;
            default:
                digitalTimerOption.setBackgroundResource(R.drawable.selected_background);
        }
    }
}