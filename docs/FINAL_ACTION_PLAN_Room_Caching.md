# ⚡ ACTIONABLE PLAN - Room Database Caching Implementation

**Priority:** 🔴 CRITICAL  
**Timeline:** 5-7 ngày (2 developers)  
**Objective:** Giảm API delay 97% (1200ms → 30ms)

---

## 🎯 EXECUTIVE SUMMARY

### Vấn đề:
- ❌ Mỗi màn hình load data phải đợi API 500-2000ms
- ❌ Không có offline support
- ❌ Lãng phí bandwidth, gọi API liên tục
- ❌ User experience kém

### Giải pháp:
- ✅ Room Database để cache local
- ✅ Cache-First strategy: Load instant từ cache (30ms), refresh background
- ✅ Singleton pattern với DependencyProvider

### Kết quả mong đợi:
- ⚡ **Load time: 1200ms → 30ms (97% faster)**
- 📴 **Offline support: App vẫn hoạt động**
- 💾 **Giảm 80% API calls**

---

## 📋 ENTITIES CẦN CACHE (Theo độ ưu tiên)

Dựa trên schema.prisma và usage patterns:

### 🔴 Priority 1 (MUST HAVE) - Implement ngay:
```java
✅ TaskEntity        - Cache tasks (dùng nhiều nhất)
✅ ProjectEntity     - Cache projects
✅ WorkspaceEntity   - Cache workspaces
✅ BoardEntity       - Cache boards (THÊM MỚI - quan trọng)
```

### 🟡 Priority 2 (NICE TO HAVE) - Implement sau:
```java
⚠️ SprintEntity      - Cache sprints
⚠️ LabelEntity       - Cache labels
⚠️ UserEntity        - Cache user info (basic)
```

### 🟢 Priority 3 (OPTIONAL):
```java
○ TimeEntryEntity    - Cache time entries
○ NotificationEntity - Cache notifications
○ EventEntity        - Cache calendar events
```

---

## 🏗️ ARCHITECTURE OVERVIEW

```
┌─────────────────────────────────────────┐
│     UI Layer (Activities/Fragments)     │
│   InboxActivity, ProjectActivity...     │
└──────────────┬──────────────────────────┘
               │
               ↓
┌─────────────────────────────────────────┐
│    DependencyProvider (Singleton)       │
│  - Quản lý tất cả Repository & DAO      │
│  - Single source of truth               │
└───────┬──────────────────┬──────────────┘
        │                  │
        ↓                  ↓
┌──────────────┐    ┌─────────────────────┐
│ Repositories │◄───│  Room Database      │
│ (Cached)     │    │  - TaskDao          │
│              │    │  - ProjectDao       │
│              │    │  - BoardDao         │
└──────┬───────┘    │  - WorkspaceDao     │
       │            └─────────────────────┘
       ↓
┌──────────────┐
│ Retrofit API │
└──────────────┘
```

### Data Flow:
```
User Action
    ↓
1. Check Cache (30ms) → Return immediately ✓
    ↓
2. API Call (background, 500-2000ms)
    ↓
3. Update Cache
    ↓
4. UI auto-refresh (if data changed)
```

---

## ✅ CODE ĐÃ TẠO SẴN - READY TO USE

### Database Layer (11 files):
```
app/src/main/java/com/example/tralalero/

data/local/database/
├── AppDatabase.java              ✅ Room database chính
├── converter/
│   └── DateConverter.java        ✅ Convert Date ↔ Long
├── entity/
│   ├── TaskEntity.java           ✅ Task table (có cachedAt, isDirty)
│   ├── ProjectEntity.java        ✅ Project table
│   └── WorkspaceEntity.java      ✅ Workspace table
└── dao/
    ├── TaskDao.java              ✅ CRUD + LiveData queries
    ├── ProjectDao.java           ✅ CRUD + LiveData queries
    └── WorkspaceDao.java         ✅ CRUD + LiveData queries

data/mapper/
├── TaskEntityMapper.java         ✅ Entity ↔ Domain Model
├── ProjectEntityMapper.java      ✅ Entity ↔ Domain Model
└── WorkspaceEntityMapper.java    ✅ Entity ↔ Domain Model

data/repository/
└── TaskRepositoryImplWithCache.java  ✅ Repository mẫu hoàn chỉnh

core/
└── DependencyProvider.java       ✅ Singleton quản lý dependencies
```

---

## 🚀 IMPLEMENTATION PLAN - THEO NGÀY

### 🔷 PERSON 1: Database Infrastructure Lead

