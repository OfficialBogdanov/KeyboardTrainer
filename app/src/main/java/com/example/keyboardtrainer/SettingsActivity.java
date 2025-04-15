package com.example.keyboardtrainer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import yuku.ambilwarna.AmbilWarnaDialog;

public class SettingsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private RadioGroup themeRadioGroup;
    private RadioButton lightThemeRadio;
    private RadioButton darkThemeRadio;
    private RadioButton systemThemeRadio;
    private static final int COLOR_PICKER_REQUEST = 1;
    private int selectedColor;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();

        Button logoutButton = findViewById(R.id.logoutButton);
        Button deleteAccountButton = findViewById(R.id.deleteAccountButton);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        themeRadioGroup = findViewById(R.id.themeRadioGroup);
        lightThemeRadio = findViewById(R.id.lightThemeRadio);
        darkThemeRadio = findViewById(R.id.darkThemeRadio);
        systemThemeRadio = findViewById(R.id.systemThemeRadio);

        setupBottomNavigation(bottomNav);
        setupThemeSelector();

        logoutButton.setOnClickListener(v -> logoutUser());
        deleteAccountButton.setOnClickListener(v -> showDeleteAccountDialog());

        prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        selectedColor = prefs.getInt("theme_color", ContextCompat.getColor(this, R.color.purple_500));

        setupColorPicker();
    }

    private void setupColorPicker() {
        Button colorPickerButton = findViewById(R.id.colorPickerButton);
        View colorPreview = findViewById(R.id.colorPreview);

        colorPreview.setBackgroundColor(selectedColor);

        colorPickerButton.setOnClickListener(v -> {
            new AmbilWarnaDialog(this, selectedColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                @Override
                public void onOk(AmbilWarnaDialog dialog, int color) {
                    selectedColor = color;
                    colorPreview.setBackgroundColor(color);
                    saveColor(color);
                    applyThemeColor();
                }

                @Override
                public void onCancel(AmbilWarnaDialog dialog) {
                }
            }).show();
        });
    }

    private void saveColor(int color) {
        prefs.edit().putInt("theme_color", color).apply();
    }

    private void applyThemeColor() {
        // Пример изменения цвета BottomNavigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setBackgroundColor(selectedColor);

        // Здесь можно добавить другие элементы интерфейса
        Toast.makeText(this, "Цвет применен", Toast.LENGTH_SHORT).show();
    }

    private void setupBottomNavigation(BottomNavigationView bottomNav) {
        bottomNav.setSelectedItemId(R.id.navigation_settings);

        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.navigation_game) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            } else if (id == R.id.navigation_stats) {
                startActivity(new Intent(this, StatsActivity.class));
                finish();
                return true;
            } else if (id == R.id.navigation_settings) {
                return true;
            }
            return false;
        });
    }

    private void setupThemeSelector() {
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        switch (currentMode) {
            case AppCompatDelegate.MODE_NIGHT_NO:
                lightThemeRadio.setChecked(true);
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                darkThemeRadio.setChecked(true);
                break;
            case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:
            default:
                systemThemeRadio.setChecked(true);
                break;
        }

        themeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.lightThemeRadio) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else if (checkedId == R.id.darkThemeRadio) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else if (checkedId == R.id.systemThemeRadio) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            }
        });
    }

    private void logoutUser() {
        mAuth.signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Удаление аккаунта")
                .setMessage("Вы уверены, что хотите удалить свой аккаунт? Это действие нельзя отменить!")
                .setPositiveButton("Удалить", (dialog, which) -> deleteAccount())
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void deleteAccount() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            user.delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(SettingsActivity.this, "Аккаунт успешно удален", Toast.LENGTH_SHORT).show();
                            logoutUser();
                        } else {
                            Toast.makeText(SettingsActivity.this, "Ошибка удаления аккаунта: " +
                                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}