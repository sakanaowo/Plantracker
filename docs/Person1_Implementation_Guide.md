# HƯỚNG DẪN TRIỂN KHAI CHI TIẾT - NGƯỜI 1
## AUTH & HOME ACTIVITIES INTEGRATION

**Thời gian:** 2.5 giờ (19:00 - 21:30)  
**Mục tiêu:** Tích hợp AuthViewModel và WorkspaceViewModel vào LoginActivity, SignupActivity và HomeActivity

---

## 📋 CHECKLIST TỔNG QUAN

- [ ] **Task 1.1:** LoginActivity Integration (40 phút)
- [ ] **Task 1.2:** SignupActivity Integration (30 phút)  
- [ ] **Task 1.3:** HomeActivity Integration (40 phút)
- [ ] **Task 1.4:** Tạo ViewModelFactoryProvider Helper (20 phút)
- [ ] **Task 1.5:** Testing & Bug Fixes (20 phút)

---

## 🎯 TASK 1.1: LOGINACTIVITY INTEGRATION (40 PHÚT)

### **File cần chỉnh sửa:**
`/home/sakana/Code/Plantracker/app/src/main/java/com/example/tralalero/feature/auth/ui/login/LoginActivity.java`

### **Hiện trạng:**
LoginActivity hiện đang:
- Gọi API trực tiếp qua Retrofit
- Xử lý Firebase authentication thủ công
- Không sử dụng ViewModel pattern

### **BƯỚC 1: Thêm imports và khai báo ViewModel (5 phút)**

Thêm các import sau vào đầu file:

```java
import androidx.lifecycle.ViewModelProvider;
import com.example.tralalero.presentation.viewmodel.AuthViewModel;
import com.example.tralalero.presentation.viewmodel.AuthViewModelFactory;
import com.example.tralalero.domain.usecase.auth.*;
import com.example.tralalero.data.repository.AuthRepositoryImpl;
import com.example.tralalero.data.repository.IAuthRepository;
import com.example.tralalero.App.App;
```

Thêm biến instance vào class LoginActivity:

```java
public class LoginActivity extends AppCompatActivity {
    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    
    // Thêm dòng này
    private AuthViewModel authViewModel;
    
    // ... rest of the code
}
```

### **BƯỚC 2: Tạo method setupViewModel() (10 phút)**

Thêm method sau vào LoginActivity (có thể đặt trước method attemptLogin):

```java
private void setupViewModel() {
    // Bước 1: Tạo Repository
    IAuthRepository authRepository = new AuthRepositoryImpl(
        ApiClient.get(App.authManager).create(AuthApi.class),
        App.authManager
    );
    
    // Bước 2: Tạo các UseCases
    LoginUseCase loginUseCase = new LoginUseCase(authRepository);
    LogoutUseCase logoutUseCase = new LogoutUseCase(authRepository);
    GetCurrentUserUseCase getCurrentUserUseCase = new GetCurrentUserUseCase(authRepository);
    IsLoggedInUseCase isLoggedInUseCase = new IsLoggedInUseCase(authRepository);
    
    // Bước 3: Tạo Factory
    AuthViewModelFactory factory = new AuthViewModelFactory(
        loginUseCase,
        logoutUseCase,
        getCurrentUserUseCase,
        isLoggedInUseCase
    );
    
    // Bước 4: Tạo ViewModel
    authViewModel = new ViewModelProvider(this, factory).get(AuthViewModel.class);
}
```

### **BƯỚC 3: Tạo method observeViewModel() (10 phút)**

Thêm method sau để observe LiveData từ ViewModel:

```java
private void observeViewModel() {
    // Observe loading state
    authViewModel.isLoading().observe(this, isLoading -> {
        if (isLoading) {
            btnLogin.setEnabled(false);
            btnLogin.setText("Logging in...");
        } else {
            btnLogin.setEnabled(true);
            btnLogin.setText("Login");
        }
    });
    
    // Observe current user (login success)
    authViewModel.getCurrentUser().observe(this, user -> {
        if (user != null) {
            Log.d("LoginActivity", "Login success: " + user.getName());
            Toast.makeText(this, "Welcome " + user.getName(), Toast.LENGTH_SHORT).show();
            
            // Navigate to HomeActivity
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            intent.putExtra("user_name", user.getName());
            intent.putExtra("user_email", user.getEmail());
            startActivity(intent);
            finish();
        }
    });
    
    // Observe errors
    authViewModel.getError().observe(this, error -> {
        if (error != null) {
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            authViewModel.clearError();
        }
    });
}
```

