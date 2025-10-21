package com.example.tralalero.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.tralalero.R;
import com.example.tralalero.feature.home.ui.Home.HomeActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Service để xử lý Firebase Cloud Messaging
 * Nhận và hiển thị thông báo từ FCM
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "plantracker_notifications";
    private static final String CHANNEL_NAME = "PlanTracker Notifications";
    private static final int NOTIFICATION_ID = 1001;

    /**
     * Được gọi khi có token FCM mới
     * Token này cần được gửi lên backend để có thể gửi push notification
     */
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "New FCM Token: " + token);

        // TODO: Gửi token này lên backend server
        // Ví dụ: sendTokenToServer(token);
        sendRegistrationToServer(token);
    }

    /**
     * Được gọi khi nhận được message từ FCM
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Kiểm tra nếu message có data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            handleDataMessage(remoteMessage);
        }

        // Kiểm tra nếu message có notification payload
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            sendNotification(title, body);
        }
    }

    /**
     * Xử lý data message từ FCM
     */
    private void handleDataMessage(RemoteMessage remoteMessage) {
        // Lấy dữ liệu từ message
        String type = remoteMessage.getData().get("type");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        // Xử lý theo loại thông báo
        if (type != null) {
            switch (type) {
                case "task_assigned":
                    // Xử lý khi có task mới được gán
                    Log.d(TAG, "New task assigned");
                    break;
                case "task_updated":
                    // Xử lý khi task được cập nhật
                    Log.d(TAG, "Task updated");
                    break;
                case "comment_added":
                    // Xử lý khi có comment mới
                    Log.d(TAG, "New comment added");
                    break;
                default:
                    Log.d(TAG, "Unknown notification type: " + type);
            }
        }

        // Hiển thị notification
        if (title != null && body != null) {
            sendNotification(title, body);
        }
    }

    /**
     * Tạo và hiển thị notification
     */
    private void sendNotification(String title, String messageBody) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder =
            new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // TODO: Tạo icon này
                .setContentTitle(title != null ? title : "PlanTracker")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Tạo notification channel cho Android O trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for PlanTracker app");
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    /**
     * Gửi token lên server
     * TODO: Implement việc gửi token lên backend
     */
    private void sendRegistrationToServer(String token) {
        // TODO: Implement gửi token lên backend API
        // Ví dụ:
        // ApiService.updateFcmToken(token);

        Log.d(TAG, "Token to send to server: " + token);

        // Lưu token vào SharedPreferences tạm thời
        getSharedPreferences("fcm_prefs", MODE_PRIVATE)
            .edit()
            .putString("fcm_token", token)
            .apply();
    }
}

