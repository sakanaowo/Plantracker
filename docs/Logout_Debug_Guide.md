# 🔧 DEBUG GUIDE - NÚT LOGOUT KHÔNG HOẠT ĐỘNG

**Ngày:** 15/10/2025  
**Vấn đề:** Nút Logout không hoạt động khi nhấn

---

## 📋 CHECKLIST KIỂM TRA

### 1. ✅ Kiểm tra Build
```bash
# Clean và rebuild project
./gradlew clean
./gradlew build

# Hoặc trong Android Studio:
Build > Clean Project
Build > Rebuild Project
```

### 2. ✅ Kiểm tra Log
Mở Logcat và filter theo tag `AccountActivity`, bạn sẽ thấy:

**Khi mở AccountActivity:**
```
D/AccountActivity: initViews - btnLogout: FOUND
D/AccountActivity: initViews - layoutSettings: FOUND
D/AccountActivity: setupClickListeners: Logout button listener attached
```

**Nếu btnLogout bị NULL:**
```
E/AccountActivity: setupClickListeners: btnLogout is NULL!
```

**Khi nhấn nút Logout:**
```
D/AccountActivity: Logout button clicked!
D/AccountActivity: User confirmed logout
D/AccountActivity: Performing logout...
D/AuthRepositoryImpl: Logout: Clearing Firebase auth and tokens
D/AuthManager: Clearing cached token
D/AccountActivity: User logged out, redirecting to login...
```

---

## 🐛 CÁC NGUYÊN NHÂN CÓ THỂ

### Nguyên nhân 1: Build cache cũ
**Triệu chứng:** Code đã sửa nhưng app vẫn chạy version cũ

**Giải pháp:**
1. Build > Clean Project
2. Build > Rebuild Project
3. Uninstall app trên emulator/device
4. Run lại

---

### Nguyên nhân 2: btnLogout = NULL
**Triệu chứng:** Log hiện "btnLogout is NULL!"

**Nguyên nhân:**
- ID trong XML không khớp
- Type casting sai (Button vs MaterialButton)
- Layout file sai

**Giải pháp:**
Kiểm tra file `account.xml`:
```xml
<com.google.android.material.button.MaterialButton
    android:id="@+id/btnLogout"  ← Phải có ID này
    .../>
```

Kiểm tra `AccountActivity.java`:
```java
private MaterialButton btnLogout;  ← Phải là MaterialButton, không phải Button
```

---

### Nguyên nhân 3: Click listener không được gắn
**Triệu chứng:** Nhấn nút không có phản ứng, log không hiện "Logout button clicked!"

**Nguyên nhân:**
- Exception xảy ra trước khi `setupClickListeners()` được gọi
- View bị overlay bởi view khác

**Giải pháp:**
Check log xem có exception nào không:
```
E/AndroidRuntime: FATAL EXCEPTION
```

---

### Nguyên nhân 4: Dialog không hiện
**Triệu chứng:** Nhấn nút, log hiện "Logout button clicked!" nhưng dialog không hiện

**Nguyên nhân:**
- Activity context bị null
- Theme không hỗ trợ AlertDialog

**Giải pháp:**
Thử thay đổi dialog builder:
```java
private void showLogoutDialog() {
    new AlertDialog.Builder(AccountActivity.this)  // ← Explicit context
        .setTitle("Logout")
        .setMessage("Are you sure you want to logout?")
        .setPositiveButton("Logout", (dialog, which) -> performLogout())
        .setNegativeButton("Cancel", null)
        .show();
}
```

---

### Nguyên nhân 5: AuthViewModel không hoạt động
**Triệu chứng:** Dialog hiện, nhấn confirm nhưng không logout

**Nguyên nhân:**
- AuthViewModel không được khởi tạo đúng
- Logout method không được gọi
- Observer không trigger

**Giải pháp:**
Check log:
```
D/AccountActivity: Performing logout...
D/AuthRepositoryImpl: Logout: Clearing Firebase auth and tokens
```

