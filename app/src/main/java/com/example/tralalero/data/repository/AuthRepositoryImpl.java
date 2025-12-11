package com.example.tralalero.data.repository;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.tralalero.R;
import com.example.tralalero.auth.remote.AuthManager;
import com.example.tralalero.auth.repository.FirebaseAuthRepository;
import com.example.tralalero.auth.storage.TokenManager;
import com.example.tralalero.data.mapper.UserMapper;
import com.example.tralalero.data.remote.dto.auth.FirebaseAuthResponse;
import com.example.tralalero.data.remote.dto.auth.UserDto;
import com.example.tralalero.domain.model.User;
import com.example.tralalero.domain.repository.IAuthRepository;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepositoryImpl implements IAuthRepository {
    private final FirebaseAuth firebaseAuth;
    private final FirebaseAuthRepository firebaseAuthRepository;
    private final TokenManager tokenManager;
    private final Context context;

    public AuthRepositoryImpl(Context context) {
        this.context = context;
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
    public void signup(String email, String password, String name, RepositoryCallback<AuthResult> callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser == null) {
                        callback.onError("Firebase user creation failed");
                        return;
                    }

                    com.google.firebase.auth.UserProfileChangeRequest profileUpdates =
                        new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build();

                    firebaseUser.updateProfile(profileUpdates)
                            .addOnSuccessListener(aVoid -> {
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
                            .addOnFailureListener(e -> callback.onError("Failed to update profile: " + e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError("Firebase signup failed: " + e.getMessage()));
    }

    @Override
    public void logout(RepositoryCallback<Void> callback) {
        Log.d("AuthRepositoryImpl", "=== LOGOUT START ===");
        
        // Sign out from Firebase
        firebaseAuth.signOut();
        Log.d("AuthRepositoryImpl", "Logout: Firebase signed out");

        // Sign out from Google Sign In - ASYNC with callback
        try {
            Log.d("AuthRepositoryImpl", "Logout: Getting Google SignIn client...");
            
            String clientId = context.getString(R.string.client_id);
            Log.d("AuthRepositoryImpl", "Logout: Client ID = " + (clientId != null ? "EXISTS" : "NULL"));
            
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                    GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(clientId)
                    .requestEmail()
                    .build();
            Log.d("AuthRepositoryImpl", "Logout: GoogleSignInOptions created");
            
            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(context, gso);
            Log.d("AuthRepositoryImpl", "Logout: GoogleSignInClient created, calling signOut()...");
            
            // Wait for Google sign-out to complete
            googleSignInClient.signOut().addOnCompleteListener(task -> {
                Log.d("AuthRepositoryImpl", "Logout: Google signOut() listener triggered");
                if (task.isSuccessful()) {
                    Log.d("AuthRepositoryImpl", "Logout: ✅ Google sign-out SUCCESSFUL");
                } else {
                    Log.e("AuthRepositoryImpl", "Logout: ❌ Google sign-out FAILED", task.getException());
                }
                
                // Clear other caches after Google sign-out completes
                if (com.example.tralalero.App.App.authManager != null) {
                    com.example.tralalero.App.App.authManager.clearCache();
                    Log.d("AuthRepositoryImpl", "Logout: Cleared AuthManager cache");
                }

                tokenManager.clearAuthData();
                Log.d("AuthRepositoryImpl", "Logout: Cleared TokenManager data");
                
                Log.d("AuthRepositoryImpl", "=== LOGOUT COMPLETE ===");
                callback.onSuccess(null);
            });
            
            Log.d("AuthRepositoryImpl", "Logout: signOut() called, waiting for listener...");
            
        } catch (Exception e) {
            Log.e("AuthRepositoryImpl", "❌ EXCEPTION during Google sign-out setup", e);
            
            // Even if Google sign-out fails, clear other caches
            if (com.example.tralalero.App.App.authManager != null) {
                com.example.tralalero.App.App.authManager.clearCache();
            }
            tokenManager.clearAuthData();
            callback.onSuccess(null);
        }
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
