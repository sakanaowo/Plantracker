package com.example.tralalero.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.tralalero.App.App;
import com.example.tralalero.BuildConfig;
import com.example.tralalero.R;
import com.example.tralalero.data.remote.api.FcmApiService;
import com.example.tralalero.data.remote.dto.fcm.DeviceResponse;
import com.example.tralalero.data.remote.dto.fcm.RegisterDeviceRequest;
import com.example.tralalero.feature.auth.ui.login.LoginActivity;
import com.example.tralalero.network.ApiClient;
import com.example.tralalero.util.FCMHelper;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "plantracker_notifications";
    private static final String CHANNEL_NAME = "PlanTracker Notifications";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    /**
     * Called when a new FCM token is generated
     * This happens when:
     * - App is first installed
     * - App data is cleared
     * - Token is refreshed by Firebase
     */
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "New FCM token: " + token);

        // Save token to SharedPreferences
        FCMHelper.saveTokenToPrefs(this, token);

        // TODO: Send token to your backend server
        sendTokenToServer(token);
    }

    /**
     * Called when a message is received
     * This method is called when:
     * - App is in foreground
     * - App is in background (only if notification has data payload)
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains data payload
        if (!remoteMessage.getData().isEmpty()) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            handleDataMessage(remoteMessage);
        }

        // Check if message contains notification payload
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            sendNotification(title, body);
        }
    }

    /**
     * Handle data messages
     * Data messages can contain custom key-value pairs
     */
    private void handleDataMessage(RemoteMessage remoteMessage) {
        // Extract data from the message
        String type = remoteMessage.getData().get("type");
        String title = remoteMessage.getData().get("title");
        String message = remoteMessage.getData().get("message");

        // Handle different notification types
        if (type != null) {
            switch (type) {
                case "task_assigned":
                case "task_updated":
                case "task_completed":
                case "comment_added":
                case "member_invited":
                case "workspace_update":
                    sendNotification(title, message);
                    break;
                default:
                    Log.d(TAG, "Unknown notification type: " + type);
                    break;
            }
        } else {
            // Default notification
            sendNotification(title, message);
        }
    }

    /**
     * Create and show a notification
     */
    private void sendNotification(String title, String messageBody) {
        // Intent to open app when notification is clicked
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        // Default notification sound
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Build notification
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title != null ? title : "PlanTracker")
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setVibrate(new long[]{0, 500, 200, 500});

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Show notification
        if (notificationManager != null) {
            notificationManager.notify(
                    (int) System.currentTimeMillis(), // Unique ID for each notification
                    notificationBuilder.build()
            );
        }
    }

    /**
     * Create notification channel for Android O and above
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for PlanTracker app");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 200, 500});

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Send token to your backend server
     */
    private void sendTokenToServer(String token) {
        Log.d(TAG, "Sending token to server: " + token);

        // Check if user is logged in
        if (App.authManager == null || !App.authManager.isSignedIn()) {
            Log.d(TAG, "User not logged in, skipping token registration");
            return;
        }

        try {
            // Get device info
            String deviceModel = Build.MANUFACTURER + " " + Build.MODEL;
            String appVersion = BuildConfig.VERSION_NAME;
            String locale = Locale.getDefault().toString();
            String timezone = TimeZone.getDefault().getID();

            // Create request
            RegisterDeviceRequest request = new RegisterDeviceRequest(
                    token,
                    "ANDROID",
                    deviceModel,
                    appVersion,
                    locale,
                    timezone
            );

            // Create API service
            FcmApiService apiService = ApiClient.get(App.authManager).create(FcmApiService.class);

            // Call API
            apiService.registerDevice(request).enqueue(new Callback<DeviceResponse>() {
                @Override
                public void onResponse(@NonNull Call<DeviceResponse> call, @NonNull Response<DeviceResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        DeviceResponse device = response.body();
                        Log.d(TAG, "Token registered successfully. Device ID: " + device.getId());

                        // Save device ID to SharedPreferences for future updates
                        SharedPreferences prefs = getSharedPreferences("fcm_prefs", MODE_PRIVATE);
                        prefs.edit().putString("device_id", device.getId()).apply();
                    } else {
                        Log.e(TAG, "Failed to register token. Response code: " + response.code());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<DeviceResponse> call, @NonNull Throwable t) {
                    Log.e(TAG, "Failed to send token to server", t);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error sending token to server", e);
        }
    }

    /**
     * Static method to register FCM token from anywhere in the app
     * @param context Application context
     * @param token FCM token to register
     */
    public static void registerTokenWithBackend(Context context, String token) {
        Log.d("FCMService", "registerTokenWithBackend called with token: " + token);
        
        // Check if user is logged in
        if (App.authManager == null || !App.authManager.isSignedIn()) {
            Log.d("FCMService", "User not logged in, skipping token registration");
            return;
        }

        try {
            // Get device info
            String deviceModel = Build.MANUFACTURER + " " + Build.MODEL;
            String appVersion = BuildConfig.VERSION_NAME;
            String locale = Locale.getDefault().toString();
            String timezone = TimeZone.getDefault().getID();

            // Create request
            RegisterDeviceRequest request = new RegisterDeviceRequest(
                    token,
                    "ANDROID",
                    deviceModel,
                    appVersion,
                    locale,
                    timezone
            );

            // Create API service
            FcmApiService apiService = ApiClient.get(App.authManager).create(FcmApiService.class);

            // Call API
            apiService.registerDevice(request).enqueue(new Callback<DeviceResponse>() {
                @Override
                public void onResponse(@NonNull Call<DeviceResponse> call, @NonNull Response<DeviceResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        DeviceResponse device = response.body();
                        Log.d("FCMService", "Token registered successfully. Device ID: " + device.getId());

                        // Save device ID to SharedPreferences
                        SharedPreferences prefs = context.getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE);
                        prefs.edit().putString("device_id", device.getId()).apply();
                    } else {
                        Log.e("FCMService", "Failed to register token. Response code: " + response.code());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<DeviceResponse> call, @NonNull Throwable t) {
                    Log.e("FCMService", "Failed to send token to server", t);
                }
            });
        } catch (Exception e) {
            Log.e("FCMService", "Error sending token to server", e);
        }
    }
}
