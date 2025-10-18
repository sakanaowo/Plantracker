# 🎯 PHÂN CÔNG CHI TIẾT - PERSON 1 & PERSON 2

**Dự án:** Room Database Caching Implementation  
**Timeline:** 5-7 ngày làm việc  
**Mục tiêu:** Giảm API delay, thêm offline support

---

## 👤 PERSON 1: Database Infrastructure Specialist

**Focus:** Xây dựng nền tảng database layer, đảm bảo Room hoạt động ổn định

### 📋 Task List Chi Tiết

#### ✅ Day 1: Setup & Build (4-6 giờ)

**1.1. Review & Build Infrastructure**
```bash
□ Mở Android Studio
□ Sync Gradle (File → Sync Project with Gradle Files)
□ Build project (Build → Rebuild Project)
□ Kiểm tra không có errors
```

**Files cần review:**
- `data/local/database/AppDatabase.java` ✓ (Đã tạo)
- `data/local/database/entity/TaskEntity.java` ✓
- `data/local/database/entity/ProjectEntity.java` ✓
- `data/local/database/entity/WorkspaceEntity.java` ✓
- `data/local/database/dao/TaskDao.java` ✓
- `data/local/database/dao/ProjectDao.java` ✓
- `data/local/database/dao/WorkspaceDao.java` ✓
- `data/local/database/converter/DateConverter.java` ✓

**Verification Steps:**
```java
// Test code để verify Room đã generate code
// Tạo file: app/src/test/java/.../DatabaseTest.java

@Test
public void testDatabaseCreation() {
    Context context = ApplicationProvider.getApplicationContext();
    AppDatabase db = AppDatabase.getInstance(context);
    assertNotNull(db);
    assertNotNull(db.taskDao());
    assertNotNull(db.projectDao());
    assertNotNull(db.workspaceDao());
}
```

**Deliverable:**
- [ ] Project builds thành công không có errors
- [ ] Room generated classes xuất hiện trong `build/generated/`
- [ ] Screenshot kết quả build gửi cho team lead

---

**1.2. Test Basic DAO Operations**

Tạo file test: `app/src/test/java/.../dao/TaskDaoTest.java`

```java
import android.content.Context;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import com.example.tralalero.data.local.database.AppDatabase;
import com.example.tralalero.data.local.database.dao.TaskDao;
import com.example.tralalero.data.local.database.entity.TaskEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import java.util.Date;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class TaskDaoTest {
    private AppDatabase database;
    private TaskDao taskDao;

    @Before
    public void setup() {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        taskDao = database.taskDao();
    }

    @After
    public void cleanup() {
        database.close();
    }

    @Test
    public void testInsertAndRetrieveTask() {
        // Create test task
        TaskEntity task = new TaskEntity();
        task.setId("test-task-1");
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setStatus("TODO");
        task.setBoardId("board-1");
        task.setCreatedAt(new Date());

        // Insert
        taskDao.insertTask(task);

        // Retrieve
        TaskEntity retrieved = taskDao.getTaskByIdSync("test-task-1");

        // Assert
        assertNotNull(retrieved);
        assertEquals("Test Task", retrieved.getTitle());
        assertEquals("Test Description", retrieved.getDescription());
        assertEquals("TODO", retrieved.getStatus());
    }

    @Test
    public void testGetTasksByBoard() {
        // Insert multiple tasks
        for (int i = 1; i <= 5; i++) {
            TaskEntity task = new TaskEntity();
            task.setId("task-" + i);
            task.setTitle("Task " + i);
            task.setBoardId("board-1");
            task.setPosition(i);
            taskDao.insertTask(task);
        }

        // Retrieve
        List<TaskEntity> tasks = taskDao.getTasksByBoardSync("board-1");

        // Assert
        assertEquals(5, tasks.size());
        assertEquals("Task 1", tasks.get(0).getTitle());
    }

    @Test
    public void testUpdateTask() {
        // Insert
        TaskEntity task = new TaskEntity();
        task.setId("task-update");
        task.setTitle("Original Title");
        taskDao.insertTask(task);

        // Update
        task.setTitle("Updated Title");
        taskDao.updateTask(task);

        // Retrieve
        TaskEntity updated = taskDao.getTaskByIdSync("task-update");
        assertEquals("Updated Title", updated.getTitle());
    }

    @Test
    public void testDeleteTask() {
        // Insert
        TaskEntity task = new TaskEntity();
        task.setId("task-delete");
        task.setTitle("To Delete");
        taskDao.insertTask(task);

        // Delete
        taskDao.deleteTaskById("task-delete");

        // Verify
        TaskEntity deleted = taskDao.getTaskByIdSync("task-delete");
        assertNull(deleted);
    }
}
```

