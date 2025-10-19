# Firebase Token Management System - Documentation

## Tổng quan

Hệ thống quản lý Firebase token tự động được thiết kế để giải quyết vấn đề token Firebase hết hạn sau 30 phút và lỗi 404/401. Hệ thống tự động refresh token và retry requests khi cần thiết.

## Kiến trúc hệ thống

### 1. **AuthManager** - Quản lý Firebase Token
- **File**: `AuthManager.java`
- **Chức năng**: 
  - Quản lý cache Firebase ID token
  - Tự động refresh token khi sắp hết hạn (5 phút trước)
  - Force refresh token khi cần thiết
  - Tracking thời gian hết hạn token

#### Các method chính:
```java
// Lấy token tự động refresh nếu cần
String getIdTokenBlocking() throws Exception

// Force refresh token (dùng cho authenticator)
String forceRefreshToken() throws Exception

// Kiểm tra trạng thái đăng nhập
boolean isSignedIn()

// Đăng xuất và clear cache
void signOut()
```

### 2. **FirebaseInterceptor** - Tự động thêm token vào requests
- **File**: `FirebaseInterceptor.java`
- **Chức năng**:
  - Tự động thêm Firebase ID token vào Authorization header
  - Bỏ qua các public endpoints
  - Sử dụng cached token hoặc refresh khi cần

#### Public endpoints được bỏ qua:
- `/auth/public`
- `/health`
- `/users/firebase/auth` (Firebase auth endpoint)
- Bất kỳ path nào chứa `/public/`

### 3. **FirebaseAuthenticator** - Tự động refresh token khi lỗi
- **File**: `FirebaseAuthenticator.java`
- **Chức năng**:
  - Xử lý lỗi 401 và 404 (token hết hạn)
  - Tự động force refresh Firebase token
  - Retry request với token mới
  - Giới hạn số lần retry (tối đa 2 lần)

#### Logic retry:
1. Detect lỗi 401/404
2. Kiểm tra số lần retry (max 2)
3. Force refresh Firebase token
4. Retry request với token mới
5. Sign out nếu không thể refresh

### 4. **FirebaseAuthRepository** - Repository với dual API
- **File**: `FirebaseAuthRepository.java`
- **Chức năng**:
  - Sử dụng PublicAuthApi cho Firebase auth ban đầu (không có auth header)
  - Sử dụng AuthApi cho các authenticated requests
  - Xử lý callback authentication

#### Dual API pattern:
```java
// Cho Firebase auth (không có interceptor)
PublicAuthApi publicAuthApi = ApiClient.get().create(PublicAuthApi.class);

// Cho authenticated requests (có interceptor + authenticator)
AuthApi authApi = ApiClient.get(authManager).create(AuthApi.class);
```

### 5. **ApiClient** - HTTP Client configuration
- **File**: `ApiClient.java`
- **Chức năng**:
  - Cấu hình OkHttp client với FirebaseInterceptor và FirebaseAuthenticator
  - Singleton pattern cho authenticated và non-authenticated clients

## Flow hoạt động

### 1. **Google Sign-In Flow**
```
User clicks Google Sign-In 
    ↓
Google OAuth authentication
    ↓
Firebase authentication với Google credential
    ↓
Đợi Firebase tạo ID token (firebaseUser.getIdToken(true))
    ↓
Gửi Firebase ID token đến backend (/users/firebase/auth)
    ↓
Backend verify với Firebase Admin SDK
    ↓
Trả về user data và success
```

### 2. **Automatic Token Management Flow**
```
API Request
    ↓
FirebaseInterceptor checks token
    ↓
Token valid? → Add to header
Token expired? → Auto refresh → Add to header
    ↓
Send request
    ↓
Response 401/404? → FirebaseAuthenticator handles
    ↓
Force refresh token → Retry request
    ↓
Success hoặc sign out nếu thất bại
```

### 3. **Token Refresh Strategy**
- **Proactive refresh**: 5 phút trước khi hết hạn
- **Reactive refresh**: Khi gặp lỗi 401/404
- **Cache management**: Token được cache và reuse
- **Error handling**: Auto sign out nếu refresh thất bại

## Files đã thay đổi

### 1. **Mới tạo**:
- `FirebaseInterceptor.java` - Thay thế AuthInterceptor cũ
- `FirebaseAuthenticator.java` - Xử lý token refresh tự động
- `PublicAuthApi.java` - API interface cho public endpoints
- `FirebaseAuthExample.java` - Ví dụ sử dụng

### 2. **Đã cập nhật**:
- `AuthManager.java` - Thêm forceRefreshToken(), logging, cải thiện cache
- `ApiClient.java` - Sử dụng FirebaseInterceptor + FirebaseAuthenticator
- `AuthApi.java` - Sửa endpoint Firebase auth
- `FirebaseAuthRepository.java` - Dual API pattern
- `ContinueWithGoogle.java` - Sửa logic lấy Firebase token

### 3. **Cần xóa** (không còn sử dụng):
- `AuthInterceptor.java` - Đã thay thế bằng FirebaseInterceptor

## Cách sử dụng

### 1. **Khởi tạo trong Activity**:
```java
// Initialize AuthManager
AuthManager authManager = new AuthManager(getApplication());

// Tạo repository với automatic token management
FirebaseAuthRepository repository = new FirebaseAuthRepository(authManager);
```

### 2. **Thực hiện API calls**:
```java
// Tất cả các API calls sẽ tự động có token và auto-retry
repository.authenticateWithFirebase(firebaseIdToken, callback);

// Hoặc sử dụng authenticated API
AuthApi api = repository.getAuthenticatedApi();
api.getMe().enqueue(callback);
```

### 3. **Không cần làm gì thêm**:
- Token tự động được thêm vào requests
- Token tự động refresh khi cần
- Requests tự động retry khi token hết hạn
- User tự động sign out khi không thể refresh

## Benefits

1. **Giải quyết vấn đề token hết hạn**: Không còn lỗi 401/404 do token expires
2. **Transparent cho developer**: Không cần xử lý token manually
3. **Tự động retry**: Network calls tự động retry với token mới
4. **Performance tốt**: Token được cache và reuse
5. **Error handling tốt**: Auto cleanup khi authentication fails
6. **Separation of concerns**: Public và authenticated APIs tách biệt

## Monitoring và Debug

### Logs để theo dõi:
- `AuthManager`: Token refresh activities
- `FirebaseInterceptor`: Token injection vào requests  
- `FirebaseAuthenticator`: Token refresh và retry activities
- `FirebaseAuthRepository`: Authentication success/failure

### Log tags:
- `AuthManager`
- `FirebaseInterceptor` 
- `FirebaseAuthenticator`
- `FirebaseAuthRepository`
- `ContinueWithGoogle`

## Troubleshooting

### 1. **Vẫn gặp lỗi 401/404**:
- Kiểm tra Firebase project configuration
- Verify Firebase Admin SDK setup ở backend
- Kiểm tra clock sync giữa client/server

### 2. **Token không được refresh**:
- Kiểm tra network connectivity
- Verify Firebase user đang signed in
- Check AuthManager logs

### 3. **Infinite retry loops**:
- FirebaseAuthenticator có giới hạn 2 lần retry
- Check backend logs để xem lý do reject token
