# 🔧 HƯỚNG DẪN FIX MINOR ISSUES
## Cache TTL & Pull-to-Refresh Implementation

**Ngày:** October 19, 2025  
**Priority:** P3 (Nice to have)  
**Estimated Time:** 2-3 giờ

---

## 📋 MINOR ISSUES OVERVIEW

### ⚠️ Issue 1: Cache không có TTL (Time To Live)
**Vấn đề:** Cache có thể stale lâu, không tự động refresh  
**Impact:** User thấy data cũ nếu server có update  
**Solution:** Thêm timestamp vào entities, check TTL  
**Time:** 1-1.5 giờ

### ⚠️ Issue 2: Không có pull-to-refresh
**Vấn đề:** User không force refresh được  
**Impact:** Phải đóng/mở app để refresh  
**Solution:** Add SwipeRefreshLayout  
**Time:** 1-1.5 giờ

---

## 🎯 SOLUTION 1: CACHE TTL IMPLEMENTATION

### Concept: Cache Expiry Strategy

```
Lưu timestamp khi cache data
    ↓
Mỗi lần load, check:
    ↓
Is cache older than 5 minutes?
    ↓
YES → Treat as empty → Force API refresh
NO  → Return cache → Silent background refresh
```

**Benefits:**
- ✅ Auto-refresh sau 5 phút
- ✅ Balance giữa performance và freshness
- ✅ Configurable TTL

---

### Step 1: Add CacheMetadata Entity (15 phút)

**File:** `app/src/main/java/com/example/tralalero/data/local/database/entity/CacheMetadata.java`

```java
package com.example.tralalero.data.local.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

/**
 * Cache metadata to track cache freshness
 * Stores timestamp of last cache update per entity type
 * 
 * @author Cache Enhancement
 * @date October 19, 2025
 */
@Entity(tableName = "cache_metadata")
public class CacheMetadata {
    
    @PrimaryKey
    @NonNull
    private String cacheKey;  // e.g., "workspaces", "tasks_userId"
    
    private long lastUpdated;  // Timestamp in milliseconds
    
    private int itemCount;     // Number of items cached
    
    // Constructors
    public CacheMetadata(@NonNull String cacheKey, long lastUpdated, int itemCount) {
        this.cacheKey = cacheKey;
        this.lastUpdated = lastUpdated;
        this.itemCount = itemCount;
    }
    
    // Getters & Setters
    @NonNull
    public String getCacheKey() {
        return cacheKey;
    }
    
    public void setCacheKey(@NonNull String cacheKey) {
        this.cacheKey = cacheKey;
    }
    
    public long getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    public int getItemCount() {
        return itemCount;
    }
    
    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }
    
    /**
     * Check if cache is expired based on TTL
     * 
     * @param ttlMillis Time to live in milliseconds
     * @return true if expired
     */
    public boolean isExpired(long ttlMillis) {
        return System.currentTimeMillis() - lastUpdated > ttlMillis;
    }
    
    /**
     * Get age of cache in seconds
     */
    public long getAgeInSeconds() {
        return (System.currentTimeMillis() - lastUpdated) / 1000;
    }
}
```

---

### Step 2: Add CacheMetadataDao (10 phút)

**File:** `app/src/main/java/com/example/tralalero/data/local/database/dao/CacheMetadataDao.java`

```java
package com.example.tralalero.data.local.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.tralalero.data.local.database.entity.CacheMetadata;

/**
 * DAO for cache metadata operations
 */
@Dao
public interface CacheMetadataDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CacheMetadata metadata);
    
    @Query("SELECT * FROM cache_metadata WHERE cacheKey = :key")
    CacheMetadata getMetadata(String key);
    
    @Query("DELETE FROM cache_metadata WHERE cacheKey = :key")
    void deleteMetadata(String key);
    
    @Query("DELETE FROM cache_metadata")
    void deleteAll();
}
```

---

### Step 3: Update AppDatabase (10 phút)

**File:** `data/local/database/AppDatabase.java`

