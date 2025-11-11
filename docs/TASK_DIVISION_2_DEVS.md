# PHÂN CÔNG CHI TIẾT 2 DEV FRONTEND - KHÔNG ĐỢI NHAU

## 🎯 NGUYÊN TẮC VÀNG

### **ZERO BLOCKING DEPENDENCIES**
- Mỗi dev làm module **HOÀN TOÀN ĐỘC LẬP**
- Không đợi code người kia để tiếp tục
- Push code riêng branch mỗi ngày
- Merge cuối cùng khi cả 2 xong

### **BRANCHES**
- Dev 1: `refactor/dev1-auth-workspace-project`
- Dev 2: `refactor/dev2-task-board-inbox`
- Main branch: `master` (không động đến cho đến khi cả 2 xong)

---

## 👤 DEV 1: AUTH + WORKSPACE + PROJECT

### **Module độc lập:**
1. Authentication flow (Login/Signup)
2. Workspace list & CRUD
3. Project list & CRUD
4. ProjectActivity refactor

### **Files thuộc sở hữu (100%):**
```
presentation/viewmodel/
  ├── AuthViewModel.java                    ← REFACTOR
  ├── AuthViewModelFactory.java             ← REFACTOR
  ├── WorkspaceViewModel.java               ← REFACTOR
  ├── WorkspaceViewModelFactory.java        ← REFACTOR
  └── ProjectViewModel.java                 ← ENHANCE

feature/auth/ui/
  ├── login/LoginActivity.java              ← REFACTOR
  ├── signup/SignupActivity.java            ← REFACTOR
  └── forgot/ForgotPasswordActivity.java    ← REFACTOR

feature/home/ui/
  ├── Home/WorkspaceActivity.java           ← REFACTOR
  └── Home/ProjectActivity.java             ← MAJOR REFACTOR

MainActivity.java                            ← REFACTOR
```

### **Không động đến (để Dev 2):**
- `TaskViewModel.java`
- `BoardViewModel.java`
- `InboxActivity.java`
- `CardDetailActivity.java`
- `TaskAdapter.java`
- `BoardAdapter.java`

---

## 📅 DEV 1 - WEEK BY WEEK PLAN

### **WEEK 1**

#### **DAY 1: AuthViewModel + LoginActivity** ⏱️ 8 hours

**Morning (4h): AuthViewModel Refactor**

File: `presentation/viewmodel/AuthViewModel.java`

**Hiện tại:**
```java
// Activities manually check success then navigate
authViewModel.login(email, password);
authViewModel.getCurrentUser().observe(...); // Manual check
```

**Refactor thành:**
```java
public class AuthViewModel extends ViewModel {
    // State
    private final MutableLiveData<User> currentUserLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoggedInLiveData = new MutableLiveData<>();
    private final MutableLiveData<AuthState> authStateLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    
    public enum AuthState {
        IDLE,           // Initial state
        LOGGING_IN,     // Login in progress
        LOGIN_SUCCESS,  // Login successful
        LOGIN_ERROR,    // Login failed
        LOGGED_OUT      // User logged out
    }
    
    // Constructor - Auto-check session on app start
    public AuthViewModel(
        LoginUseCase loginUseCase,
        SignupUseCase signupUseCase,
        LogoutUseCase logoutUseCase,
        GetCurrentUserUseCase getCurrentUserUseCase,
        IsLoggedInUseCase isLoggedInUseCase
    ) {
        this.loginUseCase = loginUseCase;
        this.signupUseCase = signupUseCase;
        this.logoutUseCase = logoutUseCase;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.isLoggedInUseCase = isLoggedInUseCase;
        
        // ✅ Auto-restore session on ViewModel creation
        checkStoredSession();
    }
    
    // Public getters
    public LiveData<User> getCurrentUser() { return currentUserLiveData; }
    public LiveData<Boolean> isLoggedIn() { return isLoggedInLiveData; }
    public LiveData<AuthState> getAuthState() { return authStateLiveData; }
    public LiveData<Boolean> isLoading() { return loadingLiveData; }
    public LiveData<String> getError() { return errorLiveData; }
    
    // ✅ Login method - Updates state automatically
    public void login(String email, String password) {
        authStateLiveData.setValue(AuthState.LOGGING_IN);
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);
        
        loginUseCase.execute(email, password, new LoginUseCase.Callback<AuthResult>() {
            @Override
            public void onSuccess(AuthResult result) {
                currentUserLiveData.setValue(result.getUser());
                isLoggedInLiveData.setValue(true);
                authStateLiveData.setValue(AuthState.LOGIN_SUCCESS);
                loadingLiveData.setValue(false);
                // Activity auto-navigates by observing authState
            }
            
            @Override
            public void onError(String error) {
                errorLiveData.setValue(error);
                authStateLiveData.setValue(AuthState.LOGIN_ERROR);
                loadingLiveData.setValue(false);
            }
        });
    }
    
    // ✅ Auto-restore session
    private void checkStoredSession() {
        isLoggedInUseCase.execute(new Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean isLoggedIn) {
                isLoggedInLiveData.setValue(isLoggedIn);
                
                if (isLoggedIn) {
                    // Load current user
                    getCurrentUserUseCase.execute(new Callback<User>() {
                        @Override
                        public void onSuccess(User user) {
                            currentUserLiveData.setValue(user);
                            authStateLiveData.setValue(AuthState.LOGIN_SUCCESS);
                        }
                    });
                } else {
                    authStateLiveData.setValue(AuthState.LOGGED_OUT);
                }
            }
        });
    }
    
    public void logout() {
        logoutUseCase.execute(new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                currentUserLiveData.setValue(null);
                isLoggedInLiveData.setValue(false);
                authStateLiveData.setValue(AuthState.LOGGED_OUT);
            }
        });
    }
    
    public void clearError() {
        errorLiveData.setValue(null);
    }
}
```