#### **DAY 1 (6-8h) - Setup & Verify**

**Morning (3-4h):**
```bash
□ 1. Sync Gradle Project
   File → Sync Project with Gradle Files
   
□ 2. Build Project
   Build → Rebuild Project
   
□ 3. Verify Room Generated Code
   Check: app/build/generated/ap_generated_sources/.../
   Files: AppDatabase_Impl.java, TaskDao_Impl.java...
   
□ 4. Fix any compilation errors
```

**Afternoon (3-4h):**
```java
□ 5. Create test file: DatabaseInfrastructureTest.java

@RunWith(RobolectricTestRunner.class)
public class DatabaseInfrastructureTest {
    private AppDatabase database;
    
    @Before
    public void setup() {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
    }
    
    @Test
    public void testDatabaseCreation() {
        assertNotNull(database);
        assertNotNull(database.taskDao());
        assertNotNull(database.projectDao());
        assertNotNull(database.workspaceDao());
    }
    
    @Test
    public void testTaskCRUD() {
        TaskDao dao = database.taskDao();
        
        // Insert
        TaskEntity task = new TaskEntity();
        task.setId("test-1");
        task.setTitle("Test Task");
        task.setBoardId("board-1");
        dao.insertTask(task);
        
        // Read
        TaskEntity retrieved = dao.getTaskByIdSync("test-1");
        assertNotNull(retrieved);
        assertEquals("Test Task", retrieved.getTitle());
        
        // Update
        retrieved.setTitle("Updated");
        dao.updateTask(retrieved);
        TaskEntity updated = dao.getTaskByIdSync("test-1");
        assertEquals("Updated", updated.getTitle());
        
        // Delete
        dao.deleteTaskById("test-1");
        assertNull(dao.getTaskByIdSync("test-1"));
    }
}

□ 6. Run tests và verify pass
```

**Deliverable Day 1:**
- [ ] Project builds thành công
- [ ] Room generated code OK
- [ ] Basic CRUD tests pass
- [ ] Screenshot test results

---

#### **DAY 2 (6-8h) - Integration**

**Morning (3-4h):**
```java
□ 1. Update App.java

package com.example.tralalero.App;

import android.app.Application;
import android.util.Log;
import com.example.tralalero.auth.AuthManager;
import com.example.tralalero.core.DependencyProvider;
import com.google.firebase.FirebaseApp;

public class App extends Application {
    private static final String TAG = "App";
    
    public static AuthManager authManager;
    public static DependencyProvider dependencyProvider; // ← ADD THIS
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "=== App Starting ===");
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        Log.d(TAG, "✓ Firebase initialized");
        
        // Initialize AuthManager
        authManager = new AuthManager(this);
        Log.d(TAG, "✓ AuthManager initialized");
        
        // Initialize DependencyProvider with Database
        dependencyProvider = DependencyProvider.getInstance(this, authManager);
        Log.d(TAG, "✓ DependencyProvider initialized");
        Log.d(TAG, "✓ Room Database ready");
    }
    
    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(TAG, "=== App Terminating - Clearing Caches ===");
        
        if (dependencyProvider != null) {
            dependencyProvider.clearAllCaches();
            Log.d(TAG, "✓ All caches cleared");
        }
    }
}

□ 2. Build và run app để verify không crash
```

**Afternoon (3-4h):**
```java
□ 3. Tìm file xử lý logout (SettingsActivity.java hoặc AccountActivity.java)

□ 4. Add clear cache on logout

private void performLogout() {
    Log.d("Logout", "Starting logout process...");
    
    // Show progress
    ProgressDialog progress = new ProgressDialog(this);
    progress.setMessage("Đang đăng xuất...");
    progress.show();
    
    new Handler().postDelayed(() -> {
        // 1. Clear authentication
        App.authManager.logout();
        Log.d("Logout", "✓ Auth cleared");
        
        // 2. Clear all database caches
        App.dependencyProvider.clearAllCaches();
        Log.d("Logout", "✓ Database cache cleared");
        
        // 3. Reset DependencyProvider
        DependencyProvider.reset();
        Log.d("Logout", "✓ DependencyProvider reset");
        
        progress.dismiss();
        
        // 4. Navigate to login
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        
        Log.d("Logout", "✓ Logout complete");
        Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
    }, 500);
}

□ 5. Test logout flow:
   - Login
   - Load some data
   - Logout
   - Check logcat for "✓" messages
   - Login again → no old data
```

**Deliverable Day 2:**
- [ ] DependencyProvider integrated
- [ ] App starts without errors
- [ ] Logout clears cache properly
- [ ] Logcat shows all ✓ messages