**Dependencies cần thêm vào build.gradle.kts:**
```kotlin
testImplementation("org.robolectric:robolectric:4.11.1")
testImplementation("androidx.test:core:1.5.0")
```

**Deliverable:**
- [ ] Tất cả tests pass
- [ ] CRUD operations hoạt động đúng
- [ ] Screenshot test results

---

#### ✅ Day 2: Entity Mappers & Integration (4-6 giờ)

**2.1. Test Entity Mappers**

Tạo file: `app/src/test/java/.../mapper/EntityMapperTest.java`

```java
import com.example.tralalero.data.local.database.entity.TaskEntity;
import com.example.tralalero.data.mapper.TaskEntityMapper;
import com.example.tralalero.domain.model.Task;
import org.junit.Test;
import java.util.Date;
import static org.junit.Assert.*;

public class EntityMapperTest {

    @Test
    public void testTaskToEntity() {
        // Create domain Task
        Task task = new Task();
        task.setId("task-1");
        task.setTitle("Domain Task");
        task.setDescription("Description");
        task.setStatus("IN_PROGRESS");
        task.setPriority("HIGH");
        task.setBoardId("board-1");
        task.setCreatedAt(new Date());

        // Convert to Entity
        TaskEntity entity = TaskEntityMapper.toEntity(task);

        // Assert
        assertNotNull(entity);
        assertEquals(task.getId(), entity.getId());
        assertEquals(task.getTitle(), entity.getTitle());
        assertEquals(task.getDescription(), entity.getDescription());
        assertEquals(task.getStatus(), entity.getStatus());
        assertFalse(entity.isDirty());
        assertTrue(entity.getCachedAt() > 0);
    }

    @Test
    public void testEntityToDomain() {
        // Create Entity
        TaskEntity entity = new TaskEntity();
        entity.setId("task-1");
        entity.setTitle("Entity Task");
        entity.setDescription("Description");
        entity.setStatus("DONE");
        entity.setPriority("LOW");
        entity.setBoardId("board-1");
        entity.setCreatedAt(new Date());

        // Convert to Domain
        Task task = TaskEntityMapper.toDomain(entity);

        // Assert
        assertNotNull(task);
        assertEquals(entity.getId(), task.getId());
        assertEquals(entity.getTitle(), task.getTitle());
        assertEquals(entity.getDescription(), task.getDescription());
        assertEquals(entity.getStatus(), task.getStatus());
    }

    @Test
    public void testListConversion() {
        // Create list of tasks
        List<Task> tasks = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Task task = new Task();
            task.setId("task-" + i);
            task.setTitle("Task " + i);
            tasks.add(task);
        }

        // Convert to entities
        List<TaskEntity> entities = TaskEntityMapper.toEntityList(tasks);
        assertEquals(3, entities.size());

        // Convert back to domain
        List<Task> converted = TaskEntityMapper.toDomainList(entities);
        assertEquals(3, converted.size());
        assertEquals("Task 1", converted.get(0).getTitle());
    }
}
```

**Deliverable:**
- [ ] Mapper tests pass
- [ ] Conversion không mất dữ liệu
- [ ] Handle null cases correctly

---

**2.2. Integrate DependencyProvider**

**File cần sửa:** `app/src/main/java/com/example/tralalero/App/App.java`

