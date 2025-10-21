# ⚡ PLAN BUỔI TỐI - PARALLEL IMPLEMENTATION
## 2 Developers làm song song, hoàn toàn độc lập

**Date:** October 18, 2025 - Tối nay  
**Timeline:** 2-2.5 giờ (parallel)  
**Team:** Person 1 & Person 2 (độc lập)

---

## 📖 NGỮ CẢNH - ĐỌC KỸ TRƯỚC KHI BẮT ĐẦU

### 🎯 **App Plantracker là gì?**

**Plantracker** là app quản lý công việc theo phong cách **Trello + Jira**, gồm:

#### Bottom Navigation (4 tabs):
```
[Workspace/Home] | [Inbox] | [Notification] | [Settings]
```

**1. Workspace/Home Tab:**
- Hiển thị danh sách **Workspaces** (của bản thân + được mời)
- Click workspace → Xem danh sách **Projects** trong workspace
- Click project → Xem **Kanban board** (TODO → IN_PROGRESS → DONE)
- User có thể tạo task, di chuyển task giữa các cột

**2. Inbox Tab:**
- Hiển thị **danh sách tasks** dạng flat list (KHÔNG phải Kanban)
- Tasks do bản thân tạo qua quick add
- Tasks được assign cho bản thân
- Quick access để xem/edit tasks nhanh

**3. Notification Tab:**
- Thông báo cho user

**4. Settings Tab:**
- Cài đặt cơ bản

---

### ❌ **VẤN ĐỀ HIỆN TẠI - TẠI SAO CẦN CACHE?**

#### Vấn đề Performance:

**Home Tab (Workspaces):**
```
User mở app → Loading... → Đợi 800-1000ms → Workspaces hiện ra
User switch tab → Loading... → Đợi lại 800ms
User đóng app, mở lại → Loading... → Đợi lại 800ms
```
❌ **Mỗi lần đều phải đợi API** → Trải nghiệm kém!

**Inbox Tab (Tasks):**
```
User mở Inbox → Loading... → Đợi 1200-1500ms → Tasks hiện ra
User tạo task mới → Loading... → Đợi lại 1200ms để reload
User đóng app, mở lại Inbox → Loading... → Đợi lại 1200ms
```
❌ **Mỗi lần đều call API** → Lãng phí thời gian & bandwidth!

#### Vấn đề Offline:
```
User không có internet → App không hoạt động
User ở vùng có mạng yếu → Loading mãi không xong
```
❌ **Không có offline support** → App vô dụng khi mất mạng!

---

### ✅ **GIẢI PHÁP: ROOM DATABASE CACHING**

#### Cách hoạt động:

**Lần 1 (First load - Cache empty):**
```
User mở Home → Check cache → Empty → Call API (800ms) → Save to cache → Show data
                                                              ↓
                                                         Cache trong database
```

**Lần 2 (Cached load):**
```
User mở Home → Check cache → Found! → Show data ngay (30ms) ⚡
                                    ↓
                              API call in background (silent refresh)
```

**Kết quả:**
- ⚡ **Load time: 800ms → 30ms** (96% faster!)
- 📴 **Offline mode:** App vẫn hiển thị data đã cache
- 🚀 **Better UX:** User thấy data ngay lập tức

---

### 🏗️ **KIẾN TRÚC ĐÃ CÓ SẴN (Infrastructure Ready)**

Team đã setup sẵn toàn bộ infrastructure:

#### ✅ **Room Database (Đã có):**
```
AppDatabase (version 3)
├── WorkspaceEntity (id, name, description, userId, dates)
├── TaskEntity (23 fields: id, title, status, position, etc.)
├── ProjectEntity (id, name, key, boardType, etc.)
└── BoardEntity (id, projectId, name, order)
```

#### ✅ **DAOs (Đã có):**
```
WorkspaceDao → CRUD + queries cho workspaces
TaskDao → CRUD + queries cho tasks
ProjectDao → CRUD + queries cho projects
BoardDao → CRUD + queries cho boards
```

#### ✅ **Mappers (Đã có):**
```
WorkspaceEntityMapper → Workspace ↔ WorkspaceEntity
TaskEntityMapper → Task ↔ TaskEntity
WorkspaceMapper → Workspace ↔ WorkspaceDTO (API)
TaskMapper → Task ↔ TaskDTO (API)
```

#### ✅ **DependencyProvider (Đã có):**
```
Singleton quản lý:
- Database instance
- All DAOs (taskDao, workspaceDao, projectDao, boardDao)
- ExecutorService (background threads)
- TokenManager (user authentication)
```

#### ✅ **TaskRepositoryImplWithCache (Đã có):**
```
Repository mẫu implement cache pattern:
- Cache-first approach
- Background API refresh
- Callbacks: onSuccess, onCacheEmpty, onError
```

**→ Bạn chỉ cần SỬ DỤNG những gì đã có, KHÔNG phải tạo từ đầu!**

---

### 🎯 **NHIỆM VỤ TỐI NAY**

**Mục tiêu:** Cache 2 screens quan trọng nhất

