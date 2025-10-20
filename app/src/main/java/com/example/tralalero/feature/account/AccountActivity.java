package com.example.tralalero.feature.account;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import com.example.tralalero.App.App;
import com.example.tralalero.core.DependencyProvider;
import com.example.tralalero.R;
import com.example.tralalero.feature.auth.ui.login.LoginActivity;
import com.example.tralalero.feature.home.ui.BaseActivity;
import com.example.tralalero.presentation.viewmodel.AuthViewModel;
import com.example.tralalero.presentation.viewmodel.ViewModelFactoryProvider;

public class AccountActivity extends BaseActivity {
    private static final String TAG = "AccountActivity";
    private TextView tvName;
    private TextView tvUsername;
    private TextView tvEmail;
    private TextView tvAvatarLetter;
    private LinearLayout layoutSettings;
    private ImageButton btnAccountOptions;

    private AuthViewModel authViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initViews();
        setupViewModel();
        observeViewModel();
        setupClickListeners();
        setupBottomNavigation(3); 
    }
    private void initViews() {
        tvName = findViewById(R.id.tvName);
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        tvAvatarLetter = findViewById(R.id.tvAvatarLetter);
        layoutSettings = findViewById(R.id.layoutSettings);
        btnAccountOptions = findViewById(R.id.btnAccountOptions);

        Log.d(TAG, "initViews - layoutSettings: " + (layoutSettings != null ? "FOUND" : "NULL"));
        Log.d(TAG, "initViews - btnAccountOptions: " + (btnAccountOptions != null ? "FOUND" : "NULL"));

        // Test click immediately
        if (btnAccountOptions != null) {
            btnAccountOptions.post(() -> {
                Toast.makeText(this, "Button found! Setting up click listener...", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Button visibility: " + btnAccountOptions.getVisibility());
                Log.d(TAG, "Button clickable: " + btnAccountOptions.isClickable());
                Log.d(TAG, "Button enabled: " + btnAccountOptions.isEnabled());
            });
        } else {
            Toast.makeText(this, "ERROR: btnAccountOptions is NULL!", Toast.LENGTH_LONG).show();
        }
    }
    private void setupViewModel() {
        authViewModel = new ViewModelProvider(
                this,
                ViewModelFactoryProvider.provideAuthViewModelFactory()
        ).get(AuthViewModel.class);
    }
    private void observeViewModel() {
        authViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                Log.d(TAG, "User loaded: " + user.getName());
                tvName.setText(user.getName());
                tvUsername.setText("@" + user.getEmail().split("@")[0]);
                tvEmail.setText(user.getEmail());
                if (user.getName() != null && !user.getName().isEmpty()) {
                    tvAvatarLetter.setText(user.getName().substring(0, 1).toUpperCase());
                }
            } else {
                Log.d(TAG, "No user logged in, redirecting to login...");
                redirectToLogin();
            }
        });
        authViewModel.isLoggedIn().observe(this, isLoggedIn -> {
            if (isLoggedIn != null && !isLoggedIn) {
                Log.d(TAG, "User logged out, redirecting to login...");
                redirectToLogin();
            }
        });
        authViewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_SHORT).show();
                authViewModel.clearError();
            }
        });
    }
    private void setupClickListeners() {
        // Setup menu icon click listener
        if (btnAccountOptions != null) {
            Log.d(TAG, "Setting up click listener for btnAccountOptions");
            btnAccountOptions.setOnClickListener(v -> {
                Log.d(TAG, "btnAccountOptions CLICKED!");
                try {
                    showAccountOptionsMenu(v);
                } catch (Exception e) {
                    Log.e(TAG, "Error showing menu", e);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e(TAG, "btnAccountOptions is NULL!");
        }

        if (layoutSettings != null) {
            layoutSettings.setOnClickListener(v -> {
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to Settings Activity
            });
        }

        // TODO: Add other click listeners for other options
        // layoutOfflineBoards, layoutBrowseTemplates, layoutShareFeedback, layoutHelp
    }

    private void showAccountOptionsMenu(android.view.View anchor) {
        Log.d(TAG, "showAccountOptionsMenu called");
        try {
            PopupMenu popup = new PopupMenu(this, anchor);
            Log.d(TAG, "PopupMenu created");

            popup.getMenuInflater().inflate(R.menu.account_options_menu, popup.getMenu());
            Log.d(TAG, "Menu inflated, item count: " + popup.getMenu().size());

            popup.setOnMenuItemClickListener(item -> {
                Log.d(TAG, "Menu item clicked: " + item.getTitle());
                int itemId = item.getItemId();
                if (itemId == R.id.action_edit_profile) {
                    Toast.makeText(this, "Edit Profile - Coming soon", Toast.LENGTH_SHORT).show();
                    // TODO: Navigate to Edit Profile Activity
                    return true;
                } else if (itemId == R.id.action_logout) {
                    showLogoutDialog();
                    return true;
                }
                return false;
            });

            Log.d(TAG, "Showing popup menu...");
            popup.show();
            Log.d(TAG, "Popup menu shown successfully");
        } catch (Exception e) {
            Log.e(TAG, "Exception in showAccountOptionsMenu", e);
            Toast.makeText(this, "Menu error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    Log.d(TAG, "User confirmed logout");
                    performLogout();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void performLogout() {
        Log.d(TAG, "Performing logout...");
        
        App.authManager.signOut();
        Log.d(TAG, "✓ Auth cleared");
        
        App.dependencyProvider.clearAllCaches();
        Log.d(TAG, "✓ Database cache cleared");
        
        DependencyProvider.reset();
        Log.d(TAG, "✓ DependencyProvider reset");
        
        redirectToLogin();
        Log.d(TAG, "✓ Logout complete");
    }
    
    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
