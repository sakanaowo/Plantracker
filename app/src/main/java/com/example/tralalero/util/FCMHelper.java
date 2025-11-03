package com.example.tralalero.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Helper class to manage FCM tokens and topics
 */
public class FCMHelper {

    private static final String TAG = "FCMHelper";
    private static final String PREF_NAME = "FCMPrefs";
    private static final String KEY_FCM_TOKEN = "fcm_token";

    /**
     * Get FCM token asynchronously
     */
    public static void getFCMToken(Context context, final FCMTokenCallback callback) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            if (callback != null) {
                                callback.onFailure(task.getException());
                            }
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        Log.d(TAG, "FCM Token: " + token);

                        // Save token to SharedPreferences
                        saveTokenToPrefs(context, token);

                        if (callback != null) {
                            callback.onSuccess(token);
                        }
                    }
                });
    }

    /**
     * Save FCM token to SharedPreferences
     */
    public static void saveTokenToPrefs(Context context, String token) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_FCM_TOKEN, token).apply();
        Log.d(TAG, "Token saved to preferences");
    }

    /**
     * Get saved FCM token from SharedPreferences
     */
    public static String getSavedToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_FCM_TOKEN, null);
    }

    /**
     * Clear FCM token (when user logs out)
     */
    public static void clearToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_FCM_TOKEN).apply();
        Log.d(TAG, "Token cleared from preferences");
    }

    /**
     * Subscribe to a topic
     */
    public static void subscribeToTopic(String topic, final TopicCallback callback) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Subscribed to topic: " + topic);
                            if (callback != null) {
                                callback.onSuccess(topic);
                            }
                        } else {
                            Log.e(TAG, "Failed to subscribe to topic: " + topic, task.getException());
                            if (callback != null) {
                                callback.onFailure(task.getException());
                            }
                        }
                    }
                });
    }

    /**
     * Unsubscribe from a topic
     */
    public static void unsubscribeFromTopic(String topic, final TopicCallback callback) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Unsubscribed from topic: " + topic);
                            if (callback != null) {
                                callback.onSuccess(topic);
                            }
                        } else {
                            Log.e(TAG, "Failed to unsubscribe from topic: " + topic, task.getException());
                            if (callback != null) {
                                callback.onFailure(task.getException());
                            }
                        }
                    }
                });
    }

    /**
     * Callback interface for FCM token operations
     */
    public interface FCMTokenCallback {
        void onSuccess(String token);
        void onFailure(Exception e);
    }

    /**
     * Callback interface for topic operations
     */
    public interface TopicCallback {
        void onSuccess(String topic);
        void onFailure(Exception e);
    }
}