#### **Person 1: Cache Workspaces (HomeActivity)**
**Tại sao quan trọng?**
- Home là màn hình đầu tiên user thấy khi mở app
- Workspaces ít thay đổi → rất phù hợp để cache
- Impact lớn: Mỗi user mở app nhiều lần/ngày

**Công việc:**
1. Tạo `WorkspaceRepositoryImplWithCache` (giống TaskRepositoryImplWithCache)
2. Add vào DependencyProvider
3. Integrate vào HomeActivity
4. Test: First load (API) vs Cached load

#### **Person 2: Cache Tasks (InboxActivity)**
**Tại sao quan trọng?**
- Inbox là nơi user check tasks thường xuyên
- Tasks thay đổi nhiều nhưng cache vẫn tốt (silent refresh)
- Quick add task cần reload nhanh

**Công việc:**
1. Integrate `TaskRepositoryImplWithCache` (đã có sẵn!) vào InboxActivity
2. Replace API calls với cached repository
3. Test: First load, cached load, create task

---

### 📊 **KẾT QUẢ MONG ĐỢI**

#### Performance:
```
BEFORE (No cache):
Home load:  800-1000ms (every time)
Inbox load: 1200-1500ms (every time)

AFTER (With cache):
Home load:  30-50ms (instant!) ⚡
Inbox load: 30-50ms (instant!) ⚡

IMPROVEMENT: 95-97% faster!
```

#### User Experience:
```
✅ Mở app → Workspaces hiện ngay lập tức
✅ Switch tab → Tasks hiện ngay lập tức
✅ Đóng/mở app → Data vẫn ở đó, không loading
✅ Offline mode → App vẫn dùng được
```

#### Technical Benefits:
```
✅ Giảm 80% API calls
✅ Giảm bandwidth usage
✅ Giảm load lên server
✅ Better battery life (ít network requests)
```

---

### 🔧 **PATTERN: CACHE-FIRST WITH SILENT REFRESH**

#### Workflow chi tiết:

```
1. User mở màn hình
   ↓
2. Check cache trong database (30ms)
   ├─ Có data → Show ngay ✓
   │           └─ Call API in background (silent)
   │                └─ Update cache khi có response
   │
   └─ Không có data → Show loading
                   └─ Call API (800-1500ms)
                        └─ Show data + Save to cache
```

**Ưu điểm:**
- User thấy data ngay (cached)
- Data luôn fresh (background refresh)
- Không block UI thread

#### Code Pattern (Example):

```java
// Step 1: Check cache
executorService.execute(() -> {
    List<Entity> cached = dao.getAll();
    
    if (cached != null && !cached.isEmpty()) {
        // Step 2: Return cache immediately
        mainHandler.post(() -> callback.onSuccess(toDomain(cached)));
    }
    
    // Step 3: Fetch from API (background)
    apiService.getData().enqueue(new Callback<DTO>() {
        @Override
        public void onResponse(Response<DTO> response) {
            // Step 4: Update cache
            dao.insertAll(toEntity(response.body()));
            
            // Step 5: Callback only if first load
            if (cached == null || cached.isEmpty()) {
                mainHandler.post(() -> callback.onSuccess(...));
            }
        }
    });
});
```

**Key points:**
- ✅ Background thread cho database operations
- ✅ Main thread cho UI updates (runOnUiThread)
- ✅ ExecutorService quản lý threads
- ✅ Handler post về main thread

---

### 🚨 **QUAN TRỌNG - ĐỌC KỸ!**

#### Thread Safety:
```java
// ❌ WRONG - Database on main thread
List<Task> tasks = taskDao.getAll(); // Crash!

// ✅ CORRECT - Use ExecutorService
executorService.execute(() -> {
    List<Task> tasks = taskDao.getAll(); // OK
    
    // Update UI on main thread
    runOnUiThread(() -> {
        adapter.setTasks(tasks);
    });
});
```

#### Callback Pattern:
```java
// Repository callback
interface TaskCallback {
    void onSuccess(List<Task> tasks);  // Data ready
    void onCacheEmpty();                // No cache, loading from API
    void onError(Exception e);          // Error occurred
}
```

#### Null Safety:
```java
if (tasks != null && !tasks.isEmpty()) {
    // Use data
} else {
    // Handle empty case
}
```

---

### 🎓 **HỌC TỪ CODE MẪU**

#### TaskRepositoryImplWithCache (Reference):

Bạn có thể tham khảo file này:
```
app/src/main/java/com/example/tralalero/data/repository/TaskRepositoryImplWithCache.java
```

**Pattern được implement:**
```java
public class TaskRepositoryImplWithCache {
    private final TaskDao taskDao;
    private final ExecutorService executorService;
    
    public void getAllTasks(TaskCallback callback) {
        executorService.execute(() -> {
            // 1. Check cache
            List<TaskEntity> cached = taskDao.getAllByUserId(userId);
            
            if (cached != null && !cached.isEmpty()) {
                // 2. Return cache
                callback.onSuccess(TaskEntityMapper.toDomainList(cached));
            } else {
                // 3. Cache empty
                callback.onCacheEmpty();
            }
            
            // 4. Fetch from API (not shown here)
        });
    }
}
```