### **BƯỚC 4: Cập nhật onCreate() (5 phút)**

Sửa method onCreate() để gọi setupViewModel() và observeViewModel():

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_login);
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
        Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
        v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
        return insets;
    });

    etEmail = findViewById(R.id.editTextEmail);
    etPassword = findViewById(R.id.editTextPassword);
    btnLogin = findViewById(R.id.buttonLogin);

    // THÊM 2 DÒNG NÀY
    setupViewModel();
    observeViewModel();

    final boolean[] isPasswordVisible = {false};
    // ... rest of password visibility code ...
    
    if (btnLogin != null) {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });
    }
}
```

### **BƯỚC 5: Refactor method attemptLogin() (10 phút)**

**QUAN TRỌNG:** Xóa toàn bộ code API call cũ và thay bằng ViewModel call đơn giản:

```java
private void attemptLogin() {
    String email = etEmail != null ? etEmail.getText().toString().trim() : "";
    String password = etPassword != null ? etPassword.getText().toString() : "";

    // Validation
    if (TextUtils.isEmpty(email)) {
        Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show();
        return;
    }
    if (TextUtils.isEmpty(password)) {
        Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show();
        return;
    }

    // THAY THẾ TOÀN BỘ API CALL CŨ BẰNG 1 DÒNG NÀY:
    authViewModel.login(email, password);
}
```

**LƯU Ý:** Xóa hoàn toàn các method sau (không cần nữa):
- `signInWithFirebase()` 
- `navigateToHome()`

ViewModel sẽ tự động xử lý Firebase authentication và navigation.

---

## 🎯 TASK 1.2: SIGNUPACTIVITY INTEGRATION (30 PHÚT)

### **File cần chỉnh sửa:**
Tìm file SignupActivity (có thể ở `/feature/auth/ui/signup/SignupActivity.java`)

### **Các bước thực hiện:**

**BƯỚC 1:** Copy toàn bộ setup từ LoginActivity:
- Thêm imports giống LoginActivity
- Thêm biến `private AuthViewModel authViewModel;`
- Copy method `setupViewModel()` 
- Copy method `observeViewModel()` (chỉnh sửa navigate logic nếu cần)

**BƯỚC 2:** Cập nhật onCreate():
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_signup);
    
    // Initialize views
    // ...
    
    // Setup ViewModel
    setupViewModel();
    observeViewModel();
    
    // Setup listeners
    // ...
}
```

**BƯỚC 3:** Refactor signup method (giả sử method tên là `attemptSignup()`):
```java
private void attemptSignup() {
    String email = etEmail.getText().toString().trim();
    String password = etPassword.getText().toString();
    String name = etName.getText().toString().trim();
    
    // Validation
    if (TextUtils.isEmpty(name)) {
        Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
        return;
    }
    if (TextUtils.isEmpty(email)) {
        Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show();
        return;
    }
    if (TextUtils.isEmpty(password)) {
        Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show();
        return;
    }
    
    // Call ViewModel (nếu có signup method)
    // authViewModel.signup(email, password, name);
    
    // HOẶC nếu chưa có signup trong ViewModel, tạm thời dùng login sau khi tạo account
    // Bạn cần check xem AuthViewModel có method signup() không
}
```

**LƯU Ý:** Kiểm tra xem AuthViewModel có method `signup()` không. Nếu không có, cần thêm vào sau.

---

## 🎯 TASK 1.3: HOMEACTIVITY INTEGRATION (40 PHÚT)

### **File cần chỉnh sửa:**
`/home/sakana/Code/Plantracker/app/src/main/java/com/example/tralalero/feature/home/ui/Home/HomeActivity.java`

### **Hiện trạng:**
HomeActivity hiện đang load workspaces từ API trực tiếp.

### **BƯỚC 1: Thêm imports và khai báo ViewModel (5 phút)**

```java
import androidx.lifecycle.ViewModelProvider;
import com.example.tralalero.presentation.viewmodel.WorkspaceViewModel;
import com.example.tralalero.presentation.viewmodel.WorkspaceViewModelFactory;
import com.example.tralalero.domain.usecase.workspace.*;
import com.example.tralalero.data.repository.WorkspaceRepositoryImpl;
import com.example.tralalero.data.repository.IWorkspaceRepository;
import com.example.tralalero.data.remote.api.WorkspaceApiService;
```

