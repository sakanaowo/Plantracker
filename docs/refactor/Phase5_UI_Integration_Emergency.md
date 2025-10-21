# PHASE 5: UI INTEGRATION - EMERGENCY TASK (TỐI NAY)
**Ngày thực hiện:** 14/10/2025 (Tối - 3-4 giờ)  
**Mục tiêu:** Tích hợp ViewModels vào UI Activities hiện có

---

## 🎯 TỔNG QUAN

**Công việc:** Refactor các Activity hiện có để sử dụng ViewModels thay vì gọi API trực tiếp

**Hiện trạng:**
- ✅ Có 7 ViewModels + 7 Factories đã hoàn thành
- ⚠️ UI Activities đang gọi API trực tiếp (cần refactor)
- ⚠️ Chưa có dependency injection setup

**Kế hoạch:**
1. Setup Factory instances cho từng Activity
2. Replace API calls bằng ViewModel methods
3. Observe LiveData và update UI

---

## 📊 PHÂN CÔNG CHO 3 NGƯỜI

| Người | Activities | ViewModels sử dụng | Độ ưu tiên | Thời gian |
|-------|-----------|-------------------|------------|-----------|
| **Người 1** | LoginActivity<br>SignupActivity<br>HomeActivity | AuthViewModel<br>WorkspaceViewModel | 🔴 CAO | 2.5 giờ |
| **Người 2** | WorkspaceActivity<br>ProjectActivity | WorkspaceViewModel<br>ProjectViewModel<br>BoardViewModel | 🟡 TRUNG | 2.5 giờ |
| **Người 3** | ProjectActivity (Tasks)<br>InboxActivity | TaskViewModel<br>NotificationViewModel | 🟡 TRUNG | 2.5 giờ |

---

## 👤 NGƯỜI 1: AUTH & HOME (CRITICAL PATH)

### **Task 1.1: LoginActivity Integration** ⭐⭐⭐

**File:** `feature/auth/ui/login/LoginActivity.java`

**Hiện trạng:**
```java
// Đang gọi API trực tiếp
AuthApi authApi = ApiClient.get(App.authManager).create(AuthApi.class);
Call<LoginResponse> call = authApi.login(new LoginRequest(email, password));
```

**Cần làm:**

#### **Bước 1: Setup AuthViewModel (15 phút)**

```java
public class LoginActivity extends AppCompatActivity {
    private AuthViewModel authViewModel;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        // Setup ViewModel
        setupViewModel();
        observeViewModel();
        
        // ... existing code ...
    }
    
    private void setupViewModel() {
        // Tạo UseCases
        IAuthRepository authRepository = new AuthRepositoryImpl(
            ApiClient.get(App.authManager).create(AuthApi.class),
            App.authManager
        );
        
        LoginUseCase loginUseCase = new LoginUseCase(authRepository);
        LogoutUseCase logoutUseCase = new LogoutUseCase(authRepository);
        GetCurrentUserUseCase getCurrentUserUseCase = new GetCurrentUserUseCase(authRepository);
        IsLoggedInUseCase isLoggedInUseCase = new IsLoggedInUseCase(authRepository);
        
        // Tạo Factory
        AuthViewModelFactory factory = new AuthViewModelFactory(
            loginUseCase,
            logoutUseCase,
            getCurrentUserUseCase,
            isLoggedInUseCase
        );
        
        // Tạo ViewModel
        authViewModel = new ViewModelProvider(this, factory).get(AuthViewModel.class);
    }
}
```

#### **Bước 2: Observe LiveData (15 phút)**

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
            Toast.makeText(this, "Welcome " + user.getName(), Toast.LENGTH_SHORT).show();
            // Navigate to HomeActivity
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
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

#### **Bước 3: Replace API call với ViewModel (10 phút)**

```java
private void attemptLogin() {
    String email = etEmail.getText().toString().trim();
    String password = etPassword.getText().toString();
    
    // Validate
    if (TextUtils.isEmpty(email)) {
        Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
        return;
    }
    
    if (TextUtils.isEmpty(password)) {
        Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
        return;
    }
    
    // THAY THẾ: Từ API call -> ViewModel
    // BEFORE: authApi.login(new LoginRequest(email, password));
    // AFTER:
    authViewModel.login(email, password);
}
```