**Person 1:** Bạn sẽ tạo `WorkspaceRepositoryImplWithCache` giống hệt pattern này!

**Person 2:** Bạn chỉ cần GỌI method `getAllTasks()` trong InboxActivity!

---

### 📱 **TESTING - QUAN TRỌNG!**

#### Test Checklist:

**First Load Test (Cache empty):**
```
1. Clear app data
2. Open app → Login
3. Navigate to your screen
4. Observe: Loading... → Data appears
5. Check logcat: "Cache empty" → "API CALL: xxxms"
6. Check toast: "🌐 API: xxxms"
```

**Cached Load Test:**
```
1. Close app completely
2. Reopen app
3. Navigate to your screen
4. Observe: Data appears INSTANTLY
5. Check logcat: "CACHE HIT: xxxms"
6. Check toast: "⚡ Cache: 30ms"
```

**Success = Second load < 100ms!**

---

### 💡 **TIPS & TRICKS**

#### Debugging:
```java
// Add logs to trace execution
Log.d(TAG, "1. Checking cache...");
Log.d(TAG, "2. Cache found: " + cached.size() + " items");
Log.d(TAG, "3. Calling API in background...");
Log.d(TAG, "4. API response: " + response.code());
```

#### Performance Measurement:
```java
long startTime = System.currentTimeMillis();
// ... load data ...
long duration = System.currentTimeMillis() - startTime;
Log.i(TAG, "Load time: " + duration + "ms");
```

#### Common Issues:

**Issue 1: "Cannot resolve symbol App.dependencyProvider"**
```java
// Add import
import com.example.tralalero.App.App;
```

**Issue 2: Data không cache**
```java
// Check: API response có save vào database không?
dao.insertAll(entities);
Log.d(TAG, "✓ Saved to cache"); // Should see this log
```

**Issue 3: UI không update**
```java
// Must run on UI thread
runOnUiThread(() -> {
    adapter.setData(data);
});
```

---

### 🤝 **TEAM COLLABORATION**

#### Communication:
```
✅ Báo cáo progress mỗi 30 phút
✅ Hỏi ngay khi stuck (đừng tự mò >15 phút)
✅ Test riêng xong → Báo "Ready for merge test"
✅ Giúp nhau review code trước khi commit
```

#### When Stuck:
```
1. Check logcat for errors
2. Verify imports
3. Clean & rebuild project
4. Check file in docs/ for reference
5. Ask teammate or leader
```

---

### 📚 **TÀI LIỆU THAM KHẢO**

**Trong project:**
```
docs/room implementation/
├── Room_Database_Caching_Implementation_Guide.md  (Chi tiết về Room)
├── All_Mappers_Complete_Summary.md                 (Danh sách mappers)
└── PARALLEL_PLAN_2_DEVS.md                          (File này!)

Code references:
├── TaskRepositoryImplWithCache.java                 (Pattern mẫu)
├── DependencyProvider.java                          (Singleton provider)
├── TaskEntityMapper.java                            (Entity ↔ Domain)
└── AppDatabase.java                                 (Database definition)
```

**Online references:**
- Room Database: https://developer.android.com/training/data-storage/room
- ExecutorService: https://developer.android.com/reference/java/util/concurrent/ExecutorService

---

## 🎯 STRATEGY: PARALLEL WORK - ZERO DEPENDENCY

### ✅ **Tại sao có thể làm song song?**

**Person 1:** Cache **Workspaces** (HomeActivity)
- ✅ Dùng WorkspaceDao (đã có)
- ✅ Tạo WorkspaceRepositoryImplWithCache
- ✅ Integrate vào HomeActivity
- ❌ **KHÔNG phụ thuộc** vào Person 2

**Person 2:** Cache **Tasks** (InboxActivity)  
- ✅ Dùng TaskRepositoryImplWithCache (đã có sẵn!)
- ✅ Chỉ cần integrate vào InboxActivity
- ❌ **KHÔNG phụ thuộc** vào Person 1

**→ 2 người code cùng lúc, không conflict, không đợi nhau!**

---

## 👤 PERSON 1: WORKSPACE CACHE (2 giờ)

### 📋 **Tasks Overview:**
1. Tạo WorkspaceRepositoryImplWithCache (50 phút)
2. Update DependencyProvider (15 phút)
3. Integrate vào HomeActivity (40 phút)
4. Test riêng (15 phút)

**Total: 2 giờ**

---

### ⏰ TASK 1.1: Create WorkspaceRepositoryImplWithCache (50 phút)

**File mới:** `data/repository/WorkspaceRepositoryImplWithCache.java`

**Full code (copy-paste):**

