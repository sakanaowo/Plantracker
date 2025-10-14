# PHASE 6: HOÀN THIỆN TÍCH HỢP UI & DEMO SẢN PHẨM
**Ngày thực hiện:** 16/10/2025 (Buổi sáng - 4 giờ)  
**Mục tiêu:** Demo được các chức năng cơ bản: Auth, Workspace, Project, Board, Task

---

## 📊 TÌNH TRẠNG HIỆN TẠI

### ✅ ĐÃ HOÀN THÀNH (Phase 1-5)
- ✅ **Domain Layer**: Models, UseCases (100%)
- ✅ **Data Layer**: Repositories, API Services (100%)
- ✅ **Presentation Layer**: 7 ViewModels + Factories (100%)
- ✅ **UI Adapters**: TaskAdapter, WorkspaceAdapter, ListProjectAdapter (100%)
- ✅ **Architecture**: Clean Architecture setup hoàn chỉnh

### ⚠️ CẦN HOÀN THIỆN (Phase 6)
- ⚠️ **Activities Integration**: Chưa tích hợp ViewModels vào Activities
- ⚠️ **Navigation Flow**: Chưa có flow hoàn chỉnh giữa các màn hình
- ⚠️ **API Endpoints**: Thiếu một số endpoints cần thiết
- ⚠️ **Error Handling**: Chưa có UI feedback đầy đủ

---

## 🎯 MỤC TIÊU PHASE 6

### Demo Flow Hoàn Chỉnh:
```
1. Login/Signup → HomeActivity
2. HomeActivity → Workspace List
3. Select Workspace → Project List
4. Select Project → Board Tabs (TO DO, IN PROGRESS, DONE)
5. View Tasks in Board → Task Details
6. Create/Edit/Delete Task
```

### Tính năng cần demo:
- ✅ Authentication (Login/Signup/Logout)
- ✅ Workspace Management (List, View)
- ✅ Project Management (List, Create, View)
- ✅ Board Management (3 tabs mặc định)
- ✅ Task Management (CRUD operations)

---

## 📋 PHÂN TÍCH API ENDPOINTS

### Endpoints Backend Đã Có:

#### 1. **Users** ✅
- `POST /api/users/local/signup` - Đăng ký
- `POST /api/users/local/signin` - Đăng nhập
- `POST /api/users/firebase/auth` - Google Sign In
- `GET /api/users/me` - Get profile
- `PUT /api/users/me` - Update profile

#### 2. **Workspaces** ✅
- `POST /api/workspaces/users/{userId}/personal` - Tạo workspace cá nhân
- `POST /api/workspaces` - Tạo workspace
- `GET /api/workspaces` - Lấy danh sách workspace
- `GET /api/workspaces/{id}` - Chi tiết workspace
- `PATCH /api/workspaces/{id}` - Cập nhật workspace
- `DELETE /api/workspaces/{id}` - Xóa workspace

#### 3. **Projects** ✅
- `GET /api/projects?workspaceId={id}` - Lấy projects của workspace
- `POST /api/projects` - Tạo project
- `PATCH /api/projects/{id}` - Cập nhật project

#### 4. **Boards** ✅
- `GET /api/boards?projectId={id}` - Lấy boards của project
- `POST /api/boards` - Tạo board
- `PATCH /api/boards/{id}` - Cập nhật board
- `DELETE /api/boards/{id}` - Xóa board

#### 5. **Tasks** ✅
- `GET /api/tasks/by-board/{boardId}` - Lấy tasks của board
- `GET /api/tasks/{id}` - Chi tiết task
- `POST /api/tasks` - Tạo task
- `POST /api/tasks/{id}/move` - Di chuyển task
- `PATCH /api/tasks/{id}` - Cập nhật task
- `DELETE /api/tasks/{id}` - Xóa task

#### 6. **Timers** ✅ (Bonus)
- `POST /api/timers/start` - Bắt đầu timer
- `PATCH /api/timers/{timerId}/stop` - Dừng timer

