package com.example.tralalero.auth.remote;

import android.util.Log;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

/**
 * Firebase Authenticator to handle token refresh when receiving 401/404 errors
 * This will automatically refresh the Firebase ID token and retry the request
 */
public class FirebaseAuthenticator implements Authenticator {
    private static final String TAG = "FirebaseAuthenticator";
    private final AuthManager authManager;
    private static final int MAX_RETRY_COUNT = 2;

    public FirebaseAuthenticator(AuthManager authManager) {
        this.authManager = authManager;
    }

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        Log.d(TAG, "Authentication required for: " + response.request().url());

        // Check if we've already tried to refresh this request
        String retryCountHeader = response.request().header("X-Retry-Count");
        int retryCount = retryCountHeader != null ? Integer.parseInt(retryCountHeader) : 0;

        // Prevent infinite retry loops
        if (retryCount >= MAX_RETRY_COUNT) {
            Log.w(TAG, "Max retry count reached, giving up");
            return null;
        }

        // Only handle 401 and 404 errors (Firebase token expiration)
        if (response.code() != 401 && response.code() != 404) {
            Log.d(TAG, "Not handling response code: " + response.code());
            return null;
        }

        // Check if user is still signed in
        if (!authManager.isSignedIn()) {
            Log.w(TAG, "User not signed in, cannot refresh token");
            authManager.signOut(); // Clean up any cached tokens
            return null;
        }

        try {
            // Force refresh the Firebase ID token
            String newToken = authManager.forceRefreshToken();

            if (newToken == null) {
                Log.w(TAG, "Failed to get new token, signing out user");
                authManager.signOut();
                return null;
            }

            Log.d(TAG, "Successfully refreshed Firebase token, retrying request");

            // Retry the request with the new token
            return response.request().newBuilder()
                    .header("Authorization", "Bearer " + newToken)
                    .header("X-Retry-Count", String.valueOf(retryCount + 1))
                    .build();

        } catch (Exception e) {
            Log.e(TAG, "Error refreshing Firebase token", e);
            authManager.signOut(); // Clean up on error
            return null;
        }
    }
}
