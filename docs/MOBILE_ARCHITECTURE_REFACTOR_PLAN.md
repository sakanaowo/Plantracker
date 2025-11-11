# PHÂN TÍCH VÀ KẾ HOẠCH REFACTOR TOÀN BỘ MOBILE APP

## 📋 TÓM TẮT EXECUTIVE

**Vấn đề:** Toàn bộ app đang vi phạm MVVM architecture - Activities đang trực tiếp gọi API, reload thủ công, quản lý state trong Activity thay vì ViewModel.

**Tác động:**
- ❌ Mất trạng thái khi xoay màn hình (configuration changes)
- ❌ Loading spinner xuất hiện liên tục gây khó chịu
- ❌ Reload thủ công ở mọi nơi: onCreate, onResume, onActivityResult
- ❌ Duplicate code logic khắp nơi
- ❌ Khó test, khó maintain
- ❌ User experience tệ (delay 800-1300ms mỗi action)

**Giải pháp:** Refactor hoàn toàn sang MVVM chuẩn với:
- ✅ Single source of truth trong ViewModel LiveData
- ✅ Observe once trong onCreate, không reload thủ công
- ✅ ViewModel survive configuration changes
- ✅ Optimistic updates cho instant feedback
- ✅ Repository pattern với caching

**Timeline:** 10-12 ngày (2 FE dev song song)

---

## 🔍 PHÂN TÍCH CHI TIẾT

### **1. ANTI-PATTERNS HIỆN TẠI**

#### **A. ProjectActivity (684 lines) - WORST OFFENDER**

```java
// ❌ ANTI-PATTERN 1: Manual reload everywhere
@Override
protected void onCreate(Bundle savedInstanceState) {
    loadProjectData();      // Load #1
    loadBoards();           // Load #2
    loadTasksForAllBoards(); // Load #3
}

@Override
protected void onResume() {
    loadProjectData();      // Reload #4 - UNNECESSARY
    loadBoards();           // Reload #5
}

@Override
protected void onActivityResult(...) {
    loadBoards();           // Reload #6
    loadTasksForBoard(boardId); // Reload #7
}

private void onMoveTaskToBoard(Task task, Board targetBoard) {
    // Move task
    taskViewModel.moveTaskToBoard(...);
    
    // ❌ ANTI-PATTERN 2: Manual reload after API call
    new Handler().postDelayed(() -> {
        loadTasksForBoard(currentBoard.getId());  // Reload #8
        loadTasksForBoard(targetBoard.getId());   // Reload #9
    }, 800);
}

// ❌ ANTI-PATTERN 3: State managed in Activity
private final Map<String, List<Task>> tasksPerBoard = new HashMap<>();
private List<Board> boards = new ArrayList<>();
// ⚠️ These should be in ViewModel LiveData!

// ❌ ANTI-PATTERN 4: Direct Adapter manipulation
boardAdapter.notifyDataSetChanged();
// Should observe LiveData instead
```

**Problems:**
- **9 manual reloads** trong một Activity
- State mất khi rotate màn hình
- Loading spinner xuất hiện 9 lần
- No caching - mỗi lần gọi API mới
- Memory leak risk (Handler in Activity)

---

#### **B. WorkspaceActivity (260 lines) - MEDIUM SEVERITY**

```java
// ❌ ANTI-PATTERN: Manual reload pattern
private void loadProjects() {
    workspaceViewModel.loadWorkspaceProjects(workspaceId);
    // Then manually call in 3 places:
    // - onCreate()
    // - After create project
    // - After delete project
}

// ❌ ANTI-PATTERN: Manual observation setup
projectViewModel.isProjectCreated().observe(this, created -> {
    if (created) {
        loadProjects(); // Manual reload again!
    }
});

projectViewModel.isProjectDeleted().observe(this, deleted -> {
    if (deleted) {
        loadProjects(); // Manual reload again!
    }
});
```

**Problems:**
- 3-4 manual reload calls
- Boolean flags for operation success (anti-pattern)
- Should auto-update LiveData instead

---

#### **C. InboxActivity (809 lines) - CRITICAL**

