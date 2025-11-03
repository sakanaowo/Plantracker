package com.example.tralalero.App;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.preference.PreferenceManager;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.tralalero.BuildConfig;
import com.example.tralalero.auth.remote.AuthManager;
import com.example.tralalero.auth.storage.TokenManager;
import com.example.tralalero.core.DependencyProvider;
import com.example.tralalero.service.AppLifecycleObserver;
import com.example.tralalero.service.NotificationWebSocketManager;
import com.example.tralalero.sync.StartupSyncWorker;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;

public class App extends Application {
    private static final String TAG = "App";
    private static App instance;
    public static AuthManager authManager;
    public static TokenManager tokenManager;
    public static DependencyProvider dependencyProvider;
    
    // WebSocket Manager for real-time notifications (DEV 1)
    private NotificationWebSocketManager wsManager;

    @Override
    public void onCreate() {
        super.onCreate();
        
        instance = this;

        FirebaseApp.initializeApp(this);

        initializeAppCheck();

        loadLanguagePreference();
        loadDarkModePreference();
        
        authManager = new AuthManager(this);
        tokenManager = new TokenManager(this);
        
        dependencyProvider = DependencyProvider.getInstance(this, tokenManager);
        Log.d(TAG, "✓ DependencyProvider initialized with Database");
        
        // Initialize WebSocket Manager (DEV 1)
        initializeWebSocket();

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
    
    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(TAG, "App terminating...");
        
        // ✨ KEEP CACHE: Don't clear cache on app termination
        // Room database persists across app restarts
        // This allows instant data display when app reopens
        // if (dependencyProvider != null) {
        //     dependencyProvider.clearAllCaches();
        //     Log.d(TAG, "✓ All caches cleared");
        // }
        Log.d(TAG, "✓ Cache persisted to disk");
    }

    private void initializeAppCheck() {
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Initializing Firebase App Check with Debug Provider");
            firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            );
        } else {
            Log.d(TAG, "Initializing Firebase App Check with Play Integrity Provider");
            firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
            );
        }

        Log.d(TAG, "Firebase App Check initialized successfully");
    }

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
    
    private void loadDarkModePreference() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isDarkMode = preferences.getBoolean("theme", false);
        
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public static App getInstance() {
        return instance;
    }
    
    /**
     * Initialize WebSocket Manager and Lifecycle Observer (DEV 1)
     */
    private void initializeWebSocket() {
        // Create WebSocket Manager
        wsManager = new NotificationWebSocketManager();
        Log.d(TAG, "✓ WebSocket Manager initialized");
        
        // Setup callback - DEV 2: Show in-app notification UI
        wsManager.setOnNotificationReceivedListener(notification -> {
            Log.d(TAG, "📬 Notification received: " + notification.getTitle());
            // DEV 2: Call NotificationUIManager to show Snackbar
            com.example.tralalero.ui.NotificationUIManager.handleInAppNotification(this, notification);
        });
        
        // Register lifecycle observer
        AppLifecycleObserver lifecycleObserver = new AppLifecycleObserver(this, wsManager);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(lifecycleObserver);
        Log.d(TAG, "✓ Lifecycle Observer registered");
        
        // DEV 2: Register Activity Tracker for in-app notifications
        registerActivityLifecycleCallbacks(new com.example.tralalero.util.ActivityTracker());
        Log.d(TAG, "✓ Activity Tracker registered");
    }
    
    /**
     * Get WebSocket Manager instance
     * Used by DEV 2 to access WebSocket functionality
     */
    public NotificationWebSocketManager getWebSocketManager() {
        return wsManager;
    }
}