---

#### **DAY 3 (4h) - Documentation & Handoff**

```markdown
□ 1. Create document: Database_Infrastructure_Status.md

# Database Infrastructure - Implementation Status

## ✅ Completed
- Room Database builds successfully
- All DAOs operational
- Entity Mappers tested
- DependencyProvider integrated
- Clear cache on logout working

## 📊 Test Results
- Database creation: PASS ✓
- CRUD operations: PASS ✓
- Cache clearing: PASS ✓
- Integration test: PASS ✓

## 🎯 Ready for Next Phase
Person 2 can now:
- Implement cached repositories
- Migrate UI to use DependencyProvider
- Start performance testing

## 📞 Contact
[Your name] - Available for questions
```

**Deliverable Day 3:**
- [ ] Documentation complete
- [ ] Handoff meeting với Person 2
- [ ] Demo cho team lead

---

### 🔶 PERSON 2: Repository & UI Integration Lead

#### **DAY 1 (6-8h) - Review & Plan**

**Morning (3-4h):**
```java
□ 1. Review TaskRepositoryImplWithCache.java
   - Understand caching strategy
   - Understand ExecutorService usage
   - Understand callback mechanism

□ 2. Review DependencyProvider.java
   - Understand singleton pattern
   - Understand how to get repositories

□ 3. Test existing implementation

public class TestCachedRepositoryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Get cached repository
        TaskRepositoryImplWithCache repo = 
            App.dependencyProvider.getTaskRepositoryWithCache();
        
        String boardId = "YOUR_TEST_BOARD_ID"; // Get from your data
        
        // Test 1: First call
        long start1 = System.currentTimeMillis();
        repo.getTasksByBoard(boardId, new ITaskRepository.RepositoryCallback<List<Task>>() {
            @Override
            public void onSuccess(List<Task> tasks) {
                long duration1 = System.currentTimeMillis() - start1;
                Log.d("CacheTest", "First call: " + duration1 + "ms, tasks: " + tasks.size());
                // Expected: 500-2000ms (network)
                
                // Test 2: Second call (should be from cache)
                testSecondCall(boardId, repo);
            }
            
            @Override
            public void onError(String error) {
                Log.e("CacheTest", "Error: " + error);
            }
        });
    }
    
    private void testSecondCall(String boardId, TaskRepositoryImplWithCache repo) {
        new Handler().postDelayed(() -> {
            long start2 = System.currentTimeMillis();
            repo.getTasksByBoard(boardId, new ITaskRepository.RepositoryCallback<List<Task>>() {
                @Override
                public void onSuccess(List<Task> tasks) {
                    long duration2 = System.currentTimeMillis() - start2;
                    Log.d("CacheTest", "Second call (cached): " + duration2 + "ms");
                    // Expected: < 50ms ✓
                    
                    if (duration2 < 100) {
                        Log.d("CacheTest", "✓✓✓ CACHE WORKING! 40x faster!");
                    }
                }
                
                @Override
                public void onError(String error) {
                    Log.e("CacheTest", "Error: " + error);
                }
            });
        }, 2000);
    }
}
```

**Afternoon (3-4h):**
```java
□ 4. Implement BoardEntity.java (QUAN TRỌNG - thiếu trong code ban đầu!)

package com.example.tralalero.data.local.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import androidx.annotation.NonNull;
import com.example.tralalero.data.local.database.converter.DateConverter;
import java.util.Date;

@Entity(tableName = "boards")
@TypeConverters({DateConverter.class})
public class BoardEntity {
    @PrimaryKey
    @NonNull
    private String id;
    
    private String projectId;
    private String name;
    private Integer order;
    private Date createdAt;
    private Date updatedAt;
    
    // Cache metadata
    private long cachedAt;
    private boolean isDirty;

    public BoardEntity() {
        this.cachedAt = System.currentTimeMillis();
        this.isDirty = false;
    }

    // Getters and Setters
    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }
    
    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Integer getOrder() { return order; }
    public void setOrder(Integer order) { this.order = order; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    
    public long getCachedAt() { return cachedAt; }
    public void setCachedAt(long cachedAt) { this.cachedAt = cachedAt; }
    
    public boolean isDirty() { return isDirty; }
    public void setDirty(boolean dirty) { isDirty = dirty; }
}

□ 5. Implement BoardDao.java
□ 6. Implement BoardEntityMapper.java
□ 7. Update AppDatabase.java to include BoardEntity
```

