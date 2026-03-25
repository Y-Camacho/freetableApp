package com.example.freetableapp.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.freetableapp.data.model.User;
import com.google.gson.Gson;

public class SessionManager {
    private static final String PREFS = "freetable_session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER = "user_json";

    private final SharedPreferences sharedPreferences;
    private final Gson gson = new Gson();

    public SessionManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void saveSession(String token, User user) {
        sharedPreferences.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_USER, gson.toJson(user))
                .apply();
    }

    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    public User getUser() {
        String userJson = sharedPreferences.getString(KEY_USER, null);
        return userJson == null ? null : gson.fromJson(userJson, User.class);
    }

    public boolean isLoggedIn() {
        String token = getToken();
        return token != null && !token.trim().isEmpty();
    }

    public void clearSession() {
        sharedPreferences.edit().clear().apply();
    }
}

