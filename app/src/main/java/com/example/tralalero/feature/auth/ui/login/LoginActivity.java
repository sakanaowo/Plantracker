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
import com.example.tralalero.R;
import com.example.tralalero.feature.auth.ui.signup.SignupActivity;
import com.example.tralalero.feature.auth.ui.forgot.ForgotPasswordActivity;
import com.example.tralalero.feature.home.ui.Home.HomeActivity;
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

    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private MaterialButton btnGoogleSignIn;
    private TextView textViewForgotPassword;
    private TextView textViewSignUp;
    private AuthViewModel authViewModel;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private TokenManager tokenManager;
    @SuppressLint("ClickableViewAccessibility")
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
        setupGoogleSignIn();
        setupGoogleSignInLauncher();

        etEmail = findViewById(R.id.editTextEmail);
        etPassword = findViewById(R.id.editTextPassword);
        btnLogin = findViewById(R.id.buttonLogin);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        textViewForgotPassword = findViewById(R.id.textViewForgotPassword);
        textViewSignUp = findViewById(R.id.textViewSignUp);
        setupViewModel();
        observeViewModel();
        setupClickListeners();
        final boolean[] isPasswordVisible = {false};
        etPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.eyeoff_svgrepo_com, 0);
        etPassword.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getX() >= (etPassword.getWidth() - etPassword.getTotalPaddingRight())) {
                    if (isPasswordVisible[0]) {
                        etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        etPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.eyeoff_svgrepo_com, 0);
                        isPasswordVisible[0] = false;
                    } else {
                        etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        etPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.eye, 0);
                        isPasswordVisible[0] = true;
                    }
                    etPassword.setTypeface(Typeface.DEFAULT);
                    etPassword.setSelection(etPassword.length());
                    return true;
                }
            }
            return false;
        });
        if (btnLogin != null) {
            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    attemptLogin();
                }
            });
        }
    }
    private void setupClickListeners() {
        if (btnGoogleSignIn != null) {
            btnGoogleSignIn.setOnClickListener(v -> {
                Log.d(TAG, "Google Sign In clicked - starting sign-in flow");
                signInWithGoogle();
            });
        }
        if (textViewForgotPassword != null) {
            textViewForgotPassword.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            });
        }
        if (textViewSignUp != null) {
            textViewSignUp.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            });
        }
    }
    private void setupViewModel() {
        authViewModel = new ViewModelProvider(this,
            ViewModelFactoryProvider.provideAuthViewModelFactory()
        ).get(AuthViewModel.class);
    }
    private void observeViewModel() {
        authViewModel.isLoading().observe(this, isLoading -> {
            if (isLoading) {
                btnLogin.setEnabled(false);
                btnLogin.setText("Logging in...");
            } else {
                btnLogin.setEnabled(true);
                btnLogin.setText("Login");
            }
        });
        authViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                Log.d("LoginActivity", "Logged in user id=" + user.id
                        + ", email=" + user.email
                        + ", firebaseUid=" + user.firebaseUid);
                Toast.makeText(this, "Welcome back, " + user.name, Toast.LENGTH_SHORT).show();
                getFirebaseToken();
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                intent.putExtra("user_name", user.getName());
                intent.putExtra("user_email", user.getEmail());
                startActivity(intent);
                finish();
            }
        });
        authViewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_SHORT).show();
                authViewModel.clearError();
            }
        });
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
            Log.e("LoginActivity", "No Firebase user found after login!");
        }
    }
    private void attemptLogin() {
        String email = etEmail != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword != null ? etPassword.getText().toString() : "";
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show();
            return;
        }
        authViewModel.login(email, password);
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
                navigateToHome(firebaseUser);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Backend authentication failed: " + error);
                showError("Authentication failed: " + error);
                FirebaseAuth.getInstance().signOut();
            }
        });
    }

    private void navigateToHome(FirebaseUser user) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("user_name", user.getDisplayName());
        intent.putExtra("user_email", user.getEmail());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
