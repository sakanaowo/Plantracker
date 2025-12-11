package com.example.tralalero.feature.home.ui;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;
import com.example.tralalero.App.App;
import com.example.tralalero.R;
import com.example.tralalero.auth.remote.AuthApi;
import com.example.tralalero.auth.storage.TokenManager;
import com.example.tralalero.data.remote.api.GoogleAuthApiService;
import com.example.tralalero.domain.model.AuthUrlResponse;
import com.example.tralalero.domain.model.GoogleCalendarStatusResponse;
import com.example.tralalero.feature.auth.ui.login.LoginActivity;
import com.example.tralalero.network.ApiClient;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class SettingsFragment extends PreferenceFragmentCompat {
    private static final String TAG = "SettingsFragment";
    private TokenManager tokenManager;
    private GoogleAuthApiService googleAuthApiService;
    private Preference googleCalendarPref;
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        tokenManager = new TokenManager(requireContext());
        googleAuthApiService = ApiClient.get(App.authManager).create(GoogleAuthApiService.class);
        
        // Setup Google Calendar integration
        setupGoogleCalendarIntegration();
        
        androidx.preference.ListPreference languagePref = findPreference("language");
        if (languagePref != null) {
            languagePref.setOnPreferenceChangeListener((preference, newValue) -> {
                String languageCode = (String) newValue;
                changeLanguage(languageCode);
                return true;
            });
        }
        
        Preference logoutPref = findPreference("logout");
        if (logoutPref != null) {
            logoutPref.setOnPreferenceClickListener(preference -> {
                showLogoutDialog();
                return true;
            });
        }
        
        Preference deleteAccountPref = findPreference("delete_account");
        if (deleteAccountPref != null) {
            deleteAccountPref.setOnPreferenceClickListener(preference -> {
                showDeleteAccountDialog();
                return true;
            });
        }
    }
    
    private void setupGoogleCalendarIntegration() {
        googleCalendarPref = findPreference("google_calendar_sync");
        if (googleCalendarPref != null) {
            // Check current status
            checkGoogleCalendarStatus();
            
            // Handle click
            googleCalendarPref.setOnPreferenceClickListener(preference -> {
                showGoogleCalendarDialog();
                return true;
            });
        }
    }
    
    private void checkGoogleCalendarStatus() {
        googleAuthApiService.getIntegrationStatus().enqueue(new Callback<GoogleCalendarStatusResponse>() {
            @Override
            public void onResponse(Call<GoogleCalendarStatusResponse> call, Response<GoogleCalendarStatusResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GoogleCalendarStatusResponse status = response.body();
                    updateGoogleCalendarUI(status);
                } else {
                    Log.e(TAG, "Failed to check Google Calendar status: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<GoogleCalendarStatusResponse> call, Throwable t) {
                Log.e(TAG, "Network error checking Google Calendar status", t);
            }
        });
    }
    
    private void updateGoogleCalendarUI(GoogleCalendarStatusResponse status) {
        if (googleCalendarPref != null) {
            if (status.isConnected()) {
                googleCalendarPref.setSummary("✓ Connected: " + status.getAccountEmail());
                googleCalendarPref.setIcon(R.drawable.ic_google_calendar);
            } else {
                googleCalendarPref.setSummary("Not connected - Tap to connect");
                googleCalendarPref.setIcon(R.drawable.ic_google_calendar);
            }
        }
    }
    
    private void showGoogleCalendarDialog() {
        // Check current status first
        googleAuthApiService.getIntegrationStatus().enqueue(new Callback<GoogleCalendarStatusResponse>() {
            @Override
            public void onResponse(Call<GoogleCalendarStatusResponse> call, Response<GoogleCalendarStatusResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GoogleCalendarStatusResponse status = response.body();
                    
                    if (status.isConnected()) {
                        // Already connected - show disconnect option
                        showDisconnectDialog(status.getAccountEmail());
                    } else {
                        // Not connected - show connect option
                        showConnectDialog();
                    }
                }
            }
            
            @Override
            public void onFailure(Call<GoogleCalendarStatusResponse> call, Throwable t) {
                Log.e(TAG, "Failed to check status", t);
                Toast.makeText(requireContext(), "Failed to check connection status", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showConnectDialog() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Connect Google Calendar")
            .setMessage("Connect your Google Calendar to sync tasks and events across devices.\n\nFeatures:\n• Automatic task reminders\n• Google Meet integration\n• Cross-device sync")
            .setPositiveButton("Connect", (dialog, which) -> startGoogleOAuth())
            .setNegativeButton("Cancel", null)
            .setIcon(R.drawable.ic_google_calendar)
            .show();
    }
    
    private void showDisconnectDialog(String email) {
        new AlertDialog.Builder(requireContext())
            .setTitle("Disconnect Google Calendar")
            .setMessage("Connected as: " + email + "\n\nDisconnecting will:\n• Stop syncing tasks to Google Calendar\n• Remove Google Meet links from events\n• Keep existing calendar events")
            .setPositiveButton("Disconnect", (dialog, which) -> disconnectGoogleCalendar())
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Re-connect", (dialog, which) -> startGoogleOAuth())
            .show();
    }
    
    private void startGoogleOAuth() {
        Toast.makeText(requireContext(), "Connecting to Google Calendar...", Toast.LENGTH_SHORT).show();
        
        googleAuthApiService.getAuthUrl().enqueue(new Callback<AuthUrlResponse>() {
            @Override
            public void onResponse(Call<AuthUrlResponse> call, Response<AuthUrlResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String authUrl = response.body().getAuthUrl();
                    openChromeCustomTab(authUrl);
                } else {
                    Log.e(TAG, "Failed to get auth URL: " + response.code());
                    Toast.makeText(requireContext(), "Failed to start Google sign-in", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<AuthUrlResponse> call, Throwable t) {
                Log.e(TAG, "Network error getting auth URL", t);
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void openChromeCustomTab(String url) {
        try {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            builder.setShowTitle(true);
            builder.setToolbarColor(getResources().getColor(R.color.primary));
            
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(requireContext(), Uri.parse(url));
        } catch (Exception e) {
            Log.e(TAG, "Failed to open Chrome Custom Tab", e);
            // Fallback to default browser
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        }
    }
    
    private void disconnectGoogleCalendar() {
        Toast.makeText(requireContext(), "Disconnecting...", Toast.LENGTH_SHORT).show();
        
        googleAuthApiService.disconnect().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "✓ Disconnected from Google Calendar", Toast.LENGTH_SHORT).show();
                    if (googleCalendarPref != null) {
                        googleCalendarPref.setSummary("Not connected - Tap to connect");
                    }
                } else {
                    Log.e(TAG, "Failed to disconnect: " + response.code());
                    Toast.makeText(requireContext(), "Failed to disconnect", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Network error disconnecting", t);
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Re-check status when returning to settings (after OAuth callback)
        checkGoogleCalendarStatus();
    }
    private void changeLanguage(String languageCode) {
        android.content.res.Configuration configuration = new android.content.res.Configuration();
        java.util.Locale locale = new java.util.Locale(languageCode);
        java.util.Locale.setDefault(locale);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            configuration.setLocale(locale);
        } else {
            configuration.locale = locale;
        }
        requireContext().getResources().updateConfiguration(configuration, 
                requireContext().getResources().getDisplayMetrics());
        requireActivity().recreate();
    }
    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void performLogout() {
        Toast.makeText(requireContext(), "Logging out...", Toast.LENGTH_SHORT).show();
        
        Log.d(TAG, "=== LOGOUT START ===");
        
        // Step 1: Sign out from Firebase
        FirebaseAuth.getInstance().signOut();
        Log.d(TAG, "Logout: Firebase signed out");
        
        // Step 2: Sign out from Google Sign In (để có thể chọn tài khoản khác khi đăng nhập lại)
        try {
            String clientId = getString(R.string.client_id);
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(clientId)
                    .requestEmail()
                    .build();
            
            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(requireContext(), gso);
            Log.d(TAG, "Logout: GoogleSignInClient created, calling signOut()...");
            
            // Revoke Google access để force chọn account lại
            googleSignInClient.revokeAccess().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Logout: ✅ Google access revoked successfully");
                } else {
                    Log.e(TAG, "Logout: ⚠️ Google revoke failed", task.getException());
                }
                
                // Step 3: Clear all local data
                clearLocalDataAndRedirect();
            });
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Exception during Google sign-out", e);
            // Even if Google sign-out fails, clear local data
            clearLocalDataAndRedirect();
        }
    }
    
    private void clearLocalDataAndRedirect() {
        // Clear TokenManager
        tokenManager.clearAll();
        Log.d(TAG, "Logout: Cleared TokenManager");
        
        // Clear AuthManager cache
        if (App.authManager != null) {
            App.authManager.clearCache();
            Log.d(TAG, "Logout: Cleared AuthManager cache");
        }
        
        // Clear calendar sync cache
        if (getContext() != null) {
            getContext().getSharedPreferences("calendar_sync_prefs", 0)
                    .edit()
                    .clear()
                    .apply();
            Log.d(TAG, "Logout: Cleared calendar sync cache");
        }
        
        Log.d(TAG, "=== LOGOUT COMPLETE ===");
        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
        
        // Redirect to login screen
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
    
    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to permanently delete your account? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteAccount())
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void deleteAccount() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(requireContext(), "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(requireContext(), "Deleting account...", Toast.LENGTH_SHORT).show();
        AuthApi authApi = ApiClient.get(App.authManager)
                .create(AuthApi.class);
        authApi.deleteAccount().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Backend handles both DB and Firebase deletion
                    // Just clear local data and logout
                    Log.d(TAG, "Account deleted successfully by backend");
                    clearDataAndLogout();
                } else {
                    Log.e(TAG, "Backend delete failed: " + response.code());
                    Toast.makeText(requireContext(), "Failed to delete account from server", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Network error deleting account", t);
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void clearDataAndLogout() {
        tokenManager.clearAll();
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(requireContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
