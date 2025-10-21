# Hướng Dẫn: FCM Đã Tạm Thời Disable

## Tổng Quan
Firebase Cloud Messaging (FCM) đã được tạm thời disable trong dự án để tránh các vấn đề trong quá trình development/testing.

## Các File Đã Bị Disable

### 1. **MyFirebaseMessagingService.java**
- **Đường dẫn**: `app/src/main/java/com/example/tralalero/service/MyFirebaseMessagingService.java`
- **Trạng thái**: Đã chuyển thành stub class rỗng
- **Chức năng gốc**: Xử lý nhận và hiển thị push notifications từ FCM

### 2. **FCMHelper.java**
- **Đường dẫn**: `app/src/main/java/com/example/tralalero/util/FCMHelper.java`
- **Trạng thái**: Đã chuyển thành stub class rỗng
- **Chức năng gốc**: Quản lý FCM tokens và topic subscriptions

### 3. **AndroidManifest.xml**
- **Các phần đã comment**:
  - FCM Permissions (`POST_NOTIFICATIONS`, `VIBRATE`)
  - FCM Service declaration
  - FCM metadata (icon, color, auto-init settings)

### 4. **build.gradle.kts**
- **Dependency đã comment**: 
  ```kotlin
  // implementation("com.google.firebase:firebase-messaging")
  ```

## Cách Kích Hoạt Lại FCM

### Bước 1: Khôi Phục Dependency
Mở `app/build.gradle.kts` và uncomment dòng:
```kotlin
implementation("com.google.firebase:firebase-messaging")
```

### Bước 2: Khôi Phục AndroidManifest.xml
Mở `app/src/main/AndroidManifest.xml` và uncomment các phần:
```xml
<!-- FCM Permissions -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.VIBRATE" />

<!-- FCM Service -->
<service
    android:name=".service.MyFirebaseMessagingService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>

<!-- FCM Metadata -->
<meta-data
    android:name="com.google.firebase.messaging.default_notification_icon"
    android:resource="@drawable/ic_notification" />
<meta-data
    android:name="com.google.firebase.messaging.default_notification_color"
    android:resource="@color/primary" />
<meta-data
    android:name="firebase_messaging_auto_init_enabled"
    android:value="true" />
<meta-data
    android:name="firebase_analytics_collection_enabled"
    android:value="true" />
```

### Bước 3: Khôi Phục MyFirebaseMessagingService.java
Bạn cần tạo lại implementation đầy đủ của service. File gốc xử lý:
- `onNewToken()`: Nhận FCM token mới và gửi lên server
- `onMessageReceived()`: Nhận và xử lý push notifications
- `sendNotification()`: Hiển thị notification cho user

### Bước 4: Khôi Phục FCMHelper.java
Khôi phục các helper methods:
- `getFCMToken()`: Lấy FCM token hiện tại
- `saveTokenToPrefs()`: Lưu token vào SharedPreferences
- `getSavedToken()`: Lấy token đã lưu
- `clearToken()`: Xóa token (khi logout)
- `subscribeToTopic()`: Subscribe vào topic
- `unsubscribeFromTopic()`: Unsubscribe khỏi topic

### Bước 5: Tạo Notification Icon
Tạo file `ic_notification.xml` trong `res/drawable/`:
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M12,22c1.1,0 2,-0.9 2,-2h-4c0,1.1 0.89,2 2,2zM18,16v-5c0,-3.07 -1.64,-5.64 -4.5,-6.32V4c0,-0.83 -0.67,-1.5 -1.5,-1.5s-1.5,0.67 -1.5,1.5v0.68C7.63,5.36 6,7.92 6,11v5l-2,2v1h16v-1l-2,-2z"/>
</vector>
```

### Bước 6: Sync Gradle
Chạy Gradle sync:
```
./gradlew --refresh-dependencies
```

### Bước 7: Clean & Rebuild
```
./gradlew clean
./gradlew build
```

## Lưu Ý Quan Trọng

### 1. Google Services JSON
Đảm bảo file `google-services.json` có cấu hình đúng cho FCM

### 2. Backend Integration
Sau khi enable, bạn cần implement logic gửi FCM token lên backend:
```java
// Trong onNewToken() hoặc khi user login
String token = FirebaseMessaging.getInstance().getToken();
// Gửi token lên backend API
```

### 3. Notification Permissions (Android 13+)
Với Android 13+, cần request runtime permission:
```java
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
        != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(this,
            new String[]{Manifest.permission.POST_NOTIFICATIONS}, 
            REQUEST_CODE);
    }
}
```

### 4. Testing FCM
- **Local Testing**: Sử dụng Firebase Console > Cloud Messaging để gửi test notification
- **Topic Testing**: Subscribe device vào topic và test
- **Backend Testing**: Verify backend có thể gửi notification đến device

## Backup Code Gốc

Các file implementation đầy đủ đã được backup tại commit trước khi disable.
Sử dụng Git để xem lại code gốc nếu cần:
```bash
git log --all --full-history -- "*MyFirebaseMessagingService.java"
git log --all --full-history -- "*FCMHelper.java"
```

## Tài Liệu Tham Khảo
- [FCM Setup Guide](FCM_SETUP_GUIDE.md)
- [Firebase Cloud Messaging Docs](https://firebase.google.com/docs/cloud-messaging)
- [Android Notification Guide](https://developer.android.com/develop/ui/views/notifications)

---
**Ngày disable**: 21/10/2025
**Lý do**: Tạm thời disable để testing/development
**Người thực hiện**: GitHub Copilot