**Checklist:**
- [ ] Add AuthState enum
- [ ] Add authStateLiveData
- [ ] Implement checkStoredSession()
- [ ] Update login() to set AuthState
- [ ] Test: Create ViewModel → should auto-check session

---

**Afternoon (4h): LoginActivity Refactor**

File: `feature/auth/ui/login/LoginActivity.java`

**Hiện tại:**
```java
// Manual navigation after login success
btnLogin.setOnClickListener(v -> {
    authViewModel.login(email, password);
});

authViewModel.getCurrentUser().observe(this, user -> {
    if (user != null) {
        // Manual navigation
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }
});
```

**Refactor thành:**
```java
public class LoginActivity extends AppCompatActivity {
    private AuthViewModel authViewModel;
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private TextView tvError;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        setupViewModel();
        initViews();
        observeViewModel(); // ✅ Observe once
        
        btnLogin.setOnClickListener(v -> handleLogin());
    }
    
    private void setupViewModel() {
        authViewModel = new ViewModelProvider(
            this,
            ViewModelFactoryProvider.provideAuthViewModelFactory()
        ).get(AuthViewModel.class);
    }
    
    private void observeViewModel() {
        // ✅ Observe AuthState - auto-navigate
        authViewModel.getAuthState().observe(this, state -> {
            switch (state) {
                case LOGGING_IN:
                    // Show loading
                    break;
                    
                case LOGIN_SUCCESS:
                    // ✅ Auto-navigate to home
                    navigateToHome();
                    break;
                    
                case LOGIN_ERROR:
                    // Show error
                    break;
                    
                default:
                    break;
            }
        });
        
        // Loading state
        authViewModel.isLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnLogin.setEnabled(!isLoading);
        });
        
        // Error state
        authViewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                tvError.setText(error);
                tvError.setVisibility(View.VISIBLE);
                
                // Clear after 3 seconds
                new Handler().postDelayed(() -> {
                    authViewModel.clearError();
                }, 3000);
            } else {
                tvError.setVisibility(View.GONE);
            }
        });
    }
    
    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        // Validation
        if (email.isEmpty()) {
            etEmail.setError("Email required");
            return;
        }
        
        if (password.isEmpty()) {
            etPassword.setError("Password required");
            return;
        }
        
        // ✅ Just call ViewModel - it handles everything
        authViewModel.login(email, password);
        // Observer auto-navigates on success
    }
    
    private void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
```

**Checklist:**
- [ ] Remove manual navigation logic
- [ ] Observe authState for auto-navigation
- [ ] Observe isLoading for button state
- [ ] Observe error for error display
- [ ] Test: Login success → auto-navigate
- [ ] Test: Rotate during login → state preserved

**End of Day 1 Deliverable:**
- [ ] AuthViewModel với auto-session check
- [ ] LoginActivity observes only, auto-navigates
- [ ] Test rotation during login

---

#### **DAY 2: SignupActivity + MainActivity** ⏱️ 8 hours

**Morning (3h): SignupActivity Refactor**

File: `feature/auth/ui/signup/SignupActivity.java`

Tương tự LoginActivity:
- Observe authState
- Auto-navigate on SIGNUP_SUCCESS
- Show loading/error via LiveData
- No manual reload

**Afternoon (3h): MainActivity Auto-Redirect**

File: `MainActivity.java`

**Hiện tại:**
```java
// Manual check then navigate
if (authManager.isLoggedIn()) {
    startActivity(new Intent(this, HomeActivity.class));
} else {
    startActivity(new Intent(this, LoginActivity.class));
}
```

**Refactor thành:**
```java
public class MainActivity extends AppCompatActivity {
    private AuthViewModel authViewModel;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        authViewModel = new ViewModelProvider(
            this,
            ViewModelFactoryProvider.provideAuthViewModelFactory()
        ).get(AuthViewModel.class);
        
        // ✅ Observe and auto-redirect
        authViewModel.isLoggedIn().observe(this, isLoggedIn -> {
            if (isLoggedIn != null) {
                if (isLoggedIn) {
                    navigateToHome();
                } else {
                    navigateToLogin();
                }
            }
        });
        
        // AuthViewModel constructor auto-checks session
    }
    
    private void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
```

**Afternoon (2h): Testing**

**Checklist:**
- [ ] SignupActivity complete
- [ ] MainActivity auto-redirects
- [ ] Test: Open app logged in → auto-redirect to Home
- [ ] Test: Open app logged out → auto-redirect to Login
- [ ] Test: Login → MainActivity → Home (no manual redirect)

**End of Day 2 Deliverable:**
- [ ] Auth flow complete với auto-navigation
- [ ] No manual navigation anywhere

---

#### **DAY 3-4: WorkspaceViewModel + WorkspaceActivity** ⏱️ 16 hours

**DAY 3 Morning (4h): WorkspaceViewModel Refactor**

File: `presentation/viewmodel/WorkspaceViewModel.java`

**Hiện tại:**
```java
// WorkspaceActivity manually calls loadWorkspaceProjects 3 times:
// - onCreate
// - After create project
// - After delete project
```

