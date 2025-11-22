package com.example.tralalero.feature.auth.ui.login;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tralalero.App.App;
import com.example.tralalero.R;
import com.example.tralalero.auth.repository.FirebaseAuthRepository;
import com.example.tralalero.auth.remote.AuthManager;
import com.example.tralalero.data.remote.dto.auth.FirebaseAuthResponse;
import com.example.tralalero.auth.storage.TokenManager;
import com.example.tralalero.feature.home.ui.MainContainerActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
public class ContinueWithGoogle extends AppCompatActivity {
    private static final String TAG = "ContinueWithGoogle";
    private GoogleSignInClient googleSignInClient;
    private AuthManager authManager;
    private TokenManager tokenManager;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        authManager = App.authManager;
        tokenManager = new TokenManager(this);
        setupGoogleSignIn();
        setupGoogleSignInLauncher();
        Button btnGoogle = findViewById(R.id.btn_google);
        if (btnGoogle != null) {
            btnGoogle.setOnClickListener(v -> signInWithGoogle());
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
        FirebaseAuthRepository repository = new FirebaseAuthRepository(authManager);
        repository.authenticateWithFirebase(firebaseIdToken, new FirebaseAuthRepository.FirebaseAuthCallback() {
            @Override
            public void onSuccess(FirebaseAuthResponse response, String token) {
                Log.d(TAG, "Backend authentication successful: " + response.message);
                tokenManager.saveAuthData(
                    token,
                    firebaseUser.getUid(),
                    firebaseUser.getEmail(),
                    firebaseUser.getDisplayName()
                );
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
        Intent intent = new Intent(this, MainContainerActivity.class);
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