**Deliverable Day 1:**
- [ ] Hiểu rõ caching strategy
- [ ] Test cache working
- [ ] BoardEntity implemented
- [ ] Plan for Day 2-3

---

#### **DAY 2-3 (12-16h) - Implement Repositories**

```java
□ 1. Implement ProjectRepositoryImplWithCache.java
   Copy pattern từ TaskRepositoryImplWithCache
   
□ 2. Implement BoardRepositoryImplWithCache.java
   
□ 3. Implement WorkspaceRepositoryImplWithCache.java

□ 4. Update DependencyProvider.java với các repositories mới

private BoardRepositoryImplWithCache boardRepositoryWithCache;

public BoardRepositoryImplWithCache getBoardRepositoryWithCache() {
    if (boardRepositoryWithCache == null) {
        BoardApiService apiService = ApiClient.get(authManager).create(BoardApiService.class);
        boardRepositoryWithCache = new BoardRepositoryImplWithCache(apiService, boardDao);
    }
    return boardRepositoryWithCache;
}

□ 5. Test từng repository
```

**Deliverable Day 2-3:**
- [ ] All repositories implemented
- [ ] All compile without errors
- [ ] Basic testing done

---

#### **DAY 4 (6-8h) - UI Migration**

**Priority Activities to Migrate:**

```java
□ 1. InboxActivity.java

// BEFORE:
private void setupViewModel() {
    TaskApiService apiService = ApiClient.get(App.authManager).create(TaskApiService.class);
    ITaskRepository repository = new TaskRepositoryImpl(apiService);
    // ...
}

// AFTER:
private void setupViewModel() {
    TaskRepositoryImplWithCache repository = 
        App.dependencyProvider.getTaskRepositoryWithCache();
    // ... rest stays same
}

□ 2. NewBoard.java (ProjectActivity)
□ 3. Any other Activity using TaskRepository

□ 4. Test each migrated activity:
   - First open: may take 1-2s (network)
   - Close and reopen: < 50ms (cache) ✓
   - Pull to refresh: works
   - Offline: still shows data ✓
```

**Deliverable Day 4:**
- [ ] 3+ key activities migrated
- [ ] Performance improved significantly
- [ ] No regression bugs

---

#### **DAY 5 (4-6h) - Polish & Testing**

```java
□ 1. Add Pull-to-Refresh (optional but recommended)

<androidx.swiperefreshlayout.widget.SwipeRefreshLayout
    android:id="@+id/swipe_refresh"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recyclerView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>
        
</androidx.swiperefreshlayout.widget.SwipeRefreshLayout>

// In Activity:
private void setupSwipeRefresh() {
    swipeRefreshLayout.setOnRefreshListener(() -> {
        TaskRepositoryImplWithCache repo = 
            App.dependencyProvider.getTaskRepositoryWithCache();
        repo.forceRefreshTasksByBoard(boardId, new Callback<>() {
            @Override
            public void onSuccess(List<Task> tasks) {
                swipeRefreshLayout.setRefreshing(false);
                adapter.updateTasks(tasks);
                Toast.makeText(this, "Đã cập nhật", Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onError(String error) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    });
}

□ 2. Performance testing với logcat
□ 3. Offline mode testing
□ 4. Final cleanup
□ 5. Documentation
```

**Deliverable Day 5:**
- [ ] All features complete
- [ ] Performance targets met
- [ ] Documentation complete

---

## 📊 SUCCESS METRICS

### Performance Targets:

| Metric | Target | How to Measure |
|--------|--------|----------------|
| Cache Load Time | < 50ms | Logcat timestamps |
| Network Refresh | < 2000ms | Logcat timestamps |
| Offline Support | ✓ Works | Turn off WiFi, test |
| API Call Reduction | 80% | Count API calls before/after |
| No Crashes | 0 crashes | Test all flows |

### Testing Checklist:

**Functional:**
- [ ] Login → Load data → Logout → Login → No old data
- [ ] Open activity → Data shows < 50ms
- [ ] Pull to refresh → Updates work
- [ ] Offline mode → App works with cached data
- [ ] Create/Update/Delete → Cache updates correctly

**Performance:**
- [ ] First load: 500-2000ms (network) ✓
- [ ] Second load: < 50ms (cache) ✓
- [ ] Memory usage: No significant increase
- [ ] Battery drain: No abnormal drain

**Edge Cases:**
- [ ] Empty cache + No network → Show appropriate message
- [ ] Large dataset (100+ items) → Still fast
- [ ] Rapid screen switching → No crashes
- [ ] App in background → Cache persists

---

## 🚨 COMMON ISSUES & QUICK FIXES

