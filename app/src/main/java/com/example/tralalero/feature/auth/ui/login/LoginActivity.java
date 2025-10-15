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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.tralalero.R;
import com.example.tralalero.auth.remote.AuthApi;
import com.example.tralalero.feature.auth.ui.signup.SignupActivity;
import com.example.tralalero.feature.auth.ui.forgot.ForgotPasswordActivity;
import com.example.tralalero.feature.home.ui.Home.HomeActivity;
import com.example.tralalero.network.ApiClient;

import com.example.tralalero.presentation.viewmodel.AuthViewModel;
import com.example.tralalero.presentation.viewmodel.AuthViewModelFactory;
import com.example.tralalero.domain.usecase.auth.*;
import com.example.tralalero.data.repository.AuthRepositoryImpl;
import com.example.tralalero.domain.repository.IAuthRepository;
import com.example.tralalero.App.App;
import com.example.tralalero.presentation.viewmodel.ViewModelFactoryProvider;
import com.google.android.material.button.MaterialButton;


public class LoginActivity extends AppCompatActivity {
    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private MaterialButton btnGoogleSignIn;
    private TextView textViewForgotPassword;
    private TextView textViewSignUp;

    private AuthViewModel authViewModel;

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

        // Set icon mặc định ban đầu
        etPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.eyeoff_svgrepo_com, 0);

        etPassword.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                // Kiểm tra click vào khu vực icon bên phải
                if (event.getX() >= (etPassword.getWidth() - etPassword.getTotalPaddingRight())) {

                    if (isPasswordVisible[0]) {
                        // Ẩn mật khẩu
                        etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        etPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.eyeoff_svgrepo_com, 0);
                        isPasswordVisible[0] = false;
                    } else {
                        // Hiện mật khẩu
                        etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        etPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.eye, 0);
                        isPasswordVisible[0] = true;
                    }

                    // Giữ font và con trỏ
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
        // Google Sign In button
        if (btnGoogleSignIn != null) {
            btnGoogleSignIn.setOnClickListener(v -> {
                Log.d("LoginActivity", "Google Sign In clicked");
                Toast.makeText(this, "Google Sign In - Coming soon!", Toast.LENGTH_SHORT).show();
                // TODO: Implement Google Sign In
                // Intent intent = new Intent(LoginActivity.this, ContinueWithGoogle.class);
                // startActivity(intent);
            });
        }

        // Forgot Password
        if (textViewForgotPassword != null) {
            textViewForgotPassword.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            });
        }

        // Sign Up
        if (textViewSignUp != null) {
            textViewSignUp.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            });
        }
    }

    private void setupViewModel() {
        // Sử dụng ViewModelFactoryProvider để có factory đúng
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

                // Get and log Firebase ID Token
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

    /**
     * Lấy và log Firebase ID Token sau khi login thành công
     */
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

                    // Log first 50 characters for quick verification
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
}