```java
// ❌ ANTI-PATTERN: Reload on every lifecycle
@Override
protected void onCreate(Bundle savedInstanceState) {
    loadAllTasks();  // Load #1
}

@Override
protected void onResume() {
    loadAllTasks();  // Load #2 - User sees loading every time
}

private void loadAllTasks() {
    // ❌ Direct API call pattern
    repositoryWithCache.getUserTasks(userId, new Callback() {
        @Override
        public void onSuccess(List<Task> tasks) {
            taskAdapter.setTasks(tasks);  // Manual update
        }
    });
}

// ❌ ANTI-PATTERN: After every action
private void createTask(String title) {
    taskViewModel.createTask(...);
    loadAllTasks(); // Reload entire list!
}

private void handleTaskCompleted(Task task) {
    taskViewModel.updateTask(...);
    loadAllTasks(); // Reload entire list!
}
```

**Problems:**
- Loads twice on screen open
- Reloads entire list after every single action
- No optimistic updates
- Cache exists but misused

---

### **2. VIEWMODELS HIỆN TẠI - ĐÃ ĐÚNG 60%**

#### **✅ TaskViewModel - Structure OK**
```java
public class TaskViewModel extends ViewModel {
    // ✅ GOOD: LiveData pattern
    private final MutableLiveData<List<Task>> tasksLiveData = new MutableLiveData<>();
    private final MutableLiveData<Task> selectedTaskLiveData = new MutableLiveData<>();
    
    // ✅ GOOD: UseCase injection
    private final GetTasksByBoardUseCase getTasksByBoardUseCase;
    
    // ✅ GOOD: Public getter
    public LiveData<List<Task>> getTasks() {
        return tasksLiveData;
    }
    
    // ❌ BAD: Method requires manual call
    public void loadTasksForBoard(String boardId) {
        // Activities calling this everywhere instead of auto-observe
    }
}
```

**What's good:** Structure follows MVVM
**What's bad:** Activities misuse it with manual calls

---

#### **⚠️ BoardViewModel - Incomplete**
```java
public class BoardViewModel extends ViewModel {
    // Has basic CRUD
    // ❌ MISSING: tasksPerBoard management
    // ❌ MISSING: Cache/sync logic
    // ❌ MISSING: Real-time updates
}
```

---

### **3. ROOT CAUSE ANALYSIS**

```
┌─────────────────────────────────────────────────────┐
│ CURRENT (WRONG) ARCHITECTURE                        │
├─────────────────────────────────────────────────────┤
│                                                     │
│  Activity                                           │
│    │                                                │
│    ├─ onCreate() ────────► loadData() ─► API ─► UI │
│    ├─ onResume() ────────► loadData() ─► API ─► UI │
│    ├─ onResult() ─────────► loadData() ─► API ─► UI │
│    ├─ after action ───────► loadData() ─► API ─► UI │
│    │                                                │
│    └─ Manages: List<Task>, List<Board> ❌          │
│       (Lost on rotate!)                             │
│                                                     │
│  ViewModel                                          │
│    └─ Has LiveData but Activities don't observe ❌  │
│                                                     │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│ CORRECT MVVM ARCHITECTURE                           │
├─────────────────────────────────────────────────────┤
│                                                     │
│  Activity                                           │
│    └─ onCreate() {                                  │
│         viewModel.getData().observe(this, data -> { │
│             adapter.setData(data);  // Auto-update  │
│         });                                         │
│       }                                             │
│       // NO MANUAL RELOAD ANYWHERE! ✅              │
│                                                     │
│  ViewModel                                          │
│    ├─ LiveData<List<Task>> (Survives rotate) ✅     │
│    ├─ LiveData<List<Board>> (Survives rotate) ✅    │
│    │                                                │
│    └─ Methods {                                     │
│         createTask() {                              │
│             // Update LiveData immediately          │
│             currentList.add(newTask);               │
│             tasksLiveData.setValue(currentList);    │
│             // API call in background               │
│         }                                           │
│       }                                             │
│                                                     │
│  Repository (with Cache)                            │
│    └─ Returns LiveData from Room/Memory ✅          │
│                                                     │
└─────────────────────────────────────────────────────┘
```

---

## 🎯 REFACTOR PLAN - CHIA ĐỀU 2 DEV

