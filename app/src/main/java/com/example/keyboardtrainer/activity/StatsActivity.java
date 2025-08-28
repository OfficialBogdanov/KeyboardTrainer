package com.example.keyboardtrainer.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.keyboardtrainer.R;
import com.example.keyboardtrainer.components.SubscriptionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StatsActivity extends BaseActivity {

    private LinearLayout statsContainer;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private EditText searchInput;
    private MaterialButton sortDateButton, sortScoreButton;
    private SubscriptionManager subscriptionManager;

    private boolean sortByDate = true;
    private boolean sortAscending = false;
    private String currentSearchQuery = "";

    // Lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.isAnonymous()) {
            showAnonymousUserMessage();
            return;
        }

        subscriptionManager = new SubscriptionManager(this);

        if (!subscriptionManager.isSubscribed()) {
            showSubscriptionRequired();
            return;
        }

        setContentView(R.layout.activity_stats);
        initializeViews();
        initializeFirebase();
        setupBottomNavigation();
        setupSortButtons();
        setupSearch();
        loadUserStats();
    }

    private void showAnonymousUserMessage() {
        setContentView(R.layout.activity_anonymous_stats);

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

    private void showSubscriptionRequired() {
        setContentView(R.layout.activity_subscription_required);

        MaterialButton btnBuy = findViewById(R.id.btn_buy);
        MaterialButton btnContinue = findViewById(R.id.btn_continue);

        btnBuy.setOnClickListener(v -> purchaseSubscription());

        btnContinue.setOnClickListener(v -> finish());
    }

    private void purchaseSubscription() {
        subscriptionManager.activateSubscription(1);
        Toast.makeText(this, "Подписка активирована!", Toast.LENGTH_SHORT).show();

        recreate();
    }

    // Initialization
    private void initializeViews() {
        statsContainer = findViewById(R.id.statsContainer);
        searchInput = findViewById(R.id.searchInput);
        sortDateButton = findViewById(R.id.sortDateButton);
        sortScoreButton = findViewById(R.id.sortScoreButton);
    }

    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    // Sorting
    private void setupSortButtons() {
        sortDateButton.setOnClickListener(v -> {
            if (sortByDate) {
                sortAscending = !sortAscending;
            } else {
                sortByDate = true;
                sortAscending = false;
            }
            updateSortButtons();
            loadUserStats();
        });

        sortScoreButton.setOnClickListener(v -> {
            if (!sortByDate) {
                sortAscending = !sortAscending;
            } else {
                sortByDate = false;
                sortAscending = false;
            }
            updateSortButtons();
            loadUserStats();
        });

        updateSortButtons();
    }

    private void updateSortButtons() {
        int arrowIcon = sortAscending ?
                R.drawable.chevron_up:
                R.drawable.chevron_down;

        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        int colorPrimary = typedValue.data;

        getTheme().resolveAttribute(R.attr.colorOnSurfaceVariant, typedValue, true);
        int colorOnSurfaceVariant = typedValue.data;

        if (sortByDate) {
            sortDateButton.setIconResource(arrowIcon);
            sortDateButton.setIconTint(ColorStateList.valueOf(colorPrimary));
            sortDateButton.setTextColor(colorPrimary);

            sortScoreButton.setIcon(null);
            sortScoreButton.setIconTint(ColorStateList.valueOf(colorOnSurfaceVariant));
            sortScoreButton.setTextColor(colorOnSurfaceVariant);
        } else {
            sortScoreButton.setIconResource(arrowIcon);
            sortScoreButton.setIconTint(ColorStateList.valueOf(colorPrimary));
            sortScoreButton.setTextColor(colorPrimary);

            sortDateButton.setIcon(null);
            sortDateButton.setIconTint(ColorStateList.valueOf(colorOnSurfaceVariant));
            sortDateButton.setTextColor(colorOnSurfaceVariant);
        }
    }

    // Search
    private void setupSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().toLowerCase();
                loadUserStats();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // Data
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

    private void loadUserStats() {
        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        Query query = db.collection("scores");

        if (sortByDate) {
            query = query.orderBy("timestamp", sortAscending ?
                    Query.Direction.ASCENDING : Query.Direction.DESCENDING);
        } else {
            query = query.orderBy("score", sortAscending ?
                    Query.Direction.ASCENDING : Query.Direction.DESCENDING);
        }

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                statsContainer.removeAllViews();
                List<GameStats> filteredStats = new ArrayList<>();

                for (QueryDocumentSnapshot document : task.getResult()) {
                    GameStats stats = createGameStatsFromDocument(document);

                    if (currentSearchQuery.isEmpty()) {
                        filteredStats.add(stats);
                    } else {
                        filterStatsByUsername(stats, currentSearchQuery);
                    }
                }

                if (currentSearchQuery.isEmpty()) {
                    displayFilteredStats(filteredStats, currentUserId);
                }
            }
        });
    }

    private GameStats createGameStatsFromDocument(QueryDocumentSnapshot document) {
        return new GameStats(
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
    }

    private void filterStatsByUsername(GameStats stats, String searchQuery) {
        db.collection("users").document(stats.userId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    String username = userDoc.getString("username");
                    if (username != null && username.toLowerCase().contains(searchQuery)) {
                        displayGameItem(stats,
                                stats.userId.equals(auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null),
                                username);
                    }
                });
    }

    private void displayFilteredStats(List<GameStats> statsList, String currentUserId) {
        for (GameStats stats : statsList) {
            displayGameItem(stats,
                    stats.userId.equals(currentUserId),
                    null);
        }
    }

    // UI
    private void displayGameItem(GameStats stats, boolean isCurrentUser, String username) {
        View statItem = LayoutInflater.from(this).inflate(R.layout.item_stat, statsContainer, false);

        TextView usernameView = statItem.findViewById(R.id.username);
        TextView scoreView = statItem.findViewById(R.id.score);
        TextView dateView = statItem.findViewById(R.id.date);
        View userIndicator = statItem.findViewById(R.id.userIndicator);

        scoreView.setText(String.valueOf(stats.score));
        dateView.setText(formatDate(stats.timestamp));

        if (username != null) {
            usernameView.setText(username);
        } else {
            loadUsernameFromFirebase(stats.userId, usernameView);
        }

        userIndicator.setVisibility(isCurrentUser ? View.VISIBLE : View.INVISIBLE);
        statItem.setOnClickListener(v -> showGameDetails(stats));
        statsContainer.addView(statItem);
    }

    private void loadUsernameFromFirebase(String userId, TextView usernameView) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    String usernameFromDb = document.getString("username");
                    usernameView.setText(usernameFromDb != null ? usernameFromDb : getString(R.string.anonymous));
                });
    }

    // Game
    private void showGameDetails(GameStats stats) {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        @SuppressLint("InflateParams") View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_game_details, null);
        dialog.setContentView(dialogView);

        setupDialogViews(dialogView, stats, dialog);
        dialog.show();

        View parent = (View) dialogView.getParent();
        parent.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    private void setupDialogViews(View dialogView, GameStats stats, BottomSheetDialog dialog) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = currentUser != null ? currentUser.getUid() : null;

        setupUsernameSection(dialogView, stats, currentUserId);
        setupDeleteSection(dialogView, stats, currentUserId, dialog);
        setupStatsDisplay(dialogView, stats);
        setupTextDisplay(dialogView, stats);
    }

    /** @noinspection DataFlowIssue*/
    private void setupUsernameSection(View dialogView, GameStats stats, String currentUserId) {
        TextView username = dialogView.findViewById(R.id.detail_username);
        if (stats.userId != null && stats.userId.equals(currentUserId)) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser.getDisplayName() != null) {
                username.setText(currentUser.getDisplayName());
            } else {
                username.setText(getString(R.string.you));
            }
        } else {
            loadUsernameForDialog(stats.userId, username);
        }
    }

    private void loadUsernameForDialog(String userId, TextView usernameView) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String name = document.getString("username");
                        usernameView.setText(name != null ? name : getString(R.string.anonymous));
                    } else {
                        usernameView.setText(getString(R.string.anonymous));
                    }
                });
    }

    private void setupDeleteSection(View dialogView, GameStats stats, String currentUserId, BottomSheetDialog dialog) {
        LinearLayout deleteSection = dialogView.findViewById(R.id.deleteSection);
        if (stats.userId != null && stats.userId.equals(currentUserId)) {
            deleteSection.setVisibility(View.VISIBLE);

            LinearLayout deleteContainer = dialogView.findViewById(R.id.deleteContainer);
            deleteContainer.setOnClickListener(v -> {
                dialog.dismiss();
                showDeleteRecordDialog(stats);
            });
        } else {
            deleteSection.setVisibility(View.GONE);
        }
    }

    private void setupStatsDisplay(View dialogView, GameStats stats) {
        TextView score = dialogView.findViewById(R.id.detail_score);
        TextView date = dialogView.findViewById(R.id.detail_date);
        TextView speed = dialogView.findViewById(R.id.detail_speed);
        TextView accuracy = dialogView.findViewById(R.id.detail_accuracy);
        TextView errors = dialogView.findViewById(R.id.detail_errors);

        score.setText(String.valueOf(stats.score));
        date.setText(formatDate(stats.timestamp));

        long charsPerMinute = (stats.correctChars * 60) / 30;
        double accuracyPercent = (stats.totalChars > 0) ?
                (stats.correctChars * 100.0 / stats.totalChars) : 0;

        speed.setText(String.format(Locale.getDefault(), getString(R.string.speed_format), charsPerMinute));
        accuracy.setText(String.format(Locale.getDefault(), "%.1f%%", accuracyPercent));
        errors.setText(String.valueOf(stats.errors));
    }

    private void setupTextDisplay(View dialogView, GameStats stats) {
        TextView originalTextView = dialogView.findViewById(R.id.detail_original_text);

        if (stats.originalText != null && !stats.originalText.isEmpty()) {
            if (stats.typedText != null && !stats.typedText.isEmpty()) {
                applyTextColoring(originalTextView, stats);
            } else {
                originalTextView.setText(stats.originalText);
            }
        } else {
            originalTextView.setText(R.string.no_text_available);
        }
    }

    private void applyTextColoring(TextView textView, GameStats stats) {
        SpannableStringBuilder coloredText = new SpannableStringBuilder(stats.originalText);
        int minLength = Math.min(stats.originalText.length(), stats.typedText.length());

        for (int i = 0; i < minLength; i++) {
            int color = ContextCompat.getColor(this,
                    stats.originalText.charAt(i) == stats.typedText.charAt(i)
                            ? R.color.green_success
                            : R.color.red_error);

            coloredText.setSpan(
                    new ForegroundColorSpan(color),
                    i, i + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
        textView.setText(coloredText);
    }

    // Delete
    private void showDeleteRecordDialog(GameStats stats) {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        @SuppressLint("InflateParams") View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_confirm_action, null);
        dialog.setContentView(dialogView);

        setupDeleteDialogViews(dialogView, stats, dialog);
        dialog.show();

        View parent = (View) dialogView.getParent();
        parent.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    private void setupDeleteDialogViews(View dialogView, GameStats stats, BottomSheetDialog dialog) {
        TextView title = dialogView.findViewById(R.id.dialog_title);
        TextView message = dialogView.findViewById(R.id.dialog_message);
        MaterialButton cancelBtn = dialogView.findViewById(R.id.btn_cancel);
        MaterialButton confirmBtn = dialogView.findViewById(R.id.btn_confirm);

        title.setText(R.string.delete_record_title);
        message.setText(R.string.delete_record_message);
        confirmBtn.setText(R.string.delete);

        cancelBtn.setOnClickListener(v -> dialog.dismiss());
        confirmBtn.setOnClickListener(v -> {
            deleteStats(stats);
            dialog.dismiss();
        });
    }

    private void deleteStats(GameStats stats) {
        db.collection("scores").document(stats.documentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, R.string.record_deleted_success, Toast.LENGTH_SHORT).show();
                    loadUserStats();
                })
                .addOnFailureListener(e -> Toast.makeText(this,
                        getString(R.string.record_deleted_error, e.getMessage()),
                        Toast.LENGTH_SHORT).show());
    }

    // Utility
    private String formatDate(Timestamp timestamp) {
        return new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                .format(new Date(timestamp.toDate().getTime()));
    }

    // Navigation
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