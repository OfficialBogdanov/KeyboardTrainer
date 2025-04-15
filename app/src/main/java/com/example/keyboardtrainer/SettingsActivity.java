package com.example.keyboardtrainer;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Random;

public class SettingsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private RadioGroup themeRadioGroup;
    private RadioButton lightThemeRadio;
    private RadioButton darkThemeRadio;
    private RadioButton systemThemeRadio;
    private TextView demoTextView, demoTimerTextView;
    private SpannableStringBuilder spannable;
    private int cursorPosition = 0;
    private Handler handler = new Handler();
    private String demoText = "Симуляция тренажёрного процесса. Так будет выглядеть тренажёрное поле.";
    private CountDownTimer countDownTimer;
    private int timerSeconds = 20;



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

        demoTextView = findViewById(R.id.demoTextView);
        demoTimerTextView = findViewById(R.id.timerTextView);
        spannable = new SpannableStringBuilder(demoText);
        startTimer();
        startCursorAnimation();
    }

    private void startTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(timerSeconds * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                demoTimerTextView.setText("Таймер: " + seconds + "");
            }

            @Override
            public void onFinish() {
                demoTimerTextView.setText("Таймер: 0");
                cursorPosition = 0;
                startTimer();
            }
        }.start();
    }

    private void startCursorAnimation() {
        Random random = new Random();
        Runnable cursorRunnable = new Runnable() {
            @Override
            public void run() {

                clearAllSpans();

                for (int i = 0; i < cursorPosition; i++) {
                    int color = random.nextBoolean() ? Color.RED : Color.GREEN;
                    spannable.setSpan(new ForegroundColorSpan(color), i, i + 1,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                if (cursorPosition < spannable.length()) {
                    spannable.setSpan(new UnderlineSpan(), cursorPosition, cursorPosition + 1,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                demoTextView.setText(spannable);


                cursorPosition++;
                if (cursorPosition > spannable.length()) {
                    cursorPosition = 0;

                    clearAllSpans();
                }

                handler.postDelayed(this, 300);
            }

            private void clearAllSpans() {
                ForegroundColorSpan[] colorSpans = spannable.getSpans(0, spannable.length(), ForegroundColorSpan.class);
                UnderlineSpan[] underlineSpans = spannable.getSpans(0, spannable.length(), UnderlineSpan.class);

                for (ForegroundColorSpan span : colorSpans) {
                    spannable.removeSpan(span);
                }
                for (UnderlineSpan span : underlineSpans) {
                    spannable.removeSpan(span);
                }
            }
        };

        handler.post(cursorRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
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