```java
package com.example.tralalero.data.repository;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.tralalero.data.local.database.dao.WorkspaceDao;
import com.example.tralalero.data.local.database.entity.WorkspaceEntity;
import com.example.tralalero.data.mapper.WorkspaceEntityMapper;
import com.example.tralalero.data.remote.api.WorkspaceApiService;
import com.example.tralalero.data.remote.dto.workspace.WorkspaceDTO;
import com.example.tralalero.data.remote.mapper.WorkspaceMapper;
import com.example.tralalero.domain.model.Workspace;
import com.example.tralalero.domain.repository.IWorkspaceRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Workspace Repository with Room Database caching
 * Pattern: Cache-first with silent background refresh
 * 
 * @author Person 1
 * @date October 18, 2025
 */
public class WorkspaceRepositoryImplWithCache implements IWorkspaceRepository {
    private static final String TAG = "WorkspaceRepoCache";
    
    private final WorkspaceApiService apiService;
    private final WorkspaceDao workspaceDao;
    private final ExecutorService executorService;
    private final Handler mainHandler;
    
    public WorkspaceRepositoryImplWithCache(
            WorkspaceApiService apiService,
            WorkspaceDao workspaceDao,
            ExecutorService executorService) {
        this.apiService = apiService;
        this.workspaceDao = workspaceDao;
        this.executorService = executorService;
        this.mainHandler = new Handler(Looper.getMainLooper());
        Log.d(TAG, "WorkspaceRepositoryImplWithCache initialized");
    }
    
    // ==================== GET ALL WORKSPACES ====================
    
    @Override
    public void getWorkspaces(RepositoryCallback<List<Workspace>> callback) {
        if (callback == null) {
            Log.e(TAG, "Callback is null");
            return;
        }
        
        executorService.execute(() -> {
            try {
                // 1. Return from cache immediately (30ms)
                List<WorkspaceEntity> cached = workspaceDao.getAll();
                if (cached != null && !cached.isEmpty()) {
                    List<Workspace> cachedWorkspaces = WorkspaceEntityMapper.toDomainList(cached);
                    mainHandler.post(() -> callback.onSuccess(cachedWorkspaces));
                    Log.d(TAG, "✓ Returned " + cached.size() + " workspaces from cache");
                }
                
                // 2. Fetch from network in background (500-1000ms)
                fetchWorkspacesFromNetwork(callback, cached == null || cached.isEmpty());
            } catch (Exception e) {
                Log.e(TAG, "Error in getWorkspaces", e);
                mainHandler.post(() -> callback.onError("Error: " + e.getMessage()));
            }
        });
    }
    
    private void fetchWorkspacesFromNetwork(
            RepositoryCallback<List<Workspace>> callback, 
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
                                Log.d(TAG, "✓ Cached " + workspaces.size() + " workspaces");
                            } catch (Exception e) {
                                Log.e(TAG, "Error caching workspaces", e);
                            }
                        });
                        
                        // Only callback if first load (no cache)
                        if (isFirstLoad && callback != null) {
                            mainHandler.post(() -> callback.onSuccess(workspaces));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing workspaces", e);
                    }
                } else if (isFirstLoad && callback != null) {
                    mainHandler.post(() -> 
                        callback.onError("Failed to load workspaces: " + response.code()));
                }
            }
            
            @Override
            public void onFailure(Call<List<WorkspaceDTO>> call, Throwable t) {
                Log.e(TAG, "Network error", t);
                if (isFirstLoad && callback != null) {
                    mainHandler.post(() -> 
                        callback.onError("Network error: " + t.getMessage()));
                }
            }
        });
    }
    
    // ==================== GET WORKSPACE BY ID ====================
    
    @Override
    public void getWorkspaceById(String workspaceId, RepositoryCallback<Workspace> callback) {
        executorService.execute(() -> {
            try {
                WorkspaceEntity cached = workspaceDao.getById(workspaceId);
                if (cached != null) {
                    Workspace workspace = WorkspaceEntityMapper.toDomain(cached);
                    mainHandler.post(() -> callback.onSuccess(workspace));
                    Log.d(TAG, "✓ Returned workspace from cache: " + workspaceId);
                }
                
                fetchWorkspaceByIdFromNetwork(workspaceId, callback, cached == null);
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Error: " + e.getMessage()));
            }
        });
    }
    
    private void fetchWorkspaceByIdFromNetwork(
            String workspaceId, 
            RepositoryCallback<Workspace> callback, 
            boolean isFirstLoad) {
        
        apiService.getWorkspaceById(workspaceId).enqueue(new Callback<WorkspaceDTO>() {
            @Override
            public void onResponse(Call<WorkspaceDTO> call, Response<WorkspaceDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Workspace workspace = WorkspaceMapper.toDomain(response.body());
                    
                    // Cache
                    executorService.execute(() -> {
                        WorkspaceEntity entity = WorkspaceEntityMapper.toEntity(workspace);
                        workspaceDao.insert(entity);
                    });
                    
                    if (isFirstLoad) {
                        mainHandler.post(() -> callback.onSuccess(workspace));
                    }
                }
            }
            
            @Override
            public void onFailure(Call<WorkspaceDTO> call, Throwable t) {
                if (isFirstLoad) {
                    mainHandler.post(() -> callback.onError("Error: " + t.getMessage()));
                }
            }
        });
    }
    
    // ==================== WRITE OPERATIONS (Delegate to API) ====================
    
    @Override
    public void createWorkspace(Workspace workspace, RepositoryCallback<Workspace> callback) {
        WorkspaceDTO dto = WorkspaceMapper.toDto(workspace);
        apiService.createWorkspace(dto).enqueue(new Callback<WorkspaceDTO>() {
            @Override
            public void onResponse(Call<WorkspaceDTO> call, Response<WorkspaceDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Workspace created = WorkspaceMapper.toDomain(response.body());
                    
                    // Cache new workspace
                    executorService.execute(() -> {
                        WorkspaceEntity entity = WorkspaceEntityMapper.toEntity(created);
                        workspaceDao.insert(entity);
                        Log.d(TAG, "✓ Cached new workspace: " + created.getId());
                    });
                    
                    mainHandler.post(() -> callback.onSuccess(created));
                } else {
                    mainHandler.post(() -> callback.onError("Failed to create workspace"));
                }
            }
            
            @Override
            public void onFailure(Call<WorkspaceDTO> call, Throwable t) {
                mainHandler.post(() -> callback.onError("Error: " + t.getMessage()));
            }
        });
    }
    
    @Override
    public void updateWorkspace(String workspaceId, Workspace workspace, 
                               RepositoryCallback<Workspace> callback) {
        WorkspaceDTO dto = WorkspaceMapper.toDto(workspace);
        apiService.updateWorkspace(workspaceId, dto).enqueue(new Callback<WorkspaceDTO>() {
            @Override
            public void onResponse(Call<WorkspaceDTO> call, Response<WorkspaceDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Workspace updated = WorkspaceMapper.toDomain(response.body());
                    
                    // Update cache
                    executorService.execute(() -> {
                        WorkspaceEntity entity = WorkspaceEntityMapper.toEntity(updated);
                        workspaceDao.update(entity);
                        Log.d(TAG, "✓ Updated workspace in cache: " + workspaceId);
                    });
                    
                    mainHandler.post(() -> callback.onSuccess(updated));
                } else {
                    mainHandler.post(() -> callback.onError("Failed to update workspace"));
                }
            }
            
            @Override
            public void onFailure(Call<WorkspaceDTO> call, Throwable t) {
                mainHandler.post(() -> callback.onError("Error: " + t.getMessage()));
            }
        });
    }
    
    @Override
    public void deleteWorkspace(String workspaceId, RepositoryCallback<Void> callback) {
        apiService.deleteWorkspace(workspaceId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Delete from cache
                    executorService.execute(() -> {
                        WorkspaceEntity entity = workspaceDao.getById(workspaceId);
                        if (entity != null) {
                            workspaceDao.delete(entity);
                            Log.d(TAG, "✓ Deleted workspace from cache: " + workspaceId);
                        }
                    });
                    
                    mainHandler.post(() -> callback.onSuccess(null));
                } else {
                    mainHandler.post(() -> callback.onError("Failed to delete workspace"));
                }
            }
            
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                mainHandler.post(() -> callback.onError("Error: " + t.getMessage()));
            }
        });
    }
    
    // Note: Other methods (getProjectsByWorkspaceId, getBoardsByWorkspaceId) 
    // can be implemented later or delegate to API for now
}
```

