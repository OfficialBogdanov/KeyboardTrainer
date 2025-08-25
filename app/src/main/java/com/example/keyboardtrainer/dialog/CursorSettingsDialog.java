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

public class CursorSettingsDialog extends BottomSheetDialogFragment {
    // UI
    private LinearLayout underlineCursorOption, lineCursorOption, blockCursorOption;

    // Constants
    private static final String PREFS_NAME = "KeyboardTrainerPrefs";
    private static final String CURSOR_TYPE_KEY = "cursorType";

    // Lifecycle
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_cursor_settings, container, false);

        initializeViews(view);
        setupCursorOptions();

        return view;
    }

    // UI
    private void initializeViews(View view) {
        underlineCursorOption = view.findViewById(R.id.underlineCursorOption);
        lineCursorOption = view.findViewById(R.id.lineCursorOption);
        blockCursorOption = view.findViewById(R.id.blockCursorOption);
    }

    private void setupCursorOptions() {
        int currentCursorType = getCurrentCursorType();
        updateSelection(currentCursorType);
        setupClickListeners();
    }

    // Preferences
    private int getCurrentCursorType() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getInt(CURSOR_TYPE_KEY, 0);
    }

    // Listeners
    private void setupClickListeners() {
        underlineCursorOption.setOnClickListener(v -> {
            selectCursorType(0);
            updateSelection(0);
        });

        lineCursorOption.setOnClickListener(v -> {
            selectCursorType(1);
            updateSelection(1);
        });

        blockCursorOption.setOnClickListener(v -> {
            selectCursorType(2);
            updateSelection(2);
        });
    }

    private void selectCursorType(int cursorType) {
        SharedPreferences.Editor editor = requireActivity().getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putInt(CURSOR_TYPE_KEY, cursorType);
        editor.apply();
        dismiss();
    }

    // Feedback
    private void updateSelection(int selectedCursorType) {
        resetAllBackgrounds();
        highlightSelectedCursor(selectedCursorType);
    }

    private void resetAllBackgrounds() {
        int unselectedBg = R.drawable.section_background;
        underlineCursorOption.setBackgroundResource(unselectedBg);
        lineCursorOption.setBackgroundResource(unselectedBg);
        blockCursorOption.setBackgroundResource(unselectedBg);
    }

    private void highlightSelectedCursor(int selectedCursorType) {
        int selectedBg = R.drawable.selected_background;
        switch (selectedCursorType) {
            case 1:
                lineCursorOption.setBackgroundResource(selectedBg);
                break;
            case 2:
                blockCursorOption.setBackgroundResource(selectedBg);
                break;
            default:
                underlineCursorOption.setBackgroundResource(selectedBg);
        }
    }
}