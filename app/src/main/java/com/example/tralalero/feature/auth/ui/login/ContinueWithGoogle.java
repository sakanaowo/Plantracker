package com.example.tralalero.feature.auth.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tralalero.R;
import com.example.tralalero.auth.repository.FirebaseAuthRepository;
import com.example.tralalero.auth.remote.AuthManager;
import com.example.tralalero.auth.remote.dto.FirebaseAuthResponse;
import com.example.tralalero.auth.storage.TokenManager;
import com.example.tralalero.feature.home.ui.Home.HomeActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class ContinueWithGoogle extends AppCompatActivity {
    private static final String TAG = "ContinueWithGoogle";
    private static final int RC_SIGN_IN = 100;
    private GoogleSignInClient mGoogleSignInClient;
    private String mClientId;
    private AuthManager authManager; // Add AuthManager field


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize AuthManager
        authManager = new AuthManager(getApplication());

        mClientId = getApplicationContext().getString(R.string.client_id);
        Log.d(TAG, "mClientId: "+ mClientId);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(mClientId) // lấy từ google-services.json
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Button btn_Google = findViewById(R.id.btn_google);
        if (btn_Google != null) {
            btn_Google.setOnClickListener(v -> signIn());
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        Log.d(TAG,"handleSignInResult:");
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account == null) {
                Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show();
                return;
            }

            String idToken = account.getIdToken();
            if (idToken == null) {
                Toast.makeText(this, "Failed to get ID token", Toast.LENGTH_SHORT).show();
                return;
            }

            // First, authenticate with Firebase (existing flow)
            AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

            FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (user != null) {
                                Log.d(TAG, "Firebase sign-in success: " + user.getEmail());
                                
                                // 👉 Now authenticate with backend using Firebase ID token
                                authenticateWithBackend(idToken, user);
                            }
                        } else {
                            Log.w(TAG, "signInWithCredential failed", task.getException());
                            Toast.makeText(ContinueWithGoogle.this, "Firebase login failed", Toast.LENGTH_SHORT).show();
                        }
                    });

        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }

    /**
     * Authenticate with backend API using Firebase ID token
     */
    private void authenticateWithBackend(String googleIdToken, FirebaseUser firebaseUser) {
        // Wait for Firebase to fully authenticate and get Firebase ID token
        firebaseUser.getIdToken(true) // Force refresh to get Firebase ID token
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    String firebaseIdToken = task.getResult().getToken();
                    Log.d(TAG, "Got Firebase ID token for backend auth");

                    // Now use Firebase ID token (not Google token) for backend
                    FirebaseAuthRepository repository = new FirebaseAuthRepository(authManager);

                    repository.authenticateWithFirebase(firebaseIdToken, new FirebaseAuthRepository.FirebaseAuthCallback() {
                        @Override
                        public void onSuccess(FirebaseAuthResponse response, String firebaseIdToken) {
                            Log.d(TAG, "Backend authentication successful: " + response.message);

                            // Save Firebase ID token and user info using TokenManager
                            TokenManager tokenManager = new TokenManager(ContinueWithGoogle.this);
                            tokenManager.saveAuthData(
                                firebaseIdToken,  // Store Firebase ID token
                                firebaseUser.getUid(),
                                firebaseUser.getEmail(),
                                firebaseUser.getDisplayName()
                            );

                            // Navigate to HomeActivity with user data
                            Intent intent = new Intent(ContinueWithGoogle.this, HomeActivity.class);
                            intent.putExtra("user_name", firebaseUser.getDisplayName());
                            intent.putExtra("user_email", firebaseUser.getEmail());
                            intent.putExtra("firebase_id_token", firebaseIdToken);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "Backend authentication failed: " + error);
                            Toast.makeText(ContinueWithGoogle.this, "Authentication failed: " + error, Toast.LENGTH_LONG).show();

                            // Sign out from Firebase on backend auth failure
                            FirebaseAuth.getInstance().signOut();
                        }
                    });
                } else {
                    Log.e(TAG, "Failed to get Firebase ID token", task.getException());
                    Toast.makeText(ContinueWithGoogle.this, "Failed to get Firebase token", Toast.LENGTH_SHORT).show();
                    FirebaseAuth.getInstance().signOut();
                }
            });
    }

}
