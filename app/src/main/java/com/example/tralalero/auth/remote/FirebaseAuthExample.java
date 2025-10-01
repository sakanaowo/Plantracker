package com.example.tralalero.auth.remote;

import android.app.Application;
import android.util.Log;

/**
 * Example usage of Firebase token management system
 * Shows how to initialize and use the AuthManager with automatic token handling
 */
public class FirebaseAuthExample {
    private static final String TAG = "FirebaseAuthExample";
    private AuthManager authManager;

    public void initializeAuth(Application application) {
        // Initialize AuthManager
        authManager = new AuthManager(application);

        Log.d(TAG, "Firebase auth system initialized with automatic token management");
        Log.d(TAG, "- Interceptor will automatically add tokens to API requests");
        Log.d(TAG, "- Authenticator will automatically refresh tokens on 401/404 errors");
        Log.d(TAG, "- Tokens are cached and refreshed 5 minutes before expiry");
    }

    public void demonstrateUsage() {
        if (authManager == null) {
            Log.e(TAG, "AuthManager not initialized");
            return;
        }

        // Check authentication status
        if (authManager.isSignedIn()) {
            Log.d(TAG, "User is signed in");

            // Check token status
            if (authManager.isTokenExpired()) {
                Log.d(TAG, "Token is expired - will be auto-refreshed on next API call");
            } else {
                Log.d(TAG, "Token is valid");
            }

            // Get current cached token (for debugging)
            String cachedToken = authManager.getCachedToken();
            if (cachedToken != null) {
                Log.d(TAG, "Current cached token: " + cachedToken.substring(0, Math.min(20, cachedToken.length())) + "...");
            }

        } else {
            Log.d(TAG, "User is not signed in");
        }
    }

    public AuthManager getAuthManager() {
        return authManager;
    }
}