**Verification:**
```
□ File created in correct package
□ No red lines/errors
□ Imports resolved
□ Build successful
```

---

### ⏰ TASK 1.2: Update DependencyProvider (15 phút)

**File:** `core/DependencyProvider.java`

**Add these sections:**

```java
// 1. Add import
import com.example.tralalero.data.remote.api.WorkspaceApiService;

// 2. Add field (after taskRepositoryWithCache)
private WorkspaceRepositoryImplWithCache workspaceRepositoryWithCache;

// 3. Add getter (after getTaskRepositoryWithCache)
public synchronized WorkspaceRepositoryImplWithCache getWorkspaceRepositoryWithCache() {
    if (workspaceRepositoryWithCache == null) {
        // Get WorkspaceApiService from ApiClient
        WorkspaceApiService apiService = com.example.tralalero.network.ApiClient
            .get(tokenManager)
            .create(WorkspaceApiService.class);
        
        workspaceRepositoryWithCache = new WorkspaceRepositoryImplWithCache(
            apiService, 
            workspaceDao, 
            executorService
        );
        Log.d(TAG, "✓ WorkspaceRepositoryImplWithCache created");
    }
    return workspaceRepositoryWithCache;
}
```

**Verification:**
```
□ Imports added
□ Field added
□ Getter added
□ No compilation errors
□ Build successful
```

---

### ⏰ TASK 1.3: Integrate vào HomeActivity (40 phút)

**File:** `feature/home/ui/Home/HomeActivity.java`

**Step 1: Add imports**

```java
import com.example.tralalero.App.App;
import com.example.tralalero.data.repository.WorkspaceRepositoryImplWithCache;
import com.example.tralalero.domain.repository.IWorkspaceRepository;
```

**Step 2: Add new method (before observeWorkspaceViewModel)**