**Refactor thành:**
```java
public class WorkspaceViewModel extends ViewModel {
    // Dependencies
    private final GetWorkspaceByIdUseCase getWorkspaceByIdUseCase;
    private final GetWorkspaceProjectsUseCase getWorkspaceProjectsUseCase;
    private final GetUserWorkspacesUseCase getUserWorkspacesUseCase;
    private final CreateWorkspaceUseCase createWorkspaceUseCase;
    private final UpdateWorkspaceUseCase updateWorkspaceUseCase;
    private final DeleteWorkspaceUseCase deleteWorkspaceUseCase;
    
    // State
    private final MutableLiveData<List<Workspace>> workspacesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Workspace> selectedWorkspaceLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Project>> projectsLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> selectedWorkspaceIdLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    
    public WorkspaceViewModel(...) {
        // Inject dependencies
    }
    
    // Getters
    public LiveData<List<Workspace>> getWorkspaces() { return workspacesLiveData; }
    public LiveData<Workspace> getSelectedWorkspace() { return selectedWorkspaceLiveData; }
    public LiveData<List<Project>> getProjects() { return projectsLiveData; }
    public LiveData<Boolean> isLoading() { return loadingLiveData; }
    public LiveData<String> getError() { return errorLiveData; }
    
    // ✅ Auto-load when workspace selected
    public void selectWorkspace(String workspaceId) {
        if (workspaceId == null) return;
        
        selectedWorkspaceIdLiveData.setValue(workspaceId);
        
        // Load workspace details
        loadingLiveData.setValue(true);
        
        getWorkspaceByIdUseCase.execute(workspaceId, new Callback<Workspace>() {
            @Override
            public void onSuccess(Workspace workspace) {
                selectedWorkspaceLiveData.setValue(workspace);
                
                // Auto-load projects for this workspace
                loadProjectsForWorkspace(workspaceId);
            }
            
            @Override
            public void onError(String error) {
                errorLiveData.setValue(error);
                loadingLiveData.setValue(false);
            }
        });
    }
    
    private void loadProjectsForWorkspace(String workspaceId) {
        getWorkspaceProjectsUseCase.execute(workspaceId, new Callback<List<Project>>() {
            @Override
            public void onSuccess(List<Project> projects) {
                projectsLiveData.setValue(projects);
                loadingLiveData.setValue(false);
            }
            
            @Override
            public void onError(String error) {
                errorLiveData.setValue(error);
                loadingLiveData.setValue(false);
            }
        });
    }
    
    // ✅ Create project - optimistic update
    public void createProject(String workspaceId, String name, String description) {
        // Get current list
        List<Project> current = projectsLiveData.getValue();
        if (current == null) current = new ArrayList<>();
        
        // Create temp project
        Project temp = new Project(
            "temp_" + System.currentTimeMillis(),
            name,
            description,
            workspaceId
        );
        
        // Add to list immediately
        current.add(0, temp);
        projectsLiveData.setValue(new ArrayList<>(current));
        
        // Background API call
        createProjectUseCase.execute(workspaceId, name, description, new Callback<Project>() {
            @Override
            public void onSuccess(Project project) {
                // Replace temp with real
                List<Project> updated = projectsLiveData.getValue();
                updated.remove(temp);
                updated.add(0, project);
                projectsLiveData.setValue(new ArrayList<>(updated));
            }
            
            @Override
            public void onError(String error) {
                // Rollback
                List<Project> updated = projectsLiveData.getValue();
                updated.remove(temp);
                projectsLiveData.setValue(new ArrayList<>(updated));
                errorLiveData.setValue(error);
            }
        });
    }
    
    // ✅ Delete project - optimistic update
    public void deleteProject(String projectId) {
        // Find and remove immediately
        List<Project> current = projectsLiveData.getValue();
        Project toDelete = null;
        
        for (Project p : current) {
            if (p.getId().equals(projectId)) {
                toDelete = p;
                break;
            }
        }
        
        if (toDelete != null) {
            final Project deleted = toDelete;
            current.remove(deleted);
            projectsLiveData.setValue(new ArrayList<>(current));
            
            // Background API
            deleteProjectUseCase.execute(projectId, new Callback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    // Already removed
                }
                
                @Override
                public void onError(String error) {
                    // Rollback
                    List<Project> updated = projectsLiveData.getValue();
                    updated.add(deleted);
                    projectsLiveData.setValue(new ArrayList<>(updated));
                    errorLiveData.setValue(error);
                }
            });
        }
    }
    
    public void clearError() {
        errorLiveData.setValue(null);
    }
}
```

**Checklist:**
- [ ] selectWorkspace() auto-loads projects
- [ ] createProject() optimistic update
- [ ] deleteProject() optimistic update
- [ ] All state in LiveData

---

**DAY 3 Afternoon + DAY 4: WorkspaceActivity Refactor**

File: `feature/home/ui/Home/WorkspaceActivity.java`

**Hiện tại:** 260 lines với 3-4 manual reload calls

**Refactor thành:**
```java
public class WorkspaceActivity extends HomeActivity {
    private static final String TAG = "WorkspaceActivity";
    
    private WorkspaceViewModel workspaceViewModel;
    private WorkspaceAdapter workspaceAdapter;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    
    private String workspaceId;
    private String workspaceName;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workspace_main);
        
        // Get workspace info from intent
        workspaceId = getIntent().getStringExtra("WORKSPACE_ID");
        workspaceName = getIntent().getStringExtra("WORKSPACE_NAME");
        
        if (workspaceId == null) {
            finish();
            return;
        }
        
        setupViewModel();
        setupUI();
        setupRecyclerView();
        observeViewModel(); // ✅ Observe once
        
        // ✅ Trigger load once
        workspaceViewModel.selectWorkspace(workspaceId);
        
        setupBottomNavigation(0);
    }
    
    // ❌ NO onResume - LiveData auto-updates
    
    private void observeViewModel() {
        // ✅ Projects observer
        workspaceViewModel.getProjects().observe(this, projects -> {
            if (projects != null) {
                workspaceAdapter.setProjectList(projects);
                
                if (projects.isEmpty()) {
                    // Show empty state
                }
            }
        });
        
        // Loading state
        workspaceViewModel.isLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
        
        // Error state
        workspaceViewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                workspaceViewModel.clearError();
            }
        });
    }
    
    private void setupRecyclerView() {
        workspaceAdapter = new WorkspaceAdapter(this);
        workspaceAdapter.setOnProjectClickListener(project -> {
            navigateToProject(project);
        });
        recyclerView.setAdapter(workspaceAdapter);
    }
    
    private void showCreateProjectDialog() {
        // ... get name, description
        
        // ✅ Just call ViewModel
        workspaceViewModel.createProject(workspaceId, name, description);
        // Observer auto-updates UI - NO MANUAL RELOAD
    }
    
    private void deleteProject(Project project) {
        // ✅ Just call ViewModel
        workspaceViewModel.deleteProject(project.getId());
        // Observer auto-updates UI - NO MANUAL RELOAD
    }
}
```