### ❌ Endpoints CẦN BỔ SUNG (Backend)

Dựa trên schema.prisma, các endpoints sau CẦN THÊM:

#### 1. **Project Members** (Quan trọng)
```
GET  /api/projects/{id}/members        - Lấy danh sách thành viên project
POST /api/projects/{id}/members        - Thêm thành viên vào project
DELETE /api/projects/{id}/members/{userId} - Xóa thành viên
```

#### 2. **Task Comments** (Quan trọng cho demo)
```
GET  /api/tasks/{id}/comments          - Lấy comments của task
POST /api/tasks/{id}/comments          - Thêm comment
DELETE /api/comments/{id}              - Xóa comment
```

#### 3. **Task Attachments** (Bonus)
```
GET  /api/tasks/{id}/attachments       - Lấy attachments
POST /api/tasks/{id}/attachments       - Thêm attachment
DELETE /api/attachments/{id}           - Xóa attachment
```

#### 4. **Checklists** (Bonus)
```
GET  /api/tasks/{id}/checklists        - Lấy checklists
POST /api/tasks/{id}/checklists        - Tạo checklist
PATCH /api/checklists/{id}/items/{itemId} - Toggle checklist item
DELETE /api/checklists/{id}            - Xóa checklist
```

#### 5. **Labels** (Bonus)
```
GET  /api/workspaces/{id}/labels       - Lấy labels của workspace
POST /api/workspaces/{id}/labels       - Tạo label
PATCH /api/labels/{id}                 - Cập nhật label
DELETE /api/labels/{id}                - Xóa label
POST /api/tasks/{id}/labels/{labelId}  - Gắn label vào task
DELETE /api/tasks/{id}/labels/{labelId} - Bỏ label khỏi task
```

---

## 🎨 FRONTEND: ACTIVITIES CẦN TÍCH HỢP

### Danh sách Activities hiện tại:
```
feature/auth/ui/login/LoginActivity.java           ⚠️ Chưa tích hợp ViewModel
feature/auth/ui/signup/SignupActivity.java         ⚠️ Chưa tích hợp ViewModel
feature/home/ui/HomeActivity.java                  ⚠️ Chưa tích hợp ViewModel
feature/home/ui/WorkspaceActivity.java             ⚠️ Chưa tích hợp ViewModel
feature/home/ui/Home/project/ProjectActivity.java  ⚠️ Chưa tích hợp ViewModel
feature/home/ui/InboxActivity.java                 ⚠️ Chưa tích hợp ViewModel
```

---

## 📅 KÊ HOẠCH TRIỂN KHAI CHI TIẾT - 4 GIỜ

### ⏰ THỜI GIAN BIỂU

| Thời gian | Người 1 | Người 2 | Người 3 |
|-----------|---------|---------|---------|
| **08:00-09:30** (1.5h) | LoginActivity + SignupActivity | WorkspaceActivity | ProjectActivity Setup |
| **09:30-11:00** (1.5h) | HomeActivity + Navigation | Project CRUD UI | Task CRUD UI |
| **11:00-12:00** (1h) | Testing & Bug Fix | Testing & Bug Fix | Testing & Bug Fix |

---

## 👤 NGƯỜI 1: AUTHENTICATION & HOME (Critical Path)

### **Task 1.1: LoginActivity Integration** (45 phút)

**File:** `feature/auth/ui/login/LoginActivity.java`

**Công việc:**
1. ✅ Setup AuthViewModel với Factory
2. ✅ Observe LiveData (loading, error, currentUser)
3. ✅ Replace API call với `authViewModel.login(email, password)`
4. ✅ Navigate to HomeActivity khi login thành công
5. ✅ Handle errors (show Toast/Snackbar)