```java
package com.example.tralalero.App;

import android.app.Application;
import com.example.tralalero.auth.AuthManager;
import com.example.tralalero.core.DependencyProvider;
import com.google.firebase.FirebaseApp;
import android.util.Log;

public class App extends Application {
    private static final String TAG = "App";
    
    public static AuthManager authManager;
    public static DependencyProvider dependencyProvider;
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "App starting...");
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        
        // Initialize AuthManager
        authManager = new AuthManager(this);
        Log.d(TAG, "AuthManager initialized");
        
        // Initialize DependencyProvider (includes Database)
        dependencyProvider = DependencyProvider.getInstance(this, authManager);
        Log.d(TAG, "DependencyProvider initialized with Database");
    }
    
    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(TAG, "App terminating, clearing caches...");
        
        if (dependencyProvider != null) {
            dependencyProvider.clearAllCaches();
        }
    }
}
```

**Test DependencyProvider:**

```java
// Trong bất kỳ Activity nào, test xem DependencyProvider hoạt động:
public class TestDependencyActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Test get repositories
        TaskRepositoryImplWithCache taskRepo = App.dependencyProvider.getTaskRepositoryWithCache();
        Log.d("Test", "TaskRepository: " + (taskRepo != null ? "OK" : "FAIL"));
        
        IProjectRepository projectRepo = App.dependencyProvider.getProjectRepository();
        Log.d("Test", "ProjectRepository: " + (projectRepo != null ? "OK" : "FAIL"));
        
        // Test get DAOs
        TaskDao taskDao = App.dependencyProvider.getTaskDao();
        Log.d("Test", "TaskDao: " + (taskDao != null ? "OK" : "FAIL"));
        
        // Test database
        AppDatabase db = App.dependencyProvider.getDatabase();
        Log.d("Test", "Database: " + (db != null ? "OK" : "FAIL"));
    }
}
```

**Deliverable:**
- [ ] App.java updated và compiles
- [ ] DependencyProvider singleton works
- [ ] Có thể get repositories và DAOs
- [ ] No crashes on app start

---

#### ✅ Day 3: Clear Cache & Logout Integration (2-4 giờ)

**3.1. Implement Clear Cache on Logout**

Tìm file xử lý logout (có thể là `SettingsActivity.java`, `AccountActivity.java`, hoặc `AuthManager.java`)

**Example trong SettingsActivity.java:**

```java
private void handleLogout() {
    // Show confirmation dialog
    new AlertDialog.Builder(this)
        .setTitle("Đăng xuất")
        .setMessage("Bạn có chắc muốn đăng xuất?")
        .setPositiveButton("Đăng xuất", (dialog, which) -> {
            performLogout();
        })
        .setNegativeButton("Hủy", null)
        .show();
}

private void performLogout() {
    Log.d("Logout", "Starting logout process...");
    
    // 1. Clear authentication
    App.authManager.logout();
    Log.d("Logout", "Auth cleared");
    
    // 2. Clear all caches
    App.dependencyProvider.clearAllCaches();
    Log.d("Logout", "Database cache cleared");
    
    // 3. Reset DependencyProvider
    DependencyProvider.reset();
    Log.d("Logout", "DependencyProvider reset");
    
    // 4. Navigate to login
    Intent intent = new Intent(this, LoginActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
    finish();
    
    Log.d("Logout", "Logout complete");
}
```

**Test logout:**
1. Login vào app
2. Load một số data (tasks, projects)
3. Logout
4. Check logcat xem cache đã cleared
5. Login lại → không có data cũ

**Deliverable:**
- [ ] Logout clears all caches
- [ ] DependencyProvider reset properly
- [ ] No crashes
- [ ] Old data không còn sau logout

---

#### ✅ Day 4: Documentation & Handoff (2-3 giờ)

**4.1. Tạo Technical Documentation**

Viết document về infrastructure đã build:

