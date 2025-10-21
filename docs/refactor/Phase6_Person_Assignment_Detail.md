# PHASE 6: KẾ HOẠCH TRIỂN KHAI CHI TIẾT CHO 3 NGƯỜI
**Ngày:** 16/10/2025 (Buổi sáng - 4 giờ)  
**Mục tiêu:** Demo sản phẩm với các tính năng cơ bản

---

## 🎯 PHÂN CÔNG THEO THỜI GIAN

### ⏰ 08:00 - 09:30 (1.5 giờ) - SETUP & INTEGRATION

#### 👤 NGƯỜI 1: Authentication Flow
**Files cần edit:**
- `feature/auth/ui/login/LoginActivity.java`
- `feature/auth/ui/signup/SignupActivity.java`
- `feature/home/ui/HomeActivity.java`

**Tasks:**
1. ✅ Tích hợp AuthViewModel vào LoginActivity
2. ✅ Tích hợp AuthViewModel vào SignupActivity
3. ✅ Setup HomeActivity navigation
4. ✅ Test login/signup flow

**Chi tiết:**
```java
// LoginActivity.java - Thêm vào
private AuthViewModel authViewModel;

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
    observeViewModel();
}

private void observeViewModel() {
    authViewModel.isLoading().observe(this, isLoading -> {
        btnLogin.setEnabled(!isLoading);
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
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

// Replace API call
private void attemptLogin() {
    String email = etEmail.getText().toString().trim();
    String password = etPassword.getText().toString();
    
    if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
        Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
        return;
    }
    
    authViewModel.login(email, password);
}
```

---

#### 👤 NGƯỜI 2: Workspace & Project List
**Files cần edit:**
- `feature/home/ui/WorkspaceActivity.java`
- Tạo dialog cho Create Project

**Tasks:**
1. ✅ Verify WorkspaceActivity đã tích hợp WorkspaceViewModel
2. ✅ Tích hợp ProjectViewModel
3. ✅ Implement create project dialog
4. ✅ Test workspace → project flow

**Chi tiết:**
```java
// WorkspaceActivity.java - Thêm ProjectViewModel
private WorkspaceViewModel workspaceViewModel;
private ProjectViewModel projectViewModel;

private void setupViewModels() {
    // WorkspaceViewModel (đã có)
    workspaceViewModel = new ViewModelProvider(this,
        ViewModelFactoryProvider.provideWorkspaceViewModelFactory()
    ).get(WorkspaceViewModel.class);
    
    // ProjectViewModel (thêm mới)
    projectViewModel = new ViewModelProvider(this,
        ViewModelFactoryProvider.provideProjectViewModelFactory()
    ).get(ProjectViewModel.class);
    
    observeViewModels();
}

private void observeViewModels() {
    // Load projects
    workspaceViewModel.getProjects().observe(this, projects -> {
        if (projects != null && !projects.isEmpty()) {
            workspaceAdapter.setProjectList(projects);
        }
    });
    
    // Create project result
    projectViewModel.getCurrentProject().observe(this, project -> {
        if (project != null) {
            workspaceAdapter.addProject(project);
            Toast.makeText(this, "Project created!", Toast.LENGTH_SHORT).show();
        }
    });
    
    projectViewModel.getError().observe(this, error -> {
        if (error != null) {
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        }
    });
}

// FAB click handler
fabCreateProject.setOnClickListener(v -> showCreateProjectDialog());

private void showCreateProjectDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Create New Project");
    
    final EditText input = new EditText(this);
    input.setHint("Project name");
    builder.setView(input);
    
    builder.setPositiveButton("Create", (dialog, which) -> {
        String projectName = input.getText().toString().trim();
        if (!TextUtils.isEmpty(projectName)) {
            createProject(projectName);
        }
    });
    
    builder.setNegativeButton("Cancel", null);
    builder.show();
}

private void createProject(String name) {
    Project newProject = new Project(
        "", // id
        name,
        "", // description
        "", // key (auto-generate)
        workspaceId,
        "KANBAN"
    );
    
    projectViewModel.createProject(newProject);
}

// Adapter click handler
workspaceAdapter.setOnProjectClickListener(project -> {
    Intent intent = new Intent(this, ProjectActivity.class);
    intent.putExtra("PROJECT_ID", project.getId());
    intent.putExtra("WORKSPACE_ID", workspaceId);
    startActivity(intent);
});
```

---

#### 👤 NGƯỜI 3: ProjectActivity Board Setup
**Files cần edit:**
- `feature/home/ui/Home/project/ProjectActivity.java`
- `res/layout/activity_project.xml` (verify)

**Tasks:**
1. ✅ Setup ViewPager2 + TabLayout
2. ✅ Tích hợp BoardViewModel
3. ✅ Load/create 3 boards mặc định
4. ✅ Test board tabs navigation

