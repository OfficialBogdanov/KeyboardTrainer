package com.example.keyboardtrainer.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.example.keyboardtrainer.R;
import com.google.firebase.auth.FirebaseAuth;

public class WelcomeActivity extends BaseActivity {
    private static final String PREFS_NAME = "KeyboardTrainerPrefs";
    private static final String IS_GUEST_KEY = "is_guest";

    // Lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkUserAuthentication();
    }

    // Authentication
    private void checkUserAuthentication() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            redirectToMainActivity();
            return;
        }

        setupWelcomeScreen();
    }

    private void redirectToMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    // UI
    private void setupWelcomeScreen() {
        setContentView(R.layout.activity_welcome);
        setupButtonListeners();
    }

    private void setupButtonListeners() {
        Button btnLogin = findViewById(R.id.btn_login);
        Button btnSignUp = findViewById(R.id.btn_signup);
        TextView btnGuest = findViewById(R.id.btn_guest);

        btnLogin.setOnClickListener(v -> navigateToLogin());
        btnSignUp.setOnClickListener(v -> navigateToRegister());
        btnGuest.setOnClickListener(v -> enterAsGuest());
    }

    // Navigation
    private void navigateToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
    }

    private void navigateToRegister() {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    // Guest
    private void enterAsGuest() {
        FirebaseAuth.getInstance().signOut();

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(IS_GUEST_KEY, true).apply();

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}