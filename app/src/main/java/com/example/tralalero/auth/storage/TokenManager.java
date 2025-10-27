package com.example.tralalero.auth.storage;

import android.content.Context;
import android.content.SharedPreferences;


public class TokenManager {
    private static final String PREF_NAME = "auth_prefs";
    private static final String KEY_FIREBASE_ID_TOKEN = "firebase_id_token";
    private static final String KEY_USER_ID = "user_id"; // Firebase UID
    private static final String KEY_INTERNAL_USER_ID = "internal_user_id"; // Internal UUID from backend
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";

    private SharedPreferences prefs;

    public TokenManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }


    public void saveAuthData(String firebaseIdToken, String userId, String email, String name) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_FIREBASE_ID_TOKEN, firebaseIdToken);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name);
        editor.apply();
    }

    /**
     * Save auth data with internal user ID from backend
     */
    public void saveAuthData(String firebaseIdToken, String userId, String email, String name, String internalUserId) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_FIREBASE_ID_TOKEN, firebaseIdToken);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_INTERNAL_USER_ID, internalUserId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name);
        editor.apply();
    }


    public String getFirebaseIdToken() {
        return prefs.getString(KEY_FIREBASE_ID_TOKEN, null);
    }


    @Deprecated
    public String getJwtToken() {
        return getFirebaseIdToken();
    }


    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    /**
     * Get internal user ID (UUID) for backend operations like assignTask
     */
    public String getInternalUserId() {
        return prefs.getString(KEY_INTERNAL_USER_ID, null);
    }


    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, null);
    }


    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, null);
    }


    public boolean isLoggedIn() {
        return getFirebaseIdToken() != null;
    }


    public void clearAuthData() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_FIREBASE_ID_TOKEN);
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_INTERNAL_USER_ID);
        editor.remove(KEY_USER_EMAIL);
        editor.remove(KEY_USER_NAME);
        editor.apply();
    }


    public void clearAll() {
        prefs.edit().clear().apply();
    }
}