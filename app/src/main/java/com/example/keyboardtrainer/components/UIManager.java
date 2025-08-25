package com.example.keyboardtrainer.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.keyboardtrainer.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UIManager {
    // UI
    private final Context context;
    private final TextView wordsTextView;
    private final TextView timerTextView;
    private final TextView scoreTextView;
    private final ProgressBar timerProgressBar;
    private final Button restartButton;
    private final BottomNavigationView bottomNavigation;

    // Style
    private int cursorType;
    private int timerStyle;

    // Color
    private final int COLOR_CORRECT = R.color.green_success;
    private final int COLOR_INCORRECT = R.color.red_error;
    private final int COLOR_DEFAULT = R.color.black;
    private final int COLOR_CURSOR = R.color.light_primary;

    // Constructor
    public UIManager(Context context, TextView wordsTextView, TextView timerTextView,
                     TextView scoreTextView, ProgressBar timerProgressBar,
                     Button restartButton, BottomNavigationView bottomNavigation) {
        this.context = context;
        this.wordsTextView = wordsTextView;
        this.timerTextView = timerTextView;
        this.scoreTextView = scoreTextView;
        this.timerProgressBar = timerProgressBar;
        this.restartButton = restartButton;
        this.bottomNavigation = bottomNavigation;
    }

    // Setter
    public void setCursorType(int cursorType) {
        this.cursorType = cursorType;
    }

    public void setTimerStyle(int timerStyle) {
        this.timerStyle = timerStyle;
        updateTimerStyle();
    }

    // Timer
    public void updateTimerStyle() {
        timerTextView.setVisibility(View.GONE);
        timerProgressBar.setVisibility(View.GONE);

        switch (timerStyle) {
            case 1:
                timerProgressBar.setVisibility(View.VISIBLE);
                break;
            case 2:
                timerTextView.setVisibility(View.VISIBLE);
                timerProgressBar.setVisibility(View.VISIBLE);
                break;
            default:
                timerTextView.setVisibility(View.VISIBLE);
        }
    }

    public void updateTimerText(String text) {
        timerTextView.setText(text);
    }

    public void updateTimerProgress(int progress) {
        if (progress > timerProgressBar.getMax()) {
            timerProgressBar.setMax(progress);
        }
        timerProgressBar.setProgress(progress);
    }

    // Keyboard
    public void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        wordsTextView.requestFocus();
        imm.showSoftInput(wordsTextView, InputMethodManager.SHOW_IMPLICIT);
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(wordsTextView.getWindowToken(), 0);
    }

    // State
    public void setupGameUI() {
        wordsTextView.setVisibility(View.VISIBLE);
        scoreTextView.setVisibility(View.GONE);
        restartButton.setVisibility(View.GONE);
        bottomNavigation.setVisibility(View.GONE);
    }

    public void resetUI(String defaultText) {
        timerTextView.setText(context.getString(R.string.timer_default));
        wordsTextView.setText(defaultText);
        wordsTextView.setVisibility(View.VISIBLE);
        scoreTextView.setVisibility(View.GONE);
        restartButton.setVisibility(View.GONE);
        bottomNavigation.setVisibility(View.VISIBLE);
        timerProgressBar.setProgress(timerProgressBar.getMax());
    }

    // Cursor
    public void updateCursor(SpannableStringBuilder spannableStringBuilder, int cursorPosition) {
        SpannableStringBuilder updatedString = new SpannableStringBuilder(spannableStringBuilder);

        if (cursorPosition < updatedString.length()) {
            applyCursorStyle(updatedString, cursorPosition);
        } else {
            appendCursor(updatedString);
        }

        wordsTextView.setText(updatedString);
    }

    private void applyCursorStyle(SpannableStringBuilder builder, int position) {
        int cursorColor = ContextCompat.getColor(context, COLOR_CURSOR);

        switch (cursorType) {
            case 1:
                builder.insert(position, "|");
                builder.setSpan(new ForegroundColorSpan(cursorColor),
                        position, position + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            case 2:
                builder.setSpan(new BackgroundColorSpan(cursorColor),
                        position, position + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.setSpan(new ForegroundColorSpan(Color.WHITE),
                        position, position + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            default:
                builder.setSpan(new UnderlineSpan(),
                        position, position + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void appendCursor(SpannableStringBuilder builder) {
        int cursorColor = ContextCompat.getColor(context, COLOR_CURSOR);

        switch (cursorType) {
            case 1:
                builder.append("|");
                builder.setSpan(new ForegroundColorSpan(cursorColor),
                        builder.length() - 1, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            case 2:
                builder.append(" ");
                builder.setSpan(new BackgroundColorSpan(cursorColor),
                        builder.length() - 1, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            default:
                builder.append("_");
                builder.setSpan(new UnderlineSpan(),
                        builder.length() - 1, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    // Text
    public void markCharCorrect(SpannableStringBuilder builder, int position) {
        builder.setSpan(
                new ForegroundColorSpan(ContextCompat.getColor(context, COLOR_CORRECT)),
                position, position + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );
    }

    public void markCharIncorrect(SpannableStringBuilder builder, int position) {
        builder.setSpan(
                new ForegroundColorSpan(ContextCompat.getColor(context, COLOR_INCORRECT)),
                position, position + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );
    }

    public void resetCharFormatting(SpannableStringBuilder builder, int position) {
        builder.setSpan(
                new ForegroundColorSpan(ContextCompat.getColor(context, COLOR_DEFAULT)),
                position, position + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );
    }

    // Results
    public void showResults(String originalText, String typedText, int score,
                            int correctChars, int totalChars, int errorCount) {
        BottomSheetDialog dialog = new BottomSheetDialog(context, R.style.BottomSheetDialogTheme);
        @SuppressLint("InflateParams") View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_game_details, null);
        dialog.setContentView(dialogView);

        setupResultsDialog(dialogView, originalText, typedText, score, correctChars, totalChars, errorCount);
        dialog.show();

        View parent = (View) dialogView.getParent();
        parent.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    private void setupResultsDialog(View dialogView, String originalText, String typedText,
                                    int score, int correctChars, int totalChars, int errorCount) {
        setupUserInfo(dialogView);
        setupStatsDisplay(dialogView, score, correctChars, totalChars, errorCount);
        setupTextDisplay(dialogView, originalText, typedText);
    }

    private void setupUserInfo(View dialogView) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userName = user != null && user.getDisplayName() != null ?
                user.getDisplayName() : context.getString(R.string.guest);

        TextView username = dialogView.findViewById(R.id.detail_username);
        username.setText(userName);

        LinearLayout deleteSection = dialogView.findViewById(R.id.deleteContainer);
        deleteSection.setVisibility(View.GONE);
    }

    private void setupStatsDisplay(View dialogView, int score, int correctChars,
                                   int totalChars, int errorCount) {
        double accuracy = (totalChars > 0) ? (double) correctChars / totalChars * 100 : 0;
        int speed = (int) ((correctChars / 30.0) * 60);

        TextView scoreView = dialogView.findViewById(R.id.detail_score);
        TextView speedView = dialogView.findViewById(R.id.detail_speed);
        TextView accuracyView = dialogView.findViewById(R.id.detail_accuracy);
        TextView errorsView = dialogView.findViewById(R.id.detail_errors);

        scoreView.setText(String.valueOf(score));
        speedView.setText(context.getString(R.string.speed_format, speed));
        accuracyView.setText(context.getString(R.string.accuracy_format, accuracy));
        errorsView.setText(String.valueOf(errorCount));
    }

    private void setupTextDisplay(View dialogView, String originalText, String typedText) {
        TextView originalTextView = dialogView.findViewById(R.id.detail_original_text);
        originalTextView.setText(formatColoredText(originalText, typedText));
    }

    private CharSequence formatColoredText(String original, String typed) {
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

    // Achievement
    public void showAchievementUnlocked(String achievementId) {
        AchievementInfo achievementInfo = getAchievementInfo(achievementId);

        BottomSheetDialog dialog = new BottomSheetDialog(context, R.style.BottomSheetDialogTheme);
        @SuppressLint("InflateParams") View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_achievement, null);
        dialog.setContentView(dialogView);

        setupAchievementDialog(dialogView, achievementInfo);
        dialog.show();

        View parent = (View) dialogView.getParent();
        parent.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    private AchievementInfo getAchievementInfo(String achievementId) {
        switch (achievementId) {
            case "beginner":
                return new AchievementInfo(
                        context.getString(R.string.achievement_beginner),
                        context.getString(R.string.achievement_beginner_desc),
                        R.drawable.home
                );
            case "pro":
                return new AchievementInfo(
                        context.getString(R.string.achievement_pro),
                        context.getString(R.string.achievement_pro_desc),
                        R.drawable.shield
                );
            case "speedster":
                return new AchievementInfo(
                        context.getString(R.string.achievement_speedster),
                        context.getString(R.string.achievement_speedster_desc),
                        R.drawable.clock
                );
            case "accuracy":
                return new AchievementInfo(
                        context.getString(R.string.achievement_accuracy),
                        context.getString(R.string.achievement_accuracy_desc),
                        R.drawable.check_circle
                );
            case "marathon":
                return new AchievementInfo(
                        context.getString(R.string.achievement_marathon),
                        context.getString(R.string.achievement_marathon_desc),
                        R.drawable.chevrons_right
                );
            case "master":
                return new AchievementInfo(
                        context.getString(R.string.achievement_master),
                        context.getString(R.string.achievement_master_desc),
                        R.drawable.keyboard
                );
            case "lightning":
                return new AchievementInfo(
                        context.getString(R.string.achievement_lightning),
                        context.getString(R.string.achievement_lightning_desc),
                        R.drawable.zap
                );
            case "sniper":
                return new AchievementInfo(
                        context.getString(R.string.achievement_sniper),
                        context.getString(R.string.achievement_sniper_desc),
                        R.drawable.target
                );
            case "legend":
                return new AchievementInfo(
                        context.getString(R.string.achievement_legend),
                        context.getString(R.string.achievement_legend_desc),
                        R.drawable.star
                );
            case "flawless":
                return new AchievementInfo(
                        context.getString(R.string.achievement_flawless),
                        context.getString(R.string.achievement_flawless_desc),
                        R.drawable.award
                );
            default:
                return new AchievementInfo("", "", R.drawable.award);
        }
    }

    private void setupAchievementDialog(View dialogView, AchievementInfo achievementInfo) {
        ImageView iconView = dialogView.findViewById(R.id.achievement_icon);
        TextView titleView = dialogView.findViewById(R.id.achievement_title);
        TextView subtitleView = dialogView.findViewById(R.id.achievement_name);
        TextView descView = dialogView.findViewById(R.id.achievement_description);

        iconView.setImageResource(achievementInfo.icon);
        titleView.setText(context.getString(R.string.achievement_unlocked));
        subtitleView.setText(achievementInfo.name);
        descView.setText(achievementInfo.description);
    }

    // Utility
    public void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    // Data
    private static class AchievementInfo {
        String name;
        String description;
        int icon;

        AchievementInfo(String name, String description, int icon) {
            this.name = name;
            this.description = description;
            this.icon = icon;
        }
    }
}