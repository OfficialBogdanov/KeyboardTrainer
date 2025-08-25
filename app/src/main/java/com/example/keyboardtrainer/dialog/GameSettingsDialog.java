package com.example.keyboardtrainer.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.example.keyboardtrainer.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class GameSettingsDialog extends BottomSheetDialogFragment {
    // Lifecycle
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        configureDialogBehavior(dialog);

        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_game_settings, container, false);

        initializeViews(view);

        return view;
    }

    // Dialog
    private void configureDialogBehavior(BottomSheetDialog dialog) {
        dialog.setCanceledOnTouchOutside(true);
    }

    // UI
    private void initializeViews(View view) {
        View difficultySettings = view.findViewById(R.id.difficultySettingsButton);
        View cursorSettings = view.findViewById(R.id.cursorSettingsButton);
        View timerSettings = view.findViewById(R.id.timerSettingsButton);

        setupClickListeners(difficultySettings, cursorSettings, timerSettings);
    }

    private void setupClickListeners(View difficultySettings, View cursorSettings, View timerSettings) {
        difficultySettings.setOnClickListener(v -> openDifficultySettings());
        cursorSettings.setOnClickListener(v -> openCursorSettings());
        timerSettings.setOnClickListener(v -> openTimerSettings());
    }

    // Navigation
    private void openDifficultySettings() {
        DifficultySettingsDialog dialog = new DifficultySettingsDialog();
        dialog.show(getParentFragmentManager(), "DifficultySettingsDialog");
    }

    private void openCursorSettings() {
        CursorSettingsDialog dialog = new CursorSettingsDialog();
        dialog.show(getParentFragmentManager(), "CursorSettingsDialog");
    }

    private void openTimerSettings() {
        TimerSettingsDialog dialog = new TimerSettingsDialog();
        dialog.show(getParentFragmentManager(), "TimerSettingsDialog");
    }
}