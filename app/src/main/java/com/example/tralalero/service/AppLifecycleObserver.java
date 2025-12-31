package com.example.tralalero.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.example.tralalero.App.App;

/**
 * App Lifecycle Observer
 * Manages WebSocket connection based on app foreground/background state
 * 
 * When app is in foreground:
 * - Connect WebSocket → Receive notifications in-app
 * - Disable FCM notifications (to avoid duplicates)
 * 
 * When app is in background:
 * - Disconnect WebSocket (save battery)
 * - Enable FCM notifications (system notifications)
 */
public class AppLifecycleObserver implements DefaultLifecycleObserver {
    
    private static final String TAG = "AppLifecycleObserver";
    private static final String PREFS_NAME = "notification_prefs";
    private static final String KEY_SHOW_FCM = "show_fcm_notifications";
    
    private final Context context;
    private final NotificationWebSocketManager wsManager;
    private final SharedPreferences prefs;
    
    public AppLifecycleObserver(Context context, NotificationWebSocketManager wsManager) {
        this.context = context;
        this.wsManager = wsManager;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * Called when app comes to foreground
     */
    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        Log.d(TAG, "🟢 App FOREGROUND - Connecting WebSocket");
        
        // Keep FCM enabled temporarily during WebSocket connection
        // This prevents missing notifications during race condition window
        Log.d(TAG, "⏳ Keeping FCM ENABLED during WebSocket connection (prevent race condition)");
        
        // Connect WebSocket on background thread (blocking call)
        new Thread(() -> {
            try {
                String token = getFirebaseToken();
                if (token != null && !token.isEmpty()) {
                    wsManager.connect(token);
                    
                    // Set user ID if available
                    String userId = getUserId();
                    if (userId != null) {
                        wsManager.setCurrentUserId(userId);
                    }
                    
                    // FCM will be disabled by WebSocket's EVENT_CONNECT listener
                    // No need to manually disable here to avoid race conditions
                    Log.d(TAG, "✅ WebSocket connecting, FCM will be disabled automatically");
                } else {
                    Log.w(TAG, "No Firebase token available, cannot connect WebSocket");
                    // Keep FCM enabled if WebSocket can't connect
                    setShowFCMNotifications(true);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error connecting WebSocket", e);
                // Keep FCM enabled if WebSocket connection fails
                setShowFCMNotifications(true);
            }
        }).start();
    }
    
    /**
     * Called when app goes to background
     */
    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        Log.d(TAG, "🔴 App BACKGROUND - Disconnecting WebSocket");
        
        // Enable FCM notifications (for background)
        setShowFCMNotifications(true);
        
        // Disconnect WebSocket (save battery)
        wsManager.disconnect();
    }
    
    /**
     * Set whether FCM notifications should be shown
     */
    private void setShowFCMNotifications(boolean show) {
        prefs.edit().putBoolean(KEY_SHOW_FCM, show).apply();
        Log.d(TAG, "FCM notifications " + (show ? "ENABLED" : "DISABLED"));
    }
    
    /**
     * Get Firebase token from AuthManager
     * MUST be called from background thread (blocking call)
     */
    private String getFirebaseToken() {
        try {
            // Try to get cached token first (fast)
            if (App.authManager != null) {
                String cachedToken = App.authManager.getCachedToken();
                if (cachedToken != null && !cachedToken.isEmpty()) {
                    Log.d(TAG, "Using cached Firebase token");
                    return cachedToken;
                }
                
                // If no cached token, get fresh one (blocking call - MUST be on background thread!)
                Log.d(TAG, "Fetching fresh Firebase token (blocking)...");
                return App.authManager.getIdTokenBlocking();
            }
            
            Log.w(TAG, "AuthManager is null");
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error getting Firebase token", e);
            return null;
        }
    }
    
    /**
     * Get user ID from SharedPreferences
     */
    private String getUserId() {
        try {
            SharedPreferences userPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            return userPrefs.getString("internal_user_id", null);
        } catch (Exception e) {
            Log.e(TAG, "Error getting user ID", e);
            return null;
        }
    }
}
