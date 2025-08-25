package com.example.keyboardtrainer.activity;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.keyboardtrainer.components.LocaleHelper;

public class BaseActivity extends AppCompatActivity {

    // Localization
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    // Lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.setLocale(this, LocaleHelper.getLanguage(this), false);
        super.onCreate(savedInstanceState);
    }
}