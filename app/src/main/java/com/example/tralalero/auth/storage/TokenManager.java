package com.example.tralalero.auth.storage;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Utility class for managing Firebase ID token storage and retrieval
 * Since backend uses Firebase ID token directly, no separate JWT needed
 */
public class TokenManager {
    private static final String PREF_NAME = "auth_prefs";
    private static final String KEY_FIREBASE_ID_TOKEN = "firebase_id_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";

    private SharedPreferences prefs;

    public TokenManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Save Firebase ID token and user info
     */
    public void saveAuthData(String firebaseIdToken, String userId, String email, String name) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_FIREBASE_ID_TOKEN, firebaseIdToken);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name);
        editor.apply();
    }

    /**
     * Get Firebase ID token
     */
    public String getFirebaseIdToken() {
        return prefs.getString(KEY_FIREBASE_ID_TOKEN, null);
    }

    /**
     * @deprecated Use getFirebaseIdToken() instead
     */
    @Deprecated
    public String getJwtToken() {
        return getFirebaseIdToken();
    }

    /**
     * Get user ID
     */
    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    /**
     * Get user email
     */
    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, null);
    }

    /**
     * Get user name
     */
    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, null);
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return getFirebaseIdToken() != null;
    }

    /**
     * Clear all auth data (logout)
     */
    public void clearAuthData() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_FIREBASE_ID_TOKEN);
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_USER_EMAIL);
        editor.remove(KEY_USER_NAME);
        editor.apply();
    }
}