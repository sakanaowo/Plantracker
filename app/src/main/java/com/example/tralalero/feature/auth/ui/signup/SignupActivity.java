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
import androidx.lifecycle.ViewModelProvider;
import com.example.tralalero.MainActivity;
import com.example.tralalero.R;
import com.example.tralalero.data.repository.AuthRepositoryImpl;
import com.example.tralalero.domain.repository.IAuthRepository;
import com.example.tralalero.domain.usecase.auth.GetCurrentUserUseCase;
import com.example.tralalero.domain.usecase.auth.IsLoggedInUseCase;
import com.example.tralalero.domain.usecase.auth.LoginUseCase;
import com.example.tralalero.domain.usecase.auth.LogoutUseCase;
import com.example.tralalero.feature.auth.ui.login.LoginActivity;
import com.example.tralalero.feature.home.ui.MainContainerActivity;
import com.example.tralalero.presentation.viewmodel.AuthViewModel;
import com.example.tralalero.presentation.viewmodel.AuthViewModelFactory;
import com.example.tralalero.presentation.viewmodel.ViewModelFactoryProvider;
public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private EditText etName;
    private Button btnSignUp;
    private AuthViewModel authViewModel;
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
        etEmail = findViewById(R.id.editTextEmailSignup);
        etPassword = findViewById(R.id.editTextPasswordSignup);
        etConfirmPassword = findViewById(R.id.editTextConfirmPasswordSignup);
        etName = findViewById(R.id.editTextNameSignup);
        btnSignUp = findViewById(R.id.buttonSignUp);
        setupViewModel();
        observeViewModel();
        final boolean[] isPasswordVisible = {false};
        etPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.eyeoff_svgrepo_com, 0);
        etConfirmPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.eyeoff_svgrepo_com, 0);
        etPassword.setOnTouchListener((v, event) -> {
            final int DRAWABLE_END = 2; // index: 0=start, 1=top, 2=end, 3=bottom

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
        etConfirmPassword.setOnTouchListener((v, event) -> {
            final int DRAWABLE_END = 2; 
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getX() >= (etConfirmPassword.getWidth() - etConfirmPassword.getTotalPaddingRight())) {
                    if (isPasswordVisible[0]) {
                        etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        etConfirmPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.eyeoff_svgrepo_com, 0);
                        isPasswordVisible[0] = false;
                    } else {
                        etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        etConfirmPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.eye, 0);
                        isPasswordVisible[0] = true;
                    }
                    etPassword.setTypeface(Typeface.DEFAULT);
                    etPassword.setSelection(etPassword.length());
                    return true;
                }
            }
            return false;
        });
        if (btnSignUp != null) {
            btnSignUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    attemptSignUp();
                }
            });
        }
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
    private void setupViewModel() {
        authViewModel = new ViewModelProvider(this,
            ViewModelFactoryProvider.provideAuthViewModelFactory()
        ).get(AuthViewModel.class);
    }
    private void observeViewModel() {
        // Observe AuthState for auto-navigation
        authViewModel.getAuthState().observe(this, state -> {
            switch (state) {
                case SIGNUP_SUCCESS:
                    // Auto-navigate to Home on successful signup
                    navigateToHome();
                    break;
                case SIGNUP_ERROR:
                    // Error is handled by error observer below
                    break;
                case SIGNING_UP:
                    // Loading state is handled by loading observer below
                    break;
                case IDLE:
                case LOGGED_OUT:
                    // Do nothing
                    break;
            }
        });

        // Observe loading state
        authViewModel.isLoading().observe(this, isLoading -> {
            if (isLoading) {
                btnSignUp.setEnabled(false);
                btnSignUp.setText("Signing up...");
            } else {
                btnSignUp.setEnabled(true);
                btnSignUp.setText("Sign Up");
            }
        });

        // Observe current user for display purposes
        authViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                Log.d("SignupActivity", "Signed up user id=" + user.id
                        + ", email=" + user.email
                        + ", firebaseUid=" + user.firebaseUid);
                Toast.makeText(this, "Welcome, " + user.name, Toast.LENGTH_SHORT).show();
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
        // Navigate through MainActivity to trigger calendar sync prompt for new users
        Intent intent = new Intent(SignupActivity.this, MainActivity.class);
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
    private void attemptSignUp() {
        String email = etEmail != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword != null ? etPassword.getText().toString() : "";
        String confirmPassword = etConfirmPassword != null ? etConfirmPassword.getText().toString() : "";
        String name = etName != null ? etName.getText().toString().trim() : "";
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Please confirm your password", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        authViewModel.signup(email, password, name);
    }
}
