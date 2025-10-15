package com.example.tralalero.auth.repository;

import android.util.Log;

import com.example.tralalero.auth.remote.AuthApi;
import com.example.tralalero.auth.remote.AuthManager;
import com.example.tralalero.auth.remote.PublicAuthApi;
import com.example.tralalero.auth.remote.dto.FirebaseAuthDto;
import com.example.tralalero.data.remote.dto.auth.FirebaseAuthResponse;
import com.example.tralalero.network.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class FirebaseAuthRepository {
    private static final String TAG = "FirebaseAuthRepository";
    private AuthApi authApi;
    private PublicAuthApi publicAuthApi;
    private AuthManager authManager;

    public FirebaseAuthRepository(AuthManager authManager) {
        this.authManager = authManager;
        this.authApi = ApiClient.get(authManager).create(AuthApi.class);
        this.publicAuthApi = ApiClient.get().create(PublicAuthApi.class);
    }


    public void authenticateWithFirebase(String idToken, FirebaseAuthCallback callback) {
        FirebaseAuthDto request = new FirebaseAuthDto(idToken);
        
        Call<FirebaseAuthResponse> call = publicAuthApi.firebaseAuth(request);
        call.enqueue(new Callback<FirebaseAuthResponse>() {
            @Override
            public void onResponse(Call<FirebaseAuthResponse> call, Response<FirebaseAuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    FirebaseAuthResponse authResponse = response.body();
                    Log.d(TAG, "Firebase auth successful: " + authResponse.message);
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


    public AuthApi getAuthenticatedApi() {
        return authApi;
    }


    public interface FirebaseAuthCallback {
        void onSuccess(FirebaseAuthResponse response, String firebaseIdToken);
        void onError(String error);
    }
}