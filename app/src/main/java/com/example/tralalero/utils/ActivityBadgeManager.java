package com.example.tralalero.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.util.Log;

import com.example.tralalero.R;

/**
 * Manager for activity notification badge and sound
 */
public class ActivityBadgeManager {
    private static final String TAG = "ActivityBadgeManager";
    private static final String PREFS_NAME = "activity_badge_prefs";
    private static final String KEY_LAST_READ_TIMESTAMP = "last_read_timestamp";
    private static final String KEY_UNREAD_COUNT = "unread_count";
    
    private final SharedPreferences prefs;
    private final Context context;
    private MediaPlayer mediaPlayer;
    
    public ActivityBadgeManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * Get the timestamp when user last viewed activity feed
     */
    public long getLastReadTimestamp() {
        return prefs.getLong(KEY_LAST_READ_TIMESTAMP, 0);
    }
    
    /**
     * Mark all activities as read (called when user opens activity tab)
     */
    public void markAllAsRead() {
        prefs.edit()
            .putLong(KEY_LAST_READ_TIMESTAMP, System.currentTimeMillis())
            .putInt(KEY_UNREAD_COUNT, 0)
            .apply();
        Log.d(TAG, "All activities marked as read");
    }
    
    /**
     * Get unread count
     */
    public int getUnreadCount() {
        return prefs.getInt(KEY_UNREAD_COUNT, 0);
    }
    
    /**
     * Increment unread count when new activity arrives
     */
    public void incrementUnreadCount() {
        int current = getUnreadCount();
        prefs.edit()
            .putInt(KEY_UNREAD_COUNT, current + 1)
            .apply();
        Log.d(TAG, "Unread count: " + (current + 1));
    }
    
    /**
     * Play notification sound for new activity
     * TODO: Add notification_sound.mp3 to res/raw/ to enable sound
     */
    public void playNotificationSound() {
        // Temporarily disabled until sound file is added
        Log.d(TAG, "🔇 Notification sound disabled (add notification_sound.mp3 to res/raw/ to enable)");
        
        /* Uncomment when notification_sound.mp3 is added:
        try {
            // Release previous player if exists
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            
            // Create and play notification sound
            mediaPlayer = MediaPlayer.create(context, R.raw.notification_sound);
            if (mediaPlayer != null) {
                mediaPlayer.setOnCompletionListener(mp -> {
                    mp.release();
                    mediaPlayer = null;
                });
                mediaPlayer.start();
                Log.d(TAG, "Notification sound played");
            } else {
                Log.w(TAG, "Failed to create MediaPlayer");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error playing notification sound", e);
        }
        */
    }
    
    /**
     * Release resources
     */
    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
