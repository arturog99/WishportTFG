package com.wishport.frontend.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private static final String PREF_NAME = "WishPortPrefs";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";

    private SharedPreferences preferences;

    public TokenManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        preferences.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return preferences.getString(KEY_TOKEN, null);
    }

    public void clearToken() {
        preferences.edit().remove(KEY_TOKEN).apply();
    }

    public void saveUserId(Integer userId) {
        preferences.edit().putInt(KEY_USER_ID, userId).apply();
    }

    public Integer getUserId() {
        return preferences.getInt(KEY_USER_ID, -1);
    }

    public void clearAll() {
        preferences.edit().clear().apply();
    }
}