```java
// Add CacheMetadata to entities
@Database(
    entities = {
        TaskEntity.class, 
        ProjectEntity.class, 
        WorkspaceEntity.class, 
        BoardEntity.class,
        CacheMetadata.class  // ← NEW
    }, 
    version = 4,  // ← INCREMENT VERSION
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    // ...existing code...
    
    public abstract CacheMetadataDao cacheMetadataDao();  // ← NEW
    
    // Migration from version 3 to 4
    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS cache_metadata (" +
                "cacheKey TEXT PRIMARY KEY NOT NULL, " +
                "lastUpdated INTEGER NOT NULL, " +
                "itemCount INTEGER NOT NULL)"
            );
        }
    };
    
    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME
                        )
                        .addMigrations(MIGRATION_3_4)  // ← ADD MIGRATION
                        .fallbackToDestructiveMigration()
                        .build();
                }
            }
        }
        return instance;
    }
}
```

---

### Step 4: Update WorkspaceRepositoryImplWithCache (30 phút)

**File:** `data/repository/WorkspaceRepositoryImplWithCache.java`

```java
public class WorkspaceRepositoryImplWithCache {
    private static final String TAG = "WorkspaceRepoCache";
    private static final long CACHE_TTL_MS = 5 * 60 * 1000; // 5 minutes
    private static final String CACHE_KEY = "workspaces";
    
    private final WorkspaceApiService apiService;
    private final WorkspaceDao workspaceDao;
    private final CacheMetadataDao cacheMetadataDao;  // ← NEW
    private final ExecutorService executorService;
    private final Handler mainHandler;
    
    public WorkspaceRepositoryImplWithCache(
            WorkspaceApiService apiService,
            WorkspaceDao workspaceDao,
            CacheMetadataDao cacheMetadataDao,  // ← NEW PARAMETER
            ExecutorService executorService) {
        this.apiService = apiService;
        this.workspaceDao = workspaceDao;
        this.cacheMetadataDao = cacheMetadataDao;
        this.executorService = executorService;
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    public void getWorkspaces(WorkspaceCallback callback) {
        if (callback == null) {
            Log.e(TAG, "Callback is null");
            return;
        }

        executorService.execute(() -> {
            try {
                // 1. Check cache metadata
                CacheMetadata metadata = cacheMetadataDao.getMetadata(CACHE_KEY);
                boolean isCacheExpired = (metadata == null || metadata.isExpired(CACHE_TTL_MS));
                
                if (isCacheExpired) {
                    Log.d(TAG, "Cache expired or empty, forcing API refresh");
                    mainHandler.post(() -> callback.onCacheEmpty());
                    
                    // Force network fetch
                    fetchWorkspacesFromNetwork(callback, true);
                    return;
                }
                
                // 2. Return from cache (still fresh)
                List<WorkspaceEntity> cached = workspaceDao.getAll();
                if (cached != null && !cached.isEmpty()) {
                    List<Workspace> cachedWorkspaces = WorkspaceEntityMapper.toDomainList(cached);
                    mainHandler.post(() -> callback.onSuccess(cachedWorkspaces));
                    
                    long ageSeconds = metadata.getAgeInSeconds();
                    Log.d(TAG, "✓ Cache hit: " + cached.size() + " workspaces (age: " + 
                          ageSeconds + "s, TTL: " + (CACHE_TTL_MS/1000) + "s)");
                }
                
                // 3. Silent background refresh
                fetchWorkspacesFromNetwork(callback, false);
                
            } catch (Exception e) {
                Log.e(TAG, "Error in getWorkspaces", e);
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }
    
    private void fetchWorkspacesFromNetwork(
            WorkspaceCallback callback,
            boolean isFirstLoad) {

        apiService.getWorkspaces().enqueue(new Callback<List<WorkspaceDTO>>() {
            @Override
            public void onResponse(Call<List<WorkspaceDTO>> call, Response<List<WorkspaceDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        List<Workspace> workspaces = WorkspaceMapper.toDomainList(response.body());

                        // Cache in background
                        executorService.execute(() -> {
                            try {
                                List<WorkspaceEntity> entities =
                                    WorkspaceEntityMapper.toEntityList(workspaces);
                                workspaceDao.insertAll(entities);
                                
                                // Update cache metadata ← NEW
                                CacheMetadata metadata = new CacheMetadata(
                                    CACHE_KEY,
                                    System.currentTimeMillis(),
                                    workspaces.size()
                                );
                                cacheMetadataDao.insert(metadata);
                                
                                Log.d(TAG, "✓ Cached " + workspaces.size() + 
                                      " workspaces with timestamp");
                            } catch (Exception e) {
                                Log.e(TAG, "Error caching workspaces", e);
                            }
                        });

                        // Only callback if first load
                        if (isFirstLoad && callback != null) {
                            mainHandler.post(() -> callback.onSuccess(workspaces));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing workspaces", e);
                        if (isFirstLoad && callback != null) {
                            mainHandler.post(() -> callback.onError(e));
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<WorkspaceDTO>> call, Throwable t) {
                Log.e(TAG, "Network error", t);
                if (isFirstLoad && callback != null) {
                    mainHandler.post(() -> callback.onError(new Exception(t)));
                }
            }
        });
    }
    
    // ...existing code...
}
```