```java
/**
 * Load workspaces with cache
 * Cache-first approach: instant load from cache, then refresh from API
 * 
 * @author Person 1
 */
private void loadWorkspacesWithCache() {
    Log.d(TAG, "Loading workspaces with cache...");
    final long startTime = System.currentTimeMillis();
    
    App.dependencyProvider.getWorkspaceRepositoryWithCache()
        .getWorkspaces(new IWorkspaceRepository.RepositoryCallback<List<Workspace>>() {
            @Override
            public void onSuccess(List<Workspace> workspaces) {
                long duration = System.currentTimeMillis() - startTime;
                
                runOnUiThread(() -> {
                    if (workspaces != null && !workspaces.isEmpty()) {
                        homeAdapter.setWorkspaceList(workspaces);
                        
                        // Performance logging
                        String message;
                        if (duration < 100) {
                            message = "⚡ Cache: " + duration + "ms (" + workspaces.size() + " workspaces)";
                            Log.i(TAG, "CACHE HIT: " + duration + "ms");
                        } else {
                            message = "🌐 API: " + duration + "ms (" + workspaces.size() + " workspaces)";
                            Log.i(TAG, "API CALL: " + duration + "ms");
                        }
                        
                        // Optional: Show toast for demo
                        Toast.makeText(HomeActivity.this, message, Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d(TAG, "No workspaces found");
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(HomeActivity.this, 
                        "Error loading workspaces: " + error, 
                        Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error: " + error);
                });
            }
        });
}
```

**Step 3: Replace loadWorkspaces call in onCreate**

Find this line (around line 95):
```java
workspaceViewModel.loadWorkspaces();
```

Replace with:
```java
loadWorkspacesWithCache();  // Use cache instead of ViewModel
```

**Verification:**
```
□ New method added
□ onCreate() updated
□ No compilation errors
□ Toast shows performance (optional)
```

---

### ⏰ TASK 1.4: Test Independently (15 phút)

**Test Flow:**

```
1. Build & Install:
   - gradlew installDebug
   - Wait for installation

2. First Load Test:
   - Clear app data (Settings → Apps → Plantracker → Clear data)
   - Open app
   - Login
   - Home tab should load
   - Check toast: "🌐 API: 800ms" (or similar)
   - Check logcat: "API CALL: xxxms"

3. Cached Load Test:
   - Close app (swipe away from recent apps)
   - Reopen app
   - Home tab loads
   - Check toast: "⚡ Cache: 30ms" (or similar)
   - Check logcat: "CACHE HIT: xxxms"
   - Workspaces appear instantly!

4. Success Criteria:
   ✓ First load: 500-1000ms from API
   ✓ Second load: <100ms from cache
   ✓ No crashes
   ✓ Workspaces display correctly
```

**Record results:**
```
First load: _______ ms
Second load: _______ ms
Improvement: _______ %
```

---

## 👤 PERSON 2: INBOX CACHE (1.5 giờ)

### 📋 **Tasks Overview:**
1. Integrate TaskRepositoryImplWithCache vào InboxActivity (60 phút)
2. Test riêng (30 phút)

**Total: 1.5 giờ (nhanh hơn vì repository đã có sẵn!)**

---

### ⏰ TASK 2.1: Integrate Cache vào InboxActivity (60 phút)

**File:** `feature/home/ui/InboxActivity.java`

**Step 1: Add imports**

```java
import com.example.tralalero.App.App;
import com.example.tralalero.data.repository.TaskRepositoryImplWithCache;
```

**Step 2: Replace loadAllTasks() method**

Find method `loadAllTasks()` (around line 200).

Replace entire method with:

```java
/**
 * Load all tasks with cache
 * Cache-first approach: instant load from cache, then refresh from API
 * 
 * @author Person 2
 */
private void loadAllTasks() {
    Log.d(TAG, "Loading inbox tasks with cache...");
    final long startTime = System.currentTimeMillis();
    
    App.dependencyProvider.getTaskRepositoryWithCache()
        .getAllTasks(new TaskRepositoryImplWithCache.TaskCallback() {
            @Override
            public void onSuccess(List<Task> tasks) {
                long duration = System.currentTimeMillis() - startTime;
                
                runOnUiThread(() -> {
                    if (tasks != null && !tasks.isEmpty()) {
                        taskAdapter.setTasks(tasks);
                        recyclerView.setVisibility(View.VISIBLE);
                        
                        // Performance logging
                        String message;
                        if (duration < 100) {
                            message = "⚡ Cache: " + duration + "ms (" + tasks.size() + " tasks)";
                            Log.i(TAG, "CACHE HIT: " + duration + "ms");
                        } else {
                            message = "🌐 API: " + duration + "ms (" + tasks.size() + " tasks)";
                            Log.i(TAG, "API CALL: " + duration + "ms");
                        }
                        
                        // Show performance toast
                        Toast.makeText(InboxActivity.this, message, Toast.LENGTH_SHORT).show();
                        
                        Log.d(TAG, "Loaded " + tasks.size() + " tasks");
                    } else {
                        taskAdapter.setTasks(new ArrayList<>());
                        recyclerView.setVisibility(View.GONE);
                        Log.d(TAG, "No tasks found");
                    }
                });
            }
            
            @Override
            public void onCacheEmpty() {
                runOnUiThread(() -> {
                    Log.d(TAG, "Cache empty - first load, waiting for API...");
                    // Loading indicator already shown in observeViewModel
                });
            }
            
            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(InboxActivity.this, 
                        "Error loading tasks: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error loading tasks", e);
                });
            }
        });
}
```