**Chi tiết:**
```java
// ProjectActivity.java
private String projectId;
private String workspaceId;
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
    
    // Get intent extras
    projectId = getIntent().getStringExtra("PROJECT_ID");
    workspaceId = getIntent().getStringExtra("WORKSPACE_ID");
    
    if (projectId == null) {
        Toast.makeText(this, "Project ID missing", Toast.LENGTH_SHORT).show();
        finish();
        return;
    }
    
    initViews();
    setupViewModels();
    setupViewPager();
    setupTabLayout();
    loadData();
}

private void initViews() {
    viewPager = findViewById(R.id.viewPager);
    tabLayout = findViewById(R.id.tabLayout);
    
    // Setup toolbar
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
    
    observeViewModels();
}

private void setupViewPager() {
    pagerAdapter = new ListProjectAdapter(this, projectId);
    viewPager.setAdapter(pagerAdapter);
}

private void setupTabLayout() {
    new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
        switch (position) {
            case 0: tab.setText("TO DO"); break;
            case 1: tab.setText("IN PROGRESS"); break;
            case 2: tab.setText("DONE"); break;
        }
    }).attach();
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
                createDefaultBoards();
            } else {
                // Update adapter với board IDs
                List<String> boardIds = new ArrayList<>();
                for (Board board : boards) {
                    boardIds.add(board.getId());
                }
                pagerAdapter.setBoardIds(boardIds);
            }
        }
    });
}

private void loadData() {
    projectViewModel.loadProject(projectId);
    boardViewModel.loadBoardsForProject(projectId);
}

private void createDefaultBoards() {
    String[] names = {"TO DO", "IN PROGRESS", "DONE"};
    
    for (int i = 0; i < names.length; i++) {
        Board board = new Board("", projectId, names[i], i);
        boardViewModel.createBoard(board);
    }
}
```

**Layout cần verify: activity_project.xml**
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <com.google.android.material.appbar.AppBarLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <androidx.appcompat.widget.Toolbar
            android:id="@+id/toolbar"
            android:layout_width="match_parent"
            android:layout_height="?attr/actionBarSize"
            app:popupTheme="@style/ThemeOverlay.AppCompat.Light"/>

        <com.google.android.material.tabs.TabLayout
            android:id="@+id/tabLayout"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            app:tabMode="fixed"/>

    </com.google.android.material.appbar.AppBarLayout>

    <androidx.viewpager2.widget.ViewPager2
        android:id="@+id/viewPager"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"/>

</LinearLayout>
```

---

### ⏰ 09:30 - 11:00 (1.5 giờ) - TASK CRUD

#### 👤 NGƯỜI 1: Navigation & Error Handling
**Files cần edit:**
- `feature/home/ui/HomeActivity.java`
- Verify all navigation flows

**Tasks:**
1. ✅ Implement navigation từ HomeActivity → WorkspaceActivity
2. ✅ Handle back button properly
3. ✅ Implement logout
4. ✅ Error handling UI (Snackbar/Toast)

---

#### 👤 NGƯỜI 2: Create Task UI
**Files cần tạo:**
- `feature/home/ui/Home/project/TaskDetailBottomSheet.java`
- `res/layout/bottom_sheet_task_detail.xml`

**Tasks:**
1. ✅ Tạo TaskDetailBottomSheet
2. ✅ Implement create task
3. ✅ Connect với ListProject fragment
4. ✅ Test create task flow

**Code:**
```java
// TaskDetailBottomSheet.java
public class TaskDetailBottomSheet extends BottomSheetDialogFragment {
    
    private Task task; // null if create mode
    private String boardId;
    private String projectId;
    
    private EditText etTitle;
    private EditText etDescription;
    private Button btnSave;
    private Button btnDelete;
    
    private OnTaskSavedListener onTaskSavedListener;
    
    public static TaskDetailBottomSheet newInstance(String boardId, String projectId) {
        return newInstance(null, boardId, projectId);
    }
    
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
        
        etTitle = view.findViewById(R.id.etTitle);
        etDescription = view.findViewById(R.id.etDescription);
        btnSave = view.findViewById(R.id.btnSave);
        btnDelete = view.findViewById(R.id.btnDelete);
        
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
        
        btnSave.setOnClickListener(v -> saveTask());
        btnDelete.setOnClickListener(v -> deleteTask());
        
