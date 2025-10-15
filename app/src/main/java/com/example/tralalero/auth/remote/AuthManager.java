package com.example.tralalero.auth.remote;

import android.app.Application;
import android.util.Log;

import com.example.tralalero.App.App;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

public class AuthManager {
    private static final String TAG = "AuthManager";
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private volatile String cachedIdToken;
    private volatile long cachedExpiryEpochSec;

    public AuthManager(Application app) {
        auth.addIdTokenListener((FirebaseAuth.IdTokenListener) firebaseAuth -> {
            FirebaseUser u = firebaseAuth.getCurrentUser();
            if (u != null) {
                u.getIdToken(false).addOnSuccessListener(res -> {
                    cachedIdToken = res.getToken();
                    cachedExpiryEpochSec = res.getExpirationTimestamp();
                    Log.d(TAG, "Token cached successfully");
                }).addOnFailureListener(e -> {
                    Log.w(TAG, "Failed to cache token", e);
                    cachedIdToken = null;
                    cachedExpiryEpochSec = 0;
                });
            } else {
                cachedIdToken = null;
                cachedExpiryEpochSec = 0;
            }
        });
    }

    public String getIdTokenBlocking() throws Exception {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Log.w(TAG, "No current Firebase user");
            return null;
        }

        boolean needRefresh = cachedIdToken == null ||
                (cachedExpiryEpochSec > 0 && (cachedExpiryEpochSec - 300) <= (System.currentTimeMillis() / 1000));

        if (needRefresh) {
            Log.d(TAG, "Refreshing Firebase ID token");
        }

        Task<GetTokenResult> task = user.getIdToken(needRefresh);
        GetTokenResult res = com.google.android.gms.tasks.Tasks.await(task);
        cachedIdToken = res.getToken();
        cachedExpiryEpochSec = res.getExpirationTimestamp();

        Log.d(TAG, "Got Firebase ID token, expires at: " + cachedExpiryEpochSec);
        return cachedIdToken;
    }

    public String forceRefreshToken() throws Exception {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Log.w(TAG, "No current Firebase user for force refresh");
            return null;
        }

        Log.d(TAG, "Force refreshing Firebase ID token");
        Task<GetTokenResult> task = user.getIdToken(true);
        GetTokenResult res = com.google.android.gms.tasks.Tasks.await(task);
        cachedIdToken = res.getToken();
        cachedExpiryEpochSec = res.getExpirationTimestamp();

        Log.d(TAG, "Force refreshed Firebase ID token successfully");
        return cachedIdToken;
    }

    public void clearCache() {
        Log.d(TAG, "Clearing cached token");
        cachedIdToken = null;
        cachedExpiryEpochSec = 0;
    }

    public Application getContext() {
        return (Application) auth.getApp().getApplicationContext();
    }

    public boolean isSignedIn() {
        return auth.getCurrentUser() != null;
    }

    public void signOut() {
        Log.d(TAG, "Signing out user");
        auth.signOut();
        cachedExpiryEpochSec = 0;
        cachedIdToken = null;

        if (App.tokenManager != null) {
            App.tokenManager.clearAuthData();
            Log.d(TAG, "Cleared auth data from SharedPreferences");
        }
    }

    public synchronized String getCachedToken() {
        return cachedIdToken;
    }

    public boolean isTokenExpired() {
        if (cachedIdToken == null || cachedExpiryEpochSec == 0) {
            return true;
        }
        return (cachedExpiryEpochSec - 60) <= (System.currentTimeMillis() / 1000);
    }
}
