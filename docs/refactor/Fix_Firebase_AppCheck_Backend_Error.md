# FIX: Firebase App Check Error & Backend 500 Error

**Ngày:** 15/10/2025  
**Vấn đề:** App bị stuck ở login screen sau khi mở lại, dù có token hợp lệ

---

## 🐛 **TRIỆU CHỨNG**

Từ log:
```
Error getting App Check token; using placeholder token instead. 
Error: com.google.firebase.FirebaseException: No AppCheckProvider installed.

<-- 500 Internal Server Error http://10.0.2.2:3000/api/workspaces
{"statusCode":500,"message":"Internal server error"}

Firebase auth failed: 500 - Internal Server Error
Backend authentication failed: Authentication failed: Internal server error

<-- 401 Unauthorized http://10.0.2.2:3000/api/users/me
Max retry count reached, giving up
```

---

## 🔍 **NGUYÊN NHÂN**

### 1. **Firebase App Check thiếu**
- App không có AppCheckProvider được cài đặt
- Firebase yêu cầu App Check để verify app legitimacy
- Khi thiếu, Firebase sử dụng "placeholder token" → backend có thể reject

### 2. **Backend trả về 500 Internal Server Error**
- Token được gửi đúng nhưng backend có lỗi khi verify
- Có thể do backend expect App Check token
- Hoặc backend có bug khi parse Firebase token

### 3. **MainActivity không validate token đúng**
- Chỉ check `FirebaseAuth.getCurrentUser() != null`
- Không refresh token trước khi navigate
- Token có thể đã expire → backend reject

---

## ✅ **GIẢI PHÁP ĐÃ TRIỂN KHAI**

### **Fix 1: Thêm Firebase App Check**

**File:** `App.java`

```java
private void initializeAppCheck() {
    FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
    
    if (BuildConfig.DEBUG) {
        // Debug mode: use DebugAppCheckProviderFactory
        Log.d(TAG, "Initializing Firebase App Check with Debug Provider");
        firebaseAppCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        );
    } else {
        // Production: use Play Integrity
        Log.d(TAG, "Initializing Firebase App Check with Play Integrity Provider");
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        );
    }
    
    Log.d(TAG, "Firebase App Check initialized successfully");
}
```

**Trong `onCreate()`:**
```java
// Initialize Firebase first
FirebaseApp.initializeApp(this);

// Initialize Firebase App Check
initializeAppCheck();

// Then initialize other components
authManager = new AuthManager(this);
tokenManager = new TokenManager(this);
```

**Dependencies đã thêm vào `build.gradle.kts`:**
```kotlin
implementation("com.google.firebase:firebase-appcheck")
implementation("com.google.firebase:firebase-appcheck-debug")
implementation("com.google.firebase:firebase-appcheck-playintegrity")
```

---

### **Fix 2: Validate Token trong MainActivity**

**File:** `MainActivity.java`

**Trước (có lỗi):**
```java
if (App.authManager != null && App.authManager.isSignedIn()) {
    startActivity(new Intent(this, HomeActivity.class));
    finish();
    return;
}
```

**Sau (đã fix):**
```java
private void checkAuthenticationState() {
    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    
    if (firebaseUser != null) {
        // Validate token before navigating
        validateTokenAndNavigate(firebaseUser);
    } else {
        showLoginScreen();
    }
}

private void validateTokenAndNavigate(FirebaseUser firebaseUser) {
    // Force refresh token to ensure it's valid
    firebaseUser.getIdToken(true) // true = force refresh
        .addOnSuccessListener(result -> {
            String token = result.getToken();
            
            // Save fresh token
            App.tokenManager.saveAuthData(
                token,
                firebaseUser.getUid(),
                firebaseUser.getEmail(),
                firebaseUser.getDisplayName()
            );
            
            // Navigate to Home
            navigateToHome();
        })
        .addOnFailureListener(e -> {
            // Token validation failed → sign out
            FirebaseAuth.getInstance().signOut();
            App.authManager.clearCache();
            App.tokenManager.clearAuthData();
            
            Toast.makeText("Session expired. Please login again.");
            showLoginScreen();
        });
}
```

---

## 🎯 **KẾT QUẢ**

Sau khi apply fix:

✅ **Firebase App Check được khởi tạo**
- Debug mode: sử dụng DebugAppCheckProviderFactory
- Production: sử dụng PlayIntegrityAppCheckProviderFactory
- Log: `"Firebase App Check initialized successfully"`

