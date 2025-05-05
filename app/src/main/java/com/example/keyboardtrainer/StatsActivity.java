package com.example.keyboardtrainer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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

        bottomNav.setOnItemSelectedListener(item -> {
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
                                    document.getId(),
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

        scoreView.setText(String.valueOf(stats.score));
        dateView.setText(formatDate(stats.timestamp));

        db.collection("users").document(stats.userId)
                .get()
                .addOnSuccessListener(document -> {
                    String username = document.getString("username");
                    usernameView.setText(username != null ? username : "Аноним");
                });

        statItem.setOnClickListener(v -> showGameDetails(stats));
        statsContainer.addView(statItem);
    }

    private void showGameDetails(GameStats stats) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_game_details, null);

        TextView username = dialogView.findViewById(R.id.detail_username);
        TextView score = dialogView.findViewById(R.id.detail_score);
        TextView date = dialogView.findViewById(R.id.detail_date);
        TextView speed = dialogView.findViewById(R.id.detail_speed);
        TextView accuracy = dialogView.findViewById(R.id.detail_accuracy);
        TextView errors = dialogView.findViewById(R.id.detail_errors);
        TextView originalText = dialogView.findViewById(R.id.detail_original_text);
        MaterialButton btnOk = dialogView.findViewById(R.id.btn_ok);
        MaterialButton btnDelete = dialogView.findViewById(R.id.btn_delete);

        db.collection("users").document(stats.userId)
                .get()
                .addOnSuccessListener(document -> {
                    String usernameText = document.getString("username");
                    username.setText(usernameText != null ? usernameText : "Аноним");
                });

        score.setText(String.valueOf(stats.score));
        date.setText(formatDate(stats.timestamp));

        long charsPerMinute = (stats.correctChars * 60) / 30;
        double accuracyPercent = (stats.totalChars > 0) ?
                (stats.correctChars * 100.0 / stats.totalChars) : 0;

        speed.setText(String.format(Locale.getDefault(), "%d зн./мин", charsPerMinute));
        accuracy.setText(String.format(Locale.getDefault(), "%.1f%%", accuracyPercent));
        errors.setText(String.valueOf(stats.errors));

        if (stats.originalText != null && stats.typedText != null) {
            originalText.setText(formatColoredText(stats.originalText, stats.typedText));
        } else {
            originalText.setText("Текст недоступен");
        }

        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        boolean isOwner = currentUserId != null && currentUserId.equals(stats.userId);

        btnDelete.setVisibility(isOwner ? View.VISIBLE : View.GONE);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        btnOk.setOnClickListener(v -> dialog.dismiss());
        btnDelete.setOnClickListener(v -> {
            dialog.dismiss();
            showDeleteRecordDialog(stats);
        });

        dialog.show();
    }

    private void showDeleteRecordDialog(GameStats stats) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirm_action, null);

        TextView title = dialogView.findViewById(R.id.dialog_title);
        TextView message = dialogView.findViewById(R.id.dialog_message);
        MaterialButton cancelBtn = dialogView.findViewById(R.id.btn_cancel);
        MaterialButton confirmBtn = dialogView.findViewById(R.id.btn_confirm);

        title.setText("Удаление записи");
        message.setText("Вы уверены, что хотите удалить эту запись? Это действие нельзя отменить.");
        confirmBtn.setText("Удалить");

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        cancelBtn.setOnClickListener(v -> dialog.dismiss());
        confirmBtn.setOnClickListener(v -> {
            dialog.dismiss();
            deleteStats(stats);
        });

        dialog.show();
    }

    private void deleteStats(GameStats stats) {
        db.collection("scores").document(stats.documentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Запись удалена", Toast.LENGTH_SHORT).show();
                    loadUserStats();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка при удалении: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
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
        String documentId;
        String userId;
        Long score;
        Long correctChars;
        Long totalChars;
        Long errors;
        String originalText;
        String typedText;
        Timestamp timestamp;

        GameStats(String documentId, String userId, Long score, Long correctChars, Long totalChars,
                  Long errors, String originalText, String typedText, Timestamp timestamp) {
            this.documentId = documentId;
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