```markdown
# Database Infrastructure - Technical Doc

## Overview
- AppDatabase: Singleton Room database
- 3 DAOs: TaskDao, ProjectDao, WorkspaceDao
- 3 Entities: TaskEntity, ProjectEntity, WorkspaceEntity
- TypeConverter: DateConverter for Date <-> Long

## How to Use

### Get Database Instance
```java
AppDatabase db = App.dependencyProvider.getDatabase();
```

### Get DAO
```java
TaskDao taskDao = App.dependencyProvider.getTaskDao();
```

### CRUD Operations
```java
// Insert
executorService.execute(() -> {
    TaskEntity task = new TaskEntity();
    task.setId("task-1");
    task.setTitle("New Task");
    taskDao.insertTask(task);
});

// Query
executorService.execute(() -> {
    List<TaskEntity> tasks = taskDao.getTasksByBoardSync("board-id");
    // Process tasks...
});
```

## Testing Results
- All DAO tests: PASS ✓
- Mapper tests: PASS ✓
- Integration tests: PASS ✓

## Known Issues
- None

## Next Steps
- Person 2 will implement cached repositories
- Migration of existing activities
```

**Deliverable:**
- [ ] Technical doc hoàn thành
- [ ] Test results documented
- [ ] Handoff meeting với Person 2

---

### 📊 Person 1 Summary Checklist

**Infrastructure:**
- [ ] Room Database builds successfully
- [ ] All DAOs tested and working
- [ ] Entity Mappers tested
- [ ] DateConverter works

**Integration:**
- [ ] DependencyProvider integrated
- [ ] App.java updated
- [ ] Clear cache on logout works
- [ ] Singleton pattern verified

**Testing:**
- [ ] Unit tests written and passing
- [ ] Integration tests passing
- [ ] Performance acceptable

**Documentation:**
- [ ] Technical doc completed
- [ ] Code commented
- [ ] Handoff done

---

## 👤 PERSON 2: Repository & UI Integration Specialist

**Focus:** Implement cached repositories, migrate UI, ensure smooth user experience

### 📋 Task List Chi Tiết

#### ✅ Day 1-2: Review & Implement Repositories (6-8 giờ)

**1.1. Review TaskRepositoryImplWithCache**

File đã tạo: `data/repository/TaskRepositoryImplWithCache.java`

**Checklist:**
- [ ] Đọc và hiểu caching strategy
- [ ] Review ExecutorService usage
- [ ] Review callback mechanism
- [ ] Hiểu flow: Cache → Callback → Network → Update Cache

**Test TaskRepositoryImplWithCache:**

```java
// Tạo test trong Activity hoặc test class
public void testCachedRepository() {
    TaskRepositoryImplWithCache repo = App.dependencyProvider.getTaskRepositoryWithCache();
    
    String boardId = "test-board-id";
    
    // First call - should fetch from network
    long start1 = System.currentTimeMillis();
    repo.getTasksByBoard(boardId, new ITaskRepository.RepositoryCallback<List<Task>>() {
        @Override
        public void onSuccess(List<Task> tasks) {
            long time1 = System.currentTimeMillis() - start1;
            Log.d("CacheTest", "First call: " + time1 + "ms, tasks: " + tasks.size());
        }
        
        @Override
        public void onError(String error) {
            Log.e("CacheTest", "Error: " + error);
        }
    });
    
    // Second call (after 2 seconds) - should return from cache instantly
    new Handler().postDelayed(() -> {
        long start2 = System.currentTimeMillis();
        repo.getTasksByBoard(boardId, new ITaskRepository.RepositoryCallback<List<Task>>() {
            @Override
            public void onSuccess(List<Task> tasks) {
                long time2 = System.currentTimeMillis() - start2;
                Log.d("CacheTest", "Second call (cached): " + time2 + "ms, tasks: " + tasks.size());
                // Expected: < 50ms
            }
            
            @Override
            public void onError(String error) {
                Log.e("CacheTest", "Error: " + error);
            }
        });
    }, 2000);
}
```

---

**1.2. Implement ProjectRepositoryImplWithCache**

Tạo file mới: `data/repository/ProjectRepositoryImplWithCache.java`

**Template (copy pattern từ TaskRepository):**

