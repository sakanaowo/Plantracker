package com.example.tralalero.data.repository;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;

import com.example.tralalero.auth.remote.AuthManager;
import com.example.tralalero.auth.repository.FirebaseAuthRepository;
import com.example.tralalero.auth.storage.TokenManager;
import com.example.tralalero.data.mapper.UserMapper;
import com.example.tralalero.data.remote.dto.auth.FirebaseAuthResponse;
import com.example.tralalero.data.remote.dto.auth.UserDto;
import com.example.tralalero.domain.model.User;
import com.example.tralalero.domain.repository.IAuthRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepositoryImpl implements IAuthRepository {
    private final FirebaseAuth firebaseAuth;
    private final FirebaseAuthRepository firebaseAuthRepository;
    private final TokenManager tokenManager;

    public AuthRepositoryImpl(Context context) {
        this.firebaseAuth = FirebaseAuth.getInstance();
        AuthManager authManager = new AuthManager((Application) context.getApplicationContext());
        this.firebaseAuthRepository = new FirebaseAuthRepository(authManager);
        this.tokenManager = new TokenManager(context);
    }

    @Override
    public void login(String email, String password, RepositoryCallback<AuthResult> callback) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser == null) {
                        callback.onError("Firebase user is null");
                        return;
                    }
                    firebaseUser.getIdToken(true)
                            .addOnSuccessListener(getTokenResult -> {
                                String idToken = getTokenResult.getToken();

                                firebaseAuthRepository.authenticateWithFirebase(idToken,
                                        new FirebaseAuthRepository.FirebaseAuthCallback() {
                                            @Override
                                            public void onSuccess(FirebaseAuthResponse response, String firebaseIdToken) {
                                                tokenManager.saveAuthData(
                                                        firebaseIdToken,
                                                        response.getUser().getId(),
                                                        response.getUser().getEmail(),
                                                        response.getUser().getName()
                                                );
                                                User user = UserMapper.toDomain(response.getUser());
                                                AuthResult authResult = new AuthResult(user, firebaseIdToken, null);
                                                callback.onSuccess(authResult);
                                            }

                                            @Override
                                            public void onError(String error) {
                                                callback.onError("Backend authentication failed: " + error);
                                            }
                                        });
                            })
                            .addOnFailureListener(e -> callback.onError("Failed to get ID token: " + e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError("Firebase sign-in failed: " + e.getMessage()));
    }

    @Override
    public void logout(RepositoryCallback<Void> callback) {
        firebaseAuth.signOut();
        tokenManager.clearAuthData();
        callback.onSuccess(null);
    }

    @Override
    public void getCurrentUser(RepositoryCallback<User> callback) {
        firebaseAuthRepository.getAuthenticatedApi().getMe()
                .enqueue(new retrofit2.Callback<UserDto>() {
                    @Override
                    public void onResponse(@NonNull retrofit2.Call<UserDto> call,
                                         @NonNull retrofit2.Response<UserDto> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            User user = UserMapper.toDomain(response.body());
                            callback.onSuccess(user);
                        } else {
                            callback.onError("Failed to fetch user: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull retrofit2.Call<UserDto> call,
                                        @NonNull Throwable t) {
                        callback.onError("Network error: " + t.getMessage());
                    }
                });
    }

    @Override
    public boolean isLoggedIn() {
        return tokenManager.isLoggedIn();
    }

    @Override
    public void refreshToken(String refreshToken, RepositoryCallback<String> callback) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            currentUser.getIdToken(true)
                    .addOnSuccessListener(result -> {
                        String newToken = result.getToken();
                        tokenManager.saveAuthData(
                                newToken,
                                tokenManager.getUserId(),
                                tokenManager.getUserEmail(),
                                tokenManager.getUserName()
                        );
                        callback.onSuccess(newToken);
                    })
                    .addOnFailureListener(e -> callback.onError("Token refresh failed: " + e.getMessage()));
        } else {
            callback.onError("No user logged in");
        }
    }
}