**Code cần thêm:**
```java
// 1. Declare ViewModel
private AuthViewModel authViewModel;

// 2. Setup ViewModel in onCreate()
private void setupViewModel() {
    IAuthRepository authRepository = new AuthRepositoryImpl(
        ApiClient.get(App.authManager).create(AuthApi.class),
        App.authManager
    );
    
    AuthViewModelFactory factory = new AuthViewModelFactory(
        new LoginUseCase(authRepository),
        new LogoutUseCase(authRepository),
        new GetCurrentUserUseCase(authRepository),
        new IsLoggedInUseCase(authRepository)
    );
    
    authViewModel = new ViewModelProvider(this, factory).get(AuthViewModel.class);
}

// 3. Observe LiveData
private void observeViewModel() {
    authViewModel.isLoading().observe(this, isLoading -> {
        btnLogin.setEnabled(!isLoading);
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    });
    
    authViewModel.getCurrentUser().observe(this, user -> {
        if (user != null) {
            navigateToHome();
        }
    });
    
    authViewModel.getError().observe(this, error -> {
        if (error != null) {
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        }
    });
}

// 4. Replace API call
private void attemptLogin() {
    String email = etEmail.getText().toString().trim();
    String password = etPassword.getText().toString();
    
    // Validation
    if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
        return;
    }
    
    // Call ViewModel
    authViewModel.login(email, password);
}

// 5. Navigation
private void navigateToHome() {
    Intent intent = new Intent(this, HomeActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
    finish();
}
```

### **Task 1.2: SignupActivity Integration** (45 phút)

**File:** `feature/auth/ui/signup/SignupActivity.java`

**Công việc:** (Tương tự LoginActivity)
1. ✅ Setup AuthViewModel
2. ✅ Observe LiveData
3. ✅ Implement signup flow
4. ✅ Navigate to HomeActivity sau khi signup thành công

**Note:** Có thể cần thêm SignupUseCase nếu chưa có

### **Task 1.3: HomeActivity Integration** (1 giờ)

**File:** `feature/home/ui/HomeActivity.java`

**Công việc:**
1. ✅ Setup WorkspaceViewModel
2. ✅ Load user's workspaces
3. ✅ Navigate to WorkspaceActivity khi chọn workspace
4. ✅ Handle logout action
5. ✅ Bottom navigation setup

**Code cần thêm:**
```java
private WorkspaceViewModel workspaceViewModel;
private AuthViewModel authViewModel;

private void setupViewModels() {
    // WorkspaceViewModel
    workspaceViewModel = new ViewModelProvider(this,
        ViewModelFactoryProvider.provideWorkspaceViewModelFactory()
    ).get(WorkspaceViewModel.class);
    
    // AuthViewModel for logout
    authViewModel = new ViewModelProvider(this,
        ViewModelFactoryProvider.provideAuthViewModelFactory()
    ).get(AuthViewModel.class);
}

private void observeViewModels() {
    // Observe workspaces
    workspaceViewModel.getWorkspaces().observe(this, workspaces -> {
        if (workspaces != null && !workspaces.isEmpty()) {
            // Show workspace list or redirect to first workspace
            navigateToWorkspace(workspaces.get(0).getId());
        }
    });
    
    // Observe logout
    authViewModel.isLoggedIn().observe(this, isLoggedIn -> {
        if (isLoggedIn != null && !isLoggedIn) {
            navigateToLogin();
        }
    });
}

private void loadWorkspaces() {
    workspaceViewModel.loadWorkspaces();
}
```

---

## 👤 NGƯỜI 2: WORKSPACE & PROJECT MANAGEMENT

### **Task 2.1: WorkspaceActivity Integration** (1 giờ)

**File:** `feature/home/ui/WorkspaceActivity.java`

**Hiện trạng:** Đã có WorkspaceAdapter, cần tích hợp ViewModel

**Công việc:**
1. ✅ Setup WorkspaceViewModel (đã có sẵn)
2. ✅ Setup ProjectViewModel
3. ✅ Load projects của workspace
4. ✅ Handle create project action
5. ✅ Navigate to ProjectActivity khi click project

