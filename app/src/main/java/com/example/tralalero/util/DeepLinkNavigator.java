package com.example.tralalero.util;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.tralalero.data.remote.dto.websocket.NotificationPayload;
import com.example.tralalero.feature.home.ui.Home.ProjectActivity;
import com.example.tralalero.feature.home.ui.Home.project.CardDetailActivity;
import com.example.tralalero.feature.home.ui.MainContainerActivity;

import java.util.Map;

/**
 * Deep Link Navigator
 * Routes notifications to appropriate screens based on notification type
 * 
 * DEV 2: Navigation Layer
 */
public class DeepLinkNavigator {
    
    private static final String TAG = "DeepLinkNavigator";
    
    /**
     * Navigate to appropriate screen based on notification
     * 
     * @param context Context (usually Activity)
     * @param notification Notification payload
     */
    public static void navigate(Context context, NotificationPayload notification) {
        if (context == null || notification == null) {
            Log.w(TAG, "Cannot navigate: context or notification is null");
            return;
        }
        
        String type = notification.getType();
        Map<String, String> data = notification.getData();
        
        if (type == null) {
            Log.w(TAG, "Notification type is null, opening Inbox");
            openInbox(context);
            return;
        }
        
        Log.d(TAG, "🗺️ Navigating for notification type: " + type);
        
        try {
            switch (type) {
                case "TASK_ASSIGNED":
                case "TASK_UPDATED":
                case "TASK_COMPLETED":
                case "TASK_MOVED":
                case "COMMENT_ADDED":
                    // Navigate to Task Detail
                    navigateToTask(context, data, notification);
                    break;
                
                case "MEETING_REMINDER":
                case "EVENT_INVITE":
                case "EVENT_UPDATED":
                case "TIME_REMINDER":
                    // Navigate to Event Detail (future implementation)
                    navigateToEvent(context, data, notification);
                    break;
                
                case "PROJECT_INVITE":
                    // Navigate to Project
                    navigateToProject(context, data);
                    break;
                
                case "MENTION":
                    // Navigate based on mentionType
                    navigateToMention(context, data, notification);
                    break;
                
                case "SYSTEM":
                default:
                    // Open Notification Center (Inbox)
                    openInbox(context);
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error during navigation", e);
            // Fallback to Inbox
            openInbox(context);
        }
    }
    
    /**
     * Navigate to Task Detail screen
     * 
     * @param context Context
     * @param data Notification data (contains taskId, boardId, projectId)
     * @param notification Full notification payload
     */
    private static void navigateToTask(Context context, Map<String, String> data, NotificationPayload notification) {
        if (data == null) {
            Log.w(TAG, "Task data is null");
            openInbox(context);
            return;
        }
        
        String taskId = data.get("taskId");
        String boardId = data.get("boardId");
        String projectId = data.get("projectId");
        
        if (taskId == null || boardId == null || projectId == null) {
            Log.w(TAG, "Missing task navigation data: taskId=" + taskId + ", boardId=" + boardId + ", projectId=" + projectId);
            openInbox(context);
            return;
        }
        
        Log.d(TAG, "📝 Opening task: " + taskId);
        
        Intent intent = new Intent(context, CardDetailActivity.class);
        intent.putExtra(CardDetailActivity.EXTRA_TASK_ID, taskId);
        intent.putExtra(CardDetailActivity.EXTRA_BOARD_ID, boardId);
        intent.putExtra(CardDetailActivity.EXTRA_PROJECT_ID, projectId);
        intent.putExtra(CardDetailActivity.EXTRA_IS_EDIT_MODE, true);
        
        // Optional: Add notification info for context
        intent.putExtra("FROM_NOTIFICATION", true);
        intent.putExtra("NOTIFICATION_ID", notification.getId());
        
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
        
        Log.d(TAG, "✅ Launched CardDetailActivity");
    }
    
    /**
     * Navigate to Event Detail screen
     * TODO: Implement when EventDetailActivity is available
     * 
     * @param context Context
     * @param data Notification data (contains eventId)
     * @param notification Full notification payload
     */
    private static void navigateToEvent(Context context, Map<String, String> data, NotificationPayload notification) {
        if (data == null) {
            Log.w(TAG, "Event data is null");
            openInbox(context);
            return;
        }
        
        String eventId = data.get("eventId");
        String projectId = data.get("projectId");
        
        if (eventId == null) {
            Log.w(TAG, "Missing eventId");
            openInbox(context);
            return;
        }
        
        Log.d(TAG, "📅 Opening event: " + eventId);
        
        // If we have projectId, open ProjectActivity with Calendar tab
        if (projectId != null) {
            Intent intent = new Intent(context, ProjectActivity.class);
            intent.putExtra("PROJECT_ID", projectId);
            intent.putExtra("EVENT_ID", eventId);
            intent.putExtra("OPEN_CALENDAR_TAB", true); // Signal to open calendar tab
            intent.putExtra("FROM_NOTIFICATION", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
            Log.d(TAG, "✅ Launched ProjectActivity with calendar tab");
        } else {
            // No projectId, fallback to Inbox
            Log.w(TAG, "Missing projectId, opening Inbox");
            openInbox(context);
        }
        
        /*
        // Future implementation:
        Intent intent = new Intent(context, EventDetailActivity.class);
        intent.putExtra("EVENT_ID", eventId);
        intent.putExtra("FROM_NOTIFICATION", true);
        intent.putExtra("NOTIFICATION_ID", notification.getId());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
        */
    }
    
    /**
     * Navigate to Project screen
     * 
     * @param context Context
     * @param data Notification data (contains projectId)
     */
    private static void navigateToProject(Context context, Map<String, String> data) {
        if (data == null) {
            Log.w(TAG, "Project data is null");
            openInbox(context);
            return;
        }
        
        String projectId = data.get("projectId");
        
        if (projectId == null) {
            Log.w(TAG, "Missing projectId");
            openInbox(context);
            return;
        }
        
        Log.d(TAG, "📁 Opening project: " + projectId);
        
        Intent intent = new Intent(context, ProjectActivity.class);
        intent.putExtra("PROJECT_ID", projectId);
        intent.putExtra("FROM_NOTIFICATION", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
        
        Log.d(TAG, "✅ Launched ProjectActivity");
    }
    
    /**
     * Navigate to mentioned location (task comment, event, etc.)
     * 
     * @param context Context
     * @param data Notification data
     * @param notification Full notification payload
     */
    private static void navigateToMention(Context context, Map<String, String> data, NotificationPayload notification) {
        if (data == null) {
            Log.w(TAG, "Mention data is null");
            openInbox(context);
            return;
        }
        
        String mentionType = data.get("mentionType");
        
        if (mentionType == null) {
            Log.w(TAG, "Missing mentionType");
            openInbox(context);
            return;
        }
        
        Log.d(TAG, "👤 Navigating mention of type: " + mentionType);
        
        // Route based on mention type
        switch (mentionType) {
            case "TASK_COMMENT":
                navigateToTask(context, data, notification);
                break;
            
            case "EVENT_COMMENT":
                navigateToEvent(context, data, notification);
                break;
            
            default:
                Log.w(TAG, "Unknown mention type: " + mentionType);
                openInbox(context);
                break;
        }
    }
    
    /**
     * Open Inbox (Notification Center)
     * Fallback screen for unknown or system notifications
     * Opens MainContainerActivity at Inbox tab (page 1)
     * 
     * @param context Context
     */
    private static void openInbox(Context context) {
        Log.d(TAG, "📫 Opening Inbox");
        
        Intent intent = new Intent(context, MainContainerActivity.class);
        intent.putExtra("INITIAL_PAGE", 1); // Open at Inbox tab
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
        
        Log.d(TAG, "✅ Launched MainContainerActivity (Inbox tab)");
    }
}
