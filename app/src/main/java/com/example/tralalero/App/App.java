package com.example.tralalero.App;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.tralalero.auth.remote.AuthManager;
import com.example.tralalero.auth.storage.TokenManager;
import com.example.tralalero.sync.StartupSyncWorker;

public class App extends Application {
    private static App instance;
    public static AuthManager authManager;
    public static TokenManager tokenManager;

    @Override
    public void onCreate() {
        super.onCreate();
        
        instance = this;

        // Load preferences before anything else
        loadLanguagePreference();
        loadDarkModePreference();
        
        authManager = new AuthManager(this);
        tokenManager = new TokenManager(this);

        OneTimeWorkRequest syncWork =
                new OneTimeWorkRequest.Builder(StartupSyncWorker.class)
                        .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                        .build();

        WorkManager.getInstance(this).enqueueUniqueWork(
                "startup_sync",
                ExistingWorkPolicy.REPLACE,
                syncWork
        );
    }
    
    /**
     * Load language preference from SharedPreferences and apply it
     */
    private void loadLanguagePreference() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String languageCode = preferences.getString("language", "en");
        
        java.util.Locale locale = new java.util.Locale(languageCode);
        java.util.Locale.setDefault(locale);
        
        android.content.res.Configuration configuration = new android.content.res.Configuration();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            configuration.setLocale(locale);
        } else {
            configuration.locale = locale;
        }
        
        getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());
    }
    
    /**
     * Load dark mode preference from SharedPreferences and apply it
     */
    private void loadDarkModePreference() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isDarkMode = preferences.getBoolean("theme", false);
        
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    /**
     * Get application instance
     * @return App instance
     */
    public static App getInstance() {
        return instance;
    }
}