**Code đã có (cần verify):**
```java
// ViewModel setup
workspaceViewModel = new ViewModelProvider(this,
    ViewModelFactoryProvider.provideWorkspaceViewModelFactory()
).get(WorkspaceViewModel.class);

// Load projects
workspaceViewModel.getProjects().observe(this, projects -> {
    if (projects != null && !projects.isEmpty()) {
        workspaceAdapter.setProjectList(projects);
    }
});

workspaceViewModel.loadProjectsForWorkspace(workspaceId);
```

**Cần thêm:**
```java
// Create project
private void showCreateProjectDialog() {
    // ... get project name from dialog
    
    Project newProject = new Project(
        "", // id will be generated by backend
        projectName,
        "", // description
        "", // key (auto-generate)
        workspaceId,
        "KANBAN" // default board type
    );
    
    projectViewModel.createProject(newProject);
}

// Observe create result
projectViewModel.getCurrentProject().observe(this, project -> {
    if (project != null) {
        workspaceAdapter.addProject(project);
        Toast.makeText(this, "Project created!", Toast.LENGTH_SHORT).show();
    }
});

// Navigate to project
private void navigateToProject(String projectId) {
    Intent intent = new Intent(this, ProjectActivity.class);
    intent.putExtra("PROJECT_ID", projectId);
    intent.putExtra("WORKSPACE_ID", workspaceId);
    startActivity(intent);
}
```

### **Task 2.2: ProjectActivity - Board Setup** (1.5 giờ)

**File:** `feature/home/ui/Home/project/ProjectActivity.java`

**Công việc:**
1. ✅ Setup ProjectViewModel, BoardViewModel
2. ✅ Load project details
3. ✅ Load boards (hoặc tạo 3 boards mặc định nếu chưa có)
4. ✅ Setup ViewPager2 với ListProjectAdapter
5. ✅ Tab layout (TO DO, IN PROGRESS, DONE)

**Code cần thêm:**
```java
private ProjectViewModel projectViewModel;
private BoardViewModel boardViewModel;
private TaskViewModel taskViewModel;
private ViewPager2 viewPager;
private TabLayout tabLayout;
private ListProjectAdapter pagerAdapter;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_project);
    
    // Get intent data
    projectId = getIntent().getStringExtra("PROJECT_ID");
    workspaceId = getIntent().getStringExtra("WORKSPACE_ID");
    
    // Setup ViewModels
    setupViewModels();
    
    // Setup UI
    setupViewPager();
    setupTabLayout();
    
    // Load data
    loadProjectData();
}

private void setupViewModels() {
    projectViewModel = new ViewModelProvider(this,
        ViewModelFactoryProvider.provideProjectViewModelFactory()
    ).get(ProjectViewModel.class);
    
    boardViewModel = new ViewModelProvider(this,
        ViewModelFactoryProvider.provideBoardViewModelFactory()
    ).get(BoardViewModel.class);
    
    taskViewModel = new ViewModelProvider(this,
        ViewModelFactoryProvider.provideTaskViewModelFactory()
    ).get(TaskViewModel.class);
}

private void setupViewPager() {
    viewPager = findViewById(R.id.viewPager);
    
    // Create adapter with project ID
    pagerAdapter = new ListProjectAdapter(this, projectId);
    viewPager.setAdapter(pagerAdapter);
}

private void setupTabLayout() {
    tabLayout = findViewById(R.id.tabLayout);
    
    new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
        switch (position) {
            case 0: tab.setText("TO DO"); break;
            case 1: tab.setText("IN PROGRESS"); break;
            case 2: tab.setText("DONE"); break;
        }
    }).attach();
}

private void loadProjectData() {
    // Load project details
    projectViewModel.loadProject(projectId);
    
    // Load boards for this project
    boardViewModel.loadBoardsForProject(projectId);
}

private void observeViewModels() {
    // Observe project
    projectViewModel.getCurrentProject().observe(this, project -> {
        if (project != null) {
            setTitle(project.getName());
        }
    });
    
    // Observe boards
    boardViewModel.getBoards().observe(this, boards -> {
        if (boards != null) {
            if (boards.isEmpty()) {
                // Create default 3 boards
                createDefaultBoards();
            } else {
                // Update adapter with board IDs
                List<String> boardIds = new ArrayList<>();
                for (Board board : boards) {
                    boardIds.add(board.getId());
                }
                pagerAdapter.setBoardIds(boardIds);
            }
        }
    });
}

private void createDefaultBoards() {
    String[] boardNames = {"TO DO", "IN PROGRESS", "DONE"};
    
    for (int i = 0; i < boardNames.length; i++) {
        Board board = new Board(
            "", // id
            projectId,
            boardNames[i],
            i // order
        );
        boardViewModel.createBoard(board);
    }
}
```