### Issue 1: "Cannot find symbol class AppDatabase_Impl"
```bash
Solution:
1. Build → Clean Project
2. Build → Rebuild Project
3. File → Invalidate Caches / Restart
```

### Issue 2: "Cannot access database on the main thread"
```java
// ❌ WRONG
TaskEntity task = taskDao.getTaskByIdSync("id"); // On main thread!

// ✅ CORRECT
executorService.execute(() -> {
    TaskEntity task = taskDao.getTaskByIdSync("id");
    mainHandler.post(() -> {
        // Update UI
    });
});
```

### Issue 3: TypeConverter not found
```java
// Make sure AppDatabase has @TypeConverters
@Database(entities = {...}, version = 1)
@TypeConverters({DateConverter.class}) // ← Important!
public abstract class AppDatabase extends RoomDatabase {
```

### Issue 4: Data not showing after cache
```java
// Debug: Check if data is being cached
executorService.execute(() -> {
    List<TaskEntity> cached = taskDao.getAllTasksSync();
    Log.d("Cache", "Cached tasks: " + cached.size());
    for (TaskEntity task : cached) {
        Log.d("Cache", "  - " + task.getTitle());
    }
});
```

---

## 📁 FILES TO CREATE/MODIFY

### CREATE (Person 2):
```
✅ BoardEntity.java
✅ BoardDao.java
✅ BoardEntityMapper.java
✅ BoardRepositoryImplWithCache.java
✅ ProjectRepositoryImplWithCache.java
✅ WorkspaceRepositoryImplWithCache.java
```

### MODIFY (Person 1):
```
✅ App.java - Add DependencyProvider
✅ SettingsActivity.java - Add clear cache on logout
✅ AppDatabase.java - Add BoardEntity to entities list
```

### MODIFY (Person 2):
```
✅ InboxActivity.java - Use cached repository
✅ NewBoard.java - Use cached repository
✅ [Other activities] - Use cached repository
✅ DependencyProvider.java - Add new repositories
```

---

## 🎯 FINAL CHECKLIST

### Person 1 (Database Infrastructure):
- [ ] Room builds successfully
- [ ] All DAOs tested and working
- [ ] DependencyProvider integrated
- [ ] Clear cache on logout works
- [ ] Documentation complete
- [ ] Handoff to Person 2 done

### Person 2 (Repository & UI):
- [ ] BoardEntity created
- [ ] All cached repositories implemented
- [ ] 3+ activities migrated
- [ ] Performance targets met (< 50ms)
- [ ] Pull-to-refresh implemented
- [ ] Offline mode tested
- [ ] Documentation complete

### Both (Final):
- [ ] All compilation errors fixed
- [ ] All tests pass
- [ ] Demo successful
- [ ] Code reviewed
- [ ] Ready for production

---

## 📞 DAILY STANDUP (15 mins @ 9:00 AM)

**Format:**
1. What did you complete yesterday?
2. What will you do today?
3. Any blockers?

**Communication:**
- Slack/Teams for quick questions
- Screen share for complex issues
- Code review before merging

---

## 🚀 LET'S GET STARTED!

### Day 1 Morning - RIGHT NOW:

**Person 1:**
```bash
1. Open Android Studio
2. File → Sync Project with Gradle Files
3. Build → Rebuild Project
4. Check for errors
5. If errors: Screenshot and share
6. If success: Start testing DAOs
```

**Person 2:**
```bash
1. Read Room_Database_Caching_Implementation_Guide.md
2. Review TaskRepositoryImplWithCache.java
3. Understand caching pattern
4. Plan BoardEntity implementation
5. Prepare questions for Person 1
```

---

## 📈 EXPECTED TIMELINE

```
Day 1: Person 1 builds & tests infrastructure
Day 2: Person 1 integrates DependencyProvider
Day 3: Person 1 documents & handoff
Day 1-3: Person 2 reviews & implements repositories
Day 4: Person 2 migrates UI
Day 5: Both test & polish
Day 6-7: Final testing & documentation
```

---

## 🎉 SUCCESS = APP 40X FASTER!

**Before:**
```
Tap screen → Loading... → 1200ms → Data shows
😞 Poor UX
```

**After:**
```
Tap screen → 30ms → Data shows instantly!
😊 Excellent UX
```

---

**🔥 START NOW! TIME IS CRITICAL! 🔥**

**Questions?**
- Check Implementation Guide for details
- Ask teammate
- Escalate to team lead if blocked > 2 hours

**Let's build the fastest Android app! 🚀**