### **PRINCIPLE: Zero Blocking Dependencies**

Mỗi dev làm các modules **HOÀN TOÀN ĐỘC LẬP**, không đợi code người kia.

---

## 👤 DEV 1: AUTH, WORKSPACE, PROJECT CORE

### **WEEK 1 (5 days)**

#### **Day 1-2: AuthViewModel Refactor** ⭐⭐⭐

**File:** `presentation/viewmodel/AuthViewModel.java`

**Current Issues:**
```java
// Activities manually call login everywhere
authViewModel.login(email, password);
// Then manually navigate
if (success) startActivity(...);
```

**Refactor:**
```java
public class AuthViewModel extends ViewModel {
    // ✅ Single source of truth
    private final MutableLiveData<User> currentUserLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoggedInLiveData = new MutableLiveData<>();
    private final MutableLiveData<AuthState> authStateLiveData = new MutableLiveData<>();
    
    public enum AuthState {
        IDLE, LOGGING_IN, SUCCESS, ERROR
    }
    
    // Constructor auto-checks session
    public AuthViewModel(...) {
        checkStoredSession(); // Auto-restore on app start
    }
    
    public LiveData<User> getCurrentUser() { return currentUserLiveData; }
    public LiveData<Boolean> isLoggedIn() { return isLoggedInLiveData; }
    public LiveData<AuthState> getAuthState() { return authStateLiveData; }
    
    public void login(String email, String password) {
        authStateLiveData.setValue(AuthState.LOGGING_IN);
        
        loginUseCase.execute(email, password, new Callback() {
            @Override
            public void onSuccess(AuthResult result) {
                currentUserLiveData.setValue(result.getUser());
                isLoggedInLiveData.setValue(true);
                authStateLiveData.setValue(AuthState.SUCCESS);
                // Activity auto-navigates by observing authState
            }
        });
    }
}
```

**Activities to fix:**
- `LoginActivity.java` - Remove manual navigation
- `SignupActivity.java` - Remove manual navigation
- `MainActivity.java` - Auto-redirect based on isLoggedIn LiveData

**Deliverable:**
- [ ] AuthViewModel refactored with auto-session
- [ ] LoginActivity observes only, no manual calls
- [ ] SignupActivity observes only
- [ ] MainActivity auto-redirects via LiveData
- [ ] Test: Rotate during login - state preserved

---

#### **Day 3-4: WorkspaceViewModel Complete Refactor** ⭐⭐

**File:** `presentation/viewmodel/WorkspaceViewModel.java`

**Current Issue:**
```java
// WorkspaceActivity manually reloads
workspaceViewModel.loadWorkspaceProjects(id); // Called 3 times
```

**Refactor:**
```java
public class WorkspaceViewModel extends ViewModel {
    private final MutableLiveData<List<Workspace>> workspacesLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Project>> projectsLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> selectedWorkspaceIdLiveData = new MutableLiveData<>();
    
    // ✅ Auto-load when workspace selected
    public void selectWorkspace(String workspaceId) {
        selectedWorkspaceIdLiveData.setValue(workspaceId);
        loadProjectsForWorkspace(workspaceId); // Auto-load
    }
    
    // ✅ Create updates LiveData immediately
    public void createProject(String workspaceId, String name, String desc) {
        // Optimistic update
        List<Project> current = projectsLiveData.getValue();
        Project temp = new Project(null, name, desc, workspaceId);
        current.add(0, temp);
        projectsLiveData.setValue(new ArrayList<>(current));
        
        // Background API
        createProjectUseCase.execute(workspaceId, name, desc, new Callback() {
            @Override
            public void onSuccess(Project project) {
                // Replace temp with real
                current.remove(temp);
                current.add(0, project);
                projectsLiveData.setValue(new ArrayList<>(current));
            }
        });
    }
    
    // ✅ Delete updates LiveData immediately
    public void deleteProject(String projectId) {
        List<Project> current = projectsLiveData.getValue();
        current.removeIf(p -> p.getId().equals(projectId));
        projectsLiveData.setValue(new ArrayList<>(current));
        
        // Background API
        deleteProjectUseCase.execute(projectId, ...);
    }
}
```

