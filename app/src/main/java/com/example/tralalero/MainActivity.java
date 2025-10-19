package com.example.tralalero;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;

import com.example.tralalero.App.App;
import com.example.tralalero.feature.auth.ui.login.ContinueWithGoogle;
import com.example.tralalero.feature.auth.ui.login.LoginActivity;
import com.example.tralalero.feature.auth.ui.signup.SignupActivity;
import com.example.tralalero.feature.home.ui.Home.HomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * MainActivity - Splash/Welcome screen with auth check
 *
 * Phase 6 Fix:
 * - Check Firebase auth state properly
 * - Validate token before navigating to Home
 * - Auto-refresh expired tokens
 * - Proper error handling
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isDarkMode = prefs.getBoolean("theme", false);

        // Áp dụng theme
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);

        // Check authentication state properly
        checkAuthenticationState();
    }

    /**
     * Check authentication state with proper token validation
     * Phase 6 Fix: Enhanced auth checking
     */
    private void checkAuthenticationState() {
        Log.d(TAG, "=== Checking Authentication State ===");

        // Check Firebase Auth
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            Log.d(TAG, "Firebase user found: " + firebaseUser.getEmail());

            // Validate token and navigate
            validateTokenAndNavigate(firebaseUser);
        } else {
            Log.d(TAG, "No Firebase user, showing login screen");
            showLoginScreen();
        }
    }

    /**
     * Validate Firebase token before navigating to Home
     */
    private void validateTokenAndNavigate(FirebaseUser firebaseUser) {
        // Get fresh token to validate
        firebaseUser.getIdToken(true) // Force refresh
                .addOnSuccessListener(result -> {
                    String token = result.getToken();
                    if (token != null && !token.isEmpty()) {
                        Log.d(TAG, "✅ Valid token obtained, navigating to Home");
                        Log.d(TAG, "Token length: " + token.length());

                        // Save token to TokenManager
                        if (App.tokenManager != null) {
                            App.tokenManager.saveAuthData(
                                    token,
                                    firebaseUser.getUid(),
                                    firebaseUser.getEmail(),
                                    firebaseUser.getDisplayName()
                            );
                            Log.d(TAG, "Token saved to TokenManager");
                        }

                        // Navigate to Home
                        navigateToHome();
                    } else {
                        Log.e(TAG, "❌ Token is null or empty");
                        showLoginScreen();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Failed to get token: " + e.getMessage(), e);

                    // Token validation failed, sign out and show login
                    FirebaseAuth.getInstance().signOut();
                    if (App.authManager != null) {
                        App.authManager.clearCache();
                    }
                    if (App.tokenManager != null) {
                        App.tokenManager.clearAuthData();
                    }

                    Toast.makeText(this,
                            "Session expired. Please login again.",
                            Toast.LENGTH_LONG).show();

                    showLoginScreen();
                });
    }

    /**
     * Navigate to Home screen
     */
    private void navigateToHome() {
        Log.d(TAG, "Navigating to HomeActivity");
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Show login screen
     */
    private void showLoginScreen() {
        Log.d(TAG, "Showing login screen");

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Navigate to Login
        Button btnSignin = findViewById(R.id.btn_signin);
        if (btnSignin != null) {
            btnSignin.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            });
        }

        // Navigate to Sign Up
        Button btnSignup = findViewById(R.id.btn_signup);
        if (btnSignup != null) {
            btnSignup.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, SignupActivity.class));
            });
        }

        // Navigate to Continue With Google
        Button continueWithGoogle = findViewById(R.id.btn_google);
        if (continueWithGoogle != null) {
            continueWithGoogle.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, ContinueWithGoogle.class));
            });
        }

    }
}