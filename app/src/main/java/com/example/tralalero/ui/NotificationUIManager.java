package com.example.tralalero.ui;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.example.tralalero.App.App;
import com.example.tralalero.R;
import com.example.tralalero.data.remote.dto.websocket.NotificationPayload;
import com.example.tralalero.util.ActivityTracker;
import com.example.tralalero.util.DeepLinkNavigator;
import com.google.android.material.snackbar.Snackbar;

/**
 * Notification UI Manager
 * Handles displaying in-app notifications as Snackbars
 * 
 * DEV 2: UI & Navigation Layer
 */
public class NotificationUIManager {
    
    private static final String TAG = "NotificationUIManager";
    
    /**
     * Handle incoming notification and show in-app UI
     * Called by WebSocketManager when notification arrives while app is foreground
     * 
     * @param context Application context
     * @param notification Notification payload from WebSocket
     */
    public static void handleInAppNotification(Context context, NotificationPayload notification) {
        if (notification == null) {
            Log.w(TAG, "Cannot show notification: payload is null");
            return;
        }
        
        Log.d(TAG, "📬 Showing in-app notification: " + notification.getTitle());
        
        // Get current activity
        Activity activity = ActivityTracker.getCurrentActivity();
        
        if (activity == null) {
            Log.w(TAG, "⚠️ No current activity, cannot show Snackbar");
            // Fallback: Could show a system notification here if needed
            return;
        }
        
        // Find root view for Snackbar
        View rootView = activity.findViewById(android.R.id.content);
        if (rootView == null) {
            Log.w(TAG, "⚠️ No root view found");
            return;
        }
        
        // Create Snackbar message with icon
        String icon = getNotificationIcon(notification.getType());
        String message = icon + " " + notification.getTitle();
        
        // Create and show Snackbar
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
        
        // Customize appearance
        snackbar.setBackgroundTint(ContextCompat.getColor(context, R.color.notification_snackbar_bg));
        snackbar.setTextColor(ContextCompat.getColor(context, R.color.white));
        snackbar.setActionTextColor(ContextCompat.getColor(context, R.color.notification_action_color));
        
        // Add action button "XEM"
        snackbar.setAction("XEM", v -> {
            Log.d(TAG, "📱 User clicked notification action");
            
            // Navigate to detail screen
            navigateToDeepLink(activity, notification);
            
            // Mark as read on backend
            markAsRead(activity, notification.getId());
        });
        
        // Show the Snackbar
        snackbar.show();
        
        Log.d(TAG, "✅ Snackbar displayed successfully");
    }
    
    /**
     * Get emoji icon for notification type
     * 
     * @param type Notification type (TASK_ASSIGNED, MEETING_REMINDER, etc.)
     * @return Emoji string
     */
    private static String getNotificationIcon(String type) {
        if (type == null) {
            return "🔔"; // Default bell
        }
        
        switch (type) {
            case "TASK_ASSIGNED":
                return "📝"; // Memo
            
            case "TASK_UPDATED":
                return "✏️"; // Pencil
            
            case "TASK_COMPLETED":
                return "✅"; // Check mark
            
            case "TASK_MOVED":
                return "🔄"; // Arrows
            
            case "MEETING_REMINDER":
                return "📅"; // Calendar
            
            case "EVENT_INVITE":
                return "🎉"; // Party popper
            
            case "EVENT_UPDATED":
                return "📆"; // Calendar with pen
            
            case "TIME_REMINDER":
                return "⏰"; // Alarm clock
            
            case "COMMENT_ADDED":
                return "💬"; // Speech bubble
            
            case "MENTION":
                return "👤"; // User
            
            case "PROJECT_INVITE":
                return "🤝"; // Handshake
            
            case "SYSTEM":
                return "ℹ️"; // Information
            
            default:
                return "🔔"; // Default bell
        }
    }
    
    /**
     * Navigate to appropriate screen based on notification type and data
     * 
     * @param activity Current activity
     * @param notification Notification payload
     */
    private static void navigateToDeepLink(Activity activity, NotificationPayload notification) {
        if (activity == null || notification == null) {
            Log.w(TAG, "Cannot navigate: activity or notification is null");
            return;
        }
        
        try {
            // Delegate to DeepLinkNavigator
            DeepLinkNavigator.navigate(activity, notification);
            
            Log.d(TAG, "✅ Navigation initiated for type: " + notification.getType());
        } catch (Exception e) {
            Log.e(TAG, "❌ Error navigating to deep link", e);
        }
    }
    
    /**
     * Mark notification as read on backend
     * 
     * @param context Context
     * @param notificationId Notification ID
     */
    private static void markAsRead(Context context, String notificationId) {
        if (notificationId == null || notificationId.isEmpty()) {
            Log.w(TAG, "Cannot mark as read: notification ID is null or empty");
            return;
        }
        
        try {
            // Get WebSocket manager from App
            App app = (App) context.getApplicationContext();
            
            if (app.getWebSocketManager() != null) {
                app.getWebSocketManager().markAsRead(notificationId);
                Log.d(TAG, "✅ Marked as read: " + notificationId);
            } else {
                Log.w(TAG, "⚠️ WebSocket manager is null, cannot mark as read");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error marking as read", e);
        }
    }
}