**Activities to fix:**
- `WorkspaceActivity.java` - Remove all manual loadProjects() calls
- Observe projectsLiveData only once in onCreate

**Deliverable:**
- [ ] WorkspaceViewModel manages all state
- [ ] WorkspaceActivity observes once, never reloads
- [ ] Create/Delete project = instant UI update
- [ ] Test: Rotate during project list - list preserved

---

#### **Day 5: ProjectViewModel Enhanced** ⭐⭐

**File:** `presentation/viewmodel/ProjectViewModel.java`

**Enhancement:**
```java
public class ProjectViewModel extends ViewModel {
    // ✅ Add boards LiveData management
    private final MutableLiveData<List<Board>> boardsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Map<String, List<Task>>> tasksPerBoardLiveData = new MutableLiveData<>();
    
    public LiveData<List<Board>> getBoards() { return boardsLiveData; }
    public LiveData<Map<String, List<Task>>> getTasksPerBoard() { return tasksPerBoardLiveData; }
    
    // ✅ Load project auto-loads boards
    public void selectProject(String projectId) {
        loadProjectById(projectId);
        loadBoardsForProject(projectId);
    }
    
    private void loadBoardsForProject(String projectId) {
        // Loads all boards
        // Auto-triggers loadTasksForAllBoards
    }
}
```

**Deliverable:**
- [ ] ProjectViewModel manages boards + tasksPerBoard
- [ ] selectProject() auto-loads everything
- [ ] Test: Change project - all data updates

---

### **WEEK 2 (5 days)**

#### **Day 6-8: ProjectActivity MAJOR REFACTOR** ⭐⭐⭐ (CRITICAL)

**File:** `feature/home/ui/Home/ProjectActivity.java`

**Current:** 684 lines, 9 manual reload calls
**Target:** ~400 lines, 0 manual reload calls

**Refactor Steps:**

**Step 1: Remove State from Activity**
```java
// ❌ DELETE THESE
private List<Board> boards = new ArrayList<>();
private final Map<String, List<Task>> tasksPerBoard = new HashMap<>();

// ✅ State lives in ViewModel only
```

**Step 2: Setup Observation Once**
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    // Setup ViewModels
    setupViewModels();
    
    // Observe ONCE - never reload manually
    observeViewModels();
    
    // Trigger initial load
    String projectId = getIntent().getStringExtra("PROJECT_ID");
    projectViewModel.selectProject(projectId);
    // That's it! ViewModel auto-loads everything
}

