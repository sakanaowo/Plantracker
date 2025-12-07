package com.example.tralalero;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.tralalero.App.App;
import com.example.tralalero.data.remote.api.GoogleAuthApiService;
import com.example.tralalero.domain.model.AuthUrlResponse;
import com.example.tralalero.feature.auth.ui.login.ContinueWithGoogle;
import com.example.tralalero.feature.auth.ui.login.LoginActivity;
import com.example.tralalero.feature.auth.ui.signup.SignupActivity;
import com.example.tralalero.feature.calendar.CalendarSyncManager;
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
    
    private AuthViewModel authViewModel;
    private GoogleAuthApiService googleAuthApiService;
    private CalendarSyncManager calendarSyncManager;
    private boolean isFirstLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set loading layout để tránh màn hình trắng khi hiện dialog
        setContentView(R.layout.activity_loading);
        
        // Initialize managers
        calendarSyncManager = CalendarSyncManager.getInstance(this);
        googleAuthApiService = ApiClient.get(App.authManager).create(GoogleAuthApiService.class);
        
        // Check if this is a first login (from intent extra)
        isFirstLogin = getIntent().getBooleanExtra("is_first_login", false);
        
        Log.d(TAG, "onCreate: isFirstLogin=" + isFirstLogin);
        
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
        
        // Điều hướng về trang chủ ngay lập tức
        Intent intent = new Intent(this, MainContainerActivity.class);
        intent.putExtra("check_calendar_sync", true); // Flag để MainContainerActivity kiểm tra
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
    

}