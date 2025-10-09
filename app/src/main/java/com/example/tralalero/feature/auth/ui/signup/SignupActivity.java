package com.example.tralalero.feature.auth.ui.signup;

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

import com.example.tralalero.R;
import com.example.tralalero.auth.remote.AuthApi;
import com.example.tralalero.data.remote.dto.auth.LoginRequest;
import com.example.tralalero.data.remote.dto.auth.LoginResponse;
import com.example.tralalero.feature.auth.ui.login.LoginActivity;
import com.example.tralalero.feature.home.ui.Home.HomeActivity;
import com.example.tralalero.network.ApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private Button btnSignUp;

    @SuppressLint("ClickableViewAccessibility")
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

        final boolean[] isPasswordVisible = {false};
        // Set icon mặc định ban đầu
        etPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.eyeoff_svgrepo_com, 0);
        etConfirmPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.eyeoff_svgrepo_com, 0);

        etPassword.setOnTouchListener((v, event) -> {
            final int DRAWABLE_END = 2; // index: 0=start, 1=top, 2=end, 3=bottom

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

        etConfirmPassword.setOnTouchListener((v, event) -> {
            final int DRAWABLE_END = 2; // index: 0=start, 1=top, 2=end, 3=bottom

            if (event.getAction() == MotionEvent.ACTION_UP) {
                // Kiểm tra click vào khu vực icon bên phải
                if (event.getX() >= (etConfirmPassword.getWidth() - etConfirmPassword.getTotalPaddingRight())) {

                    if (isPasswordVisible[0]) {
                        // Ẩn mật khẩu
                        etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        etConfirmPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.eyeoff_svgrepo_com, 0);
                        isPasswordVisible[0] = false;
                    } else {
                        // Hiện mật khẩu
                        etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        etConfirmPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.eye, 0);
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

        // Log the request data for debugging
        Log.d(TAG, "Attempting signup with email: " + email);
        Log.d(TAG, "Password length: " + password.length());

        // Call signup API
        AuthApi api = ApiClient.get().create(AuthApi.class);
        Call<LoginResponse> call = api.register(new LoginRequest(email, password));
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                Log.d(TAG, "Response code: " + response.code());
                Log.d(TAG, "Response message: " + response.message());

                if (response.isSuccessful()) {
                    LoginResponse body = response.body();
                    String msg = body != null && !TextUtils.isEmpty(body.message)
                            ? body.message
                            : "Sign up successful";
                    Toast.makeText(SignupActivity.this, msg, Toast.LENGTH_SHORT).show();
                    syncFirebaseAccount(email, password, body);
                } else {
                    btnSignUp.setEnabled(true);
                    // Try to get error message from response body
                    String errorMessage = "Sign up failed";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error response body: " + errorBody);

                            // Try to parse error response as JSON
                            try {
                                com.google.gson.Gson gson = new com.google.gson.Gson();
                                LoginResponse errorResponse = gson.fromJson(errorBody, LoginResponse.class);
                                if (errorResponse != null && !TextUtils.isEmpty(errorResponse.message)) {
                                    errorMessage = errorResponse.message;
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Failed to parse error response", e);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to read error body", e);
                    }

                    // Fallback to status code based messages
                    if (errorMessage.equals("Sign up failed")) {
                        if (response.code() == 400) {
                            errorMessage = "Invalid email or password format";
                        } else if (response.code() == 409) {
                            errorMessage = "Email already exists";
                        } else if (response.code() == 404) {
                            errorMessage = "Server endpoint not found";
                        } else if (response.code() >= 500) {
                            errorMessage = "Server error. Please try again later";
                        } else {
                            errorMessage = "Sign up failed (Code: " + response.code() + ")";
                        }
                    }

                    Toast.makeText(SignupActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                btnSignUp.setEnabled(true);
                Log.e(TAG, "Network error", t);

                String errorMessage = "Network error";
                if (t instanceof java.net.ConnectException) {
                    errorMessage = "Cannot connect to server. Please check if server is running.";
                } else if (t instanceof java.net.UnknownHostException) {
                    errorMessage = "Network connection error. Check your internet connection.";
                } else if (t instanceof java.net.SocketTimeoutException) {
                    errorMessage = "Request timeout. Please try again.";
                } else {
                    errorMessage = "Network error: " + t.getMessage();
                }

                Toast.makeText(SignupActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void syncFirebaseAccount(String email, String password, LoginResponse body) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser current = auth.getCurrentUser();
        if (current != null && current.getEmail() != null && current.getEmail().equalsIgnoreCase(email)) {
            Log.d(TAG, "Already signed into Firebase uid=" + current.getUid());
            navigateToHome(body);
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser firebaseUser = result.getUser();
                    if (firebaseUser != null) {
                        Log.d(TAG, "Firebase account created uid=" + firebaseUser.getUid());
                    }
                    navigateToHome(body);
                })
                .addOnFailureListener(error -> {
                    if (error instanceof FirebaseAuthUserCollisionException) {
                        auth.signInWithEmailAndPassword(email, password)
                                .addOnSuccessListener(signInResult -> {
                                    FirebaseUser firebaseUser = signInResult.getUser();
                                    if (firebaseUser != null) {
                                        Log.d(TAG, "Firebase sign-in success uid=" + firebaseUser.getUid());
                                    }
                                    navigateToHome(body);
                                })
                                .addOnFailureListener(signInError -> {
                                    btnSignUp.setEnabled(true);
                                    Log.e(TAG, "Firebase sign-in failed", signInError);
                                    Toast.makeText(SignupActivity.this, "Firebase login failed: " + signInError.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        btnSignUp.setEnabled(true);
                        Log.e(TAG, "Firebase create user failed", error);
                        Toast.makeText(SignupActivity.this, "Firebase signup failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToHome(LoginResponse body) {
        Intent intent = new Intent(SignupActivity.this, HomeActivity.class);
        if (body != null && body.user != null) {
            intent.putExtra("user_name", body.user.name);
            intent.putExtra("user_email", body.user.email);
        }
        startActivity(intent);
        finish();
    }
}