---

### Step 5: Update TaskRepositoryImplWithCache (30 phút)

**Similar changes for TaskRepository:**

```java
public class TaskRepositoryImplWithCache {
    private static final long CACHE_TTL_MS = 3 * 60 * 1000; // 3 minutes (tasks change more frequently)
    
    private final CacheMetadataDao cacheMetadataDao;  // ← NEW
    
    public void getAllTasks(TaskCallback callback) {
        executorService.execute(() -> {
            try {
                String userId = tokenManager.getUserId();
                String cacheKey = "tasks_" + userId;
                
                // Check cache metadata
                CacheMetadata metadata = cacheMetadataDao.getMetadata(cacheKey);
                boolean isCacheExpired = (metadata == null || metadata.isExpired(CACHE_TTL_MS));
                
                if (isCacheExpired) {
                    Log.d(TAG, "Task cache expired, forcing refresh");
                    callback.onCacheEmpty();
                    return;
                }
                
                // Return cache if fresh
                List<TaskEntity> entities = taskDao.getAllByUserId(userId);
                if (entities != null && !entities.isEmpty()) {
                    List<Task> tasks = TaskEntityMapper.toDomainList(entities);
                    callback.onSuccess(tasks);
                    
                    Log.d(TAG, "✓ Task cache hit (age: " + metadata.getAgeInSeconds() + "s)");
                } else {
                    callback.onCacheEmpty();
                }
                
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void saveTasksToCache(List<Task> tasks) {
        executorService.execute(() -> {
            try {
                String userId = tokenManager.getUserId();
                String cacheKey = "tasks_" + userId;
                
                List<TaskEntity> entities = TaskEntityMapper.toEntityList(tasks);
                taskDao.insertAll(entities);
                
                // Update metadata ← NEW
                CacheMetadata metadata = new CacheMetadata(
                    cacheKey,
                    System.currentTimeMillis(),
                    tasks.size()
                );
                cacheMetadataDao.insert(metadata);
                
                Log.d(TAG, "✓ Saved " + tasks.size() + " tasks with timestamp");
            } catch (Exception e) {
                Log.e(TAG, "Error saving tasks to cache", e);
            }
        });
    }
}
```

---

### Step 6: Update DependencyProvider (10 phút)

```java
public class DependencyProvider {
    private final CacheMetadataDao cacheMetadataDao;  // ← NEW
    
    private DependencyProvider(Context context, TokenManager tokenManager) {
        this.tokenManager = tokenManager;
        this.database = AppDatabase.getInstance(context);
        
        // Initialize DAOs
        this.taskDao = database.taskDao();
        this.projectDao = database.projectDao();
        this.workspaceDao = database.workspaceDao();
        this.boardDao = database.boardDao();
        this.cacheMetadataDao = database.cacheMetadataDao();  // ← NEW
        
        this.executorService = Executors.newFixedThreadPool(3);
    }
    
    public synchronized WorkspaceRepositoryImplWithCache getWorkspaceRepositoryWithCache() {
        if (workspaceRepositoryWithCache == null) {
            WorkspaceApiService apiService = ApiClient.get(App.authManager)
                .create(WorkspaceApiService.class);

            workspaceRepositoryWithCache = new WorkspaceRepositoryImplWithCache(
                apiService,
                workspaceDao,
                cacheMetadataDao,  // ← NEW PARAMETER
                executorService
            );
        }
        return workspaceRepositoryWithCache;
    }
    
    public synchronized TaskRepositoryImplWithCache getTaskRepositoryWithCache() {
        if (taskRepositoryWithCache == null) {
            taskRepositoryWithCache = new TaskRepositoryImplWithCache(
                taskDao,
                executorService,
                tokenManager,
                cacheMetadataDao  // ← NEW PARAMETER
            );
        }
        return taskRepositoryWithCache;
    }
}
```

---

## 🎯 SOLUTION 2: PULL-TO-REFRESH IMPLEMENTATION

### Concept: SwipeRefreshLayout

