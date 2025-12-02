package com.example.tralalero;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.example.tralalero.App.App;
import com.example.tralalero.auth.remote.AuthManager;
import com.example.tralalero.data.remote.api.GoogleAuthApiService;
import com.example.tralalero.domain.model.AuthUrlResponse;
import com.example.tralalero.domain.model.GoogleCalendarStatusResponse;
import com.example.tralalero.feature.auth.ui.login.ContinueWithGoogle;
import com.example.tralalero.feature.auth.ui.login.LoginActivity;
import com.example.tralalero.feature.auth.ui.signup.SignupActivity;
import com.example.tralalero.feature.home.ui.MainContainerActivity;
import com.example.tralalero.network.ApiClient;
import com.example.tralalero.presentation.viewmodel.AuthViewModel;
import com.example.tralalero.presentation.viewmodel.ViewModelFactoryProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String PREFS_NAME = "CalendarSyncPrefs";
    private static final String KEY_LAST_PROMPTED_PREFIX = "last_prompted_";
    private static final long PROMPT_COOLDOWN_MS = 24 * 60 * 60 * 1000; // 24 hours
    
    /**
     * Clear calendar sync cooldown for a specific user
     * Useful for logout or testing scenarios
     */
    public static void clearCalendarSyncCooldown(Context context, String userId) {
        if (context == null || userId == null) return;
        
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String userKey = KEY_LAST_PROMPTED_PREFIX + userId;
        prefs.edit().remove(userKey).apply();
        Log.d(TAG, "Cleared calendar sync cooldown for user: " + userId);
    }
    
    private AuthViewModel authViewModel;
    private GoogleAuthApiService googleAuthApiService;
    private SharedPreferences calendarSyncPrefs;
    private boolean isFirstLogin = false;
    private boolean shouldCheckCalendarPrompt = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize SharedPreferences for calendar sync
        calendarSyncPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        // Initialize Google Auth API Service
        AuthManager authManager = new AuthManager(getApplication());
        googleAuthApiService = ApiClient.get(authManager).create(GoogleAuthApiService.class);
        
        // Check if this is a first login (from intent extra)
        isFirstLogin = getIntent().getBooleanExtra("is_first_login", false);
        
        // Only prompt calendar sync if explicitly coming from login/signup
        // This prevents showing dialog on app restart when user is already logged in
        shouldCheckCalendarPrompt = isFirstLogin;
        
        Log.d(TAG, "onCreate: isFirstLogin=" + isFirstLogin + ", shouldCheckCalendarPrompt=" + shouldCheckCalendarPrompt);
        
        // Setup ViewModel
        setupViewModel();
        
        // Observe auth state for auto-redirect
        observeViewModel();
        
        // AuthViewModel constructor auto-checks session via checkStoredSession()
        // Observer will handle navigation based on isLoggedIn state
    }

    @Override
    protected void onStart() {
        super.onStart();
        // DO NOTHING - LiveData auto-updates via ViewModel
    }

    private void setupViewModel() {
        authViewModel = new ViewModelProvider(
            this,
            ViewModelFactoryProvider.provideAuthViewModelFactory()
        ).get(AuthViewModel.class);
    }

    private void observeViewModel() {
        // Observe auth state for auto-navigation
        authViewModel.getAuthState().observe(this, state -> {
            Log.d(TAG, "AuthState changed: " + state);
            
            switch (state) {
                case LOGIN_SUCCESS:
                case SIGNUP_SUCCESS:
                    // User is logged in - navigate to Home
                    navigateToHome();
                    break;
                case IDLE:
                case LOGGED_OUT:
                case LOGIN_ERROR:
                case SIGNUP_ERROR:
                    // User is not logged in - show login screen
                    showLoginScreen();
                    break;
                case LOGGING_IN:
                case SIGNING_UP:
                    // Do nothing during login/signup process
                    break;
            }
        });
    }

    private void navigateToHome() {
        Log.d(TAG, "Navigating to MainContainerActivity");
        
        // Only check calendar prompt if this is a fresh login from auth activities
        // Do NOT prompt on app restart/resume when user session is restored
        if (shouldCheckCalendarPrompt && shouldPromptCalendarSync()) {
            promptCalendarSync();
        } else {
            Log.d(TAG, "Skipping calendar prompt: shouldCheckCalendarPrompt=" + shouldCheckCalendarPrompt);
            proceedToHome();
        }
    }
    
    private void proceedToHome() {
        Log.d(TAG, "Proceeding to MainContainerActivity");
        Intent intent = new Intent(this, MainContainerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    private void showLoginScreen() {
        Log.d(TAG, "Showing login screen");

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnSignin = findViewById(R.id.btn_signin);
        if (btnSignin != null) {
            btnSignin.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            });
        }

        Button btnSignup = findViewById(R.id.btn_signup);
        if (btnSignup != null) {
            btnSignup.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, SignupActivity.class));
            });
        }

        Button continueWithGoogle = findViewById(R.id.btn_google);
        if (continueWithGoogle != null) {
            continueWithGoogle.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, ContinueWithGoogle.class));
            });
        }

    }
    
    /**
     * Check if we should prompt the user to sync calendar
     * Conditions:
     * 1. User is authenticated
     * 2. Last prompt for THIS USER was more than 24 hours ago (cooldown)
     *    - BUT if never prompted before (lastPrompted == 0), always show
     */
    private boolean shouldPromptCalendarSync() {
        // Check if user is authenticated
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.d(TAG, "User not authenticated, skipping calendar sync prompt");
            return false;
        }
        
        // Check cooldown period FOR THIS SPECIFIC USER
        String userKey = KEY_LAST_PROMPTED_PREFIX + user.getUid();
        long lastPrompted = calendarSyncPrefs.getLong(userKey, 0);
        
        // If never prompted before (lastPrompted == 0), always show dialog
        if (lastPrompted == 0) {
            Log.d(TAG, "User never prompted before, should show calendar sync for user: " + user.getUid());
            return true;
        }
        
        // Check cooldown period
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPrompted < PROMPT_COOLDOWN_MS) {
            long hoursRemaining = (PROMPT_COOLDOWN_MS - (currentTime - lastPrompted)) / (60 * 60 * 1000);
            Log.d(TAG, "Calendar sync prompt on cooldown for user: " + user.getUid() + " (" + hoursRemaining + "h remaining)");
            return false;
        }
        
        Log.d(TAG, "Cooldown expired, should prompt calendar sync for user: " + user.getUid());
        return true;
    }
    
    /**
     * Prompt user to connect Google Calendar
     * First checks if already connected, then shows dialog if not
     */
    private void promptCalendarSync() {
        Log.d(TAG, "Checking calendar integration status...");
        
        googleAuthApiService.getIntegrationStatus().enqueue(new Callback<GoogleCalendarStatusResponse>() {
            @Override
            public void onResponse(Call<GoogleCalendarStatusResponse> call, Response<GoogleCalendarStatusResponse> response) {
                if (!isFinishing() && response.isSuccessful() && response.body() != null) {
                    GoogleCalendarStatusResponse status = response.body();
                    
                    if (status.isConnected()) {
                        // Already connected, proceed to home
                        Log.d(TAG, "Calendar already connected, proceeding to home");
                        proceedToHome();
                    } else {
                        // Not connected, show prompt dialog
                        Log.d(TAG, "Calendar not connected, showing prompt");
                        showCalendarSyncDialog();
                    }
                } else {
                    // API error, proceed to home anyway
                    Log.w(TAG, "Failed to check calendar status, proceeding to home");
                    proceedToHome();
                }
            }
            
            @Override
            public void onFailure(Call<GoogleCalendarStatusResponse> call, Throwable t) {
                // Network error, proceed to home anyway
                Log.e(TAG, "Network error checking calendar status", t);
                proceedToHome();
            }
        });
    }
    
    /**
     * Show dialog prompting user to connect Google Calendar
     */
    private void showCalendarSyncDialog() {
        if (isFinishing()) {
            return;
        }
        
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.w(TAG, "Cannot show calendar dialog: user not authenticated");
            proceedToHome();
            return;
        }
        
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_calendar_sync_prompt);
        dialog.setCancelable(true);
        
        // Update last prompted timestamp FOR THIS SPECIFIC USER
        String userKey = KEY_LAST_PROMPTED_PREFIX + user.getUid();
        calendarSyncPrefs.edit()
            .putLong(userKey, System.currentTimeMillis())
            .apply();
        
        Button btnConnectNow = dialog.findViewById(R.id.btnConnectNow);
        Button btnMaybeLater = dialog.findViewById(R.id.btnMaybeLater);
        TextView btnDontAskAgain = dialog.findViewById(R.id.btnDontAskAgain);
        
        // Connect Now button
        btnConnectNow.setOnClickListener(v -> {
            Log.d(TAG, "User clicked 'Connect Now'");
            dialog.dismiss();
            initiateCalendarConnection();
        });
        
        // Maybe Later button
        btnMaybeLater.setOnClickListener(v -> {
            Log.d(TAG, "User clicked 'Maybe Later'");
            dialog.dismiss();
            proceedToHome();
        });
        
        // Don't Ask Again button - now just dismisses (user can still see in settings)
        btnDontAskAgain.setOnClickListener(v -> {
            Log.d(TAG, "User clicked 'Don't Ask Again'");
            dialog.dismiss();
            proceedToHome();
        });
        
        // On dismiss/cancel, proceed to home
        dialog.setOnCancelListener(d -> proceedToHome());
        
        dialog.show();
    }
    
    /**
     * Initiate Google Calendar connection flow
     * Gets OAuth URL and opens Chrome Custom Tab
     */
    private void initiateCalendarConnection() {
        Log.d(TAG, "Initiating calendar connection...");
        Toast.makeText(this, "Connecting to Google Calendar...", Toast.LENGTH_SHORT).show();
        
        googleAuthApiService.getAuthUrl().enqueue(new Callback<AuthUrlResponse>() {
            @Override
            public void onResponse(Call<AuthUrlResponse> call, Response<AuthUrlResponse> response) {
                if (!isFinishing() && response.isSuccessful() && response.body() != null) {
                    String authUrl = response.body().getAuthUrl();
                    Log.d(TAG, "Opening OAuth URL in Chrome Custom Tab: " + authUrl);
                    
                    // Open OAuth URL in Chrome Custom Tab
                    openChromeCustomTab(authUrl);
                    
                    // Proceed to home (user will return after OAuth)
                    proceedToHome();
                } else {
                    Log.e(TAG, "Failed to get auth URL: " + response.code());
                    Toast.makeText(MainActivity.this, 
                        "Failed to start Google sign-in", 
                        Toast.LENGTH_SHORT).show();
                    proceedToHome();
                }
            }
            
            @Override
            public void onFailure(Call<AuthUrlResponse> call, Throwable t) {
                Log.e(TAG, "Network error getting auth URL", t);
                Toast.makeText(MainActivity.this, 
                    "Network error: " + t.getMessage(), 
                    Toast.LENGTH_SHORT).show();
                proceedToHome();
            }
        });
    }
    
    /**
     * Open URL in Chrome Custom Tab with fallback to default browser
     */
    private void openChromeCustomTab(String url) {
        try {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            builder.setShowTitle(true);
            builder.setToolbarColor(getResources().getColor(R.color.primary));
            
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(this, Uri.parse(url));
            
            Log.d(TAG, "Opened Chrome Custom Tab successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to open Chrome Custom Tab, using fallback", e);
            // Fallback to default browser
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            } catch (Exception ex) {
                Log.e(TAG, "Failed to open browser", ex);
                Toast.makeText(this, "Failed to open browser", Toast.LENGTH_SHORT).show();
            }
        }
    }
}