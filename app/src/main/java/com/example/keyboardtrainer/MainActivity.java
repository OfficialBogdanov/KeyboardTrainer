package com.example.keyboardtrainer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements CustomKeyboard.KeyboardListener {
    private int cursorPosition = 0;
    private SpannableStringBuilder spannableStringBuilder;
    private TextView wordsTextView, timerTextView, scoreTextView;
    private final List<Integer> wordEndings = new ArrayList<>();
    private final List<Integer> wordStarts = new ArrayList<>();
    private int score = 0;
    private LinearLayout keyboardLayout;
    private Button restartButton;
    private boolean gameStarted = false;
    private CountDownTimer timer;

    private String originalText = "";
    private StringBuilder typedText = new StringBuilder();
    private int correctChars = 0;
    private int totalChars = 0;
    private int errorCount = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        timerTextView = findViewById(R.id.timerTextView);
        wordsTextView = findViewById(R.id.wordsTextView);
        keyboardLayout = findViewById(R.id.keyboardLayout);
        restartButton = findViewById(R.id.restartButton);
        scoreTextView = findViewById(R.id.scoreTextView);

        wordsTextView.setOnClickListener(v -> startGame());
        restartButton.setOnClickListener(v -> restartGame());

        new CustomKeyboard(this, keyboardLayout, this);

        setupBottomNavigation();
        setupKeyboardBehavior();
    }

    private void startGame() {
        if (gameStarted) return;

        score = 0;
        cursorPosition = 0;
        correctChars = 0;
        totalChars = 0;
        errorCount = 0;
        typedText.setLength(0);

        gameStarted = true;
        wordsTextView.setVisibility(View.VISIBLE);
        keyboardLayout.setVisibility(View.VISIBLE);
        scoreTextView.setVisibility(View.GONE);
        restartButton.setVisibility(View.GONE);

        WordGenerator wordGenerator = new WordGenerator();
        WordGenerator.GeneratedWords generatedWords = wordGenerator.generateWords(10);

        originalText = generatedWords.getText();
        spannableStringBuilder = new SpannableStringBuilder(originalText);
        wordStarts.clear();
        wordStarts.addAll(generatedWords.getWordStarts());
        wordEndings.clear();
        wordEndings.addAll(generatedWords.getWordEndings());

        updateCursor();

        timer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerTextView.setText("Осталось: " + millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                endGame();
            }
        }.start();
    }

    private void endGame() {
        gameStarted = false;
        wordsTextView.setVisibility(View.GONE);
        keyboardLayout.setVisibility(View.GONE);

        double accuracy = (totalChars > 0) ? (double)correctChars / totalChars * 100 : 0;

        scoreTextView.setText(String.format(Locale.getDefault(),
                "Счет: %d\nТочность: %.1f%%\nОшибок: %d",
                score, accuracy, errorCount));
        scoreTextView.setVisibility(View.VISIBLE);
        restartButton.setVisibility(View.VISIBLE);

        saveGameStats();
    }

    private void restartGame() {
        if (timer != null) {
            timer.cancel();
        }

        gameStarted = false;
        cursorPosition = 0;
        score = 0;
        correctChars = 0;
        totalChars = 0;
        errorCount = 0;
        typedText.setLength(0);

        timerTextView.setText("Таймер: 30");
        wordsTextView.setText("Нажмите сюда");
        wordsTextView.setVisibility(View.VISIBLE);
        keyboardLayout.setVisibility(View.VISIBLE);
        scoreTextView.setVisibility(View.GONE);
        restartButton.setVisibility(View.GONE);

    }

    private void saveGameStats() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        Map<String, Object> gameData = new HashMap<>();
        gameData.put("userId", currentUser.getUid());
        gameData.put("score", score);
        gameData.put("correctChars", correctChars);
        gameData.put("totalChars", totalChars);
        gameData.put("errors", errorCount);
        gameData.put("originalText", originalText);
        gameData.put("typedText", typedText.toString());
        gameData.put("timestamp", Timestamp.now());

        FirebaseFirestore.getInstance().collection("scores")
                .add(gameData)
                .addOnSuccessListener(documentReference ->
                        Log.d("Stats", "Game stats saved"))
                .addOnFailureListener(e ->
                        Log.w("Stats", "Error saving stats", e));
    }

    @Override
    public void onCharacterEntered(char c) {
        if (!gameStarted || cursorPosition >= originalText.length()) return;

        totalChars++;
        typedText.append(c);

        if (spannableStringBuilder.charAt(cursorPosition) == c) {
            spannableStringBuilder.setSpan(new ForegroundColorSpan(Color.GREEN), cursorPosition, cursorPosition + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            score++;
            correctChars++;
        } else {
            spannableStringBuilder.setSpan(new ForegroundColorSpan(Color.RED), cursorPosition, cursorPosition + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            errorCount++;
        }
        cursorPosition++;
        updateCursor();
    }

    @Override
    public void onDeletePressed() {
        if (!gameStarted || cursorPosition == 0) return;

        cursorPosition--;
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Color.BLACK), cursorPosition, cursorPosition + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        if (cursorPosition < typedText.length()) {
            typedText.deleteCharAt(cursorPosition);
        }

        updateCursor();
    }

    @Override
    public void onSpacePressed() {
        if (!gameStarted) return;

        int currentWordIndex = -1;
        for (int i = 0; i < wordStarts.size(); i++) {
            if (cursorPosition >= wordStarts.get(i) && cursorPosition <= wordEndings.get(i)) {
                currentWordIndex = i;
                break;
            }
        }

        if (currentWordIndex != -1) {
            int wordStart = wordStarts.get(currentWordIndex);
            int NextWordStart = wordStarts.get(currentWordIndex + 1);
            int wordEnd = wordEndings.get(currentWordIndex);

            for (int i = cursorPosition; i < NextWordStart; i++) {
                if (i < originalText.length()) {
                    if (i >= typedText.length()) {
                        typedText.append('_');
                    } else {
                        typedText.setCharAt(i, ' ');
                    }

                    if (originalText.charAt(i) != ' ') {
                        spannableStringBuilder.setSpan(new ForegroundColorSpan(Color.RED),
                                i, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        errorCount++;
                    }
                    totalChars++;
                }
            }

            cursorPosition = NextWordStart;

            if (cursorPosition < originalText.length() && originalText.charAt(cursorPosition) == ' ') {
                if (cursorPosition >= typedText.length()) {
                    typedText.append(' ');
                } else {
                    typedText.setCharAt(cursorPosition, ' ');
                }
                cursorPosition++;
                totalChars++;
            }
        } else {
            if (cursorPosition < originalText.length() && originalText.charAt(cursorPosition) == ' ') {
                if (cursorPosition >= typedText.length()) {
                    typedText.append(' ');
                } else {
                    typedText.setCharAt(cursorPosition, ' ');
                }
                cursorPosition++;
                totalChars++;
            }
        }

        updateCursor();
    }

    private void adjustWordPositions(int offset) {
        for (int i = 0; i < wordEndings.size(); i++) {
            if (wordEndings.get(i) >= cursorPosition) {
                wordEndings.set(i, wordEndings.get(i) + offset);
            }
        }
        for (int i = 0; i < wordStarts.size(); i++) {
            if (wordStarts.get(i) >= cursorPosition) {
                wordStarts.set(i, wordStarts.get(i) + offset);
            }
        }
    }

    private void markIncorrectChars(int start, int end) {
        for (int i = start; i < end; i++) {
            if (spannableStringBuilder.charAt(i) != ' ') {
                spannableStringBuilder.setSpan(new ForegroundColorSpan(Color.RED),
                        i, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                if (i < originalText.length() && i < typedText.length()) {
                    if (originalText.charAt(i) != typedText.charAt(i)) {
                        errorCount++;
                    }
                }
            }
        }
    }

    private void updateCursor() {
        SpannableStringBuilder updatedString = new SpannableStringBuilder(spannableStringBuilder);
        if (cursorPosition < updatedString.length()) {
            updatedString.setSpan(new UnderlineSpan(), cursorPosition, cursorPosition + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            updatedString.append("_");
            updatedString.setSpan(new UnderlineSpan(), cursorPosition, cursorPosition + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        wordsTextView.setText(updatedString);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
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
                findViewById(R.id.bottom_navigation).setVisibility(View.GONE);
            } else {
                findViewById(R.id.bottom_navigation).setVisibility(View.VISIBLE);
            }
        });
    }
}