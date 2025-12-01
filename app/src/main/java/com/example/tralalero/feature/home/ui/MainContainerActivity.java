package com.example.tralalero.feature.home.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import com.example.tralalero.R;
import com.example.tralalero.adapter.MainViewPagerAdapter;
import com.example.tralalero.feature.auth.ui.login.LoginActivity;
import com.example.tralalero.presentation.viewmodel.AuthViewModel;
import com.example.tralalero.presentation.viewmodel.ViewModelFactoryProvider;

/**
 * New Main Container Activity with ViewPager2
 * Replaces individual Activities (HomeActivity, InboxActivity, etc.)
 * Supports swipe navigation between fragments
 */
public class MainContainerActivity extends AppCompatActivity {
    private static final String TAG = "MainContainerActivity";
    
    private ViewPager2 viewPager;
    private AuthViewModel authViewModel;
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