```java
package com.example.tralalero.data.repository;

import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.LiveData;
import com.example.tralalero.data.local.database.dao.ProjectDao;
import com.example.tralalero.data.local.database.entity.ProjectEntity;
import com.example.tralalero.data.mapper.ProjectEntityMapper;
import com.example.tralalero.data.mapper.ProjectMapper;
import com.example.tralalero.data.remote.api.ProjectApiService;
import com.example.tralalero.data.remote.dto.project.ProjectDTO;
import com.example.tralalero.domain.model.Project;
import com.example.tralalero.domain.repository.IProjectRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProjectRepositoryImplWithCache implements IProjectRepository {
    private final ProjectApiService apiService;
    private final ProjectDao projectDao;
    private final ExecutorService executorService;
    private final Handler mainHandler;
    
    public ProjectRepositoryImplWithCache(ProjectApiService apiService, ProjectDao projectDao) {
        this.apiService = apiService;
        this.projectDao = projectDao;
        this.executorService = Executors.newFixedThreadPool(4);
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    @Override
    public void getProjectById(String projectId, RepositoryCallback<Project> callback) {
        executorService.execute(() -> {
            // 1. Return from cache immediately
            ProjectEntity cached = projectDao.getProjectByIdSync(projectId);
            if (cached != null) {
                Project cachedProject = ProjectEntityMapper.toDomain(cached);
                mainHandler.post(() -> callback.onSuccess(cachedProject));
            }
            
            // 2. Fetch from network in background
            fetchProjectFromNetwork(projectId, callback, cached == null);
        });
    }
    
    private void fetchProjectFromNetwork(String projectId, RepositoryCallback<Project> callback, boolean isFirstLoad) {
        apiService.getProjectById(projectId).enqueue(new Callback<ProjectDTO>() {
            @Override
            public void onResponse(Call<ProjectDTO> call, Response<ProjectDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Project project = ProjectMapper.toDomain(response.body());
                    
                    // Cache in background
                    executorService.execute(() -> {
                        projectDao.insertProject(ProjectEntityMapper.toEntity(project));
                    });
                    
                    // Only callback if first load
                    if (isFirstLoad) {
                        callback.onSuccess(project);
                    }
                } else if (isFirstLoad) {
                    callback.onError("Project not found: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<ProjectDTO> call, Throwable t) {
                if (isFirstLoad) {
                    callback.onError("Network error: " + t.getMessage());
                }
            }
        });
    }
    
    // TODO: Implement other methods following same pattern
    // - createProject
    // - updateProject
    // - deleteProject
    // - getProjectsByWorkspace (important!)
    
    @Override
    public void createProject(String workspaceId, Project project, RepositoryCallback<Project> callback) {
        // Implement: API call → Cache → Callback
        ProjectDTO dto = ProjectMapper.toDTO(project);
        dto.setWorkspaceId(workspaceId);
        
        apiService.createProject(dto).enqueue(new Callback<ProjectDTO>() {
            @Override
            public void onResponse(Call<ProjectDTO> call, Response<ProjectDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Project newProject = ProjectMapper.toDomain(response.body());
                    
                    // Cache immediately
                    executorService.execute(() -> {
                        projectDao.insertProject(ProjectEntityMapper.toEntity(newProject));
                    });
                    
                    callback.onSuccess(newProject);
                } else {
                    callback.onError("Failed to create project: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<ProjectDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    
    // Add more methods...
}
```

**Update DependencyProvider:**

```java
// Trong core/DependencyProvider.java, thêm:

private ProjectRepositoryImplWithCache projectRepositoryWithCache;

public ProjectRepositoryImplWithCache getProjectRepositoryWithCache() {
    if (projectRepositoryWithCache == null) {
        ProjectApiService apiService = ApiClient.get(authManager).create(ProjectApiService.class);
        projectRepositoryWithCache = new ProjectRepositoryImplWithCache(apiService, projectDao);
    }
    return projectRepositoryWithCache;
}
```

**Deliverable:**
- [ ] ProjectRepositoryImplWithCache implemented
- [ ] DependencyProvider updated
- [ ] Compiles without errors
- [ ] Basic testing done

