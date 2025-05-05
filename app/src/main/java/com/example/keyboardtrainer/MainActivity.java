package com.example.keyboardtrainer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements CustomKeyboard.KeyboardListener {

    private static final String PREFS_NAME = "KeyboardTrainerPrefs";
    private static final String CURSOR_TYPE_KEY = "cursorType";
    private static final String TIMER_STYLE_KEY = "timerStyle";

    private TextView wordsTextView, timerTextView, scoreTextView;
    private ProgressBar timerProgressBar;
    private LinearLayout keyboardLayout;
    private Button restartButton;
    private BottomNavigationView bottomNavigation;
    private int cursorPosition = 0;
    private int cursorType = 0;
    private int timerStyle = 0;
    private SpannableStringBuilder spannableStringBuilder;
    private final List<Integer> wordEndings = new ArrayList<>();
    private final List<Integer> wordStarts = new ArrayList<>();
    private int score = 0;
    private boolean gameStarted = false;
    private CountDownTimer timer;
    private String originalText = "";
    private final StringBuilder typedText = new StringBuilder();
    private int correctChars = 0;
    private int totalChars = 0;
    private int errorCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setVisibility(View.VISIBLE);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        cursorType = prefs.getInt(CURSOR_TYPE_KEY, 0);
        timerStyle = prefs.getInt(TIMER_STYLE_KEY, 0);

        initViews();
        setupKeyboardBehavior();
        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        cursorType = prefs.getInt(CURSOR_TYPE_KEY, 0);
        timerStyle = prefs.getInt(TIMER_STYLE_KEY, 0);
        updateTimerStyle();
        if (gameStarted) {
            updateCursor();
        }
    }

    private void initViews() {
        timerTextView = findViewById(R.id.timerTextView);
        timerProgressBar = findViewById(R.id.timerProgressBar);
        wordsTextView = findViewById(R.id.wordsTextView);
        keyboardLayout = findViewById(R.id.keyboardLayout);
        restartButton = findViewById(R.id.restartButton);
        scoreTextView = findViewById(R.id.scoreTextView);

        updateTimerStyle();

        wordsTextView.setOnClickListener(v -> startGame());
        restartButton.setOnClickListener(v -> restartGame());

        new CustomKeyboard(this, keyboardLayout, this);
    }

    private void updateTimerStyle() {
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

    private void startGame() {
        if (gameStarted) return;

        bottomNavigation.setVisibility(View.GONE);
        keyboardLayout.setVisibility(View.VISIBLE);

        resetGameStats();
        generateNewText();
        setupGameUI();
        startCountdownTimer();
    }

    private void resetGameStats() {
        score = 0;
        cursorPosition = 0;
        correctChars = 0;
        totalChars = 0;
        errorCount = 0;
        typedText.setLength(0);
    }

    private void generateNewText() {
        WordGenerator wordGenerator = new WordGenerator();
        WordGenerator.GeneratedWords generatedWords = wordGenerator.generateWords(20);

        originalText = generatedWords.getText();
        spannableStringBuilder = new SpannableStringBuilder(originalText);
        wordStarts.clear();
        wordStarts.addAll(generatedWords.getWordStarts());
        wordEndings.clear();
        wordEndings.addAll(generatedWords.getWordEndings());
    }

    private void setupGameUI() {
        gameStarted = true;
        wordsTextView.setVisibility(View.VISIBLE);
        keyboardLayout.setVisibility(View.VISIBLE);
        scoreTextView.setVisibility(View.GONE);
        restartButton.setVisibility(View.GONE);
        updateCursor();
    }

    private void startCountdownTimer() {
        final int totalTime = 30000;
        timerProgressBar.setMax(totalTime);
        timerProgressBar.setProgress(totalTime);

        timer = new CountDownTimer(totalTime, 100) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                timerTextView.setText("Осталось: " + seconds);

                if (timerProgressBar.getVisibility() == View.VISIBLE) {
                    timerProgressBar.setProgress((int) millisUntilFinished);
                }
            }

            @Override
            public void onFinish() {
                endGame();
            }
        }.start();
    }

    private void endGame() {
        gameStarted = false;

        final String finalOriginalText = originalText;
        final String finalTypedText = typedText.toString();
        final int finalScore = score;
        final int finalCorrectChars = correctChars;
        final int finalTotalChars = totalChars;
        final int finalErrorCount = errorCount;

        resetUI();

        saveGameStats(finalOriginalText, finalTypedText, finalScore, finalCorrectChars, finalTotalChars, finalErrorCount);

        showResults(finalOriginalText, finalTypedText, finalScore, finalCorrectChars, finalTotalChars, finalErrorCount);
    }

    private void saveGameStats(String originalText, String typedText, int score,
                               int correctChars, int totalChars, int errorCount) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        Map<String, Object> gameData = new HashMap<>();
        gameData.put("userId", user.getUid());
        gameData.put("score", score);
        gameData.put("correctChars", correctChars);
        gameData.put("totalChars", totalChars);
        gameData.put("errors", errorCount);
        gameData.put("originalText", originalText);
        gameData.put("typedText", typedText);
        gameData.put("timestamp", Timestamp.now());

        FirebaseFirestore.getInstance().collection("scores")
                .add(gameData)
                .addOnSuccessListener(docRef -> Log.d("Game", "Stats saved"))
                .addOnFailureListener(e -> Log.w("Game", "Save error", e));
    }


    private void showResults(String originalText, String typedText, int score,
                             int correctChars, int totalChars, int errorCount) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_game_details, null);

        TextView detailUsername = dialogView.findViewById(R.id.detail_username);
        TextView detailScore = dialogView.findViewById(R.id.detail_score);
        TextView detailSpeed = dialogView.findViewById(R.id.detail_speed);
        TextView detailAccuracy = dialogView.findViewById(R.id.detail_accuracy);
        TextView detailErrors = dialogView.findViewById(R.id.detail_errors);
        TextView detailOriginalText = dialogView.findViewById(R.id.detail_original_text);
        TextView detailDate = dialogView.findViewById(R.id.detail_date);
        MaterialButton btnOk = dialogView.findViewById(R.id.btn_ok);
        MaterialButton btnDelete = dialogView.findViewById(R.id.btn_delete);

        double accuracy = (totalChars > 0) ? (double) correctChars / totalChars * 100 : 0;
        int speed = (int) ((correctChars / 30.0) * 60);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(document -> {
                        String username = document.getString("username");
                        detailUsername.setText(username != null ? username : "Аноним");
                    });
        } else {
            detailUsername.setText("Гость");
        }

        detailScore.setText(String.valueOf(score));
        detailSpeed.setText(String.format(Locale.getDefault(), "%d зн./мин", speed));
        detailAccuracy.setText(String.format(Locale.getDefault(), "%.1f%%", accuracy));
        detailErrors.setText(String.valueOf(errorCount));

        SpannableStringBuilder coloredText = new SpannableStringBuilder(originalText);
        for (int i = 0; i < typedText.length() && i < originalText.length(); i++) {
            int color = (originalText.charAt(i) == typedText.charAt(i)) ? Color.GREEN : Color.RED;
            coloredText.setSpan(new ForegroundColorSpan(color), i, i+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        detailOriginalText.setText(coloredText);

        detailDate.setText(new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(new Date()));

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        btnOk.setOnClickListener(v -> dialog.dismiss());

        boolean isCurrentUser = user != null && user.getUid().equals(user.getUid());
        btnDelete.setVisibility(View.GONE);

        dialog.show();
    }


    @SuppressLint("SetTextI18n")
    private void resetUI() {
        timerTextView.setText("Таймер: 30");
        wordsTextView.setText("Нажмите сюда");
        wordsTextView.setVisibility(View.VISIBLE);
        keyboardLayout.setVisibility(View.GONE);
        scoreTextView.setVisibility(View.GONE);
        restartButton.setVisibility(View.GONE);
        bottomNavigation.setVisibility(View.VISIBLE);

        wordStarts.clear();
        wordEndings.clear();
        wordStarts.add(0);
        wordEndings.add("Нажмите сюда".length());

        if (timerProgressBar != null) {
            timerProgressBar.setProgress(timerProgressBar.getMax());
        }

        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void restartGame() {
        resetGameState();
        resetUI();
        updateTimerStyle();

        generateNewText();
    }

    private void resetGameState() {
        gameStarted = false;
        cursorPosition = 0;
        score = 0;
        correctChars = 0;
        totalChars = 0;
        errorCount = 0;
        typedText.setLength(0);
    }
    @Override
    public void onCharacterEntered(char c) {
        if (!gameStarted || cursorPosition >= originalText.length()) return;

        totalChars++;
        typedText.append(c);

        if (spannableStringBuilder.charAt(cursorPosition) == c) {
            markCharCorrect(cursorPosition);
            score++;
            correctChars++;
        } else {
            markCharIncorrect(cursorPosition);
            errorCount++;
        }
        cursorPosition++;
        updateCursor();
    }

    private void markCharCorrect(int position) {
        spannableStringBuilder.setSpan(
                new ForegroundColorSpan(Color.GREEN),
                position, position + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );
    }

    private void markCharIncorrect(int position) {
        spannableStringBuilder.setSpan(
                new ForegroundColorSpan(Color.RED),
                position, position + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );
    }

    @Override
    public void onDeletePressed() {
        if (!gameStarted || cursorPosition == 0) return;

        cursorPosition--;
        resetCharFormatting(cursorPosition);
        if (cursorPosition < typedText.length()) {
            typedText.deleteCharAt(cursorPosition);
        }
        updateCursor();
    }

    private void resetCharFormatting(int position) {
        spannableStringBuilder.setSpan(
                new ForegroundColorSpan(Color.BLACK),
                position, position + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );
    }

    @Override
    public void onSpacePressed() {
        if (!gameStarted) return;

        int currentWordIndex = findCurrentWordIndex();
        if (currentWordIndex != -1 && currentWordIndex + 1 < wordStarts.size()) {
            handleWordSkip(currentWordIndex);
        }
        handleTrailingSpace();
        updateCursor();
    }

    private int findCurrentWordIndex() {
        for (int i = 0; i < wordStarts.size(); i++) {
            if (cursorPosition >= wordStarts.get(i) && cursorPosition <= wordEndings.get(i)) {
                return i;
            }
        }
        return -1;
    }

    private void handleWordSkip(int currentWordIndex) {
        int nextWordStart = wordStarts.get(currentWordIndex + 1);
        for (int i = cursorPosition; i < nextWordStart; i++) {
            if (i < originalText.length()) {
                updateTypedText(i);
                if (originalText.charAt(i) != ' ') {
                    markCharIncorrect(i);
                    errorCount++;
                }
                totalChars++;
            }
        }
        cursorPosition = nextWordStart;
    }

    private void updateTypedText(int position) {
        if (position >= typedText.length()) {
            typedText.append(' ');
        } else {
            typedText.setCharAt(position, ' ');
        }
    }

    private void handleTrailingSpace() {
        if (cursorPosition < originalText.length() && originalText.charAt(cursorPosition) == ' ') {
            updateTypedText(cursorPosition);
            cursorPosition++;
            totalChars++;
        }
    }

    private void updateCursor() {
        SpannableStringBuilder updatedString = new SpannableStringBuilder(spannableStringBuilder);

        if (cursorPosition < updatedString.length()) {
            applyCursorStyle(updatedString, cursorPosition);
        } else {
            appendCursor(updatedString);
        }

        wordsTextView.setText(updatedString);
    }

    private void applyCursorStyle(SpannableStringBuilder builder, int position) {
        switch (cursorType) {
            case 1:
                builder.insert(position, "|");
                builder.setSpan(new ForegroundColorSpan(Color.GRAY),
                        position, position + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            case 2:
                builder.setSpan(new BackgroundColorSpan(Color.GRAY),
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
        switch (cursorType) {
            case 1:
                builder.append("|");
                builder.setSpan(new ForegroundColorSpan(Color.GRAY),
                        cursorPosition, cursorPosition + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            case 2:
                builder.append(" ");
                builder.setSpan(new BackgroundColorSpan(Color.GRAY),
                        cursorPosition, cursorPosition + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            default:
                builder.append("_");
                builder.setSpan(new UnderlineSpan(),
                        cursorPosition, cursorPosition + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_game) {
                return true;
            } else if (id == R.id.navigation_stats) {
                startActivity(new Intent(this, StatsActivity.class));
                return true;
            } else if (id == R.id.navigation_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return false;
        });
    }

    private void setupKeyboardBehavior() {
        keyboardLayout.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            keyboardLayout.getWindowVisibleDisplayFrame(r);
            int screenHeight = keyboardLayout.getRootView().getHeight();
            int keypadHeight = screenHeight - r.bottom;

            if (keypadHeight > screenHeight * 0.15) {
                if (!gameStarted) {
                    bottomNavigation.setVisibility(View.GONE);
                }
            } else {
                if (!gameStarted) {
                    bottomNavigation.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}