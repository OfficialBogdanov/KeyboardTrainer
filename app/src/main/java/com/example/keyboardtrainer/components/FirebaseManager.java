package com.example.keyboardtrainer.components;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FirebaseManager {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    // Interface
    public interface AchievementCallback {
        void onAchievementUnlocked(String achievementId);
    }

    // Constructor
    public FirebaseManager() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    // Game
    public void saveGameStats(String originalText, String typedText, int score,
                              int correctChars, int totalChars, int errorCount) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        Map<String, Object> gameData = createGameData(user.getUid(), originalText, typedText,
                score, correctChars, totalChars, errorCount);

        db.collection("scores")
                .add(gameData)
                .addOnSuccessListener(docRef -> Log.d("Firebase", "Game stats saved"))
                .addOnFailureListener(e -> Log.w("Firebase", "Error saving game stats", e));
    }

    private Map<String, Object> createGameData(String userId, String originalText, String typedText,
                                               int score, int correctChars, int totalChars, int errorCount) {
        Map<String, Object> gameData = new HashMap<>();
        gameData.put("userId", userId);
        gameData.put("score", score);
        gameData.put("correctChars", correctChars);
        gameData.put("totalChars", totalChars);
        gameData.put("errors", errorCount);
        gameData.put("originalText", originalText);
        gameData.put("typedText", typedText);
        gameData.put("timestamp", Timestamp.now());
        return gameData;
    }

    /** @noinspection unused*/ // Achievement
    public void checkAndUnlockAchievements(int score, int correctChars, int totalChars,
                                           int errorCount, AchievementCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || user.isAnonymous()) return;

        checkBeginnerAchievement(user.getUid(), callback);
        checkProAchievement(user.getUid(), correctChars, errorCount, callback);
        checkSpeedsterAchievement(user.getUid(), correctChars, callback);
        checkAccuracyAchievement(user.getUid(), correctChars, totalChars, callback);

        updateUserStats(user.getUid(), correctChars, totalChars);
    }

    private void checkBeginnerAchievement(String userId, AchievementCallback callback) {
        checkAchievement(userId, "beginner", callback);
    }

    private void checkProAchievement(String userId, int correctChars, int errorCount, AchievementCallback callback) {
        if (correctChars >= 1000 && errorCount == 0) {
            checkAchievement(userId, "pro", callback);
        }
    }

    private void checkSpeedsterAchievement(String userId, int correctChars, AchievementCallback callback) {
        int speed = calculateSpeed(correctChars);
        if (speed >= 200) {
            checkAchievement(userId, "speedster", callback);
        }
    }

    private void checkAccuracyAchievement(String userId, int correctChars, int totalChars, AchievementCallback callback) {
        double accuracy = calculateAccuracy(correctChars, totalChars);
        if (accuracy >= 95) {
            checkAchievement(userId, "accuracy", callback);
        }
    }

    private int calculateSpeed(int correctChars) {
        return (int) ((correctChars / 30.0) * 60);
    }

    private double calculateAccuracy(int correctChars, int totalChars) {
        return (totalChars > 0) ? (double) correctChars / totalChars * 100 : 0;
    }

    private void checkAchievement(String userId, String achievementId, AchievementCallback callback) {
        db.collection("users")
                .document(userId)
                .collection("achievements")
                .document(achievementId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists() || Boolean.FALSE.equals(documentSnapshot.getBoolean("achieved"))) {
                        unlockAchievement(userId, achievementId, callback);
                    }
                })
                .addOnFailureListener(e -> Log.w("Firebase", "Error checking achievement", e));
    }

    private void unlockAchievement(String userId, String achievementId, AchievementCallback callback) {
        Map<String, Object> achievement = createAchievementData();

        db.collection("users")
                .document(userId)
                .collection("achievements")
                .document(achievementId)
                .set(achievement)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firebase", "Achievement unlocked: " + achievementId);
                    notifyAchievementUnlocked(callback, achievementId);
                })
                .addOnFailureListener(e -> Log.w("Firebase", "Error unlocking achievement", e));
    }

    private Map<String, Object> createAchievementData() {
        Map<String, Object> achievement = new HashMap<>();
        achievement.put("achieved", true);
        achievement.put("timestamp", FieldValue.serverTimestamp());
        return achievement;
    }

    private void notifyAchievementUnlocked(AchievementCallback callback, String achievementId) {
        if (callback != null) {
            callback.onAchievementUnlocked(achievementId);
        }
    }

    // User
    private void updateUserStats(String userId, int correctChars, int totalChars) {
        int speed = calculateSpeed(correctChars);
        double accuracy = calculateAccuracy(correctChars, totalChars);

        Map<String, Object> updates = createUserStatsUpdates(correctChars, speed, accuracy);

        db.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> Log.d("Firebase", "User stats updated"))
                .addOnFailureListener(e -> Log.w("Firebase", "Error updating user stats", e));
    }

    private Map<String, Object> createUserStatsUpdates(int correctChars, int speed, double accuracy) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("totalCharsTyped", FieldValue.increment(correctChars));
        updates.put("maxSpeed", FieldValue.increment(speed));
        updates.put("maxAccuracy", FieldValue.increment(accuracy));
        updates.put("sessionsCompleted", FieldValue.increment(1));
        return updates;
    }

    public boolean isUserAuthenticated() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null && !user.isAnonymous();
    }

    /** @noinspection unused*/
    public String getCurrentUserId() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }
}