Thêm biến instance:

```java
public class HomeActivity extends BaseActivity {
    private RecyclerView recyclerBoard;
    private HomeAdapter homeAdapter;
    private static final String TAG = "HomeActivity";
    
    // Thêm dòng này
    private WorkspaceViewModel workspaceViewModel;
    
    // ... rest of the code
}
```

### **BƯỚC 2: Tạo method setupWorkspaceViewModel() (15 phút)**

```java
private void setupWorkspaceViewModel() {
    // Bước 1: Tạo Repository
    IWorkspaceRepository repository = new WorkspaceRepositoryImpl(
        ApiClient.get(App.authManager).create(WorkspaceApiService.class)
    );
    
    // Bước 2: Tạo các UseCases
    GetWorkspacesUseCase getWorkspacesUseCase = new GetWorkspacesUseCase(repository);
    GetWorkspaceByIdUseCase getWorkspaceByIdUseCase = new GetWorkspaceByIdUseCase(repository);
    CreateWorkspaceUseCase createWorkspaceUseCase = new CreateWorkspaceUseCase(repository);
    UpdateWorkspaceUseCase updateWorkspaceUseCase = new UpdateWorkspaceUseCase(repository);
    DeleteWorkspaceUseCase deleteWorkspaceUseCase = new DeleteWorkspaceUseCase(repository);
    
    // Bước 3: Tạo Factory
    WorkspaceViewModelFactory factory = new WorkspaceViewModelFactory(
        getWorkspacesUseCase,
        getWorkspaceByIdUseCase,
        createWorkspaceUseCase,
        updateWorkspaceUseCase,
        deleteWorkspaceUseCase
    );
    
    // Bước 4: Tạo ViewModel
    workspaceViewModel = new ViewModelProvider(this, factory).get(WorkspaceViewModel.class);
}
```

### **BƯỚC 3: Tạo method observeWorkspaceViewModel() (10 phút)**

```java
private void observeWorkspaceViewModel() {
    // Observe workspaces list
    workspaceViewModel.getWorkspaces().observe(this, workspaces -> {
        if (workspaces != null && !workspaces.isEmpty()) {
            Log.d(TAG, "Loaded " + workspaces.size() + " workspaces");
            homeAdapter.updateWorkspaces(workspaces);
        } else {
            Log.d(TAG, "No workspaces found");
        }
    });
    
    // Observe loading state
    workspaceViewModel.isLoading().observe(this, isLoading -> {
        if (isLoading) {
            // TODO: Show progress bar nếu có
            Log.d(TAG, "Loading workspaces...");
        } else {
            // TODO: Hide progress bar
            Log.d(TAG, "Loading complete");
        }
    });
    
    // Observe errors
    workspaceViewModel.getError().observe(this, error -> {
        if (error != null) {
            Log.e(TAG, "Error loading workspaces: " + error);
            Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
        }
    });
}
```

### **BƯỚC 4: Cập nhật onCreate() (5 phút)**

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_home);
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
        Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
        v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
        return insets;
    });

    // Initialize RecyclerView
    setupRecyclerView();

    // THÊM 2 DÒNG NÀY
    setupWorkspaceViewModel();
    observeWorkspaceViewModel();

    // XÓA DÒNG NÀY (không cần load từ API trực tiếp nữa)
    // loadWorkspacesFromApi();
    
    // THAY BẰNG DÒNG NÀY
    workspaceViewModel.loadWorkspaces();

    // Setup Test Repository Button (Development only)
    setupTestRepositoryButton();

    // ... rest of the code
}
```

### **BƯỚC 5: Cập nhật HomeAdapter (5 phút)**

Kiểm tra xem HomeAdapter có method `updateWorkspaces()` không. Nếu không có, thêm vào:

```java
public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {
    private List<Workspace> workspaces = new ArrayList<>();
    
    // Thêm method này
    public void updateWorkspaces(List<Workspace> newWorkspaces) {
        this.workspaces.clear();
        this.workspaces.addAll(newWorkspaces);
        notifyDataSetChanged();
    }
    
    // ... rest of adapter code
}
```

**LƯU Ý:** Có thể cần chỉnh sửa tên method tùy theo implementation hiện tại của adapter.

---

## 🎯 TASK 1.4: TẠO VIEWMODELFACTORYPROVIDER HELPER (20 PHÚT)

### **File mới cần tạo:**
`/home/sakana/Code/Plantracker/app/src/main/java/com/example/tralalero/presentation/viewmodel/ViewModelFactoryProvider.java`

Đây là helper class để giảm boilerplate code:

```java
package com.example.tralalero.presentation.viewmodel;

