# Hướng Dẫn Cấu Hình Firebase Cloud Messaging (FCM)

## Tổng Quan
FCM (Firebase Cloud Messaging) đã được tích hợp vào dự án PlanTracker để gửi push notifications đến người dùng.

---

## 📋 Các Bước Đã Hoàn Thành

### 1. ✅ Thêm Dependencies
- Đã thêm `firebase-messaging` vào `app/build.gradle.kts`
- Firebase BOM version: 33.2.0

### 2. ✅ Tạo Firebase Messaging Service
- File: `MyFirebaseMessagingService.java`
- Xử lý nhận thông báo từ FCM
- Xử lý refresh FCM token
- Hiển thị notification cho người dùng

### 3. ✅ Cập Nhật AndroidManifest.xml
- Thêm permissions: `POST_NOTIFICATIONS`, `VIBRATE`
- Đăng ký service FCM
- Cấu hình default notification icon và color

### 4. ✅ Tạo FCM Helper Class
- File: `FCMHelper.java`
- Lấy FCM token
- Subscribe/Unsubscribe topics
- Quản lý token trong SharedPreferences

### 5. ✅ Tạo Notification Icon
- File: `ic_notification.xml`

---

## 🚀 Cách Sử Dụng FCM Trong Code

### 1. Lấy FCM Token (Trong Activity hoặc Fragment)

```java
import com.example.tralalero.util.FCMHelper;

// Trong onCreate() hoặc onResume()
FCMHelper.getFCMToken(this, new FCMHelper.FCMTokenCallback() {
    @Override
    public void onSuccess(String token) {
        Log.d("FCM", "Token: " + token);
        // TODO: Gửi token này lên backend server
        // sendTokenToBackend(token);
    }

    @Override
    public void onFailure(Exception e) {
        Log.e("FCM", "Failed to get token", e);
    }
});
```

### 2. Subscribe to Topic (Nhận thông báo theo chủ đề)

```java
// Subscribe vào topic "all_users"
FCMHelper.subscribeToTopic("all_users", new FCMHelper.TopicCallback() {
    @Override
    public void onSuccess(String topic) {
        Log.d("FCM", "Subscribed to: " + topic);
    }

    @Override
    public void onFailure(Exception e) {
        Log.e("FCM", "Subscribe failed", e);
    }
});

// Subscribe vào workspace-specific topic
String workspaceId = "workspace_123";
FCMHelper.subscribeToTopic("workspace_" + workspaceId, null);
```

### 3. Unsubscribe from Topic

```java
FCMHelper.unsubscribeFromTopic("all_users", new FCMHelper.TopicCallback() {
    @Override
    public void onSuccess(String topic) {
        Log.d("FCM", "Unsubscribed from: " + topic);
    }

    @Override
    public void onFailure(Exception e) {
        Log.e("FCM", "Unsubscribe failed", e);
    }
});
```

### 4. Clear Token (Khi Logout)

```java
// Trong logout logic
FCMHelper.clearToken(this);
```

---

## 🔧 Tích Hợp Với Backend

### 1. Gửi Token Lên Server

Tạo API endpoint trong backend để nhận FCM token:

**Backend API (Node.js/Express Example):**
```javascript
// POST /api/users/fcm-token
router.post('/fcm-token', authenticate, async (req, res) => {
    const { fcmToken } = req.body;
    const userId = req.user.id;
    
    // Lưu token vào database
    await User.updateOne(
        { _id: userId },
        { fcmToken: fcmToken }
    );
    
    res.json({ success: true });
});
```

**Android Code:**
```java
// Trong MyFirebaseMessagingService.java
private void sendRegistrationToServer(String token) {
    // Gọi API để gửi token
    ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
    
    UpdateTokenRequest request = new UpdateTokenRequest(token);
    apiService.updateFcmToken(request).enqueue(new Callback<ApiResponse>() {
        @Override
        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
            if (response.isSuccessful()) {
                Log.d(TAG, "Token sent to server successfully");
            }
        }

        @Override
        public void onFailure(Call<ApiResponse> call, Throwable t) {
            Log.e(TAG, "Failed to send token to server", t);
        }
    });
}
```

### 2. Gửi Notification Từ Backend

**Node.js Example với Firebase Admin SDK:**

```javascript
const admin = require('firebase-admin');

// Initialize Firebase Admin
const serviceAccount = require('./path/to/serviceAccountKey.json');
admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
});

// Gửi notification đến một device
async function sendNotificationToDevice(fcmToken, title, body, data) {
    const message = {
        notification: {
            title: title,
            body: body
        },
        data: data,
        token: fcmToken
    };

    try {
        const response = await admin.messaging().send(message);
        console.log('Successfully sent message:', response);
        return response;
    } catch (error) {
        console.log('Error sending message:', error);
        throw error;
    }
}

// Gửi notification đến một topic
async function sendNotificationToTopic(topic, title, body, data) {
    const message = {
        notification: {
            title: title,
            body: body
        },
        data: data,
        topic: topic
    };

    try {
        const response = await admin.messaging().send(message);
        console.log('Successfully sent message:', response);
        return response;
    } catch (error) {
        console.log('Error sending message:', error);
        throw error;
    }
}

// Ví dụ sử dụng:
sendNotificationToDevice(
    userFcmToken,
    'New Task Assigned',
    'You have been assigned to "Update Homepage"',
    {
        type: 'task_assigned',
        taskId: '123',
        workspaceId: '456'
    }
);
```

