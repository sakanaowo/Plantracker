package com.example.tralalero.auth.remote;

import android.util.Log;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

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

        String retryCountHeader = response.request().header("X-Retry-Count");
        int retryCount = retryCountHeader != null ? Integer.parseInt(retryCountHeader) : 0;

        if (retryCount >= MAX_RETRY_COUNT) {
            Log.w(TAG, "Max retry count reached, giving up");
            return null;
        }

        if (response.code() != 401 && response.code() != 404) {
            Log.d(TAG, "Not handling response code: " + response.code());
            return null;
        }

        if (!authManager.isSignedIn()) {
            Log.w(TAG, "User not signed in, cannot refresh token");
            authManager.signOut(); 
            return null;
        }

        try {
            String newToken = authManager.forceRefreshToken();

            if (newToken == null) {
                Log.w(TAG, "Failed to get new token, signing out user");
                authManager.signOut();
                return null;
            }

            Log.d(TAG, "Successfully refreshed Firebase token, retrying request");

            return response.request().newBuilder()
                    .header("Authorization", "Bearer " + newToken)
                    .header("X-Retry-Count", String.valueOf(retryCount + 1))
                    .build();

        } catch (Exception e) {
            Log.e(TAG, "Error refreshing Firebase token", e);
            authManager.signOut(); 
            return null;
        }
    }
}