---

#### ✅ Day 3: UI Migration - Phase 1 (4-6 giờ)

**3.1. Migrate InboxActivity**

**File:** `feature/home/ui/InboxActivity.java`

**Current setupViewModel() method:**
```java
private void setupViewModel() {
    TaskApiService apiService = ApiClient.get(App.authManager).create(TaskApiService.class);
    ITaskRepository repository = new TaskRepositoryImpl(apiService); // ← Old way
    
    GetTaskByIdUseCase getTaskByIdUseCase = new GetTaskByIdUseCase(repository);
    // ... more use cases
    
    TaskViewModelFactory factory = new TaskViewModelFactory(...);
    taskViewModel = new ViewModelProvider(this, factory).get(TaskViewModel.class);
}
```

**Updated version:**
```java
private void setupViewModel() {
    // Use cached repository from DependencyProvider
    TaskRepositoryImplWithCache repository = App.dependencyProvider.getTaskRepositoryWithCache();
    
    GetTaskByIdUseCase getTaskByIdUseCase = new GetTaskByIdUseCase(repository);
    GetTasksByBoardUseCase getTasksByBoardUseCase = new GetTasksByBoardUseCase(repository);
    CreateTaskUseCase createTaskUseCase = new CreateTaskUseCase(repository);
    UpdateTaskUseCase updateTaskUseCase = new UpdateTaskUseCase(repository);
    DeleteTaskUseCase deleteTaskUseCase = new DeleteTaskUseCase(repository);
    // ... other use cases
    
    TaskViewModelFactory factory = new TaskViewModelFactory(
        getTaskByIdUseCase,
        getTasksByBoardUseCase,
        createTaskUseCase,
        updateTaskUseCase,
        deleteTaskUseCase,
        // ... other use cases
    );
    
    taskViewModel = new ViewModelProvider(this, factory).get(TaskViewModel.class);
}
```

**Testing:**
1. Run app
2. Open InboxActivity
3. Check logcat for load times
4. Close and reopen → should be instant (cached)
5. Pull to refresh → should work

**Add Pull-to-Refresh (Optional):**

```java
// In layout XML, wrap RecyclerView with SwipeRefreshLayout
// In Activity:

private SwipeRefreshLayout swipeRefreshLayout;

private void setupSwipeRefresh() {
    swipeRefreshLayout = findViewById(R.id.swipe_refresh);
    swipeRefreshLayout.setOnRefreshListener(() -> {
        refreshAllTasks();
    });
}

private void refreshAllTasks() {
    TaskRepositoryImplWithCache repo = App.dependencyProvider.getTaskRepositoryWithCache();
    
    // Force refresh from network
    repo.forceRefreshTasksByBoard(currentBoardId, new ITaskRepository.RepositoryCallback<List<Task>>() {
        @Override
        public void onSuccess(List<Task> tasks) {
            swipeRefreshLayout.setRefreshing(false);
            taskAdapter.updateTasks(tasks);
            Toast.makeText(InboxActivity.this, "Đã cập nhật", Toast.LENGTH_SHORT).show();
        }
        
        @Override
        public void onError(String error) {
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(InboxActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
        }
    });
}
```

**Deliverable:**
- [ ] InboxActivity uses cached repository
- [ ] Load time significantly improved
- [ ] Pull-to-refresh works (optional)
- [ ] No crashes

---

**3.2. Migrate Key Activities**

Áp dụng pattern tương tự cho:

**1. ProjectActivity / NewBoard.java:**
```java
// Old
ProjectApiService apiService = ApiClient.get(App.authManager).create(ProjectApiService.class);
IProjectRepository repository = new ProjectRepositoryImpl(apiService);

// New
ProjectRepositoryImplWithCache repository = App.dependencyProvider.getProjectRepositoryWithCache();
```

**2. Any Activity using TaskRepository:**
```java
// Replace all instances of:
new TaskRepositoryImpl(apiService)

// With:
App.dependencyProvider.getTaskRepositoryWithCache()
```

