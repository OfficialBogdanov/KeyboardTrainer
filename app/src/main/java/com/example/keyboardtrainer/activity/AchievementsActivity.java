package com.example.keyboardtrainer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.keyboardtrainer.components.Achievement;
import com.example.keyboardtrainer.components.AchievementsAdapter;
import com.example.keyboardtrainer.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class AchievementsActivity extends BaseActivity {
    private AchievementsAdapter adapter;
    private final List<Achievement> achievements = new ArrayList<>();

    // Lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.isAnonymous()) {
            showAnonymousUserMessage();
            return;
        }

        setContentView(R.layout.activity_achievements);
        setupBottomNavigation();

        TextView title = findViewById(R.id.achievements_title);
        title.setText(getString(R.string.achievements_title));

        RecyclerView achievementsList = findViewById(R.id.achievements_list);
        achievementsList.setLayoutManager(new LinearLayoutManager(this));

        initializeAchievements();

        adapter = new AchievementsAdapter(achievements);
        achievementsList.setAdapter(adapter);

        loadUserAchievements();
    }

    // Anonymous
    private void showAnonymousUserMessage() {
        setContentView(R.layout.activity_anonymous_achievements);

        Button loginButton = findViewById(R.id.btn_login);
        Button continueButton = findViewById(R.id.btn_continue);

        loginButton.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        continueButton.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    // Achievements
    private void initializeAchievements() {
        achievements.add(new Achievement("beginner",
                getString(R.string.achievement_beginner),
                getString(R.string.achievement_beginner_desc),
                0, 1, false));

        achievements.add(new Achievement("pro",
                getString(R.string.achievement_pro),
                getString(R.string.achievement_pro_desc),
                0, 1000, false));

        achievements.add(new Achievement("speedster",
                getString(R.string.achievement_speedster),
                getString(R.string.achievement_speedster_desc),
                0, 200, false));

        achievements.add(new Achievement("accuracy",
                getString(R.string.achievement_accuracy),
                getString(R.string.achievement_accuracy_desc),
                0, 95, false));

        achievements.add(new Achievement("marathon",
                getString(R.string.achievement_marathon),
                getString(R.string.achievement_marathon_desc),
                0, 10, false));

        achievements.add(new Achievement("master",
                getString(R.string.achievement_master),
                getString(R.string.achievement_master_desc),
                0, 1000000, false));

        achievements.add(new Achievement("lightning",
                getString(R.string.achievement_lightning),
                getString(R.string.achievement_lightning_desc),
                0, 500, false));

        achievements.add(new Achievement("sniper",
                getString(R.string.achievement_sniper),
                getString(R.string.achievement_sniper_desc),
                0, 99, false));

        achievements.add(new Achievement("legend",
                getString(R.string.achievement_legend),
                getString(R.string.achievement_legend_desc),
                0, 1000, false));

        achievements.add(new Achievement("flawless",
                getString(R.string.achievement_flawless),
                getString(R.string.achievement_flawless_desc),
                0, 1, false));
    }

    // Firebase
    private void loadUserAchievements() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.isAnonymous()) return;

        FirebaseFirestore.getInstance().collection("users")
                .document(user.getUid())
                .collection("achievements")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    checkUnlockedAchievements(user.getUid());
                    loadAchievementProgress(user.getUid());
                });
    }

    private void checkUnlockedAchievements(String userId) {
        FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .collection("achievements")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Set<String> unlocked = new HashSet<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        if (doc.getBoolean("achieved") != null && Boolean.TRUE.equals(doc.getBoolean("achieved"))) {
                            unlocked.add(doc.getId());
                        }
                    }

                    for (Achievement achievement : achievements) {
                        if (unlocked.contains(achievement.getId())) {
                            achievement.setAchieved(true);
                            achievement.setProgress(achievement.getTarget());
                        }
                    }

                    adapter.updateAchievements(achievements);
                });
    }

    private void loadAchievementProgress(String userId) {
        FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        int totalCharsTyped = documentSnapshot.getLong("totalCharsTyped") != null ?
                                Objects.requireNonNull(documentSnapshot.getLong("totalCharsTyped")).intValue() : 0;
                        int maxSpeed = documentSnapshot.getLong("maxSpeed") != null ?
                                Objects.requireNonNull(documentSnapshot.getLong("maxSpeed")).intValue() : 0;
                        float maxAccuracy = documentSnapshot.getDouble("maxAccuracy") != null ?
                                Objects.requireNonNull(documentSnapshot.getDouble("maxAccuracy")).floatValue() : 0;
                        int sessionsCompleted = documentSnapshot.getLong("sessionsCompleted") != null ?
                                Objects.requireNonNull(documentSnapshot.getLong("sessionsCompleted")).intValue() : 0;

                        for (Achievement achievement : achievements) {
                            switch (achievement.getId()) {
                                case "beginner":
                                    if (sessionsCompleted > 0) {
                                        achievement.setProgress(1);
                                    }
                                    break;
                                case "pro":
                                    achievement.setProgress(totalCharsTyped);
                                    break;
                                case "speedster":
                                    achievement.setProgress(maxSpeed);
                                    break;
                                case "accuracy":
                                    achievement.setProgress((int) maxAccuracy);
                                    break;
                                case "marathon":
                                    achievement.setProgress(sessionsCompleted);
                                    break;
                            }
                        }

                        adapter.updateAchievements(achievements);
                    }
                });
    }

    // Navigation
    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.navigation_achievements);

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