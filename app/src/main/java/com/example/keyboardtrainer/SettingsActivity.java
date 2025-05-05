package com.example.keyboardtrainer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "KeyboardTrainerPrefs";
    private static final String CURSOR_TYPE_KEY = "cursorType";
    private static final String THEME_MODE_KEY = "themeMode";

    private FirebaseAuth mAuth;
    private RadioGroup themeRadioGroup;
    private RadioGroup cursorRadioGroup;
    private static final String TIMER_STYLE_KEY = "timerStyle";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        themeRadioGroup = findViewById(R.id.themeRadioGroup);
        cursorRadioGroup = findViewById(R.id.cursorRadioGroup);

        setupBottomNavigation();
        loadSettings();
        setupThemeSelector();
        setupCursorSelector();
        setupTimerStyleSelector();

        findViewById(R.id.logoutButton).setOnClickListener(v -> logoutUser());
        findViewById(R.id.deleteAccountButton).setOnClickListener(v -> showDeleteAccountDialog());
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.navigation_settings);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_game) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            } else if (id == R.id.navigation_stats) {
                startActivity(new Intent(this, StatsActivity.class));
                finish();
                return true;
            }
            return id == R.id.navigation_settings;
        });
    }

    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        int themeMode = prefs.getInt(THEME_MODE_KEY, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        switch (themeMode) {
            case AppCompatDelegate.MODE_NIGHT_NO:
                ((RadioButton) findViewById(R.id.lightThemeRadio)).setChecked(true);
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                ((RadioButton) findViewById(R.id.darkThemeRadio)).setChecked(true);
                break;
            default:
                ((RadioButton) findViewById(R.id.systemThemeRadio)).setChecked(true);
        }

        int cursorType = prefs.getInt(CURSOR_TYPE_KEY, 0);
        switch (cursorType) {
            case 1:
                ((RadioButton) findViewById(R.id.lineCursorRadio)).setChecked(true);
                break;
            case 2:
                ((RadioButton) findViewById(R.id.blockCursorRadio)).setChecked(true);
                break;
            default:
                ((RadioButton) findViewById(R.id.underlineCursorRadio)).setChecked(true);
        }

        int timerStyle = prefs.getInt(TIMER_STYLE_KEY, 0);
        switch (timerStyle) {
            case 1:
                ((RadioButton) findViewById(R.id.progressTimerRadio)).setChecked(true);
                break;
            case 2:
                ((RadioButton) findViewById(R.id.combinedTimerRadio)).setChecked(true);
                break;
            default:
                ((RadioButton) findViewById(R.id.digitalTimerRadio)).setChecked(true);
        }
    }

    private void setupTimerStyleSelector() {
        RadioGroup timerStyleRadioGroup = findViewById(R.id.timerStyleRadioGroup);
        timerStyleRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
            int timerStyle;

            if (checkedId == R.id.progressTimerRadio) {
                timerStyle = 1;
            } else if (checkedId == R.id.combinedTimerRadio) {
                timerStyle = 2;
            } else {
                timerStyle = 0;
            }

            editor.putInt(TIMER_STYLE_KEY, timerStyle);
            editor.apply();
            Toast.makeText(this, "Стиль таймера изменен", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupThemeSelector() {
        themeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
            int themeMode;

            if (checkedId == R.id.lightThemeRadio) {
                themeMode = AppCompatDelegate.MODE_NIGHT_NO;
            } else if (checkedId == R.id.darkThemeRadio) {
                themeMode = AppCompatDelegate.MODE_NIGHT_YES;
            } else {
                themeMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            }

            editor.putInt(THEME_MODE_KEY, themeMode);
            editor.apply();
            AppCompatDelegate.setDefaultNightMode(themeMode);
        });
    }

    private void setupCursorSelector() {
        cursorRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
            int cursorType;

            if (checkedId == R.id.lineCursorRadio) {
                cursorType = 1;
            } else if (checkedId == R.id.blockCursorRadio) {
                cursorType = 2;
            } else {
                cursorType = 0;
            }

            editor.putInt(CURSOR_TYPE_KEY, cursorType);
            editor.apply();
            Toast.makeText(this, "Настройки курсора сохранены", Toast.LENGTH_SHORT).show();
        });
    }

    private void logoutUser() {
        mAuth.signOut();
        startActivity(new Intent(this, LoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
    }

    private void showDeleteAccountDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirm_action, null);

        TextView title = dialogView.findViewById(R.id.dialog_title);
        TextView message = dialogView.findViewById(R.id.dialog_message);
        MaterialButton cancelBtn = dialogView.findViewById(R.id.btn_cancel);
        MaterialButton confirmBtn = dialogView.findViewById(R.id.btn_confirm);

        title.setText("Удаление аккаунта");
        message.setText("Вы уверены, что хотите удалить свой аккаунт? Все данные будут безвозвратно утеряны.");
        confirmBtn.setText("Удалить");

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        cancelBtn.setOnClickListener(v -> dialog.dismiss());
        confirmBtn.setOnClickListener(v -> {
            dialog.dismiss();
            deleteAccount();
        });

        dialog.show();
    }

    private void deleteAccount() {
        if (mAuth.getCurrentUser() != null) {
            mAuth.getCurrentUser().delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Аккаунт удален", Toast.LENGTH_SHORT).show();
                            logoutUser();
                        } else {
                            Toast.makeText(this, "Ошибка: " + Objects.requireNonNull(task.getException()).getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}