**Checklist:**
- [ ] Remove all loadProjects() calls
- [ ] Observe projects once in onCreate
- [ ] Create project - instant UI update
- [ ] Delete project - instant remove
- [ ] Test: Rotate after create - project persists
- [ ] Test: Rotate after delete - project gone

**End of Day 3-4 Deliverable:**
- [ ] WorkspaceViewModel complete
- [ ] WorkspaceActivity zero manual reloads
- [ ] Optimistic updates working

---

#### **DAY 5: ProjectViewModel Enhancement** ⏱️ 8 hours

File: `presentation/viewmodel/ProjectViewModel.java`

**Hiện tại:** Basic CRUD, không manage boards/tasks

**Enhancement:**
```java
public class ProjectViewModel extends ViewModel {
    // Existing fields...
    
    // ✅ Add boards management
    private final MutableLiveData<List<Board>> boardsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Map<String, List<Task>>> tasksPerBoardLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> selectedProjectIdLiveData = new MutableLiveData<>();
    
    public LiveData<List<Board>> getBoards() { return boardsLiveData; }
    public LiveData<Map<String, List<Task>>> getTasksPerBoard() { return tasksPerBoardLiveData; }
    
    // ✅ Select project auto-loads everything
    public void selectProject(String projectId) {
        if (projectId == null) return;
        
        selectedProjectIdLiveData.setValue(projectId);
        
        loadingLiveData.setValue(true);
        
        // Load project details
        getProjectByIdUseCase.execute(projectId, new Callback<Project>() {
            @Override
            public void onSuccess(Project project) {
                selectedProjectLiveData.setValue(project);
                
                // Auto-load boards
                loadBoardsForProject(projectId);
            }
        });
    }
    
    private void loadBoardsForProject(String projectId) {
        getBoardsByProjectUseCase.execute(projectId, new Callback<List<Board>>() {
            @Override
            public void onSuccess(List<Board> boards) {
                boardsLiveData.setValue(boards);
                
                // Auto-load tasks for all boards
                loadTasksForAllBoards(boards);
            }
        });
    }
    
    private void loadTasksForAllBoards(List<Board> boards) {
        if (boards == null || boards.isEmpty()) {
            tasksPerBoardLiveData.setValue(new HashMap<>());
            loadingLiveData.setValue(false);
            return;
        }
        
        Map<String, List<Task>> tasksMap = new HashMap<>();
        AtomicInteger pending = new AtomicInteger(boards.size());
        
        for (Board board : boards) {
            getTasksByBoardUseCase.execute(board.getId(), new Callback<List<Task>>() {
                @Override
                public void onSuccess(List<Task> tasks) {
                    synchronized (tasksMap) {
                        tasksMap.put(board.getId(), tasks);
                        
                        if (pending.decrementAndGet() == 0) {
                            tasksPerBoardLiveData.setValue(tasksMap);
                            loadingLiveData.setValue(false);
                        }
                    }
                }
            });
        }
    }
}
```

**Checklist:**
- [ ] Add boardsLiveData
- [ ] Add tasksPerBoardLiveData
- [ ] selectProject() auto-loads boards + tasks
- [ ] Test: Select project → all data loads

**End of Day 5 Deliverable:**
- [ ] ProjectViewModel manages boards + tasks
- [ ] Ready for ProjectActivity refactor (Week 2)

---

### **WEEK 2**

#### **DAY 6-8: ProjectActivity MAJOR REFACTOR** ⏱️ 24 hours (CRITICAL)

File: `feature/home/ui/Home/ProjectActivity.java`

**Current:** 684 lines, 9 manual reload calls, state in Activity

**Target:** ~400 lines, 0 manual reload calls, all state in ViewModel

**Step-by-step:**

**DAY 6 Morning: Remove Activity State**
```java
// ❌ DELETE THESE LINES
private List<Board> boards = new ArrayList<>();
private final Map<String, List<Task>> tasksPerBoard = new HashMap<>();

// ✅ State only in ViewModel via LiveData
```

**DAY 6 Afternoon: Setup Observation**
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.project_main);
    
    // Get project ID
    projectId = getIntent().getStringExtra("PROJECT_ID");
    
    setupViewModels();
    initViews();
    setupTabs();
    setupSwipeGesture();
    observeViewModels(); // ✅ Observe once
    
    // ✅ Trigger initial load once
    projectViewModel.selectProject(projectId);
}

private void observeViewModels() {
    // ✅ Boards observer
    projectViewModel.getBoards().observe(this, boards -> {
        if (boards != null) {
            // Update adapter
            boardAdapter.setBoards(boards);
        }
    });
    
    // ✅ Tasks observer
    projectViewModel.getTasksPerBoard().observe(this, tasksMap -> {
        if (tasksMap != null) {
            boardAdapter.setTasksPerBoard(tasksMap);
            boardAdapter.notifyDataSetChanged();
        }
    });
    
    // ✅ Loading observer
    projectViewModel.isLoading().observe(this, isLoading -> {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    });
    
    // ✅ Error observer
    projectViewModel.getError().observe(this, error -> {
        if (error != null && !error.isEmpty()) {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            projectViewModel.clearError();
        }
    });
}
```

**DAY 7: Delete Manual Reload Calls**
```java
// ❌ DELETE onResume
@Override
protected void onResume() {
    // EMPTY - LiveData auto-updates
}

