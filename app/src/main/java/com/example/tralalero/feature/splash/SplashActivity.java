package com.example.tralalero.feature.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tralalero.R;
import com.example.tralalero.auth.storage.TokenManager;
import com.example.tralalero.feature.auth.ui.login.LoginActivity;
import com.example.tralalero.feature.home.ui.MainContainerActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    private TokenManager tokenManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        tokenManager = new TokenManager(this);
        
        // Check credentials after a short delay for smooth UX
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            checkAuthenticationStatus();
        }, 1500); // 1.5 seconds delay
    }
    
    private void checkAuthenticationStatus() {
        Log.d(TAG, "Checking authentication status...");
        
        // Check Firebase Auth
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        
        // Check if we have saved token
        String savedToken = tokenManager.getFirebaseIdToken();
        String savedUserId = tokenManager.getUserId();
        
        Log.d(TAG, "Firebase user: " + (firebaseUser != null ? firebaseUser.getEmail() : "null"));
        Log.d(TAG, "Saved token exists: " + (savedToken != null && !savedToken.isEmpty()));
        Log.d(TAG, "Saved userId: " + savedUserId);
        
        // User is authenticated if both Firebase user and token exist
        if (firebaseUser != null && savedToken != null && !savedToken.isEmpty()) {
            Log.d(TAG, "User is authenticated, navigating to home");
            navigateToHome(firebaseUser);
        } else {
            Log.d(TAG, "User is not authenticated, navigating to login");
            navigateToLogin();
        }
    }
    
    private void navigateToHome(FirebaseUser firebaseUser) {
        Intent intent = new Intent(SplashActivity.this, MainContainerActivity.class);
        
        // Pass user data
        intent.putExtra("user_name", firebaseUser.getDisplayName());
        intent.putExtra("user_email", firebaseUser.getEmail());
        intent.putExtra("is_from_splash", true);
        
        // Clear the task stack so user can't go back to splash
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        startActivity(intent);
        finish();
    }
    
    private void navigateToLogin() {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        
        // Clear the task stack
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        startActivity(intent);
        finish();
    }
}
