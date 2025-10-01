package com.example.tralalero.auth.remote;

import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Firebase Interceptor to automatically add Firebase ID token to API requests
 * This interceptor adds the current Firebase ID token to the Authorization header
 */
public class FirebaseInterceptor implements Interceptor {
    private static final String TAG = "FirebaseInterceptor";
    private final AuthManager authManager;

    public FirebaseInterceptor(AuthManager authManager) {
        this.authManager = authManager;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        // Skip authentication for public endpoints
        if (shouldSkipAuth(original.url().encodedPath())) {
            Log.d(TAG, "Skipping auth for public endpoint: " + original.url().encodedPath());
            return chain.proceed(original);
        }

        // Skip if this is a retry request (already has Authorization header)
        if (original.header("Authorization") != null) {
            return chain.proceed(original);
        }

        String token = null;
        try {
            // Get current Firebase ID token (will auto-refresh if needed)
            token = authManager.getIdTokenBlocking();
        } catch (Exception e) {
            Log.w(TAG, "Failed to get Firebase ID token", e);
        }

        if (token == null) {
            Log.w(TAG, "No Firebase token available, proceeding without auth");
            // No token available - proceed without Authorization header
            // The backend will return 401 and the authenticator will handle it
            return chain.proceed(original);
        }

        // Add Firebase ID token to Authorization header
        Request authenticatedRequest = original.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();

        Log.d(TAG, "Added Firebase token to request: " + original.url().encodedPath());
        return chain.proceed(authenticatedRequest);
    }

    /**
     * Check if the request should skip authentication
     * Add your public endpoints here
     */
    private boolean shouldSkipAuth(String path) {
        return path.startsWith("/auth/public") ||
               path.startsWith("/health") ||
               path.contains("/users/firebase/auth") || // Skip for Firebase auth endpoint
               path.contains("/public/");
    }
}