import com.example.tralalero.App.App;
import com.example.tralalero.auth.remote.AuthApi;
import com.example.tralalero.data.remote.api.WorkspaceApiService;
import com.example.tralalero.data.repository.*;
import com.example.tralalero.domain.usecase.auth.*;
import com.example.tralalero.domain.usecase.workspace.*;
import com.example.tralalero.network.ApiClient;

/**
 * Helper class để tạo ViewModelFactories
 * Giảm boilerplate code khi setup ViewModels
 */
public class ViewModelFactoryProvider {
    
    private static WorkspaceApiService workspaceApi;
    private static AuthApi authApi;
    
    private static void initApis() {
        if (workspaceApi == null) {
            workspaceApi = ApiClient.get(App.authManager).create(WorkspaceApiService.class);
        }
        if (authApi == null) {
            authApi = ApiClient.get(App.authManager).create(AuthApi.class);
        }
    }
    
    public static AuthViewModelFactory provideAuthViewModelFactory() {
        initApis();
        
        IAuthRepository repository = new AuthRepositoryImpl(authApi, App.authManager);
        
        return new AuthViewModelFactory(
            new LoginUseCase(repository),
            new LogoutUseCase(repository),
            new GetCurrentUserUseCase(repository),
            new IsLoggedInUseCase(repository)
        );
    }
    
    public static WorkspaceViewModelFactory provideWorkspaceViewModelFactory() {
        initApis();
        
        IWorkspaceRepository repository = new WorkspaceRepositoryImpl(workspaceApi);
        
        return new WorkspaceViewModelFactory(
            new GetWorkspacesUseCase(repository),
            new GetWorkspaceByIdUseCase(repository),
            new CreateWorkspaceUseCase(repository),
            new UpdateWorkspaceUseCase(repository),
            new DeleteWorkspaceUseCase(repository)
        );
    }
}
```

### **Sau khi tạo xong, refactor lại các Activity:**

**LoginActivity:**
```java
private void setupViewModel() {
    authViewModel = new ViewModelProvider(this, 
        ViewModelFactoryProvider.provideAuthViewModelFactory()
    ).get(AuthViewModel.class);
}
```

**HomeActivity:**
```java
private void setupWorkspaceViewModel() {
    workspaceViewModel = new ViewModelProvider(this,
        ViewModelFactoryProvider.provideWorkspaceViewModelFactory()
    ).get(WorkspaceViewModel.class);
}
```

---

## 🎯 TASK 1.5: TESTING & BUG FIXES (20 PHÚT)

### **Test Cases:**

#### **Test 1: Login Flow (5 phút)**
1. Mở LoginActivity
2. Nhập email và password hợp lệ
3. Click Login
4. Kiểm tra:
   - Button disabled và text đổi thành "Logging in..."
   - Toast hiển thị "Welcome [username]"
   - Navigate đến HomeActivity
   - Activity finish() sau khi navigate

#### **Test 2: Login Error Handling (5 phút)**
1. Nhập email/password sai
2. Click Login
3. Kiểm tra:
   - Error toast hiển thị
   - Button enabled lại
   - Không navigate

#### **Test 3: Home Load Workspaces (5 phút)**
1. Login thành công vào HomeActivity
2. Kiểm tra:
   - RecyclerView hiển thị workspaces
   - Loading state hoạt động
   - Error handling nếu API fail

#### **Test 4: Configuration Change (5 phút)**
1. Login và vào HomeActivity
2. Rotate device (hoặc change configuration)
3. Kiểm tra:
   - Data không bị mất
   - Không reload lại từ API
   - UI update đúng

### **Common Issues & Fixes:**

**Issue 1: ViewModel null**
```java
// Fix: Đảm bảo setupViewModel() được gọi trước observeViewModel()
setupViewModel();
observeViewModel();
```

**Issue 2: LiveData không update UI**
```java
// Fix: Đảm bảo observe() trong lifecycle owner đúng
viewModel.getData().observe(this, data -> { ... }); // 'this' phải là LifecycleOwner
```

**Issue 3: API không được gọi**
```java
// Fix: Đảm bảo gọi load method
workspaceViewModel.loadWorkspaces(); // Phải gọi method này
```

**Issue 4: Adapter không cập nhật**
```java
// Fix: Đảm bảo adapter có method update và gọi notifyDataSetChanged()
public void updateWorkspaces(List<Workspace> workspaces) {
    this.workspaces.clear();
    this.workspaces.addAll(workspaces);
    notifyDataSetChanged(); // QUAN TRỌNG
}
```

---

## 📊 TIMELINE CHI TIẾT

| Thời gian | Công việc | Ghi chú |
|-----------|-----------|---------|
| **19:00-19:05** | Đọc lại hướng dẫn | Hiểu rõ flow |
| **19:05-19:45** | Task 1.1: LoginActivity | 40 phút |
| **19:45-20:15** | Task 1.2: SignupActivity | 30 phút |
| **20:15-20:55** | Task 1.3: HomeActivity | 40 phút |
| **20:55-21:15** | Task 1.4: ViewModelFactoryProvider | 20 phút |
| **21:15-21:35** | Task 1.5: Testing | 20 phút |
| **21:35-21:40** | Final review | 5 phút buffer |

**Tổng:** 2 giờ 40 phút (có 20 phút buffer)

---

## ⚠️ LƯU Ý QUAN TRỌNG

### **1. Import Statements**
Đảm bảo import đúng package:
```java
// ViewModel
import com.example.tralalero.presentation.viewmodel.AuthViewModel;
import com.example.tralalero.presentation.viewmodel.WorkspaceViewModel;