// ❌ DELETE onActivityResult reload
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    // EMPTY - LiveData auto-updates
}

// ❌ DELETE loadTasksForBoard everywhere
// Find and delete all calls:
// - loadTasksForBoard(boardId)
// - loadBoards()
// - loadProjectData()

// ✅ ViewModel handles all loading
```

**DAY 7-8: Refactor Action Handlers**
```java
// ❌ OLD: Move task with manual reload
private void onMoveTaskToBoard(Task task, Board currentBoard, Board targetBoard) {
    progressBar.setVisibility(View.VISIBLE);
    
    taskViewModel.moveTaskToBoard(task.getId(), targetBoard.getId(), 0.0);
    
    new Handler().postDelayed(() -> {
        loadTasksForBoard(currentBoard.getId());  // ❌ DELETE
        loadTasksForBoard(targetBoard.getId());   // ❌ DELETE
        progressBar.setVisibility(View.GONE);
    }, 800);
}

// ✅ NEW: Just call ViewModel
private void onMoveTaskToBoard(Task task, Board currentBoard, Board targetBoard) {
    // This method will be enhanced by Dev 2 with optimistic update
    // For now, just call ViewModel
    taskViewModel.moveTaskToBoard(task.getId(), targetBoard.getId(), 0.0);
    
    // Observer auto-updates UI
    // NO MANUAL RELOAD
    // NO PROGRESS BAR (ViewModel manages loading state)
}
```

**DAY 8: Update BoardAdapter**
```java
public class BoardAdapter extends RecyclerView.Adapter<BoardAdapter.BoardViewHolder> {
    private List<Board> boards = new ArrayList<>();
    private Map<String, List<Task>> tasksPerBoard = new HashMap<>();
    
    // ✅ Setters for LiveData updates
    public void setBoards(List<Board> boards) {
        this.boards = boards;
        notifyDataSetChanged();
    }
    
    public void setTasksPerBoard(Map<String, List<Task>> tasksMap) {
        this.tasksPerBoard = tasksMap;
        notifyDataSetChanged();
    }
    
    @Override
    public void onBindViewHolder(BoardViewHolder holder, int position) {
        Board board = boards.get(position);
        List<Task> tasks = tasksPerBoard.get(board.getId());
        
        holder.bind(board, tasks != null ? tasks : new ArrayList<>());
    }
}
```

**Checklist:**
- [ ] Remove state from Activity
- [ ] Setup observers in onCreate
- [ ] Delete all manual reload calls (9 → 0)
- [ ] Delete onResume reload
- [ ] Delete onActivityResult reload
- [ ] Update BoardAdapter setters
- [ ] Test: Open project → boards + tasks load
- [ ] Test: Rotate → all data persists
- [ ] Test: Move task → UI updates (via Dev 2's optimistic update)

**End of Day 6-8 Deliverable:**
- [ ] ProjectActivity refactored
- [ ] 684 → ~400 lines
- [ ] 9 manual reloads → 0
- [ ] All state in ViewModel
- [ ] Rotation test passes

---

#### **DAY 9-10: Integration Testing & Documentation**

**DAY 9: Testing**

Test matrix:
1. Login flow
2. Workspace list
3. Project CRUD
4. Board list
5. Rotation tests
6. Memory leak check

**DAY 10: Documentation**

Create:
- `DEV1_REFACTOR_SUMMARY.md`
- Code review checklist
- Migration guide for future features

---

## 👤 DEV 2: TASK + BOARD + INBOX

### **Module độc lập:**
1. Task CRUD operations
2. Board CRUD operations
3. Inbox screen
4. Task detail screen
5. Optimistic updates

### **Files thuộc sở hữu (100%):**
```
presentation/viewmodel/
  ├── TaskViewModel.java                    ← ENHANCE
  ├── TaskViewModelFactory.java             ← UPDATE
  ├── BoardViewModel.java                   ← CREATE/ENHANCE

feature/home/ui/
  ├── InboxActivity.java                    ← MAJOR REFACTOR
  └── Home/project/
      ├── CardDetailActivity.java           ← REFACTOR
      ├── TaskAdapter.java                  ← UPDATE
      └── BoardAdapter.java                 ← COLLABORATE (with Dev 1)

adapter/
  ├── TaskAdapter.java                      ← UPDATE
  └── BoardAdapter.java                     ← COLLABORATE
```

### **Không động đến (để Dev 1):**
- `AuthViewModel.java`
- `WorkspaceViewModel.java`
- `LoginActivity.java`
- `SignupActivity.java`
- `WorkspaceActivity.java`

---

## 📅 DEV 2 - WEEK BY WEEK PLAN

### **WEEK 1**

#### **DAY 1-2: BoardViewModel Complete** ⏱️ 16 hours

File: `presentation/viewmodel/BoardViewModel.java`

**Current:** Basic CRUD only

**Create complete implementation:**
```java
public class BoardViewModel extends ViewModel {
    // Dependencies
    private final GetBoardByIdUseCase getBoardByIdUseCase;
    private final GetBoardsByProjectUseCase getBoardsByProjectUseCase;
    private final CreateBoardUseCase createBoardUseCase;
    private final UpdateBoardUseCase updateBoardUseCase;
    private final DeleteBoardUseCase deleteBoardUseCase;
    private final GetTasksByBoardUseCase getTasksByBoardUseCase;
    
    // State
    private final MutableLiveData<List<Board>> boardsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Map<String, List<Task>>> tasksPerBoardLiveData = new MutableLiveData<>();
    private final MutableLiveData<Board> selectedBoardLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    
    public BoardViewModel(...) {
        // Inject dependencies
    }
    
