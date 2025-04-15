package com.example.keyboardtrainer;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StatsActivity extends AppCompatActivity {

    private LinearLayout statsContainer;
    private FirebaseFirestore db;
    private Dialog gameDetailsDialog;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        statsContainer = findViewById(R.id.statsContainer);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        setupBottomNavigation();
        loadUserStats();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.navigation_stats);

        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_game) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            } else if (id == R.id.navigation_stats) {
                return true;
            } else if (id == R.id.navigation_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void loadUserStats() {
        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        db.collection("scores")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {


                    if (task.isSuccessful()) {
                        statsContainer.removeAllViews();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            GameStats stats = new GameStats(
                                    document.getString("userId"),
                                    document.getLong("score"),
                                    document.getLong("correctChars"),
                                    document.getLong("totalChars"),
                                    document.getLong("errors"),
                                    document.getString("originalText"),
                                    document.getString("typedText"),
                                    document.getTimestamp("timestamp")
                            );

                            displayGameItem(stats, stats.userId.equals(currentUserId));
                        }
                    }
                });
    }

    private void displayGameItem(GameStats stats, boolean isCurrentUser) {
        View statItem = LayoutInflater.from(this).inflate(R.layout.item_stat, statsContainer, false);

        TextView usernameView = statItem.findViewById(R.id.username);
        TextView scoreView = statItem.findViewById(R.id.score);
        TextView dateView = statItem.findViewById(R.id.date);
        View userIndicator = statItem.findViewById(R.id.userIndicator);

        db.collection("users").document(stats.userId)
                .get()
                .addOnSuccessListener(document -> {
                    String username = document.getString("username");
                    usernameView.setText(username != null ? username : "Аноним");
                });

        scoreView.setText(String.valueOf(stats.score));
        dateView.setText(formatDate(stats.timestamp));

        if (isCurrentUser) {
            userIndicator.setVisibility(View.VISIBLE);
        }

        statItem.setOnClickListener(v -> showGameDetails(stats));
        statsContainer.addView(statItem);
    }

    private void showGameDetails(GameStats stats) {
        gameDetailsDialog = new Dialog(this);
        gameDetailsDialog.setContentView(R.layout.dialog_game_details);
        gameDetailsDialog.setCancelable(true);

        TextView username = gameDetailsDialog.findViewById(R.id.detail_username);
        TextView score = gameDetailsDialog.findViewById(R.id.detail_score);
        TextView date = gameDetailsDialog.findViewById(R.id.detail_date);
        TextView speed = gameDetailsDialog.findViewById(R.id.detail_speed);
        TextView accuracy = gameDetailsDialog.findViewById(R.id.detail_accuracy);
        TextView errors = gameDetailsDialog.findViewById(R.id.detail_errors);
        TextView textStats = gameDetailsDialog.findViewById(R.id.detail_text_stats);
        ScrollView textScroll = gameDetailsDialog.findViewById(R.id.detail_text_scroll);
        TextView originalText = gameDetailsDialog.findViewById(R.id.detail_original_text);
        Button closeButton = gameDetailsDialog.findViewById(R.id.detail_close_btn);

        db.collection("users").document(stats.userId)
                .get()
                .addOnSuccessListener(document -> {
                    String usernameText = document.getString("username");
                    username.setText(usernameText != null ? usernameText : "Аноним");
                });

        score.setText(String.valueOf("Очков: " + stats.score));
        date.setText(formatDate(stats.timestamp));

        long charsPerMinute = (stats.correctChars * 60) / 30;
        double accuracyPercent = (stats.totalChars > 0) ?
                (stats.correctChars * 100.0 / stats.totalChars) : 0;

        speed.setText(getString(R.string.speed_format, charsPerMinute));
        accuracy.setText(getString(R.string.accuracy_format, String.format(Locale.getDefault(), "%.1f", accuracyPercent)));
        errors.setText(getString(R.string.errors_format, stats.errors));
        textStats.setText(getString(R.string.text_stats_format, stats.correctChars, stats.totalChars));

        if (stats.originalText != null && stats.typedText != null) {
            SpannableStringBuilder coloredText = formatColoredText(stats.originalText, stats.typedText);
            originalText.setText(coloredText);
        } else {
            originalText.setText("Текст недоступен");
        }

        closeButton.setOnClickListener(v -> gameDetailsDialog.dismiss());
        gameDetailsDialog.show();
    }

    private SpannableStringBuilder formatColoredText(String original, String typed) {
        SpannableStringBuilder sb = new SpannableStringBuilder(original);
        int minLength = Math.min(original.length(), typed.length());

        for (int i = 0; i < minLength; i++) {
            int color = (original.charAt(i) == typed.charAt(i)) ? Color.GREEN : Color.RED;
            sb.setSpan(new ForegroundColorSpan(color), i, i+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        if (original.length() > typed.length()) {
            for (int i = typed.length(); i < original.length(); i++) {
                sb.setSpan(new ForegroundColorSpan(Color.GRAY), i, i+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        return sb;
    }

    private String formatDate(Timestamp timestamp) {
        return new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                .format(new Date(timestamp.toDate().getTime()));
    }

    private static class GameStats {
        String userId;
        Long score;
        Long correctChars;
        Long totalChars;
        Long errors;
        String originalText;
        String typedText;
        Timestamp timestamp;

        GameStats(String userId, Long score, Long correctChars, Long totalChars,
                  Long errors, String originalText, String typedText, Timestamp timestamp) {
            this.userId = userId;
            this.score = score;
            this.correctChars = correctChars;
            this.totalChars = totalChars;
            this.errors = errors;
            this.originalText = originalText;
            this.typedText = typedText;
            this.timestamp = timestamp;
        }
    }
}