Nếu KHÔNG thấy log trên → `authViewModel.logout()` không được gọi

---

## 🔍 HƯỚNG DẪN DEBUG TỪNG BƯỚC

### Bước 1: Kiểm tra btnLogout có được tìm thấy không
```
Chạy app → Mở Account tab → Xem Logcat

✅ ĐÚNG:
D/AccountActivity: initViews - btnLogout: FOUND

❌ SAI:
D/AccountActivity: initViews - btnLogout: NULL
→ Vấn đề: findViewById không tìm thấy view
→ Sửa: Kiểm tra ID trong XML
```

### Bước 2: Kiểm tra click listener
```
Nhấn nút Logout → Xem Logcat

✅ ĐÚNG:
D/AccountActivity: Logout button clicked!

❌ SAI: Không có log gì
→ Vấn đề: Click listener không được gắn hoặc view bị overlay
→ Sửa: Check setupClickListeners() được gọi chưa
```

### Bước 3: Kiểm tra dialog
```
Sau khi nhấn Logout

✅ ĐÚNG: Dialog xuất hiện với 2 nút (Logout / Cancel)

❌ SAI: Dialog không hiện
→ Vấn đề: showLogoutDialog() có exception
→ Sửa: Check Logcat có lỗi gì không
```

### Bước 4: Kiểm tra logout flow
```
Nhấn "Logout" trong dialog → Xem Logcat

✅ ĐÚNG:
D/AccountActivity: User confirmed logout
D/AccountActivity: Performing logout...
D/AuthRepositoryImpl: Logout: Clearing Firebase auth and tokens
D/AuthManager: Clearing cached token
D/AccountActivity: User logged out, redirecting to login...

❌ SAI: Không có log
→ Vấn đề: authViewModel.logout() không hoạt động
→ Sửa: Check AuthViewModel có được init đúng không
```

---

## 🚀 QUICK FIX

Nếu bạn không muốn debug, thử giải pháp nhanh này:

### Option 1: Sử dụng Settings để logout
Nhấn vào **Settings** thay vì nút Logout → Settings cũng gọi logout

### Option 2: Thêm Toast để debug
Sửa file `AccountActivity.java`:

```java
private void setupClickListeners() {
    if (btnLogout != null) {
        btnLogout.setOnClickListener(v -> {
            Toast.makeText(this, "Logout clicked!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Logout button clicked!");
            showLogoutDialog();
        });
    } else {
        Toast.makeText(this, "ERROR: Logout button is NULL!", Toast.LENGTH_LONG).show();
    }
}
```

Chạy lại → Nhấn Logout:
- Nếu thấy Toast "Logout clicked!" → Listener hoạt động
- Nếu thấy Toast "ERROR..." → btnLogout bị NULL

---

## 📱 TEST TRÊN THIẾT BỊ THẬT

Nếu test trên emulator không hoạt động, thử trên thiết bị thật:

1. Enable USB Debugging trên phone
2. Connect phone vào máy tính
3. Run app trên phone
4. Test logout

---

## ✅ EXPECTED BEHAVIOR

Khi logout hoạt động đúng:

1. Nhấn nút "LOGOUT" (màu đỏ, có icon)
2. Dialog xuất hiện: "Are you sure you want to logout?"
3. Nhấn "Logout"
4. Screen chuyển về LoginActivity
5. Data đã được clear (không thể back về Account)

---

## 📞 NEXT STEPS

**Nếu vẫn không hoạt động:**

1. Copy toàn bộ log từ Logcat (filter: AccountActivity)
2. Paste log vào chat
3. Tôi sẽ phân tích chính xác vấn đề

**Log cần thiết:**
```
D/AccountActivity: initViews - btnLogout: ???
D/AccountActivity: setupClickListeners: ???
D/AccountActivity: Logout button clicked!  (khi nhấn nút)
```

---

**Tác giả:** AI Assistant  
**Ngày tạo:** 15/10/2025