**Thời gian:** 40 phút

---

### **Task 1.2: SignupActivity Integration** ⭐⭐

**File:** `feature/auth/ui/signup/SignupActivity.java`

**Cần làm:** Tương tự LoginActivity nhưng đơn giản hơn

**Thời gian:** 30 phút

---

### **Task 1.3: HomeActivity Integration** ⭐⭐⭐

**File:** `feature/home/ui/Home/HomeActivity.java`

**Chức năng:** Load danh sách workspaces khi mở app

#### **Bước 1: Setup WorkspaceViewModel (20 phút)**

```java
public class HomeActivity extends AppCompatActivity {
    private WorkspaceViewModel workspaceViewModel;
    private RecyclerView recyclerView;
    private WorkspaceAdapter adapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        
        setupViewModel();
        setupRecyclerView();
        observeViewModel();
        
        // Load workspaces
        workspaceViewModel.loadWorkspaces();
    }
    
    private void setupViewModel() {
        // Tạo repository
        IWorkspaceRepository repository = new WorkspaceRepositoryImpl(
            ApiClient.get(App.authManager).create(WorkspaceApiService.class)
        );
        
        // Tạo UseCases
        GetWorkspacesUseCase getWorkspacesUseCase = new GetWorkspacesUseCase(repository);
        GetWorkspaceByIdUseCase getWorkspaceByIdUseCase = new GetWorkspaceByIdUseCase(repository);
        CreateWorkspaceUseCase createWorkspaceUseCase = new CreateWorkspaceUseCase(repository);
        UpdateWorkspaceUseCase updateWorkspaceUseCase = new UpdateWorkspaceUseCase(repository);
        DeleteWorkspaceUseCase deleteWorkspaceUseCase = new DeleteWorkspaceUseCase(repository);
        
        // Tạo Factory
        WorkspaceViewModelFactory factory = new WorkspaceViewModelFactory(
            getWorkspacesUseCase,
            getWorkspaceByIdUseCase,
            createWorkspaceUseCase,
            updateWorkspaceUseCase,
            deleteWorkspaceUseCase
        );
        
        // Tạo ViewModel
        workspaceViewModel = new ViewModelProvider(this, factory).get(WorkspaceViewModel.class);
    }
}
```

#### **Bước 2: Observe và update UI (20 phút)**

```java
private void observeViewModel() {
    // Observe workspaces list
    workspaceViewModel.getWorkspaces().observe(this, workspaces -> {
        if (workspaces != null && !workspaces.isEmpty()) {
            adapter.setWorkspaces(workspaces);
        }
    });
    
    // Observe loading
    workspaceViewModel.isLoading().observe(this, isLoading -> {
        if (isLoading) {
            // Show progress bar
        } else {
            // Hide progress bar
        }
    });
    
    // Observe errors
    workspaceViewModel.getError().observe(this, error -> {
        if (error != null) {
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        }
    });
}
```

**Thời gian:** 40 phút

---

### **Checklist Người 1:**

- [ ] LoginActivity - Setup ViewModel (15p)
- [ ] LoginActivity - Observe LiveData (15p)
- [ ] LoginActivity - Replace API calls (10p)
- [ ] LoginActivity - Test login flow (10p)
- [ ] SignupActivity - Tương tự LoginActivity (30p)
- [ ] HomeActivity - Setup ViewModel (20p)
- [ ] HomeActivity - Observe và load workspaces (20p)
- [ ] HomeActivity - Test workspace list (10p)
- [ ] Final testing (20p)

**Tổng:** 2.5 giờ

---

## 👤 NGƯỜI 2: WORKSPACE & PROJECT

### **Task 2.1: WorkspaceActivity Integration** ⭐⭐⭐

**File:** `feature/home/ui/Home/WorkspaceActivity.java`

**Hiện trạng:**
```java
// Đang load projects bằng API trực tiếp
WorkspaceApiService apiService = ApiClient.get(App.authManager).create(WorkspaceApiService.class);
Call<List<ProjectDTO>> call = apiService.getProjects(workspaceId);
```

**Cần làm:**

#### **Bước 1: Setup cả 2 ViewModels (25 phút)**

