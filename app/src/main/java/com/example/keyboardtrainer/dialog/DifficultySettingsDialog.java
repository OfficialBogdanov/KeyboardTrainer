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

public class DifficultySettingsDialog extends BottomSheetDialogFragment {
    // UI
    private LinearLayout easyOption, normalOption, hardOption;

    // Constants
    private static final String PREFS_NAME = "KeyboardTrainerPrefs";
    private static final String DIFFICULTY_LEVEL_KEY = "difficultyLevel";

    public static final int DIFFICULTY_EASY = 0;
    public static final int DIFFICULTY_NORMAL = 1;
    public static final int DIFFICULTY_HARD = 2;

    // Lifecycle
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_difficulty_settings, container, false);

        initializeViews(view);
        setupDifficultyOptions();

        return view;
    }

    // UI
    private void initializeViews(View view) {
        easyOption = view.findViewById(R.id.easyDifficulty);
        normalOption = view.findViewById(R.id.normalDifficulty);
        hardOption = view.findViewById(R.id.hardDifficulty);
    }

    private void setupDifficultyOptions() {
        int currentDifficulty = getCurrentDifficulty();
        updateSelection(currentDifficulty);
        setupClickListeners();
    }

    // Preferences
    private int getCurrentDifficulty() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getInt(DIFFICULTY_LEVEL_KEY, DIFFICULTY_NORMAL);
    }

    // Listeners
    private void setupClickListeners() {
        easyOption.setOnClickListener(v -> {
            selectDifficulty(DIFFICULTY_EASY);
            updateSelection(DIFFICULTY_EASY);
        });

        normalOption.setOnClickListener(v -> {
            selectDifficulty(DIFFICULTY_NORMAL);
            updateSelection(DIFFICULTY_NORMAL);
        });

        hardOption.setOnClickListener(v -> {
            selectDifficulty(DIFFICULTY_HARD);
            updateSelection(DIFFICULTY_HARD);
        });
    }

    private void selectDifficulty(int difficultyLevel) {
        SharedPreferences.Editor editor = requireActivity().getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putInt(DIFFICULTY_LEVEL_KEY, difficultyLevel);
        editor.apply();
        dismiss();
    }

    // Feedback
    private void updateSelection(int selectedDifficulty) {
        resetAllBackgrounds();
        highlightSelectedDifficulty(selectedDifficulty);
    }

    private void resetAllBackgrounds() {
        int unselectedBg = R.drawable.section_background;
        easyOption.setBackgroundResource(unselectedBg);
        normalOption.setBackgroundResource(unselectedBg);
        hardOption.setBackgroundResource(unselectedBg);
    }

    private void highlightSelectedDifficulty(int selectedDifficulty) {
        int selectedBg = R.drawable.selected_background;
        switch (selectedDifficulty) {
            case DIFFICULTY_NORMAL:
                normalOption.setBackgroundResource(selectedBg);
                break;
            case DIFFICULTY_HARD:
                hardOption.setBackgroundResource(selectedBg);
                break;
            default:
                easyOption.setBackgroundResource(selectedBg);
        }
    }
}