**Search & Replace Guide:**
```java
// Find: new TaskRepositoryImpl(
// Replace: App.dependencyProvider.getTaskRepositoryWithCache(
// Note: May need manual adjustment
```

**Deliverable:**
- [ ] At least 3 major activities migrated
- [ ] All compile without errors
- [ ] Testing shows improved performance

---

#### ✅ Day 4: Testing & Performance (4-6 giờ)

**4.1. Performance Testing**

Create performance log utility:

```java
public class PerformanceLogger {
    private static final String TAG = "Performance";
    private long startTime;
    private String operation;
    
    public PerformanceLogger(String operation) {
        this.operation = operation;
        this.startTime = System.currentTimeMillis();
        Log.d(TAG, operation + " - START");
    }
    
    public void end() {
        long duration = System.currentTimeMillis() - startTime;
        Log.d(TAG, operation + " - END: " + duration + "ms");
        
        if (duration < 100) {
            Log.d(TAG, "✓ EXCELLENT performance");
        } else if (duration < 500) {
            Log.d(TAG, "✓ GOOD performance");
        } else {
            Log.w(TAG, "⚠ SLOW performance");
        }
    }
}

// Usage:
PerformanceLogger perf = new PerformanceLogger("Load Tasks");
repository.getTasksByBoard(boardId, new Callback<>() {
    @Override
    public void onSuccess(List<Task> tasks) {
        perf.end(); // Will log duration
    }
});
```

**Test Scenarios:**

1. **First Load (No Cache):**
   - Expected: 500-2000ms (network)
   - Log: "Load Tasks - END: 1200ms"

2. **Second Load (With Cache):**
   - Expected: < 50ms
   - Log: "Load Tasks - END: 35ms ✓ EXCELLENT"

3. **Offline Mode:**
   - Turn off WiFi/Data
   - App should still load cached data
   - Expected: < 50ms, no errors

4. **Cache Update:**
   - Load data
   - Make changes on another device
   - Pull to refresh
   - Should see updates

**Deliverable:**
- [ ] Performance logs collected
- [ ] All scenarios pass
- [ ] Screenshots of logcat results

---

**4.2. Edge Cases Testing**

Test these scenarios:

```java
// 1. Empty Cache + No Network
// Expected: Show error message, don't crash

// 2. Rapid Successive Calls
for (int i = 0; i < 10; i++) {
    repository.getTasksByBoard(boardId, callback);
}
// Expected: No crashes, handle gracefully

// 3. Large Dataset
// Load 100+ tasks
// Expected: Still fast from cache

// 4. Concurrent Access
// Open multiple activities at once
// Expected: Share same repository, no conflicts

// 5. Memory Leaks
// Open/close activity 10 times
// Check memory usage
// Expected: No significant increase
```

**Deliverable:**
- [ ] All edge cases handled
- [ ] No crashes in any scenario
- [ ] Document any issues found

---

#### ✅ Day 5: Polish & Documentation (3-4 giờ)

**5.1. Add User Feedback**

**Offline Indicator (Optional):**

```java
// Add to BaseActivity or specific activities

private void setupNetworkMonitor() {
    NetworkCallback networkCallback = new NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            runOnUiThread(() -> {
                // Hide offline banner
                offlineBanner.setVisibility(View.GONE);
            });
        }
        
        @Override
        public void onLost(Network network) {
            runOnUiThread(() -> {
                // Show offline banner
                offlineBanner.setVisibility(View.VISIBLE);
                offlineBanner.setText("📴 Offline - Showing cached data");
            });
        }
    };
    
    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    cm.registerDefaultNetworkCallback(networkCallback);
}
```

**Loading States:**

