package com.example.keyboardtrainer.components;

import android.content.Context;
import android.content.SharedPreferences;

public class SubscriptionManager {
    private static final String PREFS_NAME = "subscription_prefs";
    private static final String KEY_IS_SUBSCRIBED = "is_subscribed";
    private static final String KEY_SUBSCRIPTION_EXPIRY = "subscription_expiry";

    private final SharedPreferences prefs;

    public SubscriptionManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isSubscribed() {
        long expiryDate = prefs.getLong(KEY_SUBSCRIPTION_EXPIRY, 0);
        boolean isSubscribed = prefs.getBoolean(KEY_IS_SUBSCRIBED, false);

        if (isSubscribed && expiryDate > System.currentTimeMillis()) {
            return true;
        } else if (isSubscribed && expiryDate <= System.currentTimeMillis()) {
            setSubscribed(false, 0);
            return false;
        }
        return false;
    }

    public void setSubscribed(boolean subscribed, long expiryDate) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_SUBSCRIBED, subscribed);
        editor.putLong(KEY_SUBSCRIPTION_EXPIRY, expiryDate);
        editor.apply();
    }

    public void activateSubscription(int months) {
        long currentTime = System.currentTimeMillis();
        long expiryDate = currentTime + (months * 30L * 24 * 60 * 60 * 1000);
        setSubscribed(true, expiryDate);
    }
}