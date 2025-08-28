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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class AchievementsActivity extends BaseActivity {
    private AchievementsAdapter adapter;
    private final List<Achievement> achievements = new ArrayList<>();
    /** @noinspection MismatchedQueryAndUpdateOfCollection*/
    private final Map<String, Integer> achievementProgressMap = new HashMap<>();

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

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && !user.isAnonymous()) {
            loadUserAchievements();
        }
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
        achievements.clear();

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
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        loadUserStatsAndCheckAchievements(user.getUid(), documentSnapshot);
                    }
                });
    }

    private void loadUserStatsAndCheckAchievements(String userId, DocumentSnapshot documentSnapshot) {
        int totalCharsTyped = documentSnapshot.getLong("totalCharsTyped") != null ?
                Objects.requireNonNull(documentSnapshot.getLong("totalCharsTyped")).intValue() : 0;
        int maxSpeed = documentSnapshot.getLong("maxSpeed") != null ?
                Objects.requireNonNull(documentSnapshot.getLong("maxSpeed")).intValue() : 0;
        float maxAccuracy = documentSnapshot.getDouble("maxAccuracy") != null ?
                Objects.requireNonNull(documentSnapshot.getDouble("maxAccuracy")).floatValue() : 0;
        int sessionsCompleted = documentSnapshot.getLong("sessionsCompleted") != null ?
                Objects.requireNonNull(documentSnapshot.getLong("sessionsCompleted")).intValue() : 0;
        int flawlessSessions = documentSnapshot.getLong("flawlessSessions") != null ?
                Objects.requireNonNull(documentSnapshot.getLong("flawlessSessions")).intValue() : 0;

        for (Achievement achievement : achievements) {
            int progress = 0;

            switch (achievement.getId()) {
                case "beginner":
                    progress = sessionsCompleted > 0 ? 1 : 0;
                    break;
                case "pro":
                    progress = Math.min(totalCharsTyped, achievement.getTarget());
                    break;
                case "speedster":
                    progress = Math.min(maxSpeed, achievement.getTarget());
                    break;
                case "accuracy":
                    progress = Math.min((int) maxAccuracy, achievement.getTarget());
                    break;
                case "marathon":
                    progress = Math.min(sessionsCompleted, achievement.getTarget());
                    break;
                case "master":
                    progress = Math.min(totalCharsTyped, achievement.getTarget());
                    break;
                case "lightning":
                    progress = Math.min(maxSpeed, achievement.getTarget());
                    break;
                case "sniper":
                    progress = Math.min((int) maxAccuracy, achievement.getTarget());
                    break;
                case "legend":
                    progress = Math.min(sessionsCompleted, achievement.getTarget());
                    break;
                case "flawless":
                    progress = Math.min(flawlessSessions, achievement.getTarget());
                    break;
            }

            achievement.setProgress(progress);
            boolean isAchieved = progress >= achievement.getTarget();
            achievement.setAchieved(isAchieved);

            achievementProgressMap.put(achievement.getId(), progress);
        }

        loadUnlockedAchievements(userId);
    }

    private void loadUnlockedAchievements(String userId) {
        FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .collection("achievements")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Set<String> unlockedAchievements = new HashSet<>();

                    for (DocumentSnapshot doc : querySnapshot) {
                        if (doc.getBoolean("achieved") != null &&
                                Boolean.TRUE.equals(doc.getBoolean("achieved"))) {
                            unlockedAchievements.add(doc.getId());
                        }
                    }

                    for (Achievement achievement : achievements) {
                        if (unlockedAchievements.contains(achievement.getId())) {
                            achievement.setAchieved(true);
                            achievement.setProgress(achievement.getTarget());
                        }
                    }

                    adapter.updateAchievements(achievements);

                    checkAndUnlockNewAchievements(userId);
                });
    }

    private void checkAndUnlockNewAchievements(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        for (Achievement achievement : achievements) {
            if (achievement.getProgress() >= achievement.getTarget() && !achievement.isAchieved()) {
                achievement.setAchieved(true);

                Map<String, Object> achievementData = new HashMap<>();
                achievementData.put("achieved", true);
                achievementData.put("progress", achievement.getProgress());
                achievementData.put("unlockedAt", System.currentTimeMillis());

                db.collection("users")
                        .document(userId)
                        .collection("achievements")
                        .document(achievement.getId())
                        .set(achievementData)
                        .addOnSuccessListener(aVoid -> adapter.updateAchievements(achievements));
            }
        }
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