```java
public class WorkspaceActivity extends HomeActivity {
    private WorkspaceViewModel workspaceViewModel;
    private ProjectViewModel projectViewModel;
    
    private void setupViewModels() {
        // Setup WorkspaceViewModel
        IWorkspaceRepository workspaceRepo = new WorkspaceRepositoryImpl(
            ApiClient.get(App.authManager).create(WorkspaceApiService.class)
        );
        
        GetWorkspaceProjectsUseCase getProjectsUseCase = new GetWorkspaceProjectsUseCase(workspaceRepo);
        // ... other workspace usecases
        
        WorkspaceViewModelFactory workspaceFactory = new WorkspaceViewModelFactory(...);
        workspaceViewModel = new ViewModelProvider(this, workspaceFactory).get(WorkspaceViewModel.class);
        
        // Setup ProjectViewModel
        IProjectRepository projectRepo = new ProjectRepositoryImpl(
            ApiClient.get(App.authManager).create(WorkspaceApiService.class)
        );
        
        GetProjectByIdUseCase getProjectByIdUseCase = new GetProjectByIdUseCase(projectRepo);
        CreateProjectUseCase createProjectUseCase = new CreateProjectUseCase(projectRepo);
        UpdateProjectUseCase updateProjectUseCase = new UpdateProjectUseCase(projectRepo);
        DeleteProjectUseCase deleteProjectUseCase = new DeleteProjectUseCase(projectRepo);
        SwitchBoardTypeUseCase switchBoardTypeUseCase = new SwitchBoardTypeUseCase(projectRepo);
        UpdateProjectKeyUseCase updateProjectKeyUseCase = new UpdateProjectKeyUseCase(projectRepo);
        
        ProjectFactory projectFactory = new ProjectFactory(
            getProjectByIdUseCase,
            createProjectUseCase,
            updateProjectUseCase,
            deleteProjectUseCase,
            switchBoardTypeUseCase,
            updateProjectKeyUseCase
        );
        
        projectViewModel = new ViewModelProvider(this, projectFactory).get(ProjectViewModel.class);
    }
}
```

#### **Bước 2: Observe Projects (20 phút)**

```java
private void observeViewModels() {
    // Observe projects từ workspace
    workspaceViewModel.getProjects().observe(this, projects -> {
        if (projects != null) {
            workspaceAdapter.setProjects(projects);
        }
    });
    
    // Observe loading
    workspaceViewModel.isLoading().observe(this, isLoading -> {
        // Show/hide loading indicator
    });
    
    // Observe errors
    workspaceViewModel.getError().observe(this, error -> {
        if (error != null) {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        }
    });
}
```

#### **Bước 3: Replace loadProjects() (15 phút)**

```java
private void loadProjects() {
    // BEFORE: API call trực tiếp
    // WorkspaceApiService apiService = ...
    
    // AFTER: Dùng ViewModel
    workspaceViewModel.loadProjects(workspaceId);
}
```

**Thời gian:** 1 giờ

---

### **Task 2.2: ProjectActivity Integration** ⭐⭐⭐

**File:** `feature/home/ui/Home/ProjectActivity.java`

**Chức năng:** Hiển thị các boards (TO DO, IN PROGRESS, DONE) của project

#### **Bước 1: Setup BoardViewModel (20 phút)**

```java
public class ProjectActivity extends AppCompatActivity {
    private ProjectViewModel projectViewModel;
    private BoardViewModel boardViewModel;
    private TaskViewModel taskViewModel;
    
    private void setupViewModels() {
        // Setup ProjectViewModel (để get project details)
        // ... (tương tự WorkspaceActivity)
        
        // Setup BoardViewModel
        IBoardRepository boardRepo = new BoardRepositoryImpl(
            ApiClient.get(App.authManager).create(WorkspaceApiService.class)
        );
        
        GetBoardByIdUseCase getBoardByIdUseCase = new GetBoardByIdUseCase(boardRepo);
        CreateBoardUseCase createBoardUseCase = new CreateBoardUseCase(boardRepo);
        UpdateBoardUseCase updateBoardUseCase = new UpdateBoardUseCase(boardRepo);
        DeleteBoardUseCase deleteBoardUseCase = new DeleteBoardUseCase(boardRepo);
        ReorderBoardsUseCase reorderBoardsUseCase = new ReorderBoardsUseCase(boardRepo);
        GetBoardTasksUseCase getBoardTasksUseCase = new GetBoardTasksUseCase(boardRepo);
        
        BoardFactory boardFactory = new BoardFactory(
            getBoardByIdUseCase,
            createBoardUseCase,
            updateBoardUseCase,
            deleteBoardUseCase,
            reorderBoardsUseCase,
            getBoardTasksUseCase
        );
        
        boardViewModel = new ViewModelProvider(this, boardFactory).get(BoardViewModel.class);
    }
}
```