    // Getters
    public LiveData<List<Board>> getBoards() { return boardsLiveData; }
    public LiveData<Map<String, List<Task>>> getTasksPerBoard() { return tasksPerBoardLiveData; }
    public LiveData<Board> getSelectedBoard() { return selectedBoardLiveData; }
    public LiveData<Boolean> isLoading() { return loadingLiveData; }
    public LiveData<String> getError() { return errorLiveData; }
    
    // ✅ Load boards with tasks
    public void loadBoardsForProject(String projectId) {
        loadingLiveData.setValue(true);
        
        getBoardsByProjectUseCase.execute(projectId, new Callback<List<Board>>() {
            @Override
            public void onSuccess(List<Board> boards) {
                boardsLiveData.setValue(boards);
                
                // Auto-load tasks for all boards
                loadTasksForAllBoards(boards);
            }
            
            @Override
            public void onError(String error) {
                errorLiveData.setValue(error);
                loadingLiveData.setValue(false);
            }
        });
    }
    
    private void loadTasksForAllBoards(List<Board> boards) {
        if (boards == null || boards.isEmpty()) {
            tasksPerBoardLiveData.setValue(new HashMap<>());
            loadingLiveData.setValue(false);
            return;
        }
        
        Map<String, List<Task>> tasksMap = new HashMap<>();
        AtomicInteger pending = new AtomicInteger(boards.size());
        
        for (Board board : boards) {
            getTasksByBoardUseCase.execute(board.getId(), new Callback<List<Task>>() {
                @Override
                public void onSuccess(List<Task> tasks) {
                    synchronized (tasksMap) {
                        tasksMap.put(board.getId(), tasks);
                        
                        if (pending.decrementAndGet() == 0) {
                            tasksPerBoardLiveData.setValue(tasksMap);
                            loadingLiveData.setValue(false);
                        }
                    }
                }
                
                @Override
                public void onError(String error) {
                    if (pending.decrementAndGet() == 0) {
                        loadingLiveData.setValue(false);
                    }
                }
            });
        }
    }
    
    // ✅ Create board - optimistic update
    public void createBoard(String projectId, String name) {
        List<Board> current = boardsLiveData.getValue();
        if (current == null) current = new ArrayList<>();
        
        // Create temp board
        Board temp = new Board("temp_" + System.currentTimeMillis(), name, projectId, 0.0);
        
        // Add immediately
        current.add(temp);
        boardsLiveData.setValue(new ArrayList<>(current));
        
        // Init empty task list
        Map<String, List<Task>> tasksMap = tasksPerBoardLiveData.getValue();
        if (tasksMap == null) tasksMap = new HashMap<>();
        tasksMap.put(temp.getId(), new ArrayList<>());
        tasksPerBoardLiveData.setValue(tasksMap);
        
        // Background API
        createBoardUseCase.execute(projectId, name, new Callback<Board>() {
            @Override
            public void onSuccess(Board board) {
                List<Board> updated = boardsLiveData.getValue();
                updated.remove(temp);
                updated.add(board);
                boardsLiveData.setValue(new ArrayList<>(updated));
                
                // Update tasks map with real ID
                Map<String, List<Task>> updatedMap = tasksPerBoardLiveData.getValue();
                updatedMap.remove(temp.getId());
                updatedMap.put(board.getId(), new ArrayList<>());
                tasksPerBoardLiveData.setValue(updatedMap);
            }
            
            @Override
            public void onError(String error) {
                // Rollback
                List<Board> updated = boardsLiveData.getValue();
                updated.remove(temp);
                boardsLiveData.setValue(new ArrayList<>(updated));
                errorLiveData.setValue(error);
            }
        });
    }
    
    // ✅ Delete board - optimistic update
    public void deleteBoard(String boardId) {
        List<Board> current = boardsLiveData.getValue();
        Board toDelete = null;
        
        for (Board b : current) {
            if (b.getId().equals(boardId)) {
                toDelete = b;
                break;
            }
        }
        
        if (toDelete != null) {
            final Board deleted = toDelete;
            
            // Remove immediately
            current.remove(deleted);
            boardsLiveData.setValue(new ArrayList<>(current));
            
            // Remove from tasks map
            Map<String, List<Task>> tasksMap = tasksPerBoardLiveData.getValue();
            tasksMap.remove(boardId);
            tasksPerBoardLiveData.setValue(tasksMap);
            
            // Background API
            deleteBoardUseCase.execute(boardId, new Callback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    // Already removed
                }
                
                @Override
                public void onError(String error) {
                    // Rollback
                    List<Board> updated = boardsLiveData.getValue();
                    updated.add(deleted);
                    boardsLiveData.setValue(new ArrayList<>(updated));
                    errorLiveData.setValue(error);
                }
            });
        }
    }
}
```

**Checklist:**
- [ ] BoardViewModel manages boards + tasks
- [ ] loadBoardsForProject auto-loads all tasks
- [ ] createBoard optimistic update
- [ ] deleteBoard optimistic update
- [ ] Test: Create board → instant UI
- [ ] Test: Delete board → instant remove

**End of Day 1-2 Deliverable:**
- [ ] BoardViewModel complete
- [ ] Optimistic updates working

---

#### **DAY 3-5: TaskViewModel Enhancement** ⏱️ 24 hours (CRITICAL)

File: `presentation/viewmodel/TaskViewModel.java`

**Current:** Has structure, Activities misuse it

**Enhancement:**
```java
public class TaskViewModel extends ViewModel {
    // Existing dependencies...
    
    // ✅ Add inbox tasks management
    private final MutableLiveData<List<Task>> inboxTasksLiveData = new MutableLiveData<>();
    