---

## 👤 NGƯỜI 3: TASK MANAGEMENT & UI

### **Task 3.1: ListProject Fragment - Task Display** (1 giờ)

**File:** `feature/home/ui/Home/project/ListProject.java`

**Hiện trạng:** Đã có TaskViewModel, TaskAdapter, cần hoàn thiện UI

**Công việc:**
1. ✅ Verify TaskViewModel integration (đã làm ở Phase 5)
2. ✅ Load tasks for boardId
3. ✅ Display tasks in RecyclerView
4. ✅ Handle empty state
5. ✅ Pull to refresh

**Code cần verify:**
```java
private void loadTasksForBoard() {
    if (boardId != null && !boardId.isEmpty()) {
        taskViewModel.loadTasksForBoard(boardId);
    }
}

private void observeViewModel() {
    taskViewModel.getTasks().observe(getViewLifecycleOwner(), tasks -> {
        if (tasks != null) {
            taskAdapter.updateTasks(tasks);
            
            // Handle empty state
            if (tasks.isEmpty()) {
                emptyView.setVisibility(View.VISIBLE);
                emptyView.setText(getEmptyMessage());
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }
    });
    
    taskViewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    });
}
```

### **Task 3.2: Task CRUD Operations** (1.5 giờ)

**Files:**
- `ListProject.java` (Fragment)
- `TaskDetailBottomSheet.java` (BottomSheet Dialog)

**Công việc:**

#### 3.2.1: Create Task
```java
// In ListProject.java
private void showCreateTaskDialog() {
    TaskDetailBottomSheet bottomSheet = TaskDetailBottomSheet.newInstance(
        null, // no task (create mode)
        boardId,
        projectId
    );
    
    bottomSheet.setOnTaskSavedListener(task -> {
        taskViewModel.createTask(task);
    });
    
    bottomSheet.show(getParentFragmentManager(), "CREATE_TASK");
}

// Observe create result
taskViewModel.getCurrentTask().observe(this, task -> {
    if (task != null) {
        // Task created successfully
        loadTasksForBoard(); // Reload list
    }
});
```

#### 3.2.2: View/Edit Task
```java
// In TaskAdapter
taskAdapter.setOnTaskClickListener(task -> {
    showTaskDetailBottomSheet(task);
});

private void showTaskDetailBottomSheet(Task task) {
    TaskDetailBottomSheet bottomSheet = TaskDetailBottomSheet.newInstance(
        task,
        boardId,
        projectId
    );
    
    bottomSheet.setOnTaskSavedListener(updatedTask -> {
        taskViewModel.updateTask(updatedTask);
    });
    
    bottomSheet.setOnTaskDeletedListener(deletedTask -> {
        taskViewModel.deleteTask(deletedTask.getId());
    });
    
    bottomSheet.show(getParentFragmentManager(), "TASK_DETAIL");
}
```

#### 3.2.3: TaskDetailBottomSheet.java (CẦN TẠO MỚI)

**File mới:** `feature/home/ui/Home/project/TaskDetailBottomSheet.java`

