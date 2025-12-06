package com.example.tralalero.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.tralalero.R;
import com.example.tralalero.data.remote.dto.websocket.NotificationPayload;
import com.example.tralalero.MainActivity;

import java.util.Map;

/**
 * NotificationHelper - Xử lý hiển thị notifications
 * 
 * Hỗ trợ cả WebSocket (foreground) và FCM (background):
 * - WebSocket: In-app notification (Toast/Snackbar + update UI)
 * - FCM: System notification (status bar)
 * 
 * Usage:
 *   NotificationHelper.showInAppNotification(context, notification);
 *   NotificationHelper.showSystemNotification(context, notification);
 */
public class NotificationHelper {
    private static final String TAG = "NotificationHelper";
    
    // Notification channels
    private static final String CHANNEL_TASKS = "task_updates";
    private static final String CHANNEL_PROJECTS = "project_invites";
    private static final String CHANNEL_EVENTS = "event_notifications";
    private static final String CHANNEL_REMINDERS = "task_reminders";
    private static final String CHANNEL_DEFAULT = "plantracker_notifications";
    
    /**
     * Show in-app notification (for WebSocket - app foreground)
     * Display as Toast or Snackbar
     */
    public static void showInAppNotification(Context context, NotificationPayload notification) {
        if (notification == null) {
            Log.w(TAG, "Cannot show null notification");
            return;
        }
        
        Log.d(TAG, "📬 Showing in-app notification:");
        Log.d(TAG, "   Type: " + notification.getType());
        Log.d(TAG, "   Title: " + notification.getTitle());
        Log.d(TAG, "   Body: " + notification.getBody());
        
        // Show Toast notification
        String message = notification.getTitle() + ": " + notification.getBody();
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        
        // TODO: Optionally show Snackbar in MainActivity if available
        // TODO: Update notification badge count
        // TODO: Play notification sound (optional)
    }
    
    /**
     * Show system notification (for FCM - app background)
     * Display in status bar
     */
    public static void showSystemNotification(Context context, NotificationPayload notification) {
        if (notification == null) {
            Log.w(TAG, "Cannot show null notification");
            return;
        }
        
        Log.d(TAG, "📱 Showing system notification:");
        Log.d(TAG, "   Type: " + notification.getType());
        Log.d(TAG, "   Title: " + notification.getTitle());
        
        // Create notification channel (Android 8.0+)
        String channelId = getChannelIdForType(notification.getType());
        createNotificationChannel(context, channelId);
        
        // Build notification intent
        Intent intent = buildIntentForNotification(context, notification);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notification.getId().hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification) // TODO: Add icon
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getBody())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        
        // Add sound
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(defaultSoundUri);
        
        // Show notification
        NotificationManager notificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (notificationManager != null) {
            notificationManager.notify(notification.getId().hashCode(), builder.build());
            Log.d(TAG, "✅ System notification displayed");
        }
    }
    
    /**
     * Get notification channel ID based on notification type
     */
    private static String getChannelIdForType(String type) {
        if (type == null) return CHANNEL_DEFAULT;
        
        switch (type) {
            case "TASK_ASSIGNED":
            case "TASK_COMMENT":
            case "TASK_MOVED":
                return CHANNEL_TASKS;
                
            case "PROJECT_INVITE":
                return CHANNEL_PROJECTS;
                
            case "EVENT_INVITE":
            case "EVENT_UPDATED":
            case "EVENT_REMINDER":
                return CHANNEL_EVENTS;
                
            case "TASK_REMINDER":
                return CHANNEL_REMINDERS;
                
            default:
                return CHANNEL_DEFAULT;
        }
    }
    
    /**
     * Create notification channel (Android 8.0+)
     */
    private static void createNotificationChannel(Context context, String channelId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = 
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            
            if (notificationManager == null) return;
            
            // Check if channel already exists
            if (notificationManager.getNotificationChannel(channelId) != null) {
                return;
            }
            
            // Create channel
            String name;
            String description;
            int importance = NotificationManager.IMPORTANCE_HIGH;
            
            switch (channelId) {
                case CHANNEL_TASKS:
                    name = "Task Updates";
                    description = "Notifications về task assignments, comments, và updates";
                    break;
                    
                case CHANNEL_PROJECTS:
                    name = "Project Invites";
                    description = "Lời mời tham gia project";
                    break;
                    
                case CHANNEL_EVENTS:
                    name = "Events";
                    description = "Lời mời sự kiện và event updates";
                    break;
                    
                case CHANNEL_REMINDERS:
                    name = "Reminders";
                    description = "Task và event reminders";
                    importance = NotificationManager.IMPORTANCE_DEFAULT;
                    break;
                    
                default:
                    name = "PlanTracker Notifications";
                    description = "General notifications";
            }
            
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);
            channel.setShowBadge(true);
            
            notificationManager.createNotificationChannel(channel);
            Log.d(TAG, "Created notification channel: " + channelId);
        }
    }
    
    /**
     * Build intent for notification click
     */
    private static Intent buildIntentForNotification(Context context, NotificationPayload notification) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        // Add notification data to intent
        intent.putExtra("notification_id", notification.getId());
        intent.putExtra("notification_type", notification.getType());
        
        // Add deeplink data
        Map<String, String> data = notification.getData();
        if (data != null) {
            // Task notifications
            if (data.containsKey("taskId")) {
                intent.putExtra("taskId", data.get("taskId"));
                intent.putExtra("deeplink", "task_detail");
            }
            // Event notifications
            else if (data.containsKey("eventId")) {
                intent.putExtra("eventId", data.get("eventId"));
                intent.putExtra("deeplink", "event_detail");
            }
            // Project notifications
            else if (data.containsKey("projectId")) {
                intent.putExtra("projectId", data.get("projectId"));
                intent.putExtra("deeplink", "project_detail");
            }
        }
        
        return intent;
    }
    
    /**
     * Clear all notifications
     */
    public static void clearAllNotifications(Context context) {
        NotificationManager notificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (notificationManager != null) {
            notificationManager.cancelAll();
            Log.d(TAG, "All notifications cleared");
        }
    }
    
    /**
     * Clear specific notification
     */
    public static void clearNotification(Context context, String notificationId) {
        NotificationManager notificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (notificationManager != null) {
            notificationManager.cancel(notificationId.hashCode());
            Log.d(TAG, "Notification cleared: " + notificationId);
        }
    }
}
