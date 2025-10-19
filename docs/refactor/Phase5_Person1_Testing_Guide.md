# HƯỚNG DẪN TEST PHASE 5 - NGƯỜI 1
**Ngày:** 14/10/2025  
**Thời gian:** 30-40 phút  
**Mục tiêu:** Test LoginActivity, SignupActivity, HomeActivity với ViewModel

---

## 🎯 CHUẨN BỊ TRƯỚC KHI TEST

### **1. Kiểm tra Backend Server**

**QUAN TRỌNG:** App cần backend server để hoạt động!

```bash
# Kiểm tra backend có đang chạy không
# Mở browser và truy cập:
http://localhost:3000/health
# hoặc
http://10.0.2.2:3000/health  (nếu dùng Android Emulator)
```

**Nếu backend chưa chạy:**
```bash
# Di chuyển đến thư mục backend
cd /path/to/backend

# Cài dependencies (lần đầu)
npm install

# Chạy backend
npm run dev
# hoặc
npm start
```

**Expected response:**
```json
{
  "status": "ok",
  "timestamp": "2025-10-14T..."
}
```

### **2. Kiểm tra API Endpoint trong ApiClient**

File: `app/src/main/java/com/example/tralalero/network/ApiClient.java`

**Đảm bảo BASE_URL đúng:**
```java
// Nếu test trên Emulator:
private static final String BASE_URL = "http://10.0.2.2:3000/";

// Nếu test trên thiết bị thật:
private static final String BASE_URL = "http://YOUR_COMPUTER_IP:3000/";
// Ví dụ: "http://192.168.1.100:3000/"
```

**Để tìm IP máy tính:**
```bash
# Windows:
ipconfig
# Tìm IPv4 Address của WiFi/Ethernet adapter

# Mac/Linux:
ifconfig
# Tìm inet address
```

### **3. Tạo Test Account (Nếu cần)**

**Option 1: Tạo qua backend directly**
```bash
# POST đến API signup
curl -X POST http://localhost:3000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "test123456"
  }'
```

**Option 2: Sẽ tạo qua app trong quá trình test**

### **4. Build và Install App**

```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Install trên device/emulator
./gradlew installDebug

# Hoặc chỉ cần Run trong Android Studio (Shift+F10)
```

---

## 🧪 TEST CASE 1: LOGINACTIVITY

### **Test 1.1: Login Thành Công (Happy Path)**

**Steps:**
1. Mở app → Màn hình MainActivity
2. Click button "Login" (hoặc "Sign In")
3. Nhập email: `test@example.com`
4. Nhập password: `test123456`
5. Click button "Login"

**Expected Results:**
```
✅ Button "Login" disabled và text đổi thành "Logging in..."
✅ Loading state (nếu có)
✅ Toast message: "Welcome back, [User Name]"
✅ Navigate to HomeActivity
✅ Trong HomeActivity thấy danh sách workspaces (nếu có)
```

**Logcat để debug:**
```bash
# Filter logcat
adb logcat | grep -E "LoginActivity|AuthViewModel|AuthRepository"
```

**Expected logs:**
```
LoginActivity: Attempting login with email: test@example.com
AuthViewModel: login() called
AuthRepositoryImpl: Signing in with Firebase...
AuthRepositoryImpl: Firebase sign-in success
AuthRepositoryImpl: Authenticating with backend...
AuthRepositoryImpl: Backend auth success, user: {...}
LoginActivity: Login success, navigating to Home
HomeActivity: onCreate()
```

**Nếu thất bại, check:**
- [ ] Backend server có chạy không?
- [ ] BASE_URL trong ApiClient đúng không?
- [ ] Email/password có trong database không?
- [ ] Firebase config (google-services.json) đúng không?
- [ ] Internet permission trong Manifest?

### **Test 1.2: Login Sai Password**

**Steps:**
1. Mở LoginActivity
2. Nhập email: `test@example.com`
3. Nhập password: `wrongpassword`
4. Click "Login"

**Expected Results:**
```
✅ Button disabled → "Logging in..." → enabled trở lại "Login"
✅ Toast error: "Invalid email or password"
✅ Vẫn ở màn hình Login (không navigate)
```

**Expected logs:**
```
AuthRepositoryImpl: Firebase sign-in failed: auth/wrong-password
AuthViewModel: setError("Invalid email or password")
LoginActivity: Error: Invalid email or password
```

### **Test 1.3: Login Email Không Tồn Tại**

**Steps:**
1. Nhập email: `nonexistent@example.com`
2. Nhập password: `anything123`
3. Click "Login"

**Expected Results:**
```
✅ Toast error: "Invalid email or password"
✅ Vẫn ở LoginActivity
```

### **Test 1.4: Validation - Email Rỗng**