    public LiveData<List<Task>> getInboxTasks() { return inboxTasksLiveData; }
    
    // ✅ Load inbox tasks
    public void loadInboxTasks(String userId) {
        loadingLiveData.setValue(true);
        
        getUserTasksUseCase.execute(userId, new Callback<List<Task>>() {
            @Override
            public void onSuccess(List<Task> tasks) {
                inboxTasksLiveData.setValue(tasks);
                loadingLiveData.setValue(false);
            }
            
            @Override
            public void onError(String error) {
                errorLiveData.setValue(error);
                loadingLiveData.setValue(false);
            }
        });
    }
    
    // ✅ Create task - optimistic update
    public void createTask(String boardId, String title) {
        // Create temp task
        Task temp = new Task(
            "temp_" + System.currentTimeMillis(),
            null, // project ID
            boardId,
            title,
            "",
            0.0,
            false
        );
        
        // Add to inbox immediately
        List<Task> current = inboxTasksLiveData.getValue();
        if (current == null) current = new ArrayList<>();
        current.add(0, temp);
        inboxTasksLiveData.setValue(new ArrayList<>(current));
        
        // Background API
        createTaskUseCase.execute(boardId, title, new Callback<Task>() {
            @Override
            public void onSuccess(Task task) {
                List<Task> updated = inboxTasksLiveData.getValue();
                updated.remove(temp);
                updated.add(0, task);
                inboxTasksLiveData.setValue(new ArrayList<>(updated));
            }
            
            @Override
            public void onError(String error) {
                // Rollback
                List<Task> updated = inboxTasksLiveData.getValue();
                updated.remove(temp);
                inboxTasksLiveData.setValue(new ArrayList<>(updated));
                errorLiveData.setValue(error);
            }
        });
    }
    
    // ✅ Update task - optimistic update
    public void updateTask(Task task) {
        // Find and update immediately
        List<Task> current = inboxTasksLiveData.getValue();
        if (current != null) {
            for (int i = 0; i < current.size(); i++) {
                if (current.get(i).getId().equals(task.getId())) {
                    current.set(i, task);
                    break;
                }
            }
            inboxTasksLiveData.setValue(new ArrayList<>(current));
        }
        
        // Background API
        updateTaskUseCase.execute(task, new Callback<Task>() {
            @Override
            public void onSuccess(Task updated) {
                // Already updated optimistically
            }
            
            @Override
            public void onError(String error) {
                errorLiveData.setValue(error);
                // Could rollback here
            }
        });
    }
    
    // ✅ Delete task - optimistic update
    public void deleteTask(String taskId) {
        List<Task> current = inboxTasksLiveData.getValue();
        if (current != null) {
            current.removeIf(t -> t.getId().equals(taskId));
            inboxTasksLiveData.setValue(new ArrayList<>(current));
        }
        
        // Background API
        deleteTaskUseCase.execute(taskId, new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Already removed
            }
            
            @Override
            public void onError(String error) {
                errorLiveData.setValue(error);
            }
        });
    }
    
    // ✅ Toggle complete - optimistic update
    public void toggleTaskComplete(Task task) {
        Task updated = new Task(
            task.getId(),
            task.getProjectId(),
            task.getBoardId(),
            task.getTitle(),
            task.getDescription(),
            task.getPosition(),
            !task.isCompleted() // Toggle
        );
        
        updateTask(updated);
    }
    
    // ✅ Move task to board - optimistic update
    public void moveTaskToBoard(String taskId, String targetBoardId, double newPosition) {
        // Find task
        List<Task> current = inboxTasksLiveData.getValue();
        Task taskToMove = null;
        
        if (current != null) {
            for (Task t : current) {
                if (t.getId().equals(taskId)) {
                    taskToMove = t;
                    break;
                }
            }
        }
        
        if (taskToMove != null) {
            // Update immediately
            Task moved = new Task(
                taskToMove.getId(),
                taskToMove.getProjectId(),
                targetBoardId, // New board
                taskToMove.getTitle(),
                taskToMove.getDescription(),
                newPosition,
                taskToMove.isCompleted()
            );
            
            updateTask(moved);
        }
        
        // Background API
        moveTaskToBoardUseCase.execute(taskId, targetBoardId, newPosition, new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Already moved optimistically
            }
            
            @Override
            public void onError(String error) {
                errorLiveData.setValue(error);
            }
        });
    }
}
```

**Checklist:**
- [ ] inboxTasksLiveData added
- [ ] loadInboxTasks implemented
- [ ] createTask optimistic update
- [ ] updateTask optimistic update
- [ ] deleteTask optimistic update
- [ ] toggleTaskComplete optimistic update
- [ ] moveTaskToBoard optimistic update
- [ ] Test each operation

**End of Day 3-5 Deliverable:**
- [ ] TaskViewModel complete with all optimistic updates
- [ ] Ready for InboxActivity refactor

---

### **WEEK 2**

#### **DAY 6-8: InboxActivity MAJOR REFACTOR** ⏱️ 24 hours (CRITICAL)

File: `feature/home/ui/InboxActivity.java`

**Current:** 809 lines, reloads everywhere

**Target:** ~500 lines, observe once

**Step-by-step:**

**DAY 6: Remove Manual Reloads**
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.inbox_main);
    
    setupViewModel();
    initViews();
    setupRecyclerView();
    setupSwipeRefresh();
    observeViewModel(); // ✅ Observe once
    
    // ✅ Trigger load once
    String userId = getCurrentUserId();
    taskViewModel.loadInboxTasks(userId);
}

// ❌ DELETE onResume reload
@Override
protected void onResume() {
    // EMPTY - LiveData auto-updates
}

private void observeViewModel() {
    // ✅ Inbox tasks observer
    taskViewModel.getInboxTasks().observe(this, tasks -> {
        if (tasks != null && !tasks.isEmpty()) {
            taskAdapter.setTasks(tasks);
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    });
    
    // Loading state
    taskViewModel.isLoading().observe(this, isLoading -> {
        if (isLoading) {
            loadingView.setVisibility(View.VISIBLE);
        } else {
            loadingView.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
        }
    });
    
    // Error state
    taskViewModel.getError().observe(this, error -> {
        if (error != null && !error.isEmpty()) {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            taskViewModel.clearError();
        }
    });
}
```

