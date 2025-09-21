package com.example.tralalero.feature.auth.ui.signup;

import android.os.Bundle;
import android.content.Intent;
import android.text.TextUtils;
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

import com.example.tralalero.R;
import com.example.tralalero.auth.remote.AuthApi;
import com.example.tralalero.auth.remote.dto.LoginRequest;
import com.example.tralalero.auth.remote.dto.LoginResponse;
import com.example.tralalero.feature.auth.ui.login.LoginActivity;
import com.example.tralalero.network.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private Button btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI components
        etEmail = findViewById(R.id.editTextEmailSignup);
        etPassword = findViewById(R.id.editTextPasswordSignup);
        etConfirmPassword = findViewById(R.id.editTextConfirmPasswordSignup);
        btnSignUp = findViewById(R.id.buttonSignUp);

        // Set up Sign Up button click listener
        if (btnSignUp != null) {
            btnSignUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    attemptSignUp();
                }
            });
        }

        // Navigate back to Login
        TextView tvLoginLink = findViewById(R.id.textViewLoginLink);
        if (tvLoginLink != null) {
            tvLoginLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                }
            });
        }
    }

    private void attemptSignUp() {
        String email = etEmail != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword != null ? etPassword.getText().toString() : "";
        String confirmPassword = etConfirmPassword != null ? etConfirmPassword.getText().toString() : "";

        // Validate email
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Basic email validation
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Password length validation
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate confirm password
        if (TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Please confirm your password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button during API call
        btnSignUp.setEnabled(false);

        // Call signup API
        AuthApi api = ApiClient.get().create(AuthApi.class);
        Call<LoginResponse> call = api.register(new LoginRequest(email, password));
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                btnSignUp.setEnabled(true);
                if (response.isSuccessful()) {
                    LoginResponse body = response.body();
                    String msg = body != null && !TextUtils.isEmpty(body.message)
                            ? body.message
                            : "Sign up successful";
                    Toast.makeText(SignupActivity.this, msg, Toast.LENGTH_SHORT).show();

                    // Navigate to Home screen with basic user info
                    Intent intent = new Intent(SignupActivity.this, com.example.tralalero.feature.home.ui.HomeActivity.class);
                    if (body != null && body.user != null) {
                        intent.putExtra("user_name", body.user.name);
                        intent.putExtra("user_email", body.user.email);
                    }
                    startActivity(intent);
                    finish();
                } else {
                    String errorMessage = "Sign up failed";
                    if (response.code() == 400) {
                        errorMessage = "Invalid email or password";
                    } else if (response.code() == 409) {
                        errorMessage = "Email already exists";
                    } else {
                        errorMessage = "Sign up failed: " + response.code();
                    }
                    Toast.makeText(SignupActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                btnSignUp.setEnabled(true);
                Toast.makeText(SignupActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