```java
public class TaskDetailBottomSheet extends BottomSheetDialogFragment {
    private Task task; // null nếu create mode
    private String boardId;
    private String projectId;
    
    private EditText etTitle;
    private EditText etDescription;
    private Button btnSave;
    private Button btnDelete;
    
    private OnTaskSavedListener onTaskSavedListener;
    private OnTaskDeletedListener onTaskDeletedListener;
    
    public static TaskDetailBottomSheet newInstance(Task task, String boardId, String projectId) {
        TaskDetailBottomSheet fragment = new TaskDetailBottomSheet();
        Bundle args = new Bundle();
        if (task != null) {
            args.putString("TASK_ID", task.getId());
            args.putString("TITLE", task.getTitle());
            args.putString("DESCRIPTION", task.getDescription());
        }
        args.putString("BOARD_ID", boardId);
        args.putString("PROJECT_ID", projectId);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_task_detail, container, false);
        
        // Init views
        etTitle = view.findViewById(R.id.etTitle);
        etDescription = view.findViewById(R.id.etDescription);
        btnSave = view.findViewById(R.id.btnSave);
        btnDelete = view.findViewById(R.id.btnDelete);
        
        // Get arguments
        if (getArguments() != null) {
            boardId = getArguments().getString("BOARD_ID");
            projectId = getArguments().getString("PROJECT_ID");
            
            String taskId = getArguments().getString("TASK_ID");
            if (taskId != null) {
                // Edit mode
                etTitle.setText(getArguments().getString("TITLE"));
                etDescription.setText(getArguments().getString("DESCRIPTION"));
                btnDelete.setVisibility(View.VISIBLE);
            } else {
                // Create mode
                btnDelete.setVisibility(View.GONE);
            }
        }
        
        // Setup listeners
        btnSave.setOnClickListener(v -> saveTask());
        btnDelete.setOnClickListener(v -> deleteTask());
        
        return view;
    }
    
    private void saveTask() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(getContext(), "Title required", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Task newTask = new Task(
            task != null ? task.getId() : "",
            projectId,
            boardId,
            title,
            description,
            null, // assigneeId
            null, // dueAt
            null, // priority
            0,    // position
            null, // issueKey
            null, // type
            TaskStatus.TO_DO
        );
        
        if (onTaskSavedListener != null) {
            onTaskSavedListener.onTaskSaved(newTask);
        }
        
        dismiss();
    }
    
    private void deleteTask() {
        if (task != null && onTaskDeletedListener != null) {
            onTaskDeletedListener.onTaskDeleted(task);
        }
        dismiss();
    }
    
    public void setOnTaskSavedListener(OnTaskSavedListener listener) {
        this.onTaskSavedListener = listener;
    }
    
    public void setOnTaskDeletedListener(OnTaskDeletedListener listener) {
        this.onTaskDeletedListener = listener;
    }
    
    public interface OnTaskSavedListener {
        void onTaskSaved(Task task);
    }
    
    public interface OnTaskDeletedListener {
        void onTaskDeleted(Task task);
    }
}
```

**Layout mới:** `res/layout/bottom_sheet_task_detail.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="16dp">

    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Task Details"
        android:textSize="20sp"
        android:textStyle="bold"
        android:layout_marginBottom="16dp"/>

    <com.google.android.material.textfield.TextInputLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Title"
        android:layout_marginBottom="8dp">
        
        <com.google.android.material.textfield.TextInputEditText
            android:id="@+id/etTitle"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:inputType="text"/>
    </com.google.android.material.textfield.TextInputLayout>

    <com.google.android.material.textfield.TextInputLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Description"
        android:layout_marginBottom="16dp">
        
        <com.google.android.material.textfield.TextInputEditText
            android:id="@+id/etDescription"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:inputType="textMultiLine"
            android:minLines="3"/>
    </com.google.android.material.textfield.TextInputLayout>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal">

        <Button
            android:id="@+id/btnSave"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Save"
            android:layout_marginEnd="8dp"/>

        <Button
            android:id="@+id/btnDelete"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Delete"
            android:backgroundTint="@android:color/holo_red_dark"
            android:visibility="gone"/>
    </LinearLayout>

</LinearLayout>
```

