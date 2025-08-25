package com.example.keyboardtrainer.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.keyboardtrainer.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginActivity extends BaseActivity {
    private static final String PREFS_NAME = "KeyboardTrainerPrefs";
    private static final String IS_GUEST_KEY = "is_guest";

    // Lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setupViews();
    }

    // UI
    private void setupViews() {
        EditText email = findViewById(R.id.email);
        EditText password = findViewById(R.id.password);
        MaterialButton btnLogin = findViewById(R.id.btn_login);
        TextView btnSwitchToSignup = findViewById(R.id.btn_switch_to_signup);

        btnSwitchToSignup.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        btnLogin.setOnClickListener(v -> loginUser(
                email.getText().toString().trim(),
                password.getText().toString().trim()
        ));

        btnSwitchToSignup.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });
    }

    // Login
    private void loginUser(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isNetworkAvailable()) {
            Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
            return;
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, getString(R.string.login_success), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    } else {
                        handleLoginError(task.getException());
                    }
                });
    }

    /** @noinspection deprecation*/ // Network
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // Error
    private void handleLoginError(Exception exception) {
        String errorMessage;

        if (exception instanceof FirebaseNetworkException) {
            errorMessage = getString(R.string.no_internet_connection);
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            errorMessage = getString(R.string.invalid_email_or_password);
        } else if (exception instanceof FirebaseAuthInvalidUserException) {
            errorMessage = getString(R.string.user_not_found);
        } else if (exception != null) {
            errorMessage = getString(R.string.login_error) + ": " + exception.getMessage();
        } else {
            errorMessage = getString(R.string.login_error);
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    /** @noinspection unused*/ // Guest
    private void enterAsGuest() {
        FirebaseAuth.getInstance().signOut();

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(IS_GUEST_KEY, true).apply();

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}