**Step 3: Verify createTask() already reloads**

Check method `createTask()` (around line 218).

Should have:
```java
new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
    loadAllTasks();  // ✅ Already calls loadAllTasks, no change needed
}, 500);
```

**→ No changes needed for createTask()!**

**Verification:**
```
□ loadAllTasks() replaced
□ Imports added
□ No compilation errors
□ Toast shows performance
□ createTask() already reloads (no change)
```

---

### ⏰ TASK 2.2: Test Independently (30 phút)

**Test Flow:**

```
1. Build & Install:
   - gradlew installDebug
   - Wait for installation

2. First Load Test:
   - Clear app data
   - Open app
   - Login
   - Navigate to Inbox tab
   - Check toast: "🌐 API: 1200ms" (or similar)
   - Check logcat: "API CALL: xxxms"

3. Cached Load Test:
   - Close app completely
   - Reopen app
   - Navigate to Inbox tab
   - Check toast: "⚡ Cache: 30ms" (or similar)
   - Check logcat: "CACHE HIT: xxxms"
   - Tasks appear instantly!

4. Create Task Test:
   - Click "Add Card" in Inbox
   - Enter task title
   - Click "Add"
   - Wait 500ms
   - Verify new task appears in list

5. Success Criteria:
   ✓ First load: 1000-2000ms from API
   ✓ Second load: <100ms from cache
   ✓ No crashes
   ✓ Tasks display correctly
   ✓ New task appears after create
```

**Record results:**
```
First load: _______ ms
Second load: _______ ms
Improvement: _______ %
```

---

## 🤝 PHASE 3: MERGE & FINAL TEST (30 phút) - Both Together

**After both finish their individual tasks**

### Test Together (30 phút)

**Full App Test:**

```
1. Clear app data (fresh start)

2. Login

3. Test Home (Person 1's work):
   - Home tab loads
   - First time: "🌐 API: 800ms"
   - Workspaces visible

4. Test Inbox (Person 2's work):
   - Inbox tab loads
   - First time: "🌐 API: 1200ms"
   - Tasks visible

5. Close & Reopen App:
   - Home tab: "⚡ Cache: xxxms" (instant!)
   - Inbox tab: "⚡ Cache: xxxms" (instant!)

6. Test Logout:
   - Settings → Logout
   - Login again
   - Both screens fetch fresh data

7. Offline Mode Test (Optional):
   - With internet: Load both screens
   - Enable Airplane Mode
   - Close & reopen app
   - Both screens should show cached data
```

**Document Final Results:**

```
PERSON 1 - HOME (WORKSPACES):
- First load: _______ ms
- Cached load: _______ ms
- Improvement: _______ %

PERSON 2 - INBOX (TASKS):
- First load: _______ ms
- Cached load: _______ ms
- Improvement: _______ %

OFFLINE MODE:
- Home works offline: ☐ YES ☐ NO
- Inbox works offline: ☐ YES ☐ NO
```

---

## 📊 WHY THIS PLAN WORKS (PARALLEL)

### ✅ **Zero Dependency:**

```
Person 1:
WorkspaceDao (exists) 
    → WorkspaceRepositoryImplWithCache (new)
    → HomeActivity (modify)
    → Test independently

Person 2:
TaskDao (exists)
    → TaskRepositoryImplWithCache (exists!)
    → InboxActivity (modify)
    → Test independently

❌ NO OVERLAP!
❌ NO WAITING!
```

### ✅ **No Merge Conflicts:**

**Different files:**
- Person 1: WorkspaceRepositoryImplWithCache.java (new file)
- Person 2: InboxActivity.java (existing file)

**Same file (DependencyProvider.java):**
- Person 1: Add WorkspaceRepositoryImplWithCache getter
- Person 2: Nothing to change (TaskRepositoryImplWithCache already exists)
- ✅ No conflict!

**HomeActivity vs InboxActivity:**
- Completely different files
- ✅ No conflict!

---

## ⏰ TIMELINE COMPARISON

### ❌ **Sequential (Old plan - 3.5 giờ):**
```
Person 1: Phase 1 (90m) 
    ↓ Wait...
Person 2: Phase 2 (90m)
    ↓ Wait...
Both: Phase 3 (60m)

Total: 240 minutes (4 giờ)
```

### ✅ **Parallel (New plan - 2 giờ):**
```
Person 1: Workspace (2h)  ┐
                          ├─ Song song
Person 2: Inbox (1.5h)    ┘

Both: Merge & Test (30m)

Total: 150 minutes (2.5 giờ)
```

**→ Nhanh hơn 40% !!! ⚡**

---

## 📋 SUCCESS CRITERIA