#### **Bước 2: Load boards cho từng tab (25 phút)**

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.project_main);
    
    String projectId = getIntent().getStringExtra("project_id");
    
    setupViewModels();
    setupTabs(projectId);
    observeViewModels();
}

private void setupTabs(String projectId) {
    TabLayout tabLayout = findViewById(R.id.tabLayout1);
    ViewPager2 viewPager2 = findViewById(R.id.PrjViewPager2);
    
    // Adapter sẽ nhận boardViewModel để load tasks
    ListProjectAdapter adapter = new ListProjectAdapter(this, projectId, boardViewModel);
    viewPager2.setAdapter(adapter);
    
    new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
        switch (position) {
            case 0: tab.setText("TO DO"); break;
            case 1: tab.setText("IN PROGRESS"); break;
            case 2: tab.setText("DONE"); break;
        }
    }).attach();
}
```

**Thời gian:** 45 phút

---

### **Task 2.3: Update ListProjectAdapter** ⭐⭐

**File:** `feature/home/ui/Home/project/ListProjectAdapter.java`

**Cần làm:** Refactor adapter để nhận BoardViewModel và load tasks từ đó

**Thời gian:** 30 phút

---

### **Checklist Người 2:**

- [ ] WorkspaceActivity - Setup ViewModels (25p)
- [ ] WorkspaceActivity - Observe projects (20p)
- [ ] WorkspaceActivity - Replace API calls (15p)
- [ ] WorkspaceActivity - Test (10p)
- [ ] ProjectActivity - Setup BoardViewModel (20p)
- [ ] ProjectActivity - Setup tabs với ViewModel (25p)
- [ ] ProjectActivity - Test boards loading (10p)
- [ ] ListProjectAdapter - Refactor to use ViewModel (30p)
- [ ] Final testing (15p)

**Tổng:** 2.5 giờ

---

## 👤 NGƯỜI 3: TASK & NOTIFICATION

### **Task 3.1: ListProject Fragment Integration** ⭐⭐⭐

**File:** `feature/home/ui/Home/project/ListProject.java`

**Hiện trạng:** Fragment load tasks từ API

**Cần làm:**

#### **Bước 1: Inject TaskViewModel vào Fragment (20 phút)**

```java
public class ListProject extends Fragment {
    private TaskViewModel taskViewModel;
    private String boardId;
    
    public ListProject(String boardId, TaskViewModel taskViewModel) {
        this.boardId = boardId;
        this.taskViewModel = taskViewModel;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.board_detail_item, container, false);
        
        setupRecyclerView(view);
        observeViewModel();
        
        // Load tasks for this board
        taskViewModel.loadTasksByBoard(boardId);
        
