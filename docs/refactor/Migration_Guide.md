# Migration Guide - Firebase Token Management System

## Tóm tắt thay đổi

### ✅ Files mới được tạo:

1. **`FirebaseInterceptor.java`** - Thay thế AuthInterceptor cũ
   - Tự động thêm Firebase token vào requests
   - Skip public endpoints
   - Logging chi tiết

2. **`FirebaseAuthenticator.java`** - Xử lý token refresh tự động
   - Auto refresh khi gặp lỗi 401/404
   - Retry mechanism với giới hạn
   - Auto sign out nếu thất bại

3. **`PublicAuthApi.java`** - API interface cho public endpoints
   - Dành cho Firebase auth ban đầu
   - Không có Authorization header

4. **`FirebaseAuthExample.java`** - Ví dụ và demo usage

### 🔄 Files đã được cập nhật:

1. **`AuthManager.java`** 
   - ✅ Thêm `forceRefreshToken()` method
   - ✅ Cải thiện token caching (5 phút buffer)
   - ✅ Thêm logging chi tiết
   - ✅ Thêm helper methods: `isTokenExpired()`, `getCachedToken()`

2. **`ApiClient.java`**
   - ✅ Thay `AuthInterceptor` → `FirebaseInterceptor`
   - ✅ Thêm `FirebaseAuthenticator`
   - ✅ Cấu hình dual client pattern

3. **`AuthApi.java`**
   - ✅ Sửa endpoint: `"firebase/auth"` → `"auth/firebase/auth"`

4. **`FirebaseAuthRepository.java`**
   - ✅ Implement dual API pattern
   - ✅ Sử dụng `PublicAuthApi` cho Firebase auth
   - ✅ Sử dụng `AuthApi` cho authenticated requests

5. **`ContinueWithGoogle.java`**
   - ✅ Khởi tạo `AuthManager`
   - ✅ Sửa logic: Đợi Firebase token thay vì dùng Google token
   - ✅ Proper error handling

### 🗑️ Files cần xóa:

1. **`AuthInterceptor.java`** - ⚠️ DEPRECATED
   - Đã được thay thế bằng `FirebaseInterceptor.java`
   - File đã được đánh dấu để xóa
   - Không còn được sử dụng trong `ApiClient.java`

## Before vs After

### Trước đây:
```java
// AuthInterceptor cũ - không có auto refresh
public class AuthInterceptor implements Interceptor {
    // Chỉ thêm token, không xử lý refresh
    // Không có retry logic
    // Logging hạn chế
}

// ContinueWithGoogle - gửi sai token type
authenticateWithBackend(idToken, user); // idToken từ Google OAuth
```

### Bây giờ:
```java
// FirebaseInterceptor + FirebaseAuthenticator
public class FirebaseInterceptor implements Interceptor {
    // Tự động thêm token với smart caching
    // Skip public endpoints
    // Chi tiết logging
}

public class FirebaseAuthenticator implements Authenticator {
    // Auto refresh token khi 401/404
    // Retry với giới hạn
    // Clean error handling
}

// ContinueWithGoogle - đúng token type
firebaseUser.getIdToken(true).addOnCompleteListener(task -> {
    String firebaseIdToken = task.getResult().getToken(); // Firebase token
    authenticateWithBackend(firebaseIdToken, user);
});
```

## Lợi ích sau khi migration:

### 1. **Giải quyết token expiration**
- ❌ Trước: Token hết hạn sau 30 phút → 401/404 errors
- ✅ Sau: Auto refresh 5 phút trước khi hết hạn

### 2. **Automatic retry**
- ❌ Trước: Request fail → user phải retry manually
- ✅ Sau: Auto retry với fresh token

### 3. **Developer experience**
- ❌ Trước: Phải handle token manually
- ✅ Sau: Transparent token management

### 4. **Error handling**
- ❌ Trước: Basic error handling
- ✅ Sau: Smart retry + auto cleanup

### 5. **Performance**
- ❌ Trước: Token được fetch mỗi request
- ✅ Sau: Smart caching + proactive refresh

## Action items:

1. **✅ COMPLETED**: Tạo Firebase token management system
2. **✅ COMPLETED**: Update tất cả files liên quan  
3. **✅ COMPLETED**: Tạo documentation
4. **🔄 TODO**: Xóa `AuthInterceptor.java` (đã đánh dấu deprecated)
5. **🔄 TODO**: Test flow hoàn chỉnh
6. **🔄 TODO**: Monitor logs để đảm bảo hoạt động đúng

## Testing checklist:

- [ ] Google Sign-In thành công
- [ ] Firebase auth với backend thành công  
- [ ] Token tự động refresh trước khi hết hạn
- [ ] Auto retry khi gặp 401/404
- [ ] Auto sign out khi không thể refresh
- [ ] Logs hiển thị đúng token activities

## Rollback plan (nếu cần):

1. Revert `ApiClient.java` để dùng lại `AuthInterceptor`
2. Revert `ContinueWithGoogle.java` về logic cũ
3. Xóa các files mới tạo
4. Restore `AuthApi.java` endpoint cũ

**Note**: Không nên rollback vì hệ thống mới giải quyết được vấn đề token expiration căn bản.