### ✅ Person 1 DONE khi:
- [ ] WorkspaceRepositoryImplWithCache created
- [ ] DependencyProvider updated
- [ ] HomeActivity uses cache
- [ ] First load: API call (~800ms)
- [ ] Second load: Cache hit (<100ms)
- [ ] No crashes
- [ ] Toast shows performance

### ✅ Person 2 DONE khi:
- [ ] InboxActivity uses TaskRepositoryImplWithCache
- [ ] First load: API call (~1200ms)
- [ ] Second load: Cache hit (<100ms)
- [ ] Create task works
- [ ] No crashes
- [ ] Toast shows performance

### ✅ TEAM DONE khi:
- [ ] Both Home & Inbox cache work
- [ ] Performance improvement >90%
- [ ] Offline mode works
- [ ] Logout clears cache
- [ ] No merge conflicts
- [ ] Code committed

---

## 🚨 COMMUNICATION PROTOCOL

### Start of work:
```
Person 1: "Starting Workspace cache"
Person 2: "Starting Inbox cache"
```

### Every 30 minutes:
```
Person 1: "Progress: [Task 1.1] 50% done"
Person 2: "Progress: [Task 2.1] 70% done"
```

### When finished individual work:
```
Person 1: "✓ Workspace cache done, tested OK"
Person 2: "✓ Inbox cache done, tested OK"
```

### Then:
```
Both: "Ready for merge test"
→ Meet & test together (Phase 3)
```

---

## ✅ COMMIT STRATEGY

### Person 1 commits:
```bash
git add data/repository/WorkspaceRepositoryImplWithCache.java
git add core/DependencyProvider.java
git add feature/home/ui/Home/HomeActivity.java
git commit -m "feat(cache): Add workspace cache for HomeActivity

- Create WorkspaceRepositoryImplWithCache
- Update DependencyProvider with workspace repo
- Integrate cache into HomeActivity
- Performance: 800ms → 30ms (96% faster)

Tested: First load & cached load work correctly
"
```

### Person 2 commits:
```bash
git add feature/home/ui/InboxActivity.java
git commit -m "feat(cache): Add task cache for InboxActivity

- Integrate TaskRepositoryImplWithCache into InboxActivity
- Replace taskViewModel with cached repository
- Performance: 1200ms → 30ms (97% faster)

Tested: First load, cached load, create task work correctly
"
```

### Or merge into one commit:
```bash
git add .
git commit -m "feat(cache): Add Room cache for Home and Inbox

Person 1:
- Create WorkspaceRepositoryImplWithCache
- Update DependencyProvider
- Integrate cache into HomeActivity
- Performance: 800ms → 30ms (96% faster)

Person 2:
- Integrate TaskRepositoryImplWithCache into InboxActivity
- Performance: 1200ms → 30ms (97% faster)

Overall improvement: 95% faster load times
Offline mode supported for both screens

BREAKING CHANGE: Database version 3
"
```

---

## 🎯 FINAL DEMO SCRIPT (Sau 2.5 giờ)

**Demo cho Leader:**

```
1. Show Infrastructure:
   - "Database đã setup: 4 entities, 4 DAOs, version 3"
   - "2 cached repositories: Workspace & Task"

2. Demo Home Performance:
   - Clear data
   - Open app → "🌐 API: 800ms"
   - Close & reopen → "⚡ Cache: 30ms"
   - "96% faster!"

3. Demo Inbox Performance:
   - Navigate to Inbox → "🌐 API: 1200ms" (if first time)
   - Close & reopen → Navigate to Inbox → "⚡ Cache: 30ms"
   - "97% faster!"

4. Demo Offline Mode:
   - Enable Airplane Mode
   - Close & reopen app
   - Home: Workspaces visible ✓
   - Inbox: Tasks visible ✓
   - "App works without internet!"

5. Show Logcat:
   HomeActivity: ⚡ CACHE HIT: 25ms (5 workspaces)
   InboxActivity: ⚡ CACHE HIT: 30ms (15 tasks)
   DependencyProvider: ✓ All caches cleared (on logout)
```

---

## 📊 SUMMARY

### ✅ **Parallel Plan Advantages:**

1. **No waiting:** 2 people code cùng lúc
2. **No conflicts:** Different files + different features
3. **Faster:** 2.5 giờ thay vì 4 giờ
4. **Independent testing:** Mỗi người test riêng được
5. **Clean separation:** Rõ ràng ai làm gì

### 📋 **Task Assignment:**

**Person 1 (2 giờ):**
- Create WorkspaceRepositoryImplWithCache ← File mới
- Update DependencyProvider ← 1 section nhỏ
- Update HomeActivity ← 1 method mới

**Person 2 (1.5 giờ):**
- Update InboxActivity ← Replace 1 method
- Test thoroughly ← Quan trọng

**Both (30 phút):**
- Merge & test together
- Verify no conflicts
- Final demo

---

**SẴN SÀNG BẮT ĐẦU SONG SONG! ⚡⚡**

**Person 1:** Start with WorkspaceRepositoryImplWithCache creation  
**Person 2:** Start with InboxActivity integration  
**No coordination needed until Phase 3!**
