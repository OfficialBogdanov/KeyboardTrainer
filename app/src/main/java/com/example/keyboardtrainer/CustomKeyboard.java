package com.example.keyboardtrainer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Button;
import android.widget.LinearLayout;

public class CustomKeyboard {
    private final Context context;
    private final LinearLayout keyboardLayout;
    private boolean isUpperCase = false;
    private final KeyboardListener listener;

    public interface KeyboardListener {
        void onCharacterEntered(char c);
        void onDeletePressed();
        void onSpacePressed();
    }

    public CustomKeyboard(Context context, LinearLayout keyboardLayout, KeyboardListener listener) {
        this.context = context;
        this.keyboardLayout = keyboardLayout;
        this.listener = listener;
        createKeyboard();
    }

    @SuppressLint("SetTextI18n")
    private void createKeyboard() {
        keyboardLayout.removeAllViews();

        String[] rows = {"qwertyuiop", "asdfghjkl", "zxcvbnm"};
        for (String row : rows) {
            LinearLayout rowLayout = new LinearLayout(context);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            for (char c : row.toCharArray()) {
                Button keyButton = new Button(context);
                keyButton.setText(String.valueOf(isUpperCase ? Character.toUpperCase(c) : c));
                keyButton.setAllCaps(isUpperCase);
                keyButton.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                keyButton.setOnClickListener(view -> listener.onCharacterEntered(isUpperCase ? Character.toUpperCase(c) : c));
                rowLayout.addView(keyButton);
            }
            keyboardLayout.addView(rowLayout);
        }

        LinearLayout spaceRow = new LinearLayout(context);
        spaceRow.setOrientation(LinearLayout.HORIZONTAL);

        Button shiftButton = new Button(context);
        shiftButton.setText("⇧");
        shiftButton.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        shiftButton.setOnClickListener(view -> toggleCase());
        spaceRow.addView(shiftButton);

        Button spaceButton = new Button(context);
        spaceButton.setText("Space");
        spaceButton.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 4));
        spaceButton.setOnClickListener(view -> listener.onSpacePressed());
        spaceRow.addView(spaceButton);

        Button deleteButton = new Button(context);
        deleteButton.setText("Del");
        deleteButton.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        deleteButton.setOnClickListener(view -> listener.onDeletePressed());
        spaceRow.addView(deleteButton);

        keyboardLayout.addView(spaceRow);
    }

    private void toggleCase() {
        isUpperCase = !isUpperCase;
        createKeyboard();
    }
}

