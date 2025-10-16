# HƯỚNG DẪN TEST REPOSITORY TRONG ACTIVITY

## 📱 CÁCH 1: SỬ DỤNG REPOSITORY TEST ACTIVITY (Đã tạo sẵn)

### Bước 1: Đăng ký Activity trong AndroidManifest.xml

Thêm vào file `AndroidManifest.xml`:

```xml
<activity
    android:name=".test.RepositoryTestActivity"
    android:label="Repository Test"
    android:exported="false" />
```

### Bước 2: Mở Test Activity

Có 2 cách để mở:

#### Cách A: Từ Activity khác (ví dụ MainActivity)
```java
// Thêm button trong MainActivity
Button btnOpenTest = findViewById(R.id.btnOpenTest);
btnOpenTest.setOnClickListener(v -> {
    Intent intent = new Intent(MainActivity.this, RepositoryTestActivity.class);
    startActivity(intent);
});
```

#### Cách B: Tạm thời set làm launcher activity
```xml
<!-- Trong AndroidManifest.xml -->
<activity
    android:name=".test.RepositoryTestActivity"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

### Bước 3: Chạy app và test

1. **Build & Run** app
2. Mở **RepositoryTestActivity**
3. Nhấn các button để test:
   - **Workspace** - Test WorkspaceRepository
   - **Project** - Test ProjectRepository
   - **Board** - Test BoardRepository
   - **Task** - Test TaskRepository
   - **Notification** - Test NotificationRepository
   - **Test All** - Test tất cả
4. Xem kết quả trong **Log Output** màn hình
5. Kiểm tra **Logcat** để xem chi tiết hơn (filter: `RepositoryTest`)

### Bước 4: Đọc kết quả

**Thành công:**
```
✓ getWorkspaces: SUCCESS
  → Tìm thấy 3 workspaces
  → Workspace đầu tiên: My Workspace
  → ID: abc-123-def
```

**Thất bại:**
```
✗ getWorkspaces: FAILED
  → Error: Network error: timeout
```

---

## 📱 CÁCH 2: TEST TRONG ACTIVITY HIỆN CÓ

Nếu bạn muốn test trực tiếp trong Activity hiện có (ví dụ: HomeActivity):

### Ví dụ 1: Test trong HomeActivity

```java
public class HomeActivity extends AppCompatActivity {
    
    private IWorkspaceRepository workspaceRepository;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        
        // Khởi tạo repository
        initRepository();
        
        // Test repository
        testGetWorkspaces();
    }
    
    private void initRepository() {
        // Tạo API Service
        WorkspaceApiService apiService = RetrofitClient.getInstance()
            .create(WorkspaceApiService.class);
        
        // Tạo Repository
        workspaceRepository = new WorkspaceRepositoryImpl(apiService);
    }
    
    private void testGetWorkspaces() {
        // Hiển thị loading
        showLoading();
        
        // Gọi repository
        workspaceRepository.getWorkspaces(
            new IWorkspaceRepository.RepositoryCallback<List<Workspace>>() {
                @Override
                public void onSuccess(List<Workspace> result) {
                    hideLoading();
                    
                    // Log kết quả
                    Log.d("HomeActivity", "✓ Lấy được " + result.size() + " workspaces");
                    
                    // Hiển thị lên UI
                    displayWorkspaces(result);
                    
                    // Hiển thị toast
                    Toast.makeText(HomeActivity.this, 
                        "Lấy được " + result.size() + " workspaces", 
                        Toast.LENGTH_SHORT).show();
                }
                
                @Override
                public void onError(String error) {
                    hideLoading();
                    
                    // Log lỗi
                    Log.e("HomeActivity", "✗ Lỗi: " + error);
                    
                    // Hiển thị lỗi
                    Toast.makeText(HomeActivity.this, 
                        "Lỗi: " + error, 
                        Toast.LENGTH_LONG).show();
                }
            }
        );
    }
    
    private void displayWorkspaces(List<Workspace> workspaces) {
        // TODO: Hiển thị lên RecyclerView hoặc ListView
    }
}
```

### Ví dụ 2: Test Task Repository trong TaskBoardActivity

```java
public class TaskBoardActivity extends AppCompatActivity {
    