        return view;
    }
    
    private void saveTask() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(getContext(), "Title is required", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Task newTask = new Task(
            task != null ? task.getId() : "",
            projectId,
            boardId,
            title,
            description,
            null, null, null, 0, null, null, TaskStatus.TO_DO
        );
        
        if (onTaskSavedListener != null) {
            onTaskSavedListener.onTaskSaved(newTask);
        }
        
        dismiss();
    }
    
    private void deleteTask() {
        // TODO: Implement delete
        dismiss();
    }
    
    public void setOnTaskSavedListener(OnTaskSavedListener listener) {
        this.onTaskSavedListener = listener;
    }
    
    public interface OnTaskSavedListener {
        void onTaskSaved(Task task);
    }
}
```

---

#### 👤 NGƯỜI 3: Task Display & Actions
**Files cần edit:**
- `feature/home/ui/Home/project/ListProject.java`

**Tasks:**
1. ✅ Verify task loading (đã có ở Phase 5)
2. ✅ Implement FAB create task
3. ✅ Implement task click → edit
4. ✅ Test CRUD operations

**Code thêm vào ListProject.java:**
```java
// Add FAB in layout
private FloatingActionButton fabCreateTask;

@Override
public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_list_project, container, false);
    
    // ... existing code ...
    
    fabCreateTask = view.findViewById(R.id.fabCreateTask);
    fabCreateTask.setOnClickListener(v -> showCreateTaskDialog());
    
    return view;
}

private void showCreateTaskDialog() {
    TaskDetailBottomSheet bottomSheet = TaskDetailBottomSheet.newInstance(boardId, projectId);
    
    bottomSheet.setOnTaskSavedListener(task -> {
        taskViewModel.createTask(task);
    });
    
    bottomSheet.show(getParentFragmentManager(), "CREATE_TASK");
}

private void showTaskDetailBottomSheet(Task task) {
    TaskDetailBottomSheet bottomSheet = TaskDetailBottomSheet.newInstance(task, boardId, projectId);
    
    bottomSheet.setOnTaskSavedListener(updatedTask -> {
        taskViewModel.updateTask(updatedTask);
    });
    
    bottomSheet.show(getParentFragmentManager(), "TASK_DETAIL");
}

// In setupRecyclerView()
taskAdapter.setOnTaskClickListener(task -> {
    showTaskDetailBottomSheet(task);
});
```

---

### ⏰ 11:00 - 12:00 (1 giờ) - TESTING & BUG FIX

#### 👥 TẤT CẢ 3 NGƯỜI: Testing

**Test Flow:**
```
1. Launch app
2. Login với test account
3. Verify navigate to HomeActivity
4. Navigate to WorkspaceActivity
5. Create new project "Test Project"
6. Click vào project → see 3 tabs
7. Click TO DO tab → create task "Task 1"
8. Click IN PROGRESS tab → create task "Task 2"
9. Click task → edit title
10. Delete task
11. Logout
```

**Checklist:**
- [ ] Login works
- [ ] Signup works
- [ ] Workspace list loads
- [ ] Create project works
- [ ] Project list updates
- [ ] Navigate to project works
- [ ] 3 tabs display correctly
- [ ] Create task works
- [ ] Task appears in list
- [ ] Edit task works
- [ ] Delete task works
- [ ] Error messages show properly
- [ ] Loading states show
- [ ] Logout works

---

## 🐛 COMMON ISSUES & QUICK FIXES

### Issue 1: Backend API 401 Unauthorized
```java
// Check token
Log.d("DEBUG", "Token: " + App.authManager.getIdToken());

// Verify ApiClient
ApiClient.get(App.authManager) // Must pass authManager
```

### Issue 2: ViewModel null
```java
// Must use factory
new ViewModelProvider(this, factory).get(AuthViewModel.class);
// NOT: new ViewModelProvider(this).get(...) ❌
```

### Issue 3: RecyclerView empty
```java
// Check adapter is set
recyclerView.setAdapter(taskAdapter);

// Check data is not null
Log.d("DEBUG", "Tasks size: " + tasks.size());
```

### Issue 4: Fragment not updating
```java
// Use getViewLifecycleOwner() in Fragment
taskViewModel.getTasks().observe(getViewLifecycleOwner(), tasks -> {
    // ...
});
```

---

## 📝 DELIVERABLES (12:00)

1. ✅ Working app với flow: Login → Workspace → Project → Tasks
2. ✅ Task CRUD hoàn chỉnh
3. ✅ Code clean, có comments
4. ✅ Testing report (screenshot/video)

---

## 🚀 DEMO SCRIPT (For presentation)

```
1. Mở app → Login screen
2. Login với account → Navigate to Home
3. Click Workspace → See projects list
4. Click Create Project → Enter "Demo Project"
5. Click "Demo Project" → See 3 tabs
6. Click TO DO → Click FAB
7. Create task "Buy groceries"
8. Task appears in list
9. Click task → Edit description
10. Save → See updated
11. Create more tasks in IN PROGRESS
12. Navigate back → All data persists
```

**Good luck! 🎉**

