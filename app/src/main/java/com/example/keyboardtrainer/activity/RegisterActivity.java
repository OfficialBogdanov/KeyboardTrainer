package com.example.keyboardtrainer.activity;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.keyboardtrainer.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends BaseActivity {

    // Lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        setupViews();
    }

    // UI
    private void setupViews() {
        EditText username = findViewById(R.id.username);
        EditText email = findViewById(R.id.email);
        EditText password = findViewById(R.id.password);
        EditText passwordConfirm = findViewById(R.id.passwordConfirm);
        MaterialButton btnRegister = findViewById(R.id.btn_register);
        TextView btnSwitchToLogin = findViewById(R.id.btn_switch_to_login);

        btnRegister.setOnClickListener(v -> registerUser(
                username.getText().toString().trim(),
                email.getText().toString().trim(),
                password.getText().toString().trim(),
                passwordConfirm.getText().toString().trim()
        ));

        btnSwitchToLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    // Registration
    private void registerUser(String username, String email, String password, String confirmPassword) {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
            return;
        }

        if (email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6 || password.length() > 12) {
            Toast.makeText(this, getString(R.string.password_length_error), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, getString(R.string.passwords_not_match), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            Toast.makeText(this, getString(R.string.username_format_error), Toast.LENGTH_SHORT).show();
            return;
        }

        if (username.length() < 3 || username.length() > 20) {
            Toast.makeText(this, getString(R.string.username_length_error), Toast.LENGTH_SHORT).show();
            return;
        }

        checkUsernameAvailability(username, email, password);
    }

    // Username
    private void checkUsernameAvailability(String username, String email, String password) {
        FirebaseFirestore.getInstance().collection("usernames")
                .document(username.toLowerCase())
                .get()
                .addOnCompleteListener(usernameTask -> {
                    if (!usernameTask.isSuccessful()) {
                        handleFirestoreError(usernameTask.getException());
                        return;
                    }

                    if (usernameTask.getResult().exists()) {
                        Toast.makeText(this, getString(R.string.username_taken), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    createFirebaseUser(username, email, password);
                });
    }

    // Firebase
    private void createFirebaseUser(String username, String email, String password) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(authTask -> {
                    if (!authTask.isSuccessful()) {
                        handleRegistrationError(authTask.getException());
                        return;
                    }

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        saveUserData(user, username, email);
                    }
                });
    }

    // User
    private void saveUserData(FirebaseUser user, String username, String email) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("email", email);
        userData.put("createdAt", Timestamp.now());

        FirebaseFirestore.getInstance().collection("users")
                .document(user.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> reserveUsername(user, username))
                .addOnFailureListener(e -> {
                    user.delete();
                    handleFirestoreError(e);
                });
    }

    private void reserveUsername(FirebaseUser user, String username) {
        Map<String, Object> usernameReservation = new HashMap<>();
        usernameReservation.put("userId", user.getUid());

        FirebaseFirestore.getInstance().collection("usernames")
                .document(username.toLowerCase())
                .set(usernameReservation)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, getString(R.string.registration_success), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    user.delete();
                    handleFirestoreError(e);
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
    private void handleRegistrationError(Exception exception) {
        String errorMessage = getString(R.string.registration_error);

        if (exception instanceof FirebaseNetworkException) {
            errorMessage = getString(R.string.no_internet_connection);
        } else if (exception instanceof FirebaseAuthWeakPasswordException) {
            errorMessage = getString(R.string.weak_password_error);
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            errorMessage = getString(R.string.invalid_email_error);
        } else if (exception instanceof FirebaseAuthUserCollisionException) {
            errorMessage = getString(R.string.email_exists_error);
        } else if (exception != null) {
            errorMessage += ": " + exception.getMessage();
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    private void handleFirestoreError(Exception exception) {
        String errorMessage = getString(R.string.registration_error);

        if (exception instanceof FirebaseNetworkException) {
            errorMessage = getString(R.string.no_internet_connection);
        } else if (exception != null) {
            errorMessage += ": " + exception.getMessage();
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }
}