**Steps:**
1. Để trống email
2. Nhập password: `test123456`
3. Click "Login"

**Expected Results:**
```
✅ Toast: "Email is required"
✅ KHÔNG gọi API (check logcat)
✅ Vẫn ở LoginActivity
```

### **Test 1.5: Validation - Password Rỗng**

**Steps:**
1. Nhập email: `test@example.com`
2. Để trống password
3. Click "Login"

**Expected Results:**
```
✅ Toast: "Password is required"
✅ KHÔNG gọi API
✅ Vẫn ở LoginActivity
```

### **Test 1.6: Network Error**

**Steps:**
1. Tắt backend server
2. Nhập email/password đúng
3. Click "Login"

**Expected Results:**
```
✅ Toast error về network
✅ Button enabled trở lại
✅ Vẫn ở LoginActivity
```

**Expected logs:**
```
AuthRepositoryImpl: Network error: Unable to resolve host...
AuthViewModel: setError("Network error...")
```

---

## 🧪 TEST CASE 2: SIGNUPACTIVITY

### **Test 2.1: Signup Thành Công (Happy Path)**

**Steps:**
1. Từ MainActivity, click "Sign Up"
2. Nhập email MỚI: `newuser@example.com`
3. Nhập password: `newpass123`
4. Nhập confirm password: `newpass123`
5. Click "Sign Up"

**Expected Results:**
```
✅ Button disabled → "Signing up..."
✅ Toast: "Welcome [User Name]" (không có "back")
✅ Navigate to HomeActivity
✅ User mới được tạo trong Firebase + Backend
```

**Expected logs:**
```
SignupActivity: Attempting signup with email: newuser@example.com
AuthViewModel: login() called (yes, login - vì backend tự xử lý signup)
AuthRepositoryImpl: Firebase user doesn't exist, creating...
AuthRepositoryImpl: Firebase account created
AuthRepositoryImpl: Syncing with backend...
AuthRepositoryImpl: User created in backend
SignupActivity: Signup success
```

### **Test 2.2: Email Đã Tồn Tại**

**Steps:**
1. Nhập email đã dùng: `test@example.com`
2. Password: `test123456`
3. Confirm: `test123456`
4. Click "Sign Up"

**Expected Results:**
```
✅ Có thể thành công (vì backend tự xử lý)
   HOẶC
✅ Error: "Email already exists"
```

**Note:** Tùy logic backend, có thể signup lại = login

### **Test 2.3: Validation - Email Format Sai**

**Steps:**
1. Email: `invalidemail` (không có @)
2. Password: `test123456`
3. Confirm: `test123456`
4. Click "Sign Up"

**Expected Results:**
```
✅ Toast: "Please enter a valid email address"
✅ KHÔNG gọi API
```

### **Test 2.4: Validation - Password < 6 Chars**

**Steps:**
1. Email: `test2@example.com`
2. Password: `123` (< 6 chars)
3. Confirm: `123`
4. Click "Sign Up"

**Expected Results:**
```
✅ Toast: "Password must be at least 6 characters"
✅ KHÔNG gọi API
```

### **Test 2.5: Validation - Passwords Không Match**

**Steps:**
1. Email: `test2@example.com`
2. Password: `password123`
3. Confirm: `password456` (khác)
4. Click "Sign Up"

**Expected Results:**
```
✅ Toast: "Passwords do not match"
✅ KHÔNG gọi API
```

### **Test 2.6: Validation - Email Rỗng**

**Expected:** Toast "Email is required"

### **Test 2.7: Validation - Password Rỗng**

**Expected:** Toast "Password is required"

### **Test 2.8: Validation - Confirm Password Rỗng**

**Expected:** Toast "Please confirm your password"

---

## 🧪 TEST CASE 3: HOMEACTIVITY

### **Prerequisite: Login Trước**

Để test HomeActivity, bạn phải login trước:
```
MainActivity → LoginActivity → login success → HomeActivity
```

### **Test 3.1: Load Workspaces Thành Công**

**Steps:**
1. Login thành công
2. Tự động navigate to HomeActivity
3. Observe RecyclerView

**Expected Results:**
```
✅ ViewModel gọi loadWorkspaces()
✅ RecyclerView hiển thị danh sách workspaces
✅ Mỗi workspace item hiển thị:
   - Tên workspace
   - Icon/Avatar
   - (Các thông tin khác nếu có)
```

**Expected logs:**
```
HomeActivity: onCreate()
HomeActivity: setupWorkspaceViewModel()
HomeActivity: observeWorkspaceViewModel()
WorkspaceViewModel: loadWorkspaces()
HomeActivity: Loading workspaces...
WorkspaceRepositoryImpl: Fetching workspaces from API...
HomeActivity: Loaded 3 workspaces from ViewModel
HomeActivity: Finished loading workspaces.
```