```
User swipes down
    ↓
Show refresh indicator
    ↓
Force API call (bypass cache)
    ↓
Update UI + cache
    ↓
Hide refresh indicator
```

---

### Step 1: Add SwipeRefreshLayout to HomeActivity Layout (10 phút)

**File:** `res/layout/activity_home.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout 
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <!-- Add SwipeRefreshLayout wrapping RecyclerView -->
    <androidx.swiperefreshlayout.widget.SwipeRefreshLayout
        android:id="@+id/swipeRefreshLayout"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toTopOf="@id/bottomNavigation">
        
        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/recyclerBoard"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:padding="16dp" />
            
    </androidx.swiperefreshlayout.widget.SwipeRefreshLayout>
    
    <!-- ...existing bottom navigation... -->
    
</androidx.constraintlayout.widget.ConstraintLayout>
```

---

### Step 2: Implement Pull-to-Refresh in HomeActivity (20 phút)

**File:** `feature/home/ui/Home/HomeActivity.java`

```java
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class HomeActivity extends BaseActivity {
    private SwipeRefreshLayout swipeRefreshLayout;  // ← NEW
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        
        setupViewModels();
        observeWorkspaceViewModel();
        setupRecyclerView();
        setupSwipeRefresh();  // ← NEW
        
        loadWorkspacesWithCache();
        setupBottomNavigation(0);
    }
    
    /**
     * Setup pull-to-refresh
     */
    private void setupSwipeRefresh() {
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        
        // Configure refresh colors
        swipeRefreshLayout.setColorSchemeResources(
            R.color.colorPrimary,
            R.color.colorAccent,
            R.color.colorPrimaryDark
        );
        
        // Set refresh listener
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.d(TAG, "User triggered pull-to-refresh");
            forceRefreshWorkspaces();
        });
    }
    
    /**
     * Force refresh workspaces (bypass cache)
     * Called by pull-to-refresh
     */
    private void forceRefreshWorkspaces() {
        Log.d(TAG, "Force refreshing workspaces from API...");
        
        // Clear cache first
        App.dependencyProvider.clearWorkspaceCache();
        
        // Show refreshing indicator
        swipeRefreshLayout.setRefreshing(true);
        
        // Load from API directly (cache empty will trigger API call)
        loadWorkspacesWithCache();
        
        // Stop refreshing after 3 seconds max (timeout)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
                Log.d(TAG, "Refresh timeout");
            }
        }, 3000);
    }
    
    /**
     * Load workspaces with cache (UPDATED)
     */
    private void loadWorkspacesWithCache() {
        final long startTime = System.currentTimeMillis();
        
        App.dependencyProvider.getWorkspaceRepositoryWithCache()
            .getWorkspaces(new WorkspaceRepositoryImplWithCache.WorkspaceCallback() {
                @Override
                public void onSuccess(List<Workspace> workspaces) {
                    long duration = System.currentTimeMillis() - startTime;
                    
                    runOnUiThread(() -> {
                        // Stop refreshing indicator ← NEW
                        if (swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        
                        if (workspaces != null && !workspaces.isEmpty()) {
                            homeAdapter.setWorkspaceList(workspaces);
                            
                            String message;
                            if (duration < 100) {
                                message = "⚡ Cache: " + duration + "ms (" + workspaces.size() + " workspaces)";
                            } else {
                                message = "🌐 API: " + duration + "ms (" + workspaces.size() + " workspaces)";
                            }
                            Toast.makeText(HomeActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                
                @Override
                public void onCacheEmpty() {
                    runOnUiThread(() -> {
                        workspaceViewModel.loadWorkspaces();
                    });
                }
                
                @Override
                public void onError(Exception e) {
                    runOnUiThread(() -> {
                        // Stop refreshing on error ← NEW
                        if (swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        
                        Toast.makeText(HomeActivity.this, 
                            "Error: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    });
                }
            });
    }
}
```

---

### Step 3: Add Pull-to-Refresh to InboxActivity (20 phút)

**File:** `res/layout/inbox_main.xml`

```xml
<!-- Wrap RecyclerView with SwipeRefreshLayout -->
<androidx.swiperefreshlayout.widget.SwipeRefreshLayout
    android:id="@+id/swipeRefreshLayout"
    android:layout_width="match_parent"
    android:layout_height="0dp"
    app:layout_constraintTop_toBottomOf="@id/inboxHeader"
    app:layout_constraintBottom_toTopOf="@id/bottomNavigation">
    
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recyclerViewInbox"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
        
</androidx.swiperefreshlayout.widget.SwipeRefreshLayout>
```