    private ITaskRepository taskRepository;
    private String boardId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_board);
        
        // Lấy boardId từ Intent
        boardId = getIntent().getStringExtra("BOARD_ID");
        
        // Khởi tạo repository
        TaskApiService apiService = RetrofitClient.getInstance()
            .create(TaskApiService.class);
        taskRepository = new TaskRepositoryImpl(apiService);
        
        // Load tasks
        loadTasks();
    }
    
    private void loadTasks() {
        showLoading();
        
        taskRepository.getTasksByBoard(boardId, 
            new ITaskRepository.RepositoryCallback<List<Task>>() {
                @Override
                public void onSuccess(List<Task> result) {
                    hideLoading();
                    
                    Log.d("TaskBoard", "✓ Lấy được " + result.size() + " tasks");
                    
                    // Hiển thị tasks lên UI
                    displayTasks(result);
                }
                
                @Override
                public void onError(String error) {
                    hideLoading();
                    
                    Log.e("TaskBoard", "✗ Lỗi: " + error);
                    
                    showErrorDialog("Không thể tải tasks: " + error);
                }
            }
        );
    }
    
    // Test tạo task mới
    private void testCreateTask() {
        Task newTask = new Task(
            null, // id - sẽ được tạo bởi server
            boardId,
            "Test Task",
            "This is a test task",
            null, // assigneeId
            null, // createdBy
            null, // dueAt
            null, // startAt
            Priority.MEDIUM,
            0.0,  // position
            null, // issueKey
            IssueType.TASK,
            IssueStatus.TO_DO,
            null, // sprintId
            null, // epicId
            null, // parentTaskId
            null, // storyPoints
            null, // originalEstimateSec
            null, // remainingEstimateSec
            new Date(), // createdAt
            new Date(), // updatedAt
            null  // deletedAt
        );
        
        taskRepository.createTask(boardId, newTask, 
            new ITaskRepository.RepositoryCallback<Task>() {
                @Override
                public void onSuccess(Task result) {
                    Log.d("TaskBoard", "✓ Tạo task thành công: " + result.getId());
                    
                    Toast.makeText(TaskBoardActivity.this, 
                        "Đã tạo task: " + result.getTitle(), 
                        Toast.LENGTH_SHORT).show();
                    
                    // Reload tasks
                    loadTasks();
                }
                
                @Override
                public void onError(String error) {
                    Log.e("TaskBoard", "✗ Không thể tạo task: " + error);
                    
                    showErrorDialog("Lỗi tạo task: " + error);
                }
            }
        );
    }
}
```

### Ví dụ 3: Test Notification Repository

```java
public class NotificationActivity extends AppCompatActivity {
    
    private INotificationRepository notificationRepository;
    private TextView tvUnreadCount;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        
        tvUnreadCount = findViewById(R.id.tvUnreadCount);
        
        // Khởi tạo repository
        NotificationApiService apiService = RetrofitClient.getInstance()
            .create(NotificationApiService.class);
        notificationRepository = new NotificationRepositoryImpl(apiService);
        