**Verify trong UI:**
- [ ] RecyclerView có items
- [ ] Tên workspace hiển thị đúng
- [ ] Có thể scroll nếu nhiều workspaces

### **Test 3.2: Empty Workspaces**

**Steps:**
1. Login với user MỚI (chưa có workspace)
2. Check HomeActivity

**Expected Results:**
```
✅ Log: "No workspaces found"
✅ RecyclerView rỗng
✅ (Optional) Empty state message
```

### **Test 3.3: Click Workspace Item**

**Steps:**
1. Trong danh sách workspaces
2. Click vào 1 workspace item

**Expected Results:**
```
✅ Log: "Clicked workspace: [Name] (ID: [UUID])"
✅ Navigate to WorkspaceActivity
✅ WorkspaceActivity nhận đúng:
   - WORKSPACE_ID
   - WORKSPACE_NAME
```

**Verify trong WorkspaceActivity:**
```java
// Check trong WorkspaceActivity.onCreate()
String workspaceId = getIntent().getStringExtra("WORKSPACE_ID");
String workspaceName = getIntent().getStringExtra("WORKSPACE_NAME");
Log.d("WorkspaceActivity", "Received ID: " + workspaceId + ", Name: " + workspaceName);
```

### **Test 3.4: Loading State**

**Steps:**
1. (Optional) Thêm delay trong API để dễ observe
2. Login và observe loading

**Expected Results:**
```
✅ Log: "Loading workspaces..."
✅ (Nếu có UI) ProgressBar visible
✅ Sau khi load xong: "Finished loading workspaces."
✅ (Nếu có UI) ProgressBar gone
```

### **Test 3.5: Network Error**

**Steps:**
1. Login thành công
2. Tắt backend server
3. Force reload workspaces (kill app và restart)

**Expected Results:**
```
✅ Toast: "Error loading workspaces: [error message]"
✅ RecyclerView rỗng hoặc giữ nguyên data cũ
✅ Error được clear sau khi hiển thị
```

**Expected logs:**
```
WorkspaceRepositoryImpl: Error fetching workspaces
WorkspaceViewModel: setError("...")
HomeActivity: Error loading workspaces: ...
```

### **Test 3.6: Mapper Conversion**

**Verify trong code/logs:**
```java
// Trong observeWorkspaceViewModel()
Log.d(TAG, "Domain workspace: " + domainWorkspace.toString());
Log.d(TAG, "Converted old workspace: " + old.toString());
```

**Check:**
- [ ] Domain model fields được map đúng sang old model
- [ ] Không có field nào bị null
- [ ] Type conversion đúng (String, Date, etc.)

---

## 🧪 TEST CASE 4: NAVIGATION FLOW

### **Test 4.1: Complete Flow - New User**

**Steps:**
1. Open app → MainActivity
2. Click "Sign Up"
3. Enter new email: `flowtest@example.com`
4. Password: `flowtest123`
5. Confirm: `flowtest123`
6. Click "Sign Up"
7. Redirected to HomeActivity
8. See empty workspace list
9. (Future) Create workspace

**Expected:** Smooth flow, no crashes

### **Test 4.2: Complete Flow - Existing User**

**Steps:**
1. Open app → MainActivity
2. Click "Login"
3. Enter existing email/password
4. Login success
5. HomeActivity shows workspaces
6. Click a workspace
7. WorkspaceActivity opens

**Expected:** Smooth navigation

### **Test 4.3: Auto-Login (Session Persistence)**

**Steps:**
1. Login thành công
2. Close app (swipe away from recent apps)
3. Open app again

**Expected Results:**
```
✅ MainActivity checks isSignedIn()
✅ Tự động navigate to HomeActivity
✅ KHÔNG cần login lại
✅ Workspaces load tự động
```

**This tests:** AuthManager token persistence

---

## 📊 CHECKLIST TESTING

### **LoginActivity**
- [ ] Login thành công → HomeActivity
- [ ] Login sai password → Error toast
- [ ] Login email không tồn tại → Error
- [ ] Validation email rỗng
- [ ] Validation password rỗng
- [ ] Network error handling
- [ ] Loading state (button disabled)

### **SignupActivity**
- [ ] Signup email mới thành công
- [ ] Email đã tồn tại (xử lý đúng)
- [ ] Validation email format
- [ ] Validation password < 6 chars
- [ ] Validation passwords không match
- [ ] Validation email rỗng
- [ ] Validation password rỗng
- [ ] Validation confirm password rỗng
- [ ] Loading state