**File:** `feature/home/ui/InboxActivity.java`

```java
public class InboxActivity extends BaseActivity {
    private SwipeRefreshLayout swipeRefreshLayout;  // ← NEW
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inbox_main);
        
        setupViewModel();
        initViews();
        setupRecyclerView();
        setupSwipeRefresh();  // ← NEW
        setupNotificationCard();
        setupQuickAddTask();
        observeViewModel();
        setupBottomNavigation(1);
        
        loadAllTasks();
    }
    
    private void setupSwipeRefresh() {
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        
        swipeRefreshLayout.setColorSchemeResources(
            R.color.colorPrimary,
            R.color.colorAccent
        );
        
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.d(TAG, "User triggered pull-to-refresh for tasks");
            forceRefreshTasks();
        });
    }
    
    private void forceRefreshTasks() {
        Log.d(TAG, "Force refreshing tasks from API...");
        
        // Clear task cache
        App.dependencyProvider.clearTaskCache();
        
        swipeRefreshLayout.setRefreshing(true);
        
        // Reload tasks (will fetch from API)
        loadAllTasks();
        
        // Timeout after 3 seconds
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 3000);
    }
    
    private void loadAllTasks() {
        final long startTime = System.currentTimeMillis();
        
        App.dependencyProvider.getTaskRepositoryWithCache()
            .getAllTasks(new TaskRepositoryImplWithCache.TaskCallback() {
                @Override
                public void onSuccess(List<Task> tasks) {
                    long duration = System.currentTimeMillis() - startTime;
                    
                    runOnUiThread(() -> {
                        // Stop refreshing ← NEW
                        if (swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        
                        if (tasks != null && !tasks.isEmpty()) {
                            taskAdapter.setTasks(tasks);
                            recyclerView.setVisibility(View.VISIBLE);
                            
                            String message = duration < 100 
                                ? "⚡ Cache: " + duration + "ms" 
                                : "🌐 API: " + duration + "ms";
                            Toast.makeText(InboxActivity.this, message, Toast.LENGTH_SHORT).show();
                        } else {
                            taskAdapter.setTasks(new ArrayList<>());
                            recyclerView.setVisibility(View.GONE);
                        }
                    });
                }
                
                @Override
                public void onCacheEmpty() {
                    runOnUiThread(() -> {
                        Log.d(TAG, "Cache empty - first load");
                    });
                }
                
                @Override
                public void onError(Exception e) {
                    runOnUiThread(() -> {
                        if (swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        Toast.makeText(InboxActivity.this, 
                            "Error: " + e.getMessage(), 
                            Toast.LENGTH_LONG).show();
                    });
                }
            });
    }
}
```

---

## ✅ TESTING CHECKLIST

### Test Issue 1: Cache TTL

**Scenario 1: Fresh Cache**
```
1. Open app → Load Home
   Expected: Cache hit (30ms)
   
2. Wait 2 minutes
3. Close & reopen app
   Expected: Still cache hit (not expired)
```

**Scenario 2: Expired Cache**
```
1. Open app → Load Home
   Expected: Cache hit
   
2. Wait 6 minutes (beyond 5 min TTL)
3. Close & reopen app
   Expected: "Cache expired", force API call
```

**Scenario 3: Verify Metadata**
```
1. Check database: SELECT * FROM cache_metadata
   Expected: 
   - cacheKey: "workspaces"
   - lastUpdated: recent timestamp
   - itemCount: number of workspaces
```

---

### Test Issue 2: Pull-to-Refresh

**Scenario 1: Basic Pull-to-Refresh**
```
1. Open Home tab
2. Swipe down from top
   Expected: Refresh indicator appears
   
3. Wait for refresh
   Expected: 
   - Data reloaded from API
   - Toast shows "🌐 API: Xms"
   - Indicator disappears
```

**Scenario 2: Pull-to-Refresh on Inbox**
```
1. Open Inbox tab
2. Swipe down
   Expected: Tasks refresh from API
```

**Scenario 3: Offline Pull-to-Refresh**
```
1. Turn off wifi/data
2. Swipe down on Home
   Expected:
   - Refresh indicator appears
   - After timeout (3s), indicator disappears
   - Toast shows error
   - Old cache data still visible
```

---

