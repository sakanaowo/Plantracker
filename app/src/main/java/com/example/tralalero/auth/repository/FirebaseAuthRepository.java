package com.example.tralalero.auth.repository;

import android.util.Log;

import com.example.tralalero.auth.remote.AuthApi;
import com.example.tralalero.auth.remote.AuthManager;
import com.example.tralalero.auth.remote.PublicAuthApi;
import com.example.tralalero.auth.remote.dto.FirebaseAuthDto;
import com.example.tralalero.auth.remote.dto.FirebaseAuthResponse;
import com.example.tralalero.network.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository class to handle Firebase Authentication with backend
 * Uses PublicAuthApi for initial Firebase auth (no Authorization header)
 * Uses AuthManager for authenticated API calls after login
 */
public class FirebaseAuthRepository {
    private static final String TAG = "FirebaseAuthRepository";
    private AuthApi authApi;
    private PublicAuthApi publicAuthApi;
    private AuthManager authManager;

    public FirebaseAuthRepository(AuthManager authManager) {
        this.authManager = authManager;
        // Use authenticated ApiClient for protected endpoints
        this.authApi = ApiClient.get(authManager).create(AuthApi.class);
        // Use public ApiClient for Firebase auth endpoint (no auth header)
        this.publicAuthApi = ApiClient.get().create(PublicAuthApi.class);
    }

    /**
     * Validate Firebase ID token with backend and sync user data
     * Uses PublicAuthApi to avoid adding Authorization header to initial auth request
     */
    public void authenticateWithFirebase(String idToken, FirebaseAuthCallback callback) {
        FirebaseAuthDto request = new FirebaseAuthDto(idToken);
        
        // Use public API for initial Firebase authentication
        Call<FirebaseAuthResponse> call = publicAuthApi.firebaseAuth(request);
        call.enqueue(new Callback<FirebaseAuthResponse>() {
            @Override
            public void onResponse(Call<FirebaseAuthResponse> call, Response<FirebaseAuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    FirebaseAuthResponse authResponse = response.body();
                    Log.d(TAG, "Firebase auth successful: " + authResponse.message);
                    // Get the current Firebase ID token (may have been refreshed by interceptor)
                    String currentToken = authManager.getCachedToken();
                    callback.onSuccess(authResponse, currentToken != null ? currentToken : idToken);
                } else {
                    Log.e(TAG, "Firebase auth failed: " + response.code() + " - " + response.message());
                    callback.onError("Authentication failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<FirebaseAuthResponse> call, Throwable t) {
                Log.e(TAG, "Firebase auth network error", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Make authenticated API calls - the interceptor will automatically handle tokens
     */
    public AuthApi getAuthenticatedApi() {
        return authApi;
    }

    /**
     * Callback interface for Firebase authentication
     */
    public interface FirebaseAuthCallback {
        void onSuccess(FirebaseAuthResponse response, String firebaseIdToken);
        void onError(String error);
    }
}