        return view;
    }
}
```

#### **Bước 2: Observe tasks (20 phút)**

```java
private void observeViewModel() {
    // Observe tasks list
    taskViewModel.getTasks().observe(getViewLifecycleOwner(), tasks -> {
        if (tasks != null) {
            taskAdapter.setTasks(tasks);
        }
    });
    
    // Observe loading
    taskViewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
        if (isLoading) {
            // Show shimmer/progress
        } else {
            // Hide shimmer/progress
        }
    });
    
    // Observe errors
    taskViewModel.getError().observe(getViewLifecycleOwner(), error -> {
        if (error != null) {
            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
        }
    });
}
```

**Thời gian:** 40 phút

---

### **Task 3.2: Task Detail/Actions** ⭐⭐

**Chức năng:** Khi click vào task, có thể:
- Assign/Unassign
- Update status
- Add comment
- Move to another board

#### **Setup các actions (30 phút)**

```java
private void setupTaskActions() {
    // Assign task
    btnAssign.setOnClickListener(v -> {
        String taskId = currentTask.getId();
        String userId = selectedUserId;
        taskViewModel.assignTask(taskId, userId);
    });
    
    // Move task
    btnMove.setOnClickListener(v -> {
        String taskId = currentTask.getId();
        String targetBoardId = selectedBoardId;
        double position = calculatePosition();
        taskViewModel.moveTaskToBoard(taskId, targetBoardId, position);
    });
    
    // Add comment
    btnAddComment.setOnClickListener(v -> {
        String taskId = currentTask.getId();
        TaskComment comment = new TaskComment(
            null, // id will be generated by backend
            taskId,
            currentUserId,
            commentBody,
            null // createdAt will be set by backend
        );
        taskViewModel.addComment(taskId, comment);
    });
}
```

**Thời gian:** 30 phút

---

### **Task 3.3: InboxActivity Integration** ⭐⭐

**File:** `feature/home/ui/InboxActivity.java`

**Chức năng:** Hiển thị notifications

#### **Setup NotificationViewModel (25 phút)**

```java
public class InboxActivity extends AppCompatActivity {
    private NotificationViewModel notificationViewModel;
    
    private void setupViewModel() {
        INotificationRepository repository = new NotificationRepositoryImpl(
            ApiClient.get(App.authManager).create(WorkspaceApiService.class)
        );
        
        GetNotificationsUseCase getNotificationsUseCase = new GetNotificationsUseCase(repository);
        GetUnreadNotificationsUseCase getUnreadNotificationsUseCase = new GetUnreadNotificationsUseCase(repository);
        GetNotificationByIdUseCase getNotificationByIdUseCase = new GetNotificationByIdUseCase(repository);
        GetNotificationCountUseCase getNotificationCountUseCase = new GetNotificationCountUseCase(repository);
        MarkAsReadUseCase markAsReadUseCase = new MarkAsReadUseCase(repository);
        MarkAllAsReadUseCase markAllAsReadUseCase = new MarkAllAsReadUseCase(repository);
        DeleteNotificationUseCase deleteNotificationUseCase = new DeleteNotificationUseCase(repository);
        DeleteAllNotificationsUseCase deleteAllNotificationsUseCase = new DeleteAllNotificationsUseCase(repository);
        
        NotificationViewModelFactory factory = new NotificationViewModelFactory(
            getNotificationsUseCase,
            getUnreadNotificationsUseCase,
            getNotificationByIdUseCase,
            getNotificationCountUseCase,
            markAsReadUseCase,
            markAllAsReadUseCase,
            deleteNotificationUseCase,
            deleteAllNotificationsUseCase
        );
        
        notificationViewModel = new ViewModelProvider(this, factory).get(NotificationViewModel.class);
    }
}
```

#### **Observe notifications (20 phút)**

```java
private void observeViewModel() {
    // Observe notifications
    notificationViewModel.getNotifications().observe(this, notifications -> {
        if (notifications != null) {
            adapter.setNotifications(notifications);
        }
    });
    
    // Observe unread count
    notificationViewModel.getUnreadCount().observe(this, count -> {
        updateBadge(count);
    });
    
    // Setup actions
    adapter.setOnNotificationClickListener(notification -> {
        notificationViewModel.markAsRead(notification.getId());
        // Navigate to related screen
    });
    
    // Mark all as read
    btnMarkAllRead.setOnClickListener(v -> {
        notificationViewModel.markAllAsRead();
    });
}
```

**Thời gian:** 45 phút

---

### **Checklist Người 3:**

- [ ] ListProject Fragment - Inject TaskViewModel (20p)
- [ ] ListProject Fragment - Observe tasks (20p)
- [ ] ListProject Fragment - Test loading (10p)
- [ ] Task Actions - Setup assign/move/comment (30p)
- [ ] Task Actions - Test actions (15p)
- [ ] InboxActivity - Setup NotificationViewModel (25p)
- [ ] InboxActivity - Observe notifications (20p)
- [ ] InboxActivity - Setup mark as read (10p)
- [ ] Final testing (20p)

**Tổng:** 2.5 giờ

---

## 🛠️ HELPER CLASS: ViewModelProvider Helper

**Tạo class tiện ích để giảm boilerplate code:**

**File mới:** `presentation/viewmodel/ViewModelFactory.java`

```java
package com.example.tralalero.presentation.viewmodel;

