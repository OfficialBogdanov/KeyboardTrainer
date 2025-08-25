package com.example.keyboardtrainer.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.keyboardtrainer.components.FirebaseManager;
import com.example.keyboardtrainer.components.GameManager;
import com.example.keyboardtrainer.R;
import com.example.keyboardtrainer.components.UIManager;
import com.example.keyboardtrainer.dialog.DifficultySettingsDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements GameManager.GameCallback, FirebaseManager.AchievementCallback {
    private static final String DIFFICULTY_LEVEL_KEY = "difficultyLevel";
    private static final String PREFS_NAME = "KeyboardTrainerPrefs";
    private static final String CURSOR_TYPE_KEY = "cursorType";
    private static final String TIMER_STYLE_KEY = "timerStyle";

    private UIManager uiManager;
    private GameManager gameManager;
    private FirebaseManager firebaseManager;

    // Lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupManagers();
        setupBottomNavigation();
        setupEventListeners();
        loadPreferences();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPreferences();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gameManager.cancelTimer();
    }

    // UI
    private void initializeViews() {
        TextView wordsTextView = findViewById(R.id.wordsTextView);
        TextView timerTextView = findViewById(R.id.timerTextView);
        TextView scoreTextView = findViewById(R.id.scoreTextView);
        ProgressBar timerProgressBar = findViewById(R.id.timerProgressBar);
        Button restartButton = findViewById(R.id.restartButton);
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);

        uiManager = new UIManager(this, wordsTextView, timerTextView, scoreTextView,
                timerProgressBar, restartButton, bottomNavigation);
    }

    // Manager
    private void setupManagers() {
        gameManager = new GameManager(this, uiManager, this);
        firebaseManager = new FirebaseManager();
    }

    // Event
    private void setupEventListeners() {
        TextView wordsTextView = findViewById(R.id.wordsTextView);
        Button restartButton = findViewById(R.id.restartButton);

        wordsTextView.setOnClickListener(v -> gameManager.startGame());
        restartButton.setOnClickListener(v -> gameManager.restartGame());

        wordsTextView.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_SPACE) {
                gameManager.handleSpace();
                return true;
            }
            return false;
        });
    }

    // Preferences
    private void loadPreferences() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int cursorType = prefs.getInt(CURSOR_TYPE_KEY, 0);
        int timerStyle = prefs.getInt(TIMER_STYLE_KEY, 0);
        int difficultyLevel = prefs.getInt(DIFFICULTY_LEVEL_KEY, DifficultySettingsDialog.DIFFICULTY_NORMAL);

        uiManager.setCursorType(cursorType);
        uiManager.setTimerStyle(timerStyle);
        gameManager.setDifficulty(difficultyLevel);
    }

    // Navigation
    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.navigation_game);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_stats) {
                startActivity(new Intent(this, StatsActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            } else if (id == R.id.navigation_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            } else if (id == R.id.navigation_achievements) {
                if (!firebaseManager.isUserAuthenticated()) {
                    uiManager.showToast(getString(R.string.achievements_anonymous_message));
                    return true;
                }
                startActivity(new Intent(this, AchievementsActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            }
            return id == R.id.navigation_game;
        });
    }

    // Key
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (!gameManager.isGameStarted()) return super.dispatchKeyEvent(event);

        int action = event.getAction();
        int keyCode = event.getKeyCode();

        if (action == KeyEvent.ACTION_UP) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DEL:
                    gameManager.handleDelete();
                    return true;
                case KeyEvent.KEYCODE_SPACE:
                    gameManager.handleSpace();
                    return true;
                default:
                    int unicodeChar = event.getUnicodeChar(event.getMetaState());
                    if (unicodeChar != 0) {
                        char c = (char) unicodeChar;
                        if (Character.isLetterOrDigit(c) || isAllowedPunctuation(c)) {
                            gameManager.handleCharacter(c);
                            return true;
                        }
                    }
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private boolean isAllowedPunctuation(char c) {
        switch (c) {
            case '@':
            case '#':
            case '$':
            case '%':
            case '^':
            case '&':
            case '*':
            case '(':
            case ')':
            case ',':
            case '-':
            case '+':
            case '=':
            case '{':
            case '}':
            case '[':
            case ']':
            case '|':
            case ':':
            case ';':
            case '<':
            case '>':
            case '.':
            case '?':
            case '/':
            case '~':
            case '`':
                return true;
            default:
                return false;
        }
    }

    // Game
    @Override
    public void onGameEnded(String originalText, String typedText, int score,
                            int correctChars, int totalChars, int errorCount) {
        uiManager.showResults(originalText, typedText, score, correctChars, totalChars, errorCount);
        firebaseManager.saveGameStats(originalText, typedText, score, correctChars, totalChars, errorCount);
        firebaseManager.checkAndUnlockAchievements(score, correctChars, totalChars, errorCount, this);
    }

    @Override
    public void onTimerTick(int millisUntilFinished) {
    }

    // Achievement
    @Override
    public void onAchievementUnlocked(String achievementId) {
        uiManager.showAchievementUnlocked(achievementId);
    }
}