---

## 📱 Request Notification Permission (Android 13+)

Đối với Android 13 (API 33) trở lên, cần request permission từ người dùng:

```java
// Trong Activity (ví dụ: HomeActivity)
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

private static final int REQUEST_NOTIFICATION_PERMISSION = 1001;

private void requestNotificationPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.POST_NOTIFICATIONS},
                REQUEST_NOTIFICATION_PERMISSION
            );
        }
    }
}

@Override
public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("FCM", "Notification permission granted");
            // Lấy FCM token
            FCMHelper.getFCMToken(this, null);
        } else {
            Log.d("FCM", "Notification permission denied");
        }
    }
}

// Gọi trong onCreate()
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home);
    
    requestNotificationPermission();
}
```

---

## 🧪 Test FCM

### 1. Test Từ Firebase Console

1. Vào Firebase Console: https://console.firebase.google.com
2. Chọn project "Plantracker"
3. Vào **Engage** → **Cloud Messaging**
4. Click **Send your first message**
5. Nhập notification title và text
6. Click **Send test message**
7. Paste FCM token (lấy từ Logcat)
8. Click **Test**

### 2. Test Bằng cURL

```bash
# Lấy Server Key từ Firebase Console
# Project Settings → Cloud Messaging → Server key

curl -X POST https://fcm.googleapis.com/fcm/send \
  -H "Authorization: key=YOUR_SERVER_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "to": "FCM_TOKEN_HERE",
    "notification": {
      "title": "Test Notification",
      "body": "This is a test message"
    },
    "data": {
      "type": "test",
      "message": "Hello from FCM"
    }
  }'
```

### 3. Test Bằng Postman

**URL:** `https://fcm.googleapis.com/fcm/send`

**Headers:**
- `Authorization`: `key=YOUR_SERVER_KEY`
- `Content-Type`: `application/json`

**Body (JSON):**
```json
{
    "to": "FCM_TOKEN_HERE",
    "notification": {
        "title": "New Task",
        "body": "You have a new task assigned"
    },
    "data": {
        "type": "task_assigned",
        "taskId": "123",
        "taskTitle": "Update homepage"
    }
}
```

---

## 📝 Các Use Cases Cho PlanTracker

### 1. Task Assigned (Gán task mới)
```java
// Data payload
{
    "type": "task_assigned",
    "taskId": "123",
    "taskTitle": "Update Homepage",
    "workspaceId": "456",
    "assignedBy": "John Doe"
}
```

### 2. Task Updated (Cập nhật task)
```java
{
    "type": "task_updated",
    "taskId": "123",
    "updateType": "status_changed",
    "newStatus": "in_progress"
}
```

### 3. Comment Added (Có comment mới)
```java
{
    "type": "comment_added",
    "taskId": "123",
    "commentId": "789",
    "commentBy": "Jane Smith",
    "commentPreview": "I've completed the design..."
}
```

### 4. Deadline Reminder (Nhắc deadline)
```java
{
    "type": "deadline_reminder",
    "taskId": "123",
    "taskTitle": "Submit report",
    "deadlineIn": "2 hours"
}
```

---

## 🔒 Best Practices

1. **Luôn gửi token lên server** sau khi người dùng đăng nhập
2. **Xóa token khỏi server** khi người dùng logout
3. **Sử dụng topics** cho notifications nhóm (workspace, project)
4. **Kiểm tra permission** trước khi hiển thị notification (Android 13+)
5. **Handle notification click** để navigate đến đúng màn hình
6. **Lưu notification history** để người dùng có thể xem lại
7. **Rate limiting** trên backend để tránh spam notifications

---

## 📌 TODO - Cần Implement

- [ ] Tạo API endpoint `/api/users/fcm-token` trong backend
- [ ] Implement `sendTokenToServer()` trong `MyFirebaseMessagingService`
- [ ] Thêm notification permission request vào `HomeActivity`
- [ ] Tạo màn hình Notification History (trong InboxActivity)
- [ ] Handle notification click để navigate đến task detail
- [ ] Implement badge count cho unread notifications
- [ ] Tạo notification preferences trong Settings
- [ ] Test FCM với backend server thực tế

---

## 🐛 Troubleshooting

### Token không được generate
- Kiểm tra `google-services.json` đã được thêm chính xác
- Kiểm tra package name trong Firebase Console khớp với `applicationId`
- Build lại project: `./gradlew clean build`

### Không nhận được notification
- Kiểm tra device có internet
- Kiểm tra notification permission đã được cấp
- Kiểm tra app không bị force stop
- Xem Logcat có lỗi gì không

### Notification không hiển thị khi app đang mở
- Foreground notifications cần được handle thủ công trong `onMessageReceived()`

---

## 📚 Tài Liệu Tham Khảo

- [Firebase Cloud Messaging Documentation](https://firebase.google.com/docs/cloud-messaging)
- [Android Notifications Guide](https://developer.android.com/develop/ui/views/notifications)
- [FCM HTTP Protocol](https://firebase.google.com/docs/cloud-messaging/http-server-ref)