import com.example.tralalero.App.App;
import com.example.tralalero.auth.remote.AuthApi;
import com.example.tralalero.data.remote.api.WorkspaceApiService;
import com.example.tralalero.data.repository.*;
import com.example.tralalero.domain.usecase.auth.*;
import com.example.tralalero.domain.usecase.workspace.*;
import com.example.tralalero.domain.usecase.project.*;
import com.example.tralalero.domain.usecase.board.*;
import com.example.tralalero.domain.usecase.task.*;
import com.example.tralalero.domain.usecase.notification.*;
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
    
    public static ProjectFactory provideProjectViewModelFactory() {
        initApis();
        
        IProjectRepository repository = new ProjectRepositoryImpl(workspaceApi);
        
        return new ProjectFactory(
            new GetProjectByIdUseCase(repository),
            new CreateProjectUseCase(repository),
            new UpdateProjectUseCase(repository),
            new DeleteProjectUseCase(repository),
            new SwitchBoardTypeUseCase(repository),
            new UpdateProjectKeyUseCase(repository)
        );
    }
    
    public static BoardFactory provideBoardViewModelFactory() {
        initApis();
        
        IBoardRepository repository = new BoardRepositoryImpl(workspaceApi);
        
        return new BoardFactory(
            new GetBoardByIdUseCase(repository),
            new CreateBoardUseCase(repository),
            new UpdateBoardUseCase(repository),
            new DeleteBoardUseCase(repository),
            new ReorderBoardsUseCase(repository),
            new GetBoardTasksUseCase(repository)
        );
    }
    
    public static TaskViewModelFactory provideTaskViewModelFactory() {
        initApis();
        
        ITaskRepository repository = new TaskRepositoryImpl(workspaceApi);
        
        return new TaskViewModelFactory(
            new GetTaskByIdUseCase(repository),
            new GetTasksByBoardUseCase(repository),
            new CreateTaskUseCase(repository),
            new UpdateTaskUseCase(repository),
            new DeleteTaskUseCase(repository),
            new AssignTaskUseCase(repository),
            new UnassignTaskUseCase(repository),
            new MoveTaskToBoardUseCase(repository),
            new UpdateTaskPositionUseCase(repository),
            new AddCommentUseCase(repository),
            new GetTaskCommentsUseCase(repository),
            new AddAttachmentUseCase(repository),
            new GetTaskAttachmentsUseCase(repository),
            new AddChecklistUseCase(repository),
            new GetTaskChecklistsUseCase(repository)
        );
    }
    
    public static NotificationViewModelFactory provideNotificationViewModelFactory() {
        initApis();
        
        INotificationRepository repository = new NotificationRepositoryImpl(workspaceApi);
        
        return new NotificationViewModelFactory(
            new GetNotificationsUseCase(repository),
            new GetUnreadNotificationsUseCase(repository),
            new GetNotificationByIdUseCase(repository),
            new GetNotificationCountUseCase(repository),
            new MarkAsReadUseCase(repository),
            new MarkAllAsReadUseCase(repository),
            new DeleteNotificationUseCase(repository),
            new DeleteAllNotificationsUseCase(repository)
        );
    }
}
```

**Sử dụng:**

```java
// Thay vì viết dài dòng:
AuthViewModelFactory factory = new AuthViewModelFactory(...);

// Chỉ cần:
authViewModel = new ViewModelProvider(this, 
    ViewModelFactoryProvider.provideAuthViewModelFactory()
).get(AuthViewModel.class);
```

---

## 📝 PATTERN CHUNG CHO TẤT CẢ ACTIVITIES

### **Template Integration:**

```java
public class XxxActivity extends AppCompatActivity {
    