### **HomeActivity**
- [ ] Load workspaces thành công
- [ ] Empty workspaces hiển thị đúng
- [ ] Click workspace → WorkspaceActivity
- [ ] Loading state (logs hoặc UI)
- [ ] Error handling (network error)
- [ ] Mapper convert domain → old model
- [ ] RecyclerView scrolling

### **Navigation & Integration**
- [ ] MainActivity → LoginActivity → HomeActivity
- [ ] MainActivity → SignupActivity → HomeActivity
- [ ] HomeActivity → WorkspaceActivity
- [ ] Auto-login (session persistence)
- [ ] Back button navigation đúng

---

## 🐛 COMMON ISSUES & FIXES

### **Issue 1: "Unable to resolve host"**

**Cause:** Backend không chạy hoặc BASE_URL sai

**Fix:**
```java
// ApiClient.java
private static final String BASE_URL = "http://10.0.2.2:3000/";  // Emulator
// hoặc
private static final String BASE_URL = "http://192.168.1.X:3000/";  // Real device
```

### **Issue 2: "FirebaseApp not initialized"**

**Cause:** google-services.json chưa đúng

**Fix:**
1. Download google-services.json từ Firebase Console
2. Đặt vào `app/google-services.json`
3. Rebuild project

### **Issue 3: Toast không hiển thị**

**Cause:** Toast.makeText context sai hoặc thread issue

**Fix:**
```java
// Đảm bảo Toast chạy trên UI thread
runOnUiThread(() -> {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
});
```

### **Issue 4: ViewModel không observe**

**Cause:** Lifecycle issue hoặc observe sau khi data đã emit

**Fix:**
```java
// Đảm bảo observe TRƯỚC khi load data
setupViewModel();
observeViewModel();  // ← Observe trước
viewModel.loadData();  // ← Load sau
```

### **Issue 5: RecyclerView rỗng dù có data**

**Cause:** Adapter chưa set hoặc mapper lỗi

**Fix:**
```java
// Check adapter setup
recyclerView.setAdapter(adapter);
recyclerView.setLayoutManager(new LinearLayoutManager(this));

// Check mapper
Log.d(TAG, "Workspaces size: " + workspaces.size());
Log.d(TAG, "Converted size: " + oldWorkspaces.size());
```

### **Issue 6: App crash khi navigate**

**Cause:** Intent extras null hoặc Activity không declared

**Fix:**
```xml
<!-- AndroidManifest.xml -->
<activity android:name=".feature.home.ui.Home.WorkspaceActivity" />
```

---

## 📱 TEST DEVICES

**Recommended:**
- Android Emulator (API 30+)
- Real device (Android 8.0+)

**Configurations to test:**
- [ ] Portrait orientation
- [ ] Landscape orientation (if supported)
- [ ] Different screen sizes
- [ ] Dark mode / Light mode

---

## 📝 TEST REPORT TEMPLATE

```markdown
## Test Report - Phase 5 Person 1
**Date:** 14/10/2025
**Tester:** [Your Name]
**Device:** [Emulator/Real Device Model]
**Android Version:** [e.g., 12]

### LoginActivity
- [ ] PASS: Login success
- [ ] PASS: Login wrong password
- [ ] PASS: Validation empty email
- [ ] PASS: Validation empty password
- [ ] FAIL: [Describe issue if any]

### SignupActivity
- [ ] PASS: Signup new user
- [ ] PASS: Email format validation
- [ ] PASS: Password length validation
- [ ] PASS: Passwords match validation
- [ ] FAIL: [Describe issue]

### HomeActivity
- [ ] PASS: Load workspaces
- [ ] PASS: Click workspace
- [ ] PASS: Error handling
- [ ] FAIL: [Describe issue]

### Issues Found:
1. [Issue description]
   - Steps to reproduce:
   - Expected:
   - Actual:
   - Logs:

### Overall Status:
✅ PASS / ❌ FAIL

### Notes:
[Any additional observations]
```

---

## ⚡ QUICK START TESTING

**5-Minute Smoke Test:**
```
1. Start backend server
2. Run app on emulator
3. Click "Sign Up"
4. Enter: test123@example.com / test123456 / test123456
5. Click "Sign Up"
6. Verify: Navigate to HomeActivity
7. Verify: See workspaces (if any)
8. Click a workspace
9. Verify: Navigate to WorkspaceActivity

✅ If all pass → GOOD TO GO
❌ If any fail → Check logs and debug
```

---

## 🎯 SUCCESS CRITERIA

**Phase 5 - Person 1 considered COMPLETE when:**
- [x] All LoginActivity tests pass
- [x] All SignupActivity tests pass
- [x] All HomeActivity tests pass
- [x] Navigation flows work smoothly
- [x] No crashes during normal usage
- [x] Error handling works properly
- [x] Loading states display correctly

**Estimated testing time:** 30-40 minutes for full suite

Good luck! 🚀

