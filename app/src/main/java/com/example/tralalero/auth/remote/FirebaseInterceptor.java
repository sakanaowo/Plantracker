package com.example.tralalero.auth.remote;

import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


public class FirebaseInterceptor implements Interceptor {
    private static final String TAG = "FirebaseInterceptor";
    private final AuthManager authManager;

    public FirebaseInterceptor(AuthManager authManager) {
        this.authManager = authManager;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        if (shouldSkipAuth(original.url().encodedPath())) {
            Log.d(TAG, "Skipping auth for public endpoint: " + original.url().encodedPath());
            return chain.proceed(original);
        }

        if (original.header("Authorization") != null) {
            return chain.proceed(original);
        }

        String token = null;
        try {
            token = authManager.getIdTokenBlocking();
        } catch (Exception e) {
            Log.w(TAG, "Failed to get Firebase ID token", e);
        }

        if (token == null) {
            Log.w(TAG, "No Firebase token available, proceeding without auth");
            return chain.proceed(original);
        }

        Request authenticatedRequest = original.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();

        Log.d(TAG, "Added Firebase token to request: " + original.url().encodedPath());
        return chain.proceed(authenticatedRequest);
    }

    
    private boolean shouldSkipAuth(String path) {
        return path.startsWith("/auth/public") ||
               path.startsWith("/health") ||
               path.contains("/users/firebase/auth") || 
               path.contains("/public/");
    }
}