    // Step 1: Declare ViewModel
    private XxxViewModel viewModel;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xxx);
        
        // Step 2: Setup ViewModel
        setupViewModel();
        
        // Step 3: Setup UI
        setupUI();
        
        // Step 4: Observe ViewModel
        observeViewModel();
        
        // Step 5: Load initial data
        loadData();
    }
    
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this, 
            ViewModelFactoryProvider.provideXxxViewModelFactory()
        ).get(XxxViewModel.class);
    }
    
    private void setupUI() {
        // Setup RecyclerView, listeners, etc.
    }
    
    private void observeViewModel() {
        // Loading
        viewModel.isLoading().observe(this, isLoading -> {
            // Show/hide loading indicator
        });
        
        // Data
        viewModel.getData().observe(this, data -> {
            // Update UI
        });
        
        // Errors
        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadData() {
        viewModel.loadData();
    }
}
```

---

## ⚠️ LƯU Ý QUAN TRỌNG

### **1. Lifecycle Awareness**
```java
// ĐÚNG: Observe trong Activity/Fragment
viewModel.getData().observe(this, data -> { ... });

// SAI: Observe trực tiếp LiveData value
viewModel.getData().getValue(); // KHÔNG nên dùng trong UI
```

### **2. Clear observers khi không cần**
```java
@Override
protected void onDestroy() {
    super.onDestroy();
    // LiveData tự clear, nhưng có thể clear manual nếu cần
}
```

### **3. Handle configuration changes**
```java
// ViewModel tự động survive configuration changes
// Không cần save/restore state manually
```

### **4. Error handling**
```java
// Luôn clear error sau khi hiển thị
viewModel.getError().observe(this, error -> {
    if (error != null) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        viewModel.clearError(); // Important!
    }
});
```

---

## 🧪 TESTING CHECKLIST

### **Sau khi hoàn thành, test:**

- [ ] Login flow hoạt động
- [ ] Load workspaces khi vào HomeActivity
- [ ] Click workspace -> load projects
- [ ] Click project -> load boards với tasks
- [ ] Task actions (assign, move, comment) hoạt động
- [ ] Notifications load và mark as read hoạt động
- [ ] Loading states hiển thị đúng
- [ ] Error messages hiển thị đúng
- [ ] Không crash khi rotate device
- [ ] Không memory leak

---

## 📊 TIMELINE TỐI NAY

| Thời gian | Người 1 | Người 2 | Người 3 |
|-----------|---------|---------|---------|
| **19:00-19:40** | LoginActivity (40p) | WorkspaceActivity ViewModel (40p) | ListProject Fragment (40p) |
| **19:40-20:10** | SignupActivity (30p) | WorkspaceActivity Observe (30p) | Task Actions (30p) |
| **20:10-20:50** | HomeActivity (40p) | ProjectActivity Setup (40p) | InboxActivity Setup (40p) |
| **20:50-21:20** | Testing Login+Home (30p) | ListProjectAdapter refactor (30p) | InboxActivity Observe (30p) |
| **21:20-21:40** | Tạo ViewModelFactoryProvider (20p) | Testing (20p) | Testing (20p) |
| **21:40-22:00** | Final review & merge | Final review & merge | Final review & merge |

**Tổng:** 3 giờ (19:00 - 22:00)

---

## 🎯 MỤC TIÊU HOÀN THÀNH

✅ **Minimum (Phải có):**
- Login/Signup hoạt động với AuthViewModel
- HomeActivity load workspaces
- WorkspaceActivity load projects
- ProjectActivity hiển thị tasks

✅ **Nice to have:**
- Task actions (assign, move, comment)
- Notifications
- Tất cả loading/error states

✅ **Bonus:**
- ViewModelFactoryProvider helper
- Clean code, no warnings

---

## 📞 HỖ TRỢ KHI GẶP VẤN ĐỀ

**Vấn đề thường gặp:**

1. **ViewModel null:** Kiểm tra Factory có inject đủ dependencies
2. **LiveData không update:** Kiểm tra observe() trong đúng lifecycle
3. **Crash khi rotate:** ViewModel đã được tạo đúng cách với ViewModelProvider
4. **API không được gọi:** Kiểm tra đã gọi `viewModel.loadXxx()` chưa

**Debug tips:**
```java
// Log để check ViewModel
Log.d("TAG", "ViewModel: " + (viewModel != null));
Log.d("TAG", "LiveData value: " + viewModel.getData().getValue());
```

---

**GOOD LUCK! 💪 Let's integrate those ViewModels!**