```java
// Show subtle loading for network refresh
private void loadTasksWithFeedback() {
    // Show cached data immediately
    repository.getTasksByBoard(boardId, tasks -> {
        adapter.updateTasks(tasks);
        progressBar.setVisibility(View.GONE);
    });
    
    // Show small loading indicator for refresh
    refreshIndicator.setVisibility(View.VISIBLE);
    
    repository.forceRefreshTasksByBoard(boardId, new Callback<>() {
        @Override
        public void onSuccess(List<Task> tasks) {
            refreshIndicator.setVisibility(View.GONE);
        }
        
        @Override
        public void onError(String error) {
            refreshIndicator.setVisibility(View.GONE);
            // Silent fail - cached data already shown
        }
    });
}
```

**Deliverable:**
- [ ] User feedback implemented
- [ ] UX smooth and intuitive
- [ ] No confusing loading states

---

**5.2. Final Documentation**

Create implementation report:

```markdown
# Implementation Report - Cached Repositories

## Summary
Successfully migrated app to use Room Database caching system.

## Changes Made

### Repositories Implemented:
- ✅ TaskRepositoryImplWithCache
- ✅ ProjectRepositoryImplWithCache
- ⚠️ WorkspaceRepositoryImplWithCache (TODO)

### Activities Migrated:
- ✅ InboxActivity
- ✅ ProjectActivity
- ✅ [Other activity]
- ⚠️ [Activity] (TODO)

## Performance Improvements

| Metric | Before | After | Improvement |
|--------|--------|-------|-------------|
| First Load | 1200ms | 35ms | 97% faster |
| Reload | 1200ms | 30ms | 97% faster |
| Offline | Crash | Works | ✓ |

## Test Results
- Unit Tests: ✅ PASS
- Integration Tests: ✅ PASS
- Performance Tests: ✅ PASS
- Edge Cases: ✅ PASS

## Known Issues
- None

## Recommendations
- Consider implementing WorkspaceRepository caching
- Add more offline features
- Consider sync conflict resolution
```

**Deliverable:**
- [ ] Implementation report completed
- [ ] All code committed with good messages
- [ ] Demo ready for team lead

---

### 📊 Person 2 Summary Checklist

**Repository Implementation:**
- [ ] TaskRepositoryImplWithCache reviewed
- [ ] ProjectRepositoryImplWithCache implemented
- [ ] DependencyProvider updated
- [ ] All repositories tested

**UI Migration:**
- [ ] InboxActivity migrated
- [ ] 2-3 other activities migrated
- [ ] Pull-to-refresh added (optional)
- [ ] No regression bugs

**Testing:**
- [ ] Performance tests conducted
- [ ] Edge cases tested
- [ ] Offline mode verified
- [ ] Memory leaks checked

**Polish:**
- [ ] User feedback added
- [ ] Documentation completed
- [ ] Code cleaned up
- [ ] Ready for production

---

## 🔄 COORDINATION BETWEEN PERSON 1 & 2

### Daily Sync (15 mins)
- Share progress
- Discuss blockers
- Coordinate on shared files

### Handoff Points

**Day 2 (Person 1 → Person 2):**
- Person 1: Database infrastructure complete
- Person 2: Can start implementing repositories

**Day 3 (Both working):**
- Person 1: Support Person 2 với database issues
- Person 2: Focus on UI migration

**Day 5 (Final Review):**
- Both: Code review together
- Both: Test all features
- Both: Prepare demo

---

## 📞 SUPPORT & ESCALATION

**If Stuck > 30 mins:**
1. Check documentation
2. Review existing code examples
3. Ask the other person
4. Ask team lead

**Critical Issues:**
- Database won't build
- App crashes on start
- Data loss on logout

→ **Immediately escalate to team lead**

---

## ✅ DEFINITION OF DONE

### Person 1:
- [ ] All tests pass
- [ ] Database builds successfully
- [ ] DependencyProvider works
- [ ] Clear cache on logout works
- [ ] Documentation complete

### Person 2:
- [ ] All repositories implemented
- [ ] All activities migrated
- [ ] Performance improved > 90%
- [ ] No regression bugs
- [ ] Documentation complete

### Team:
- [ ] Code reviewed
- [ ] Demo completed
- [ ] Production ready
- [ ] Team lead approval

---

**Good luck! Work smart, communicate often, and ask for help when needed! 🚀**

