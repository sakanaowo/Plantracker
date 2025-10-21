//package com.example.tralalero.util;
//
//import android.Manifest;
//import android.app.Activity;
//import android.content.pm.PackageManager;
//import android.os.Build;
//import android.util.Log;
//
//import androidx.annotation.NonNull;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//
///**
// * Helper class để request notification permission cho Android 13+
// */
//public class NotificationPermissionHelper {
//
//    private static final String TAG = "NotificationPermission";
//    public static final int REQUEST_CODE_NOTIFICATION_PERMISSION = 1001;
//
//    /**
//     * Kiểm tra xem đã có notification permission chưa
//     */
//    public static boolean hasNotificationPermission(Activity activity) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            return ContextCompat.checkSelfPermission(
//                activity,
//                Manifest.permission.POST_NOTIFICATIONS
//            ) == PackageManager.PERMISSION_GRANTED;
//        }
//        // Android 12 trở xuống không cần runtime permission
//        return true;
//    }
//
//    /**
//     * Request notification permission
//     */
//    public static void requestNotificationPermission(Activity activity) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            if (!hasNotificationPermission(activity)) {
//                Log.d(TAG, "Requesting notification permission");
//                ActivityCompat.requestPermissions(
//                    activity,
//                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
//                    REQUEST_CODE_NOTIFICATION_PERMISSION
//                );
//            } else {
//                Log.d(TAG, "Notification permission already granted");
//            }
//        }
//    }
//
//    /**
//     * Xử lý kết quả permission request
//     * Gọi method này trong onRequestPermissionsResult của Activity
//     */
//    public static boolean handlePermissionResult(
//        int requestCode,
//        @NonNull int[] grantResults,
//        PermissionCallback callback
//    ) {
//        if (requestCode == REQUEST_CODE_NOTIFICATION_PERMISSION) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Log.d(TAG, "Notification permission granted");
//                if (callback != null) {
//                    callback.onPermissionGranted();
//                }
//                return true;
//            } else {
//                Log.d(TAG, "Notification permission denied");
//                if (callback != null) {
//                    callback.onPermissionDenied();
//                }
//                return false;
//            }
//        }
//        return false;
//    }
//
//    /**
//     * Callback interface cho permission result
//     */
//    public interface PermissionCallback {
//        void onPermissionGranted();
//        void onPermissionDenied();
//    }
//}
//
