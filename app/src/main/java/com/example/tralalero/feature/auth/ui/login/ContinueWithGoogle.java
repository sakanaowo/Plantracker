package com.example.tralalero.feature.auth.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tralalero.App.App;
import com.example.tralalero.MainActivity;
import com.example.tralalero.R;
import com.example.tralalero.feature.auth.ui.login.LoginActivity;
import com.example.tralalero.feature.auth.ui.signup.SignupActivity;
import com.example.tralalero.feature.home.ui.HomeActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.SignInButton;
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


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

            AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

            FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (user != null) {
                                Log.d(TAG,  "Firebase sign-in success: " + user.getEmail());

                                // 👉 Điều hướng HomeActivity (giống navigateToHome trong LoginActivity)
                                Intent intent = new Intent(ContinueWithGoogle.this, com.example.tralalero.feature.home.ui.HomeActivity.class);
                                intent.putExtra("user_name", user.getDisplayName());
                                intent.putExtra("user_email", user.getEmail());
                                startActivity(intent);
                                finish();
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

}
