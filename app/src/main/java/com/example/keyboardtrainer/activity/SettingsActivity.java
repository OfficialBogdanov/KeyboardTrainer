package com.example.keyboardtrainer.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.keyboardtrainer.dialog.AccountSettingsDialog;
import com.example.keyboardtrainer.dialog.LanguageSettingsDialog;
import com.example.keyboardtrainer.dialog.ThemeSettingsDialog;
import com.example.keyboardtrainer.dialog.GameSettingsDialog;
import com.example.keyboardtrainer.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsActivity extends BaseActivity {

    private static final String PREFS_NAME = "KeyboardTrainerPrefs";
    private FirebaseAuth mAuth;

    // Lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initializeAuth();
        setupBottomNavigation();
        setupUserInfo();
        setupClickListeners();
    }

    // Initialization
    private void initializeAuth() {
        mAuth = FirebaseAuth.getInstance();
    }

    // User
    private void setupUserInfo() {
        TextView userNameText = findViewById(R.id.userNameText);
        TextView userEmailText = findViewById(R.id.userEmailText);
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        if (prefs.getBoolean("is_guest", false)) {
            userNameText.setText(getString(R.string.guest));
            userEmailText.setText("");
        } else {
            loadUserInfoFromFirebase(userNameText, userEmailText);
        }
    }

    /** @noinspection CodeBlock2Expr*/
    private void loadUserInfoFromFirebase(TextView userNameText, TextView userEmailText) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(document -> {
                        String username = document.getString("username");
                        String email = currentUser.getEmail();

                        userNameText.setText(
                                username != null ? username :
                                        (currentUser.getDisplayName() != null ? currentUser.getDisplayName() :
                                                (email != null ? email.split("@")[0] : getString(R.string.default_user_name)))
                        );

                        userEmailText.setText(email != null ? email : "");
                    })
                    .addOnFailureListener(e -> {
                        handleFirestoreError(currentUser, userNameText, userEmailText, e);
                    });
        }
    }

    private void handleFirestoreError(FirebaseUser currentUser, TextView userNameText, TextView userEmailText, Exception e) {
        String name = currentUser.getDisplayName();
        String email = currentUser.getEmail();
        userNameText.setText(
                name != null ? name :
                        (email != null ? email.split("@")[0] : getString(R.string.default_user_name))
        );
        userEmailText.setText(email != null ? email : "");
        Log.e("SettingsActivity", "Firestore error", e);
    }

    public void updateUserName(String name) {
        TextView userNameText = findViewById(R.id.userNameText);
        userNameText.setText(name);
    }

    // UI
    private void setupClickListeners() {
        findViewById(R.id.logoutButton).setOnClickListener(v -> logoutUser());

        findViewById(R.id.accountSettingsButton).setOnClickListener(v -> {
            AccountSettingsDialog dialog = new AccountSettingsDialog();
            dialog.show(getSupportFragmentManager(), "account_settings");
        });

        findViewById(R.id.gameSettingsButton).setOnClickListener(v -> {
            GameSettingsDialog dialog = new GameSettingsDialog();
            dialog.show(getSupportFragmentManager(), "GameSettingsDialog");
        });

        findViewById(R.id.themeSettingsButton).setOnClickListener(v -> {
            ThemeSettingsDialog dialog = new ThemeSettingsDialog();
            dialog.show(getSupportFragmentManager(), "theme_settings");
        });

        findViewById(R.id.languageSettingsButton).setOnClickListener(v -> {
            LanguageSettingsDialog dialog = new LanguageSettingsDialog();
            dialog.show(getSupportFragmentManager(), "language_settings");
        });
    }

    // Authentication
    private void logoutUser() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().remove("is_guest").apply();
        mAuth.signOut();

        startActivity(new Intent(this, WelcomeActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
    }

    public void deleteAccount() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, R.string.account_deleted, Toast.LENGTH_SHORT).show();
                    logoutUser();
                } else {
                    handleAccountDeletionError(task);
                }
            });
        }
    }

    private void handleAccountDeletionError(com.google.android.gms.tasks.Task<Void> task) {
        String error = task.getException() != null ?
                task.getException().getMessage() : String.valueOf(R.string.unknown_error);
        Toast.makeText(this,
                getString(R.string.account_deletion_error, error),
                Toast.LENGTH_SHORT).show();
    }

    // Navigation
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
            } else if (id == R.id.navigation_achievements) {
                startActivity(new Intent(this, AchievementsActivity.class));
                finish();
                return true;
            } else if (id == R.id.navigation_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }
}