**DAY 7: Refactor Action Handlers**
```java
// ❌ OLD
private void createTask(String title) {
    taskViewModel.createTask(...);
    loadAllTasks(); // DELETE THIS
}

// ✅ NEW
private void createTask(String title) {
    String boardId = getDefaultBoardId();
    taskViewModel.createTask(boardId, title);
    // Observer auto-updates UI
}

// ❌ OLD
private void handleTaskCompleted(Task task) {
    taskViewModel.updateTask(...);
    loadAllTasks(); // DELETE THIS
}

// ✅ NEW
private void handleTaskCompleted(Task task) {
    taskViewModel.toggleTaskComplete(task);
    // Observer auto-updates UI
}

// ❌ OLD
private void deleteTask(Task task) {
    taskViewModel.deleteTask(task.getId());
    loadAllTasks(); // DELETE THIS
}

// ✅ NEW
private void deleteTask(Task task) {
    taskViewModel.deleteTask(task.getId());
    // Observer auto-updates UI
}
```

**DAY 8: Swipe Refresh + Testing**
```java
private void setupSwipeRefresh() {
    swipeRefreshLayout.setOnRefreshListener(() -> {
        String userId = getCurrentUserId();
        taskViewModel.loadInboxTasks(userId);
        // Observer handles UI update
        // Loading observer hides swipeRefreshLayout spinner
    });
}
```

**Checklist:**
- [ ] Delete loadAllTasks() method
- [ ] Remove onCreate reload
- [ ] Remove onResume reload
- [ ] Remove all manual reloads after actions
- [ ] Observe inboxTasks once
- [ ] Test: Create task → instant UI
- [ ] Test: Complete task → instant toggle
- [ ] Test: Delete task → instant remove
- [ ] Test: Swipe refresh → reload
- [ ] Test: Rotate → list persists

**End of Day 6-8 Deliverable:**
- [ ] InboxActivity refactored
- [ ] 809 → ~500 lines
- [ ] 0 manual reloads
- [ ] All optimistic updates working

---

#### **DAY 9: CardDetailActivity Refactor** ⏱️ 8 hours

File: `feature/home/ui/Home/project/CardDetailActivity.java`

**Refactor:**
```java
public class CardDetailActivity extends AppCompatActivity {
    private TaskViewModel taskViewModel;
    private Task currentTask;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        String taskId = getIntent().getStringExtra("TASK_ID");
        
        setupViewModel();
        initViews();
        observeViewModel();
        
        // Load task
        taskViewModel.loadTaskById(taskId);
    }
    
    private void observeViewModel() {
        // ✅ Observe selected task
        taskViewModel.getSelectedTask().observe(this, task -> {
            if (task != null) {
                currentTask = task;
                updateUI(task);
            }
        });
    }
    
    private void updateTaskTitle(String newTitle) {
        if (currentTask == null) return;
        
        Task updated = new Task(
            currentTask.getId(),
            currentTask.getProjectId(),
            currentTask.getBoardId(),
            newTitle, // Updated
            currentTask.getDescription(),
            currentTask.getPosition(),
            currentTask.isCompleted()
        );
        
        // ✅ Update via ViewModel
        taskViewModel.updateTask(updated);
        // Observer auto-updates UI
    }
}
```

**Checklist:**
- [ ] Observe selectedTask
- [ ] All updates via ViewModel
- [ ] Test: Edit title → parent Activity updates

**End of Day 9 Deliverable:**
- [ ] CardDetailActivity refactored
- [ ] Parent Activities auto-update

---

#### **DAY 10: Integration Testing**

Test all features with Dev 1's code:
1. Create task in Inbox → instant
2. Complete task → instant toggle
3. Open ProjectActivity → move task → instant
4. Rotate during operations
5. Memory leak check

**Deliverable:**
- [ ] All test cases pass
- [ ] Documentation: `DEV2_REFACTOR_SUMMARY.md`

---

## 🎯 DAILY SYNC (Both Devs)

### **Daily Standup (15 min):**
- What I completed yesterday
- What I'm doing today
- Any blockers

### **Code Review (30 min/day):**
- Review partner's PR
- Check for anti-patterns
- Ensure no manual reloads

### **Weekly Sync (1 hour):**
- Demo progress
- Integration planning
- Adjust timeline if needed

---

## ✅ FINAL CHECKLIST

### **Dev 1:**
- [ ] AuthViewModel với auto-session
- [ ] Login/Signup auto-navigate
- [ ] MainActivity auto-redirect
- [ ] WorkspaceViewModel optimistic updates
- [ ] WorkspaceActivity zero reloads
- [ ] ProjectViewModel manages boards + tasks
- [ ] ProjectActivity major refactor (684 → 400 lines)
- [ ] All rotation tests pass

### **Dev 2:**
- [ ] BoardViewModel complete
- [ ] TaskViewModel với optimistic updates
- [ ] InboxActivity major refactor (809 → 500 lines)
- [ ] CardDetailActivity refactor
- [ ] All action handlers optimistic
- [ ] All rotation tests pass

### **Both:**
- [ ] Zero manual reload calls
- [ ] All LiveData observed once
- [ ] Performance: 0ms perceived delay
- [ ] Memory: No leaks
- [ ] Documentation complete

---

**GOOD LUCK! 🚀**
