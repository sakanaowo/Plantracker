package com.example.tralalero.feature.home.ui;

import android.app.Dialog;
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
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import com.example.tralalero.App.App;
import com.example.tralalero.R;
import com.example.tralalero.adapter.MainViewPagerAdapter;
import com.example.tralalero.data.remote.api.GoogleAuthApiService;
import com.example.tralalero.domain.model.AuthUrlResponse;
import com.example.tralalero.feature.auth.ui.login.LoginActivity;
import com.example.tralalero.feature.calendar.CalendarSyncManager;
import com.example.tralalero.network.ApiClient;
import com.example.tralalero.presentation.viewmodel.AuthViewModel;
import com.example.tralalero.presentation.viewmodel.ViewModelFactoryProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * New Main Container Activity with ViewPager2
 * Replaces individual Activities (HomeActivity, InboxActivity, etc.)
 * Supports swipe navigation between fragments
 */
public class MainContainerActivity extends AppCompatActivity {
    private static final String TAG = "MainContainerActivity";
    
    private ViewPager2 viewPager;
    private AuthViewModel authViewModel;
    private CalendarSyncManager calendarSyncManager;
    private GoogleAuthApiService googleAuthApiService;
    private int currentPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_container);
        
        // Setup window insets
        View container = findViewById(R.id.mainContainer);
        View bottomNav = findViewById(R.id.bottomNavigation);
        
        ViewCompat.setOnApplyWindowInsetsListener(container, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return WindowInsetsCompat.CONSUMED;
        });
        
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
        
        // Setup ViewModel for auth checking
        setupViewModel();
        observeViewModel();
        
        // Setup ViewPager2
        setupViewPager();
        
        // Setup back press handler
        setupBackPressHandler();
        
        // Initialize calendar sync manager
        calendarSyncManager = CalendarSyncManager.getInstance(this);
        googleAuthApiService = ApiClient.get(App.authManager).create(GoogleAuthApiService.class);
        
        // Kiểm tra calendar sync nếu được yêu cầu từ MainActivity
        boolean checkCalendarSync = getIntent().getBooleanExtra("check_calendar_sync", false);
        if (checkCalendarSync) {
            // Delay một chút để UI render xong
            viewPager.postDelayed(this::checkAndShowCalendarPrompt, 500);
        }
    }
    
    private void checkAndShowCalendarPrompt() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.w(TAG, "User not authenticated, skip calendar prompt");
            return;
        }
        
        calendarSyncManager.shouldShowSyncPrompt(user.getUid(), shouldShow -> {
            if (shouldShow && !isFinishing()) {
                Log.d(TAG, "Showing calendar sync prompt");
                showCalendarSyncDialog();
            } else {
                Log.d(TAG, "Skip calendar prompt");
            }
        });
    }
    
    private void showCalendarSyncDialog() {
        if (isFinishing()) {
            return;
        }
        
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.w(TAG, "Cannot show calendar dialog: user not authenticated");
            return;
        }
        
        final String userId = user.getUid();
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_calendar_sync_prompt);
        dialog.setCancelable(true);
        
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
            calendarSyncManager.markUserDeclined(userId);
            dialog.dismiss();
        });
        
        // Don't Ask Again button
        if (btnDontAskAgain != null) {
            btnDontAskAgain.setOnClickListener(v -> {
                Log.d(TAG, "User clicked 'Don't Ask Again'");
                calendarSyncManager.markUserDeclined(userId);
                dialog.dismiss();
            });
        }
        
        // On dismiss/cancel
        dialog.setOnCancelListener(d -> {
            calendarSyncManager.markUserDeclined(userId);
        });
        
        dialog.show();
    }
    
    private void initiateCalendarConnection() {
        Log.d(TAG, "Initiating calendar connection...");
        Toast.makeText(this, "Connecting to Google Calendar...", Toast.LENGTH_SHORT).show();
        
        googleAuthApiService.getAuthUrl().enqueue(new Callback<AuthUrlResponse>() {
            @Override
            public void onResponse(Call<AuthUrlResponse> call, Response<AuthUrlResponse> response) {
                if (!isFinishing() && response.isSuccessful() && response.body() != null) {
                    String authUrl = response.body().getAuthUrl();
                    Log.d(TAG, "Opening OAuth URL: " + authUrl);
                    openChromeCustomTab(authUrl);
                } else {
                    Log.e(TAG, "Failed to get auth URL: " + response.code());
                    Toast.makeText(MainContainerActivity.this, 
                        "Failed to start connection", 
                        Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<AuthUrlResponse> call, Throwable t) {
                Log.e(TAG, "Network error getting auth URL", t);
                Toast.makeText(MainContainerActivity.this, 
                    "Network error: " + t.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void openChromeCustomTab(String url) {
        try {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            builder.setShowTitle(true);
            builder.setToolbarColor(getResources().getColor(R.color.primary));
            
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(this, Uri.parse(url));
            
            Log.d(TAG, "Opened Chrome Custom Tab successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to open Chrome Custom Tab", e);
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            } catch (Exception ex) {
                Log.e(TAG, "Failed to open browser", ex);
                Toast.makeText(this, "Failed to open browser", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // If not on first page, go back to first page
                if (viewPager.getCurrentItem() != 0) {
                    viewPager.setCurrentItem(0);
                } else {
                    // Otherwise, exit app
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }
    
    private void setupViewModel() {
        authViewModel = new ViewModelProvider(
            this,
            ViewModelFactoryProvider.provideAuthViewModelFactory()
        ).get(AuthViewModel.class);
    }
    
    private void observeViewModel() {
        authViewModel.getAuthState().observe(this, state -> {
            Log.d(TAG, "AuthState changed: " + state);
            
            switch (state) {
                case LOGGED_OUT:
                case LOGIN_ERROR:
                case SIGNUP_ERROR:
                    // User is not logged in - navigate to login
                    navigateToLogin();
                    break;
                case LOGIN_SUCCESS:
                case SIGNUP_SUCCESS:
                case IDLE:
                    // User is logged in - stay on this screen
                    break;
            }
        });
    }
    
    private void navigateToLogin() {
        Log.d(TAG, "Navigating to LoginActivity");
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    private void setupViewPager() {
        viewPager = findViewById(R.id.viewPager);
        MainViewPagerAdapter adapter = new MainViewPagerAdapter(this);
        viewPager.setAdapter(adapter);
        
        // Enable smooth scrolling
        viewPager.setUserInputEnabled(true);
        
        // Set offscreen page limit for smooth swiping
        viewPager.setOffscreenPageLimit(1);
        
        // Listen for page changes
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPage = position;
                // BottomNavigationFragment will handle UI updates
                Log.d(TAG, "Page changed to: " + position);
            }
        });
        
        // Get initial page from intent (default 0) - e.g., from notifications
        int initialPage = getIntent().getIntExtra("INITIAL_PAGE", 0);
        if (initialPage >= 0 && initialPage < 4) {
            viewPager.setCurrentItem(initialPage, false);
            currentPage = initialPage;
        }
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Handle navigation from deep links (e.g., Google Calendar callback)
        int targetTab = intent.getIntExtra("navigate_to_tab", -1);
        if (targetTab >= 0 && targetTab < 4 && viewPager != null) {
            viewPager.setCurrentItem(targetTab, true);
        }
    }
}