private void observeViewModels() {
    // ✅ Boards observer
    projectViewModel.getBoards().observe(this, boards -> {
        if (boards != null) {
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
    
    // ✅ Loading observer (single spinner)
    projectViewModel.isLoading().observe(this, isLoading -> {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    });
}
```

**Step 3: Remove All Manual Reloads**
```java
// ❌ DELETE onResume reload
@Override
protected void onResume() {
    // DO NOTHING - LiveData auto-updates
}

// ❌ DELETE onActivityResult reload
@Override
protected void onActivityResult(...) {
    // DO NOTHING - LiveData auto-updates
}

// ❌ DELETE manual reload in move task
private void onMoveTaskToBoard(Task task, Board targetBoard) {
    // Just call ViewModel
    projectViewModel.moveTaskToBoard(task.getId(), targetBoard.getId(), 0.0);
    
    // ✅ ViewModel updates LiveData
    // ✅ Observer auto-updates UI
    // NO MANUAL RELOAD!
}
```

**Step 4: BoardAdapter Integration**
```java
// BoardAdapter receives data from LiveData
public class BoardAdapter {
    private List<Board> boards = new ArrayList<>();
    private Map<String, List<Task>> tasksPerBoard = new HashMap<>();
    
    public void setBoards(List<Board> boards) {
        this.boards = boards;
        notifyDataSetChanged();
    }
    
    public void setTasksPerBoard(Map<String, List<Task>> map) {
        this.tasksPerBoard = map;
        notifyDataSetChanged();
    }
}
```

**Deliverable:**
- [ ] ProjectActivity: onCreate observes once
- [ ] Remove all manual reload calls (9 → 0)
- [ ] Remove state from Activity
- [ ] All updates via LiveData observation
- [ ] Test: Rotate during task move - state preserved
- [ ] Test: Move task - instant UI update, no reload
- [ ] Performance: 0ms perceived delay

**Metrics:**
- Lines of code: 684 → ~400 (-40%)
- Manual reload calls: 9 → 0 (-100%)
- Loading spinners per action: 1-2 → 0 (-100%)

---

#### **Day 9-10: Integration Testing & Bug Fixes**

**Test Cases:**
1. Login → Navigate to Workspace → List persists on rotate
2. Create Project → Instant UI update → Rotate → Still there
3. Delete Project → Instant remove → Rotate → Correctly gone
4. Open Project → Boards load → Rotate → Boards persist
5. Move Task → Instant move → Rotate → Task in correct board
6. Background app → Resume → No reload, cached data

**Deliverable:**
- [ ] All test cases pass
- [ ] No regressions
- [ ] Performance improvement measured

---

## 👤 DEV 2: TASK, BOARD, INBOX OPERATIONS

### **WEEK 1 (5 days)**

#### **Day 1-2: BoardViewModel Complete Implementation** ⭐⭐⭐

**File:** `presentation/viewmodel/BoardViewModel.java`

**Current:** Basic CRUD only
**Target:** Full board + task management

**Implementation:**
```java
public class BoardViewModel extends ViewModel {
    // State
    private final MutableLiveData<List<Board>> boardsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Map<String, List<Task>>> tasksPerBoardLiveData = new MutableLiveData<>();
    
    // Loading states
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    
    // Getters
    public LiveData<List<Board>> getBoards() { return boardsLiveData; }
    public LiveData<Map<String, List<Task>>> getTasksPerBoard() { return tasksPerBoardLiveData; }
    
    // ✅ Load boards with tasks
    public void loadBoardsForProject(String projectId) {
        loadingLiveData.setValue(true);
        
        getBoardsByProjectUseCase.execute(projectId, new Callback() {
            @Override
            public void onSuccess(List<Board> boards) {
                boardsLiveData.setValue(boards);
                
                // Auto-load tasks for all boards
                loadTasksForAllBoards(boards);
            }
        });
    }
    
    private void loadTasksForAllBoards(List<Board> boards) {
        Map<String, List<Task>> tasksMap = new HashMap<>();
        AtomicInteger pending = new AtomicInteger(boards.size());
        
        for (Board board : boards) {
            getTasksByBoardUseCase.execute(board.getId(), new Callback() {
                @Override
                public void onSuccess(List<Task> tasks) {
                    tasksMap.put(board.getId(), tasks);
                    
                    if (pending.decrementAndGet() == 0) {
                        tasksPerBoardLiveData.setValue(tasksMap);
                        loadingLiveData.setValue(false);
                    }
                }
            });
        }
    }
    
    // ✅ Create board - optimistic update
    public void createBoard(String projectId, String name) {
        List<Board> current = boardsLiveData.getValue();
        Board temp = new Board(null, name, projectId, 0.0);
        current.add(temp);
        boardsLiveData.setValue(new ArrayList<>(current));
        
        createBoardUseCase.execute(projectId, name, new Callback() {
            @Override
            public void onSuccess(Board board) {
                current.remove(temp);
                current.add(board);
                boardsLiveData.setValue(new ArrayList<>(current));
                
                // Init empty task list
                Map<String, List<Task>> tasksMap = tasksPerBoardLiveData.getValue();
                tasksMap.put(board.getId(), new ArrayList<>());
                tasksPerBoardLiveData.setValue(tasksMap);
            }
        });
    }
}
```

**Deliverable:**
- [ ] BoardViewModel manages boards + tasksPerBoard
- [ ] Optimistic updates for create/delete board
- [ ] loadBoardsForProject auto-loads all tasks
- [ ] Test: Create board - instant UI update

---

#### **Day 3-5: TaskViewModel MAJOR Enhancement** ⭐⭐⭐ (CRITICAL)

**File:** `presentation/viewmodel/TaskViewModel.java`

**Current:** Has structure but Activities misuse
**Target:** Complete task operations with optimistic updates

**Enhancement:**
```java
public class TaskViewModel extends ViewModel {
    // ✅ Add inbox tasks LiveData
    private final MutableLiveData<List<Task>> inboxTasksLiveData = new MutableLiveData<>();
    
    public LiveData<List<Task>> getInboxTasks() { return inboxTasksLiveData; }
    
    // ✅ Load inbox tasks once
    public void loadInboxTasks(String userId) {
        getUserTasksUseCase.execute(userId, new Callback() {
            @Override
            public void onSuccess(List<Task> tasks) {
                inboxTasksLiveData.setValue(tasks);
            }
        });
    }
    
    // ✅ Create task - optimistic update
    public void createTask(String boardId, String title) {
        // Create temp task
        Task temp = new Task(null, null, boardId, title, "", 0.0);
        
        // Add to inbox immediately
        List<Task> current = inboxTasksLiveData.getValue();
        current.add(0, temp);
        inboxTasksLiveData.setValue(new ArrayList<>(current));
        
        // Background API
        createTaskUseCase.execute(boardId, title, new Callback() {
            @Override
            public void onSuccess(Task task) {
                current.remove(temp);
                current.add(0, task);
                inboxTasksLiveData.setValue(new ArrayList<>(current));
            }
        });
    }
    
    // ✅ Update task - optimistic update
    public void updateTask(Task task) {
        // Update in list immediately
        List<Task> current = inboxTasksLiveData.getValue();
        for (int i = 0; i < current.size(); i++) {
            if (current.get(i).getId().equals(task.getId())) {
                current.set(i, task);
                break;
            }
        }
        inboxTasksLiveData.setValue(new ArrayList<>(current));
        
        // Background API
        updateTaskUseCase.execute(task, ...);
    }
    
    // ✅ Delete task - optimistic update
    public void deleteTask(String taskId) {
        List<Task> current = inboxTasksLiveData.getValue();
        current.removeIf(t -> t.getId().equals(taskId));
        inboxTasksLiveData.setValue(new ArrayList<>(current));
        
        // Background API
        deleteTaskUseCase.execute(taskId, ...);
    }
    
    // ✅ Complete task - optimistic update
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
}
```

**Deliverable:**
- [ ] TaskViewModel manages inbox tasks LiveData
- [ ] All CRUD operations with optimistic updates
- [ ] No manual reload needed anywhere
- [ ] Test: Create/Update/Delete - instant UI feedback

---

### **WEEK 2 (5 days)**

#### **Day 6-8: InboxActivity MAJOR REFACTOR** ⭐⭐⭐ (CRITICAL)

**File:** `feature/home/ui/InboxActivity.java`

**Current:** 809 lines, reloads on onCreate + onResume + after every action
**Target:** ~500 lines, observe once only

**Refactor Steps:**

**Step 1: Remove Manual Reloads**
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    setupViewModel();
    initViews();
    
    // ✅ Observe once
    observeViewModel();
    
    // ✅ Trigger initial load once
    String userId = getCurrentUserId();
    taskViewModel.loadInboxTasks(userId);
}

// ❌ DELETE onResume reload
@Override
protected void onResume() {
    // DO NOTHING - LiveData auto-updates
}

private void observeViewModel() {
    // ✅ Single observer
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
    
    taskViewModel.isLoading().observe(this, isLoading -> {
        if (isLoading) {
            loadingView.setVisibility(View.VISIBLE);
        } else {
            loadingView.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
        }
    });
}
```

**Step 2: Remove Reload After Actions**
```java
// ❌ OLD
private void createTask(String title) {
    taskViewModel.createTask(...);
    loadAllTasks(); // Manual reload - DELETE THIS
}

// ✅ NEW
private void createTask(String title) {
    taskViewModel.createTask(currentBoardId, title);
    // That's it! ViewModel updates LiveData
    // Observer auto-updates UI
}

// ❌ OLD
private void handleTaskCompleted(Task task) {
    taskViewModel.updateTask(...);
    loadAllTasks(); // Manual reload - DELETE THIS
}

// ✅ NEW
private void handleTaskCompleted(Task task) {
    taskViewModel.toggleTaskComplete(task);
    // That's it! ViewModel updates LiveData
}
```

**Step 3: Swipe Refresh**
```java
private void setupSwipeRefresh() {
    swipeRefreshLayout.setOnRefreshListener(() -> {
        // Just reload from ViewModel
        String userId = getCurrentUserId();
        taskViewModel.loadInboxTasks(userId);
        // Observer handles UI update
    });
}
```

**Deliverable:**
- [ ] InboxActivity: onCreate observes once
- [ ] Remove onResume reload
- [ ] Remove all manual reloads after actions
- [ ] Swipe refresh works via ViewModel
- [ ] Test: Create task - instant UI update
- [ ] Test: Complete task - instant checkbox toggle
- [ ] Test: Rotate - list preserved
- [ ] Performance: 0ms perceived delay

**Metrics:**
- Lines of code: 809 → ~500 (-38%)
- Manual reload calls: 5+ → 0 (-100%)
- Perceived delay: 800ms → 0ms (-100%)

---

#### **Day 9-10: CardDetailActivity Refactor** ⭐⭐

**File:** `feature/home/ui/Home/project/CardDetailActivity.java`

**Refactor:**
```java
// ✅ Observe selected task from ViewModel
taskViewModel.getSelectedTask().observe(this, task -> {
    if (task != null) {
        updateUI(task);
    }
});

// ✅ Update operations
private void updateTaskTitle(String newTitle) {
    Task current = taskViewModel.getSelectedTask().getValue();
    Task updated = current.withTitle(newTitle);
    taskViewModel.updateTask(updated);
    // Auto-updates via observer
}
```

**Deliverable:**
- [ ] CardDetailActivity observes selectedTask
- [ ] All updates via ViewModel
- [ ] Test: Edit task - parent Activity auto-updates

---

## 📊 INTEGRATION & TESTING (Both Devs)

### **Day 11-12: Integration Testing**

**Test Matrix:**

| Feature | Test Case | Expected Result |
|---------|-----------|-----------------|
| **Auth** | Login → Rotate | User stays logged in |
| **Workspace** | Create Project → Rotate | Project in list |
| **Project** | Open → Rotate → Boards | Boards persist |
| **Board** | Create Board → Rotate | Board persists |
| **Task** | Move Task → Rotate | Task in new board |
| **Inbox** | Create Task → Rotate | Task persists |
| **Performance** | Task move | 0ms delay, instant |
| **Memory** | Open 10 projects | No leak |

**Deliverable:**
- [ ] All test cases pass
- [ ] No manual reload anywhere in codebase
- [ ] All LiveData observed once in onCreate
- [ ] Performance benchmarks met

---

## 🎨 CODE STANDARDS

### **ViewModel Template**
```java
public class XxxViewModel extends ViewModel {
    // ========== Dependencies ==========
    private final XxxUseCase useCase;
    
    // ========== State (LiveData) ==========
    private final MutableLiveData<List<Xxx>> itemsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    
    // ========== Constructor ==========
    public XxxViewModel(XxxUseCase useCase) {
        this.useCase = useCase;
    }
    
    // ========== Public Getters ==========
    public LiveData<List<Xxx>> getItems() { return itemsLiveData; }
    public LiveData<Boolean> isLoading() { return loadingLiveData; }
    public LiveData<String> getError() { return errorLiveData; }
    
    // ========== Public Methods ==========
    public void loadItems() {
        loadingLiveData.setValue(true);
        
        useCase.execute(new Callback() {
            @Override
            public void onSuccess(List<Xxx> items) {
                itemsLiveData.setValue(items);
                loadingLiveData.setValue(false);
            }
            
            @Override
            public void onError(String error) {
                errorLiveData.setValue(error);
                loadingLiveData.setValue(false);
            }
        });
    }
    
    public void createItem(Xxx item) {
        // Optimistic update
        List<Xxx> current = itemsLiveData.getValue();
        current.add(0, item);
        itemsLiveData.setValue(new ArrayList<>(current));
        
        // Background API
        createUseCase.execute(item, ...);
    }
}
```

### **Activity Template**
```java
public class XxxActivity extends AppCompatActivity {
    private XxxViewModel viewModel;
    private XxxAdapter adapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setupViewModel();
        setupUI();
        observeViewModel(); // ✅ Observe once
        
        // Trigger initial load
        viewModel.loadItems();
    }
    
    private void observeViewModel() {
        viewModel.getItems().observe(this, items -> {
            adapter.setItems(items);
        });
        
        viewModel.isLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? VISIBLE : GONE);
        });
    }
    
    // ❌ NO onResume reload
    // ❌ NO onActivityResult reload
    // ❌ NO manual reload after actions
}
```

---

## 📈 SUCCESS METRICS

### **Before Refactor**
- Manual reload calls: **~30+ across all Activities**
- Loading spinners per screen: **2-3 times**
- Perceived delay per action: **800-1300ms**
- Configuration change: **Lose all state, reload everything**
- Code duplication: **High (reload logic everywhere)**

### **After Refactor**
- Manual reload calls: **0** ✅
- Loading spinners per screen: **1 time on initial load only** ✅
- Perceived delay per action: **0ms (optimistic updates)** ✅
- Configuration change: **Preserve all state** ✅
- Code duplication: **Low (single observer pattern)** ✅

---

## ⚠️ CRITICAL RULES FOR BOTH DEVS

### **DO:**
- ✅ State lives in ViewModel LiveData
- ✅ Observe LiveData once in onCreate
- ✅ ViewModel methods update LiveData immediately (optimistic)
- ✅ Use optimistic updates for instant feedback
- ✅ Test rotation after every feature

### **DON'T:**
- ❌ Manual reload in onResume
- ❌ Manual reload in onActivityResult
- ❌ Manual reload after API calls
- ❌ State in Activity (List<>, Map<>)
- ❌ Boolean flags for operation success
- ❌ Direct Adapter updates (use LiveData)

---

## 🚀 TIMELINE SUMMARY

| Week | Dev 1 | Dev 2 |
|------|-------|-------|
| **Week 1** | AuthViewModel (2d)<br>WorkspaceViewModel (2d)<br>ProjectViewModel (1d) | BoardViewModel (2d)<br>TaskViewModel (3d) |
| **Week 2** | ProjectActivity (3d)<br>Integration (2d) | InboxActivity (3d)<br>CardDetail (1d)<br>Integration (1d) |

**Total:** 10 working days (2 weeks)

---

## 📝 DAILY CHECKLIST

### **Dev 1 Daily:**
- [ ] Pushed code to branch: `dev1/auth-workspace-refactor`
- [ ] ViewModel updates LiveData, not manual reload
- [ ] Activity observes once in onCreate
- [ ] Tested rotation - state preserved
- [ ] No blocking code for Dev 2

### **Dev 2 Daily:**
- [ ] Pushed code to branch: `dev2/task-board-refactor`
- [ ] ViewModel updates LiveData, not manual reload
- [ ] Activity observes once in onCreate
- [ ] Tested rotation - state preserved
- [ ] No blocking code for Dev 1

### **Both Daily:**
- [ ] Code review partner's PR
- [ ] Sync on shared interfaces (if any)
- [ ] Update progress in Slack/Discord

---

## 🎯 FINAL DELIVERABLES

### **Code Quality:**
- [ ] Zero manual reload calls
- [ ] All Activities observe LiveData once
- [ ] All ViewModels manage state
- [ ] 100% rotation test coverage

### **Performance:**
- [ ] 0ms perceived delay for all actions
- [ ] Loading spinner appears once per screen
- [ ] No memory leaks

### **Documentation:**
- [ ] Updated architecture diagram
- [ ] MVVM best practices guide
- [ ] Migration guide for future features

---

## 💡 LESSONS LEARNED

### **What Went Wrong Before:**
1. **Misunderstood MVVM** - Had ViewModels but Activities didn't use them correctly
2. **Manual reload mindset** - Thinking API call = must reload
3. **No optimistic updates** - Waiting for backend every time
4. **State in Activities** - Lost on configuration changes

### **What We Fixed:**
1. **True MVVM** - Single source of truth in ViewModel
2. **Reactive pattern** - LiveData auto-updates UI
3. **Optimistic updates** - Instant feedback, sync later
4. **Proper separation** - Activity = View, ViewModel = State

---

**END OF REFACTOR PLAN**