---

## 🧪 TESTING CHECKLIST (1 giờ cuối)

### Tất cả 3 người cùng test:

#### ✅ Auth Flow
- [ ] Signup với email mới
- [ ] Login với tài khoản vừa tạo
- [ ] Logout
- [ ] Login lại
- [ ] Google Sign In (nếu có setup)

#### ✅ Workspace Flow
- [ ] Tự động tạo personal workspace
- [ ] Hiển thị workspace list
- [ ] Navigate vào workspace

#### ✅ Project Flow
- [ ] Tạo project mới
- [ ] Hiển thị project list
- [ ] Navigate vào project
- [ ] Xem project details

#### ✅ Board & Task Flow
- [ ] Tự động tạo 3 boards (TO DO, IN PROGRESS, DONE)
- [ ] Tab navigation hoạt động
- [ ] Tạo task mới trong TO DO
- [ ] Xem task details
- [ ] Edit task
- [ ] Delete task
- [ ] Tạo task trong IN PROGRESS
- [ ] Tạo task trong DONE

#### ✅ Error Handling
- [ ] Login với sai password → Show error
- [ ] Tạo task không có title → Show error
- [ ] Network error → Show error

---

## 🐛 COMMON ISSUES & SOLUTIONS

### Issue 1: NullPointerException khi load tasks
**Solution:**
```java
// Luôn check null trước khi sử dụng
if (boardId != null && !boardId.isEmpty()) {
    taskViewModel.loadTasksForBoard(boardId);
}
```

### Issue 2: RecyclerView không update
**Solution:**
```java
// Đảm bảo gọi notifyDataSetChanged() hoặc dùng DiffUtil
taskAdapter.updateTasks(tasks);
taskAdapter.notifyDataSetChanged();
```

### Issue 3: ViewModel bị reset khi rotate màn hình
**Solution:**
```java
// Sử dụng ViewModelProvider với activity scope
new ViewModelProvider(requireActivity(), factory).get(TaskViewModel.class);
```

### Issue 4: Backend API trả về 401 Unauthorized
**Solution:**
```java
// Check token trong AuthManager
Log.d("DEBUG", "Current token: " + App.authManager.getIdToken());
// Verify ApiClient có inject AuthManager đúng không
```

---

## 📝 DELIVERABLES

### Sau 4 giờ, cần có:

1. ✅ **Working Demo** với flow hoàn chỉnh:
   - Login → Home → Workspace → Project → Tasks

2. ✅ **3 Activities đã tích hợp ViewModels:**
   - LoginActivity
   - WorkspaceActivity
   - ProjectActivity

3. ✅ **Task CRUD hoàn chỉnh:**
   - Create task
   - View task details
   - Edit task
   - Delete task

4. ✅ **Code clean và có comments**

5. ✅ **Testing report** (danh sách test cases đã pass)

---

## 🚀 NEXT STEPS (Sau Phase 6)

### Phase 7 - Polish & Advanced Features (Tuần sau):
1. Task assignment (assign user vào task)
2. Task move between boards (drag & drop)
3. Task comments
4. Task attachments
5. Labels & filtering
6. Timer integration
7. Notifications
8. Search functionality

### Phase 8 - Performance & UX:
1. Offline support (Room database)
2. Caching strategy
3. Pull to refresh
4. Pagination
5. Image loading optimization
6. Animation & transitions

---

## 📞 SUPPORT & COMMUNICATION

### Nếu gặp vấn đề:
1. Check error logs trong Logcat
2. Verify backend API response (dùng Postman)
3. Check ViewModel LiveData values
4. Ask team members
5. Review Phase 5 documentation

### Daily standup (9:00 AM):
- What did you complete yesterday?
- What will you work on today?
- Any blockers?

---

**Good luck! Let's build an awesome demo! 🚀**