        // Load notifications
        loadNotifications();
        loadUnreadCount();
    }
    
    private void loadNotifications() {
        notificationRepository.getNotifications(
            new INotificationRepository.RepositoryCallback<List<Notification>>() {
                @Override
                public void onSuccess(List<Notification> result) {
                    Log.d("Notification", "✓ Lấy được " + result.size() + " notifications");
                    
                    // Hiển thị lên RecyclerView
                    displayNotifications(result);
                }
                
                @Override
                public void onError(String error) {
                    Log.e("Notification", "✗ Lỗi: " + error);
                }
            }
        );
    }
    
    private void loadUnreadCount() {
        notificationRepository.getUnreadCount(
            new INotificationRepository.RepositoryCallback<Integer>() {
                @Override
                public void onSuccess(Integer result) {
                    Log.d("Notification", "✓ Có " + result + " notifications chưa đọc");
                    
                    // Hiển thị badge
                    tvUnreadCount.setText(String.valueOf(result));
                    tvUnreadCount.setVisibility(result > 0 ? View.VISIBLE : View.GONE);
                }
                
                @Override
                public void onError(String error) {
                    Log.e("Notification", "✗ Lỗi: " + error);
                }
            }
        );
    }
    
    // Mark notification as read
    private void markAsRead(String notificationId) {
        notificationRepository.markAsRead(notificationId, 
            new INotificationRepository.RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Log.d("Notification", "✓ Đã đánh dấu đã đọc");
                    
                    // Reload
                    loadUnreadCount();
                }
                
                @Override
                public void onError(String error) {
                    Log.e("Notification", "✗ Lỗi: " + error);
                }
            }
        );
    }
}
```

---

## 🔍 DEBUG & TROUBLESHOOTING

### Vấn đề 1: Network Error
**Triệu chứng:**
```
✗ Error: Network error: Unable to resolve host
```

**Giải pháp:**
1. Kiểm tra kết nối internet
2. Kiểm tra BASE_URL trong RetrofitClient
3. Kiểm tra permission INTERNET trong AndroidManifest
4. Thử ping backend API

### Vấn đề 2: 401 Unauthorized
**Triệu chứng:**
```
✗ Error: Failed to fetch: 401
```

**Giải pháp:**
1. Kiểm tra token đã được lưu chưa
2. Kiểm tra token có hết hạn không
3. Kiểm tra Interceptor có add token vào header không
4. Đăng nhập lại để lấy token mới

### Vấn đề 3: Null Response
**Triệu chứng:**
```
✗ Error: Response body is null
```

**Giải pháp:**
1. Kiểm tra API endpoint có đúng không
2. Kiểm tra DTO mapping có đúng với API response không
3. Xem response raw trong Logcat
4. Kiểm tra Gson converter

### Vấn đề 4: Mapper Error
**Triệu chứng:**
```
NullPointerException in Mapper
```

**Giải pháp:**
1. Kiểm tra DTO có null fields không
2. Thêm null check trong Mapper
3. Đảm bảo DateFormat đúng format với API

---

## 📊 KIỂM TRA KẾT QUẢ

### Trong Logcat
Filter: `RepositoryTest` hoặc tag của bạn

**Thành công:**
```
D/RepositoryTest: ✓ getWorkspaces: SUCCESS
D/RepositoryTest:   → Tìm thấy 3 workspaces
```

**Thất bại:**
```
E/RepositoryTest: ✗ getWorkspaces: FAILED
E/RepositoryTest:   → Error: Network timeout
```

### Trong Network Profiler
1. Mở **View** → **Tool Windows** → **Profiler**
2. Chọn **Network**
3. Xem request/response chi tiết
4. Kiểm tra HTTP status code

---

## ✅ CHECKLIST TEST

- [ ] Đã thêm RepositoryTestActivity vào AndroidManifest
- [ ] Đã build & run app thành công
- [ ] Test WorkspaceRepository → ✓ SUCCESS
- [ ] Test ProjectRepository → ✓ SUCCESS
- [ ] Test BoardRepository → ✓ SUCCESS
- [ ] Test TaskRepository → ✓ SUCCESS
- [ ] Test NotificationRepository → ✓ SUCCESS
- [ ] Không có lỗi trong Logcat
- [ ] Network requests thành công (200 OK)
- [ ] Data được map đúng từ DTO → Domain Model

---

## 🎯 BƯỚC TIẾP THEO

Sau khi test thành công các Repository:

1. **✅ Phase 2 hoàn thành** - Data Layer đã sẵn sàng
2. **→ Phase 3** - Tạo UseCases để xử lý business logic
3. **→ Phase 4** - Tạo ViewModels để kết nối với UI
4. **→ Phase 5** - Refactor Activities/Fragments để sử dụng architecture mới

---

**Document này được tạo tự động bởi AI Assistant**
**Ngày: 10/10/2025**

