package com.example.tralalero.feature.home.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tralalero.R;

/**
 * Activity to handle Google Calendar OAuth callback
 * This receives the deep link: plantracker://calendar/connected?success=true&email=...
 */
public class GoogleCalendarCallbackActivity extends AppCompatActivity {
    private static final String TAG = "GoogleCalendarCallback";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading); // Simple loading layout
        
        handleDeepLink(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleDeepLink(intent);
    }

    private void handleDeepLink(Intent intent) {
        Uri data = intent.getData();
        
        if (data == null) {
            Log.e(TAG, "No data in intent");
            finishWithError("Invalid callback");
            return;
        }

        Log.d(TAG, "Received deep link: " + data.toString());

        // Parse query parameters
        String success = data.getQueryParameter("success");
        String email = data.getQueryParameter("email");
        String error = data.getQueryParameter("error");

        if ("true".equals(success)) {
            // Success!
            Log.d(TAG, "Google Calendar connected successfully: " + email);
            Toast.makeText(this, 
                "✓ Connected to Google Calendar\n" + email, 
                Toast.LENGTH_LONG).show();
            
            // Navigate back to Settings
            navigateToSettings();
        } else {
            // Error
            String errorMsg = error != null ? error : "Unknown error";
            Log.e(TAG, "Google Calendar connection failed: " + errorMsg);
            Toast.makeText(this, 
                "Failed to connect: " + errorMsg, 
                Toast.LENGTH_LONG).show();
            
            finishWithError(errorMsg);
        }
    }

    private void navigateToSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void finishWithError(String error) {
        // Could show a dialog here if needed
        finish();
    }
}