## 📊 EXPECTED RESULTS

### After Implementing TTL:

**Before:**
```
Cache forever → Stale data indefinitely
```

**After:**
```
Cache for 5 minutes → Auto-refresh when expired
User always sees relatively fresh data
```

### After Implementing Pull-to-Refresh:

**Before:**
```
No manual refresh → Must restart app
```

**After:**
```
Swipe down → Force refresh anytime
User has control over data freshness
```

---

## 🎯 CONFIGURATION OPTIONS

### Customize TTL per Entity:

```java
// In repositories
private static final long WORKSPACE_TTL = 5 * 60 * 1000;  // 5 min (slow changing)
private static final long TASK_TTL = 3 * 60 * 1000;       // 3 min (fast changing)
private static final long PROJECT_TTL = 10 * 60 * 1000;   // 10 min (very slow)
```

### Make TTL Configurable:

```java
public class CacheConfig {
    public static long getWorkspaceTTL() {
        // Could read from SharedPreferences
        return BuildConfig.DEBUG 
            ? 1 * 60 * 1000   // 1 min in debug
            : 5 * 60 * 1000;  // 5 min in production
    }
}
```

---

## 📝 IMPLEMENTATION TIMELINE

### Day 1 (1.5 giờ):
- ✅ Create CacheMetadata entity (15 min)
- ✅ Create CacheMetadataDao (10 min)
- ✅ Update AppDatabase with migration (10 min)
- ✅ Update WorkspaceRepositoryImplWithCache (30 min)
- ✅ Update TaskRepositoryImplWithCache (30 min)

### Day 2 (1 giờ):
- ✅ Update DependencyProvider (10 min)
- ✅ Add SwipeRefreshLayout to layouts (10 min)
- ✅ Implement pull-to-refresh HomeActivity (20 min)
- ✅ Implement pull-to-refresh InboxActivity (20 min)

### Day 3 (30 min):
- ✅ Test all scenarios
- ✅ Fix any issues
- ✅ Document changes

**Total:** ~3 giờ

---

## 💡 ALTERNATIVE SOLUTIONS

### Alternative 1: Server-Driven Cache Policy

```java
// Server returns cache headers
Cache-Control: max-age=300  // 5 minutes

// Client respects server policy
if (response.headers().get("Cache-Control") != null) {
    long maxAge = parseCacheControl(response);
    metadata.setTtl(maxAge);
}
```

### Alternative 2: Smart Refresh

```java
// Only refresh if user is active
if (isUserActive() && isCacheStale()) {
    silentRefresh();
}

// Don't refresh if user is idle
if (isUserIdle()) {
    keepUsingCache();
}
```

### Alternative 3: Conditional Refresh

```java
// Check with server if data changed (ETag)
if (serverETag != cachedETag) {
    downloadNewData();
} else {
    useCachedData();
}
```

---

## 🎖️ BENEFITS SUMMARY

### After Implementation:

| Metric | Before | After | Improvement |
|--------|--------|-------|-------------|
| **Cache Freshness** | Forever | 5 minutes | ✅ Auto-refresh |
| **Manual Refresh** | ❌ No option | ✅ Pull-to-refresh | User control |
| **Data Staleness** | Indefinite | Max 5 minutes | ✅ 97% better |
| **User Experience** | Stale data | Fresh data | ✅ Much better |

---

## 📋 CHECKLIST

### Implementation:
- [ ] Create CacheMetadata entity
- [ ] Create CacheMetadataDao
- [ ] Update AppDatabase (v3 → v4)
- [ ] Update WorkspaceRepositoryImplWithCache
- [ ] Update TaskRepositoryImplWithCache
- [ ] Update DependencyProvider
- [ ] Add SwipeRefreshLayout to layouts
- [ ] Implement pull-to-refresh in HomeActivity
- [ ] Implement pull-to-refresh in InboxActivity

### Testing:
- [ ] Test fresh cache (< TTL)
- [ ] Test expired cache (> TTL)
- [ ] Test pull-to-refresh online
- [ ] Test pull-to-refresh offline
- [ ] Verify database migration
- [ ] Check memory usage
- [ ] Performance testing

### Documentation:
- [ ] Update README
- [ ] Add inline comments
- [ ] Create user guide
- [ ] Update changelog

---

**Prepared by:** Lead Developer  
**Date:** October 19, 2025  
**Priority:** P3 (Nice to have)  
**Status:** Ready for implementation

