package com.example.tralalero.feature.auth.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.tralalero.network.ApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;

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

        if (btnLogin != null) {
            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    attemptLogin();
                }
            });
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

        btnLogin.setEnabled(false);

        AuthApi api = ApiClient.get().create(AuthApi.class);
        Call<LoginResponse> call = api.login(new LoginRequest(email, password));
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful()) {
                    LoginResponse body = response.body();
                    if (body != null && body.user != null) {
                        Log.d("LoginActivity", "Logged in user id=" + body.user.id
                                + ", email=" + body.user.email
                                + ", firebaseUid=" + body.user.firebaseUid);
                    }
                    String msg = body != null && !TextUtils.isEmpty(body.message)
                            ? body.message
                            : "Login success";
                    Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();

                    btnLogin.setEnabled(false);
                    signInWithFirebase(email, password, body);
                } else {
                    btnLogin.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "Login failed: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                btnLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void signInWithFirebase(String email, String password, LoginResponse body) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser current = auth.getCurrentUser();
        if (current != null && current.getEmail() != null && current.getEmail().equalsIgnoreCase(email)) {
            Log.d("LoginActivity", "Already signed in to Firebase uid=" + current.getUid());
            navigateToHome(body);
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser firebaseUser = result.getUser();
                    if (firebaseUser != null) {
                        Log.d("LoginActivity", "Firebase sign-in success uid=" + firebaseUser.getUid());
                    }
                    navigateToHome(body);
                })
                .addOnFailureListener(error -> {
                    btnLogin.setEnabled(true);
                    Log.e("LoginActivity", "Firebase sign-in failed", error);
                    Toast.makeText(LoginActivity.this, "Firebase login failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateToHome(LoginResponse body) {
        Intent intent = new Intent(LoginActivity.this, com.example.tralalero.feature.home.ui.HomeActivity.class);
        if (body != null && body.user != null) {
            intent.putExtra("user_name", body.user.name);
            intent.putExtra("user_email", body.user.email);
        }
        startActivity(intent);
        finish();
    }
}
