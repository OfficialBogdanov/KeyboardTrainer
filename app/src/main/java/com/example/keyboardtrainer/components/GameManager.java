package com.example.keyboardtrainer.components;

import android.content.Context;
import android.os.CountDownTimer;
import android.text.SpannableStringBuilder;

import com.example.keyboardtrainer.R;
import com.example.keyboardtrainer.dialog.DifficultySettingsDialog;

import java.util.ArrayList;
import java.util.List;

public class GameManager {
    /** @noinspection unused*/ // Constants
    private static final int GAME_DURATION = 30000;
    private static final int TIMER_INTERVAL = 100;

    // Variables
    private int currentDifficulty = DifficultySettingsDialog.DIFFICULTY_NORMAL;
    private final UIManager uiManager;
    private final GameCallback callback;
    private final Context context;
    private int cursorPosition = 0;
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

    // Interface
    public interface GameCallback {
        void onGameEnded(String originalText, String typedText, int score,
                         int correctChars, int totalChars, int errorCount);
        void onTimerTick(int millisUntilFinished);
    }

    // Constructor
    public GameManager(Context context, UIManager uiManager, GameCallback callback) {
        this.context = context;
        this.uiManager = uiManager;
        this.callback = callback;
    }

    // Difficulty
    public void setDifficulty(int difficulty) {
        this.currentDifficulty = difficulty;
    }

    // Game
    public void startGame() {
        if (gameStarted) return;

        resetGameStats();
        generateNewText();
        setupGameUI();
        startCountdownTimer();
        uiManager.showKeyboard();
    }

    public void restartGame() {
        resetGameState();
        uiManager.resetUI(context.getString(R.string.tap_to_start));
        generateNewText();
    }

    private void resetGameStats() {
        score = 0;
        cursorPosition = 0;
        correctChars = 0;
        totalChars = 0;
        errorCount = 0;
        typedText.setLength(0);
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

    private void endGame() {
        gameStarted = false;
        uiManager.hideKeyboard();
        callback.onGameEnded(originalText, typedText.toString(), score,
                correctChars, totalChars, errorCount);
        uiManager.resetUI(context.getString(R.string.tap_to_start));
    }

    // Text
    private void generateNewText() {
        WordGenerator wordGenerator = new WordGenerator();
        WordGenerator.GeneratedWords generatedWords =
                wordGenerator.generateWords(getSentenceCountForDifficulty(), currentDifficulty);

        originalText = generatedWords.getText();
        spannableStringBuilder = new SpannableStringBuilder(originalText);
        wordStarts.clear();
        wordStarts.addAll(generatedWords.getWordStarts());
        wordEndings.clear();
        wordEndings.addAll(generatedWords.getWordEndings());
    }

    /** @noinspection DuplicateBranchesInSwitch*/
    private int getSentenceCountForDifficulty() {
        switch (currentDifficulty) {
            case DifficultySettingsDialog.DIFFICULTY_EASY: return 15;
            case DifficultySettingsDialog.DIFFICULTY_NORMAL: return 20;
            case DifficultySettingsDialog.DIFFICULTY_HARD: return 25;
            default: return 20;
        }
    }

    // UI
    private void setupGameUI() {
        gameStarted = true;
        uiManager.setupGameUI();
        updateCursor();
    }

    private void updateCursor() {
        uiManager.updateCursor(spannableStringBuilder, cursorPosition);
    }

    // Timer
    private void startCountdownTimer() {
        int gameDuration = getGameDurationForDifficulty();
        uiManager.updateTimerProgress(gameDuration);

        timer = new CountDownTimer(gameDuration, TIMER_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                uiManager.updateTimerText(context.getString(R.string.time_remaining, seconds));
                uiManager.updateTimerProgress((int) millisUntilFinished);
                callback.onTimerTick((int) millisUntilFinished);
            }

            @Override
            public void onFinish() {
                uiManager.updateTimerProgress(0);
                endGame();
            }
        }.start();
    }

    /** @noinspection DuplicateBranchesInSwitch*/
    private int getGameDurationForDifficulty() {
        switch (currentDifficulty) {
            case DifficultySettingsDialog.DIFFICULTY_EASY: return 45000;
            case DifficultySettingsDialog.DIFFICULTY_NORMAL: return 30000;
            case DifficultySettingsDialog.DIFFICULTY_HARD: return 20000;
            default: return 30000;
        }
    }

    public void cancelTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

    // Input
    public void handleCharacter(char c) {
        if (cursorPosition >= originalText.length()) return;

        totalChars++;
        typedText.append(c);

        char originalChar = originalText.charAt(cursorPosition);
        boolean isCorrect = (originalChar == c);

        if (isCorrect) {
            uiManager.markCharCorrect(spannableStringBuilder, cursorPosition);
            score += getPointsForCharacter(c);
            correctChars++;
        } else {
            uiManager.markCharIncorrect(spannableStringBuilder, cursorPosition);
            errorCount++;
        }
        cursorPosition++;
        updateCursor();
    }

    private int getPointsForCharacter(char c) {
        if (Character.isLetterOrDigit(c)) {
            return 1;
        } else if (Character.isWhitespace(c)) {
            return 1;
        } else {
            return currentDifficulty == DifficultySettingsDialog.DIFFICULTY_HARD ? 3 : 2;
        }
    }

    public void handleDelete() {
        if (cursorPosition == 0) return;

        cursorPosition--;
        uiManager.resetCharFormatting(spannableStringBuilder, cursorPosition);
        if (cursorPosition < typedText.length()) {
            typedText.deleteCharAt(cursorPosition);
        }
        updateCursor();
    }

    public void handleSpace() {
        if (!gameStarted) return;

        if (cursorPosition < originalText.length()) {
            typedText.append(' ');
            totalChars++;

            if (originalText.charAt(cursorPosition) == ' ') {
                uiManager.markCharCorrect(spannableStringBuilder, cursorPosition);
                score += getPointsForCharacter(' ');
                correctChars++;
            } else {
                uiManager.markCharIncorrect(spannableStringBuilder, cursorPosition);
                errorCount++;
            }
            cursorPosition++;
            updateCursor();
        }
    }

    /** @noinspection unused*/ // Utility
    private int findCurrentWordIndex() {
        for (int i = 0; i < wordStarts.size(); i++) {
            if (cursorPosition >= wordStarts.get(i) && cursorPosition <= wordEndings.get(i)) {
                return i;
            }
        }
        return -1;
    }

    // Getter
    public boolean isGameStarted() {
        return gameStarted;
    }

    /** @noinspection unused*/
    public int getScore() {
        return score;
    }

    /** @noinspection unused*/
    public int getCorrectChars() {
        return correctChars;
    }

    /** @noinspection unused*/
    public int getTotalChars() {
        return totalChars;
    }

    /** @noinspection unused*/
    public int getErrorCount() {
        return errorCount;
    }
}