// Factory
import com.example.tralalero.presentation.viewmodel.AuthViewModelFactory;
import com.example.tralalero.presentation.viewmodel.WorkspaceViewModelFactory;

// Repository
import com.example.tralalero.data.repository.IAuthRepository;
import com.example.tralalero.data.repository.AuthRepositoryImpl;

// UseCases
import com.example.tralalero.domain.usecase.auth.*;
```

### **2. Lifecycle Awareness**
```java
// ĐÚNG: Observe với lifecycle owner
viewModel.getData().observe(this, data -> { ... });

// SAI: Get value trực tiếp
viewModel.getData().getValue(); // Không dùng trong UI layer
```

### **3. Error Handling**
```java
// Luôn clear error sau khi hiển thị
authViewModel.getError().observe(this, error -> {
    if (error != null) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        authViewModel.clearError(); // QUAN TRỌNG!
    }
});
```

### **4. Memory Management**
- ViewModel tự động survive configuration changes
- LiveData tự động cleanup observers
- Không cần unregister observers manually

---

## 🐛 DEBUG TIPS

### **Enable Logging:**
```java
private static final String TAG = "LoginActivity";

// Log trong observe
authViewModel.getCurrentUser().observe(this, user -> {
    Log.d(TAG, "User updated: " + (user != null ? user.getName() : "null"));
    // ...
});
```

### **Check ViewModel State:**
```java
// Trong onCreate sau setup
Log.d(TAG, "ViewModel initialized: " + (authViewModel != null));
Log.d(TAG, "Current user: " + authViewModel.getCurrentUser().getValue());
```

### **Monitor API Calls:**
```java
// Trong Repository hoặc UseCase
Log.d(TAG, "Calling login API with email: " + email);
```

---

## ✅ COMPLETION CHECKLIST

Sau khi hoàn thành tất cả tasks, kiểm tra:

- [ ] LoginActivity sử dụng AuthViewModel
- [ ] SignupActivity sử dụng AuthViewModel  
- [ ] HomeActivity sử dụng WorkspaceViewModel
- [ ] ViewModelFactoryProvider được tạo và sử dụng
- [ ] Tất cả API calls trực tiếp đã bị xóa
- [ ] LiveData observers hoạt động đúng
- [ ] Loading states hiển thị
- [ ] Error handling hoạt động
- [ ] Navigation flow đúng
- [ ] Không crash khi rotate device
- [ ] No memory leaks
- [ ] Code clean, có comments

---

## 📞 HỖ TRỢ

**Nếu gặp lỗi compile:**
1. Check imports
2. Check package names
3. Sync Gradle
4. Clean & Rebuild project

**Nếu gặp runtime error:**
1. Check Logcat
2. Verify ViewModel initialization
3. Check LiveData observers
4. Verify API client setup

**Contacts:**
- Review code với Người 2 và Người 3
- Merge conflicts: prioritize ViewModel approach
- Testing: cross-test với team

---

**GOOD LUCK! 🚀 Let's build this clean architecture!**

