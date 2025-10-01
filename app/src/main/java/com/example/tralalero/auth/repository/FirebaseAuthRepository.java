package com.example.tralalero.auth.repository;

import android.util.Log;

import com.example.tralalero.auth.remote.AuthApi;
import com.example.tralalero.auth.remote.dto.FirebaseAuthDto;
import com.example.tralalero.auth.remote.dto.FirebaseAuthResponse;
import com.example.tralalero.network.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository class to handle Firebase Authentication with backend
 * Since backend only uses Firebase ID token, this just validates the token with backend
 * and stores user info locally
 */
public class FirebaseAuthRepository {
    private static final String TAG = "FirebaseAuthRepository";
    private AuthApi authApi;

    public FirebaseAuthRepository() {
        this.authApi = ApiClient.get().create(AuthApi.class);
    }

    /**
     * Validate Firebase ID token with backend and sync user data
     * Backend returns user info but no separate JWT (uses Firebase ID token directly)
     */
    public void authenticateWithFirebase(String idToken, FirebaseAuthCallback callback) {
        FirebaseAuthDto request = new FirebaseAuthDto(idToken);
        
        Call<FirebaseAuthResponse> call = authApi.firebaseAuth(request);
        call.enqueue(new Callback<FirebaseAuthResponse>() {
            @Override
            public void onResponse(Call<FirebaseAuthResponse> call, Response<FirebaseAuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    FirebaseAuthResponse authResponse = response.body();
                    Log.d(TAG, "Firebase auth successful: " + authResponse.message);
                    // Backend validates token and returns user data
                    // No separate JWT token needed - we'll use Firebase ID token directly
                    callback.onSuccess(authResponse, idToken);
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
     * Callback interface for Firebase authentication
     */
    public interface FirebaseAuthCallback {
        void onSuccess(FirebaseAuthResponse response, String firebaseIdToken);
        void onError(String error);
    }
}