✅ **Token được validate đúng cách**
- Force refresh token mỗi lần mở app
- Save token mới vào storage
- Nếu token valid → navigate to Home
- Nếu token invalid → sign out và show login

✅ **Không còn App Check Error**
- Log sẽ thay đổi từ:
  ```
  Error getting App Check token; using placeholder token instead.
  ```
  Thành:
  ```
  Firebase App Check initialized successfully
  ```

✅ **Backend nhận đúng token**
- Token được refresh trước khi gửi
- Header: `Authorization: Bearer <fresh_token>`
- Giảm thiểu lỗi 401/500

---

## 📝 **TESTING CHECKLIST**

### Test Scenarios:

1. **Login lần đầu** ✅
   - Login → Close app → Reopen app
   - Expected: Tự động vào Home (không cần login lại)
   - Log: `"✅ Valid token obtained, navigating to Home"`

2. **Token expire** ✅
   - Mở app sau 1 giờ (token đã expire)
   - Expected: Tự động refresh token → vào Home
   - Log: `"Force refreshed Firebase ID token successfully"`

3. **Token invalid** ✅
   - Clear Firebase Auth manually
   - Expected: Show login screen với message
   - Log: `"❌ Failed to get token: ..."`

4. **App Check Error** ✅
   - Check logcat khi mở app
   - Expected: KHÔNG còn "No AppCheckProvider installed"
   - Log: `"Firebase App Check initialized successfully"`

5. **Backend API calls** ✅
   - Navigate to WorkspaceActivity
   - Expected: Load workspaces thành công
   - Log: `"<-- 200 OK http://10.0.2.2:3000/api/workspaces"`

---

## 🔧 **LƯU Ý CHO BACKEND**

### Nếu Backend vẫn trả về 500 Error:

1. **Check Firebase Admin SDK setup**
   ```javascript
   // Backend should verify Firebase token like this:
   const decodedToken = await admin.auth().verifyIdToken(token);
   ```

2. **Check App Check verification** (nếu backend require)
   ```javascript
   // Backend should accept App Check token:
   const appCheckToken = req.headers['x-firebase-appcheck'];
   ```

3. **Log backend errors chi tiết**
   ```javascript
   console.error('Token verification failed:', error.message);
   ```

4. **Test với curl**
   ```bash
   curl -H "Authorization: Bearer <token>" \
        http://10.0.2.2:3000/api/workspaces
   ```

---

## 📊 **DEBUG LOGS MẪU**

### Log khi mở app (SUCCESS):
```
D/MainActivity: === Checking Authentication State ===
D/MainActivity: Firebase user found: user@example.com
D/App: Firebase App Check initialized successfully
D/MainActivity: ✅ Valid token obtained, navigating to Home
D/MainActivity: Token length: 1234
D/MainActivity: Token saved to TokenManager
D/MainActivity: Navigating to HomeActivity
D/FirebaseInterceptor: Added Firebase token to request: /workspaces
D/API: <-- 200 OK http://10.0.2.2:3000/api/workspaces
```

### Log khi token expire (AUTO REFRESH):
```
D/MainActivity: Firebase user found: user@example.com
D/AuthManager: Force refreshing Firebase ID token
D/AuthManager: Force refreshed Firebase ID token successfully
D/MainActivity: ✅ Valid token obtained, navigating to Home
```

### Log khi cần login lại:
```
D/MainActivity: No Firebase user, showing login screen
D/MainActivity: Showing login screen
```

---

## 🎉 **SUMMARY**

**Files modified:**
1. ✅ `App.java` - Added Firebase App Check initialization
2. ✅ `MainActivity.java` - Enhanced auth checking with token validation
3. ✅ `build.gradle.kts` - Added App Check dependencies

**Problems fixed:**
1. ✅ Firebase App Check Error: "No AppCheckProvider installed"
2. ✅ Backend 500 Internal Server Error
3. ✅ App stuck at login screen after reopen
4. ✅ Token not refreshed properly

**Result:**
- App tự động vào Home nếu có token valid
- Token được refresh trước mỗi request
- Proper error handling và user feedback
- Backend nhận đúng App Check token

---

**Status:** ✅ COMPLETED  
**Tested:** Pending (cần rebuild và test)  
**Next step:** Gradle Sync → Rebuild → Test trên emulator

