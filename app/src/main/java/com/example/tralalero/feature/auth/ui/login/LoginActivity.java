package com.example.tralalero.feature.auth.ui.login;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import com.example.tralalero.MainActivity;
import com.example.tralalero.R;
import com.example.tralalero.feature.auth.ui.signup.SignupActivity;
import com.example.tralalero.feature.auth.ui.forgot.ForgotPasswordActivity;
import com.example.tralalero.feature.home.ui.MainContainerActivity;
import com.example.tralalero.presentation.viewmodel.AuthViewModel;
import com.example.tralalero.presentation.viewmodel.ViewModelFactoryProvider;
import com.example.tralalero.auth.repository.FirebaseAuthRepository;
import com.example.tralalero.data.remote.dto.auth.FirebaseAuthResponse;
import com.example.tralalero.auth.storage.TokenManager;
import com.example.tralalero.App.App;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private MaterialButton btnGoogleSignIn;
    private MaterialButton btnGoogleSignUp;
    private AuthViewModel authViewModel;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private TokenManager tokenManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        tokenManager = new TokenManager(this);
        
        // Initialize views
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        btnGoogleSignUp = findViewById(R.id.btnGoogleSignUp);
        
        // Setup Google Sign In
        setupGoogleSignIn();
        setupGoogleSignInLauncher();
        setupViewModel();
        observeViewModel();
        setupClickListeners();
    }
    
    private void setupClickListeners() {
        // Both buttons use same Google Sign In flow
        if (btnGoogleSignIn != null) {
            btnGoogleSignIn.setOnClickListener(v -> {
                Log.d(TAG, "Sign In with Google clicked");
                signInWithGoogle();
            });
        }
        
        if (btnGoogleSignUp != null) {
            btnGoogleSignUp.setOnClickListener(v -> {
                Log.d(TAG, "Sign Up with Google clicked");
                // Same flow as sign in - Google handles account creation
                signInWithGoogle();
            });
        }
    }
    private void setupViewModel() {
        authViewModel = new ViewModelProvider(this,
            ViewModelFactoryProvider.provideAuthViewModelFactory()
        ).get(AuthViewModel.class);
    }
    private void observeViewModel() {
        // Observe AuthState for auto-navigation
        authViewModel.getAuthState().observe(this, state -> {
            switch (state) {
                case LOGIN_SUCCESS:
                    // Auto-navigate to Home on successful login
                    navigateToHome();
                    break;
                case LOGIN_ERROR:
                    // Error is handled by error observer below
                    break;
                case LOGGING_IN:
                    // Loading state is handled by loading observer below
                    break;
                case IDLE:
                case LOGGED_OUT:
                    // Do nothing
                    break;
            }
        });

        // Observe current user for display purposes
        authViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                Log.d("LoginActivity", "Logged in user id=" + user.id
                        + ", email=" + user.email
                        + ", firebaseUid=" + user.firebaseUid);
                Toast.makeText(this, "Welcome back, " + user.name, Toast.LENGTH_SHORT).show();
                getFirebaseToken();
            }
        });

        // Observe error state
        authViewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_SHORT).show();
                authViewModel.clearError();
            }
        });
    }

    private void navigateToHome() {
        // Navigate through MainActivity to trigger calendar sync prompt
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("is_first_login", true); // Flag for calendar sync prompt
        
        com.example.tralalero.domain.model.User user = authViewModel.getCurrentUser().getValue();
        if (user != null) {
            intent.putExtra("user_name", user.getName());
            intent.putExtra("user_email", user.getEmail());
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void getFirebaseToken() {
        com.google.firebase.auth.FirebaseUser firebaseUser =
            com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            firebaseUser.getIdToken(true)
                .addOnSuccessListener(result -> {
                    String token = result.getToken();
                    Log.d("LoginActivity", "==========================================");
                    Log.d("LoginActivity", "FIREBASE ID TOKEN:");
                    Log.d("LoginActivity", token);
                    Log.d("LoginActivity", "==========================================");
                    Log.d("LoginActivity", "Token length: " + (token != null ? token.length() : 0));
                    if (token != null && token.length() > 50) {
                        Log.d("LoginActivity", "Token preview: " + token.substring(0, 50) + "...");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("LoginActivity", "Failed to get Firebase token", e);
                    Toast.makeText(this, "Failed to get token: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        } else {
            Log.e(TAG, "No Firebase user found after login!");
        }
    }
    
    private void setupGoogleSignIn() {
        String clientId = getString(R.string.client_id);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(clientId)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupGoogleSignInLauncher() {
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getData() != null) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        handleGoogleSignInResult(task);
                    }
                }
        );
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> task) {
        if (!task.isSuccessful()) {
            Exception exception = task.getException();
            Log.e(TAG, "Google sign-in failed", exception);
            showError("Google sign-in failed");
            return;
        }

        GoogleSignInAccount account = task.getResult();
        if (account == null || account.getIdToken() == null) {
            showError("Failed to get Google account");
            return;
        }

        String googleIdToken = account.getIdToken();
        signInToFirebase(googleIdToken);
    }

    private void signInToFirebase(String googleIdToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(googleIdToken, null);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        Log.d(TAG, "Firebase sign-in successful: " + user.getEmail());
                        authenticateWithBackend(user);
                    } else {
                        showError("Firebase user is null");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firebase sign-in failed", e);
                    showError("Firebase authentication failed");
                });
    }

    private void authenticateWithBackend(FirebaseUser firebaseUser) {
        firebaseUser.getIdToken(true)
                .addOnSuccessListener(result -> {
                    String firebaseIdToken = result.getToken();
                    if (firebaseIdToken == null) {
                        showError("Failed to get Firebase token");
                        return;
                    }
                    syncWithBackend(firebaseUser, firebaseIdToken);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get Firebase ID token", e);
                    showError("Failed to get authentication token");
                });
    }

    private void syncWithBackend(FirebaseUser firebaseUser, String firebaseIdToken) {
        FirebaseAuthRepository repository = new FirebaseAuthRepository(App.authManager);
        repository.authenticateWithFirebase(firebaseIdToken, new FirebaseAuthRepository.FirebaseAuthCallback() {
            @Override
            public void onSuccess(FirebaseAuthResponse response, String token) {
                Log.d(TAG, "Backend authentication successful: " + response.message);
                
                // Save both Firebase UID and internal UUID
                String internalUserId = response.getUser() != null ? response.getUser().getId() : null;
                
                tokenManager.saveAuthData(
                        token,
                        firebaseUser.getUid(),
                        firebaseUser.getEmail(),
                        firebaseUser.getDisplayName(),
                        internalUserId  // Internal UUID from backend
                );
                
                Log.d(TAG, "Saved internal user ID: " + internalUserId);
                navigateToHomeWithUserData(firebaseUser.getDisplayName(), firebaseUser.getEmail());
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Backend authentication failed: " + error);
                showError("Authentication failed: " + error);
                FirebaseAuth.getInstance().signOut();
            }
        });
    }

    private void navigateToHomeWithUserData(String userName, String userEmail) {
        // Navigate through MainActivity to trigger calendar sync prompt
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("is_first_login", true); // Flag for calendar sync prompt
        intent.putExtra("user_name", userName);
        intent.putExtra("user_email", userEmail);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
