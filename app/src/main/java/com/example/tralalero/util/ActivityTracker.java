package com.example.tralalero.util;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Activity Tracker
 * Tracks the current foreground activity for NotificationUIManager
 * 
 * DEV 2: Utility for accessing current activity
 */
public class ActivityTracker implements Application.ActivityLifecycleCallbacks {
    
    private static Activity currentActivity;
    
    /**
     * Get current foreground activity
     * 
     * @return Current activity or null if no activity is in foreground
     */
    public static Activity getCurrentActivity() {
        return currentActivity;
    }
    
    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        // Not needed
    }
    
    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        // Not needed
    }
    
    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        // Activity is now in foreground
        currentActivity = activity;
    }
    
    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        // Activity going to background
        if (currentActivity == activity) {
            currentActivity = null;
        }
    }
    
    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        // Not needed
    }
    
    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
        // Not needed
    }
    
    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        // Clean up reference
        if (currentActivity == activity) {
            currentActivity = null;
        }
    }
}
