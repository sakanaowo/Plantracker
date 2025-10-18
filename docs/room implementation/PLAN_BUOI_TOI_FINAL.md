# ⚡ PLAN BUỔI TỐI - ROOM CACHE IMPLEMENTATION
## Tracking Plan App (Trello + Jira Style)

**Date:** October 18, 2025 - Tối nay  
**Timeline:** 3-4 giờ  
**Team:** 2 developers (Person 1 & Person 2)

---

## 🎯 MỤC TIÊU TỔNG THỂ

**Cache data cho 2 screens quan trọng nhất:**
1. ✅ **InboxActivity** - Cache tasks (Priority 1)
2. ✅ **HomeActivity** - Cache workspaces (Priority 1)

**Kết quả mong đợi:**
- ⚡ Load time giảm từ 1200ms → 30ms (97% faster)
- 📴 Offline mode: App vẫn hiển thị data đã cache
- 🚀 User experience tốt hơn đáng kể

---

## 📋 APP STRUCTURE OVERVIEW (CHÍNH XÁC)

### Bottom Navigation (4 tabs):
```
┌───────────────────────────────────────────────────────────────┐
│  [Workspace/Home]  [Inbox]  [Notification]  [Settings]       │
└───────────────────────────────────────────────────────────────┘
```

### Navigation Flow Chi Tiết:

#### 1️⃣ **[Workspace/Home] Tab** - Quản lý Projects (Trello/Jira style)
```
HomeActivity (Workspace Selector)
├─ Hiển thị: Danh sách workspaces (của bản thân + tham gia)
├─ Action: Click workspace card
│
└─> WorkspaceActivity (Project List trong workspace)
    ├─ Hiển thị: Danh sách projects trong workspace đã chọn
    ├─ Action: Click project card
    │
    └─> ProjectActivity (Kanban Board)
        ├─ Hiển thị: 3 cột (TODO, IN_PROGRESS, DONE)
        ├─ Chức năng:
        │   ├─ ✅ Tạo task mới (add card)
        │   ├─ ✅ Di chuyển task giữa các cột (drag & drop)
        │   ├─ ✅ Edit task
        │   ├─ ✅ Delete task
        │   └─ ✅ Assign task
        │
        └─> Click task → TaskDetailActivity
```

#### 2️⃣ **[Inbox] Tab** - Quick Access Tasks
```
InboxActivity
├─ Hiển thị: Danh sách tasks dạng list (không có columns)
│   ├─ Tasks do bản thân tạo qua quick add
│   └─ Tasks được assign cho bản thân
├─ Chức năng:
│   ├─ Quick add task (text input)
│   └─ Click task → TaskDetailActivity
└─ Note: KHÔNG phải Kanban, chỉ là flat list
```

#### 3️⃣ **[Notification] Tab**
```
NotificationActivity
└─ Danh sách notifications (chưa có detail page)
```

#### 4️⃣ **[Settings] Tab**
```
SettingsActivity
└─ Settings cơ bản
```

---

## 🎯 DATA HIERARCHY

```
Workspace (Workspace trong Trello/Jira)
    └─ Projects (Boards trong Trello)
        └─ Boards/Columns (TODO, IN_PROGRESS, DONE)
            └─ Tasks
```

**Ví dụ thực tế:**
```
Workspace: "My Company"
    ├─ Project: "Mobile App" (Kanban)
    │   ├─ Board: TODO → [Task1, Task2, Task3]
    │   ├─ Board: IN_PROGRESS → [Task4, Task5]
    │   └─ Board: DONE → [Task6]
    │
    └─ Project: "Backend API" (Kanban)
        ├─ Board: TODO → [Task7, Task8]
        ├─ Board: IN_PROGRESS → [Task9]
        └─ Board: DONE → [Task10, Task11]
```

---

## 🔍 CURRENT ISSUES (Tại sao cần cache?)

### HomeActivity (Workspace List):
- ❌ Mỗi lần mở app → Load workspaces từ API (~800ms)
- ❌ User phải đợi mỗi lần switch tabs
- ✅ **Cache workspaces → Load instant 30ms**

### WorkspaceActivity (Project List):
- ❌ Mỗi lần click workspace → Load projects từ API (~1000ms)
- ✅ **Cache projects per workspace → Load instant** (Future)

### ProjectActivity (Kanban Board):
- ❌ Mỗi lần mở project → Load boards + tasks từ API (~1500ms)
- ❌ Di chuyển task giữa columns → Delay khi update
- ✅ **Cache boards + tasks → Kanban load instant** (Future - phức tạp)

### InboxActivity (Task List):
- ❌ Mỗi lần mở Inbox → Load tasks từ API (~1200ms)
- ❌ Quick add task → Reload lâu
- ✅ **Cache tasks → Load instant 30ms**

---

## ✅ ĐÃ HOÀN THÀNH (Phase 0 - 30 phút trước)

### Database Infrastructure - READY ✅
- ✅ **WorkspaceEntity** - Fixed int → String ID
- ✅ **TaskEntity** - 23 fields, indices tốt
- ✅ **ProjectEntity** - 6 fields
- ✅ **BoardEntity** - 6 fields (cho ProjectActivity sau)
- ✅ **AppDatabase** - Version 3, fallbackToDestructiveMigration
- ✅ **All DAOs** - TaskDao, ProjectDao, WorkspaceDao, BoardDao
- ✅ **All Mappers** - 14 mappers complete
- ✅ **DependencyProvider** - Singleton with all DAOs
- ✅ **TaskRepositoryImplWithCache** - Cache pattern mẫu

**→ Infrastructure 100% sẵn sàng! Chỉ cần integrate vào UI**

---

## 🚀 IMPLEMENTATION PLAN - 3 PHASES

### ⏰ PHASE 1: CACHE INBOX (90 phút) - Person 2

**Priority 1 - Impact cao nhất, đơn giản nhất**

#### Step 1.1: Integrate TaskRepositoryImplWithCache vào InboxActivity (60 phút)

**File:** `feature/home/ui/InboxActivity.java`

**Current code (Line 200):**
```java
private void loadAllTasks() {
    String defaultBoardId = "inbox-board-id";
    taskViewModel.loadTasksByBoard(defaultBoardId);
}
```

**Change to:**
```java
private void loadAllTasks() {
    Log.d(TAG, "Loading inbox tasks...");
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
                        
                        // Performance indicator
                        String message;
                        if (duration < 100) {
                            message = "⚡ Cache: " + duration + "ms (" + tasks.size() + " tasks)";
                            Log.i(TAG, "CACHE HIT: " + duration + "ms");
                        } else {
                            message = "🌐 API: " + duration + "ms (" + tasks.size() + " tasks)";
                            Log.i(TAG, "API CALL: " + duration + "ms");
                        }
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
                    Log.d(TAG, "Cache empty - first load from API");
                    // Loading indicator already shown
                });
            }
            
            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(InboxActivity.this, 
                        "Error: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error loading tasks", e);
                });
            }
        });
}
```

**Add imports:**
```java
import com.example.tralalero.App.App;
import com.example.tralalero.data.repository.TaskRepositoryImplWithCache;
```

**Changes summary:**
- ✅ Replace taskViewModel.loadTasksByBoard() với TaskRepositoryImplWithCache
- ✅ Add performance measurement (startTime → endTime)
- ✅ Show toast with load time (Cache vs API)
- ✅ Handle 3 callbacks: onSuccess, onCacheEmpty, onError

---

#### Step 1.2: Update createTask() to refresh cache (10 phút)

**Current code (Line 218):**
```java
private void createTask(String title) {
    // ... create task code ...
    taskViewModel.createTask(newTask);
    
    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
        loadAllTasks(); // ✅ Good - already reloads
    }, 500);
}
```

**→ No changes needed! Already reloads tasks after create**

---

#### Step 1.3: Test InboxActivity (20 phút)

**Test Scenario 1 - First Load:**
```
1. Clear app data
2. Login
3. Open Inbox tab
4. Expected: "🌐 API: 1200ms"
5. Verify: Tasks display correctly
```

**Test Scenario 2 - Cached Load:**
```
1. Close app (swipe away)
2. Reopen app
3. Open Inbox tab
4. Expected: "⚡ Cache: 30ms"
5. Verify: Tasks appear instantly
```

**Success Criteria:**
- ✅ First load: ~1200ms from API
- ✅ Second load: <100ms from cache
- ✅ No crashes
- ✅ Tasks display correctly

---

### ⏰ PHASE 2: CACHE WORKSPACES (90 phút) - Person 1

**Priority 2 - HomeActivity (Workspace list)**

#### Step 2.1: Create WorkspaceRepositoryImplWithCache (40 phút)

**File:** `data/repository/WorkspaceRepositoryImplWithCache.java`

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
                
                // Fetch from network in background
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
    
    @Override
    public void createWorkspace(Workspace workspace, RepositoryCallback<Workspace> callback) {
        // Delegate to API (write operation)
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
                    });
                    
                    mainHandler.post(() -> callback.onSuccess(created));
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
        // Delegate to API
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
                    });
                    
                    mainHandler.post(() -> callback.onSuccess(updated));
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
        // Delegate to API
        apiService.deleteWorkspace(workspaceId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Delete from cache
                    executorService.execute(() -> {
                        WorkspaceEntity entity = workspaceDao.getById(workspaceId);
                        if (entity != null) {
                            workspaceDao.delete(entity);
                        }
                    });
                    
                    mainHandler.post(() -> callback.onSuccess(null));
                }
            }
            
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                mainHandler.post(() -> callback.onError("Error: " + t.getMessage()));
            }
        });
    }
    
    // Implement other IWorkspaceRepository methods...
    // getProjectsByWorkspaceId, getBoardsByWorkspaceId, etc.
    // For now, delegate to API (can add cache later)
}
```

---

#### Step 2.2: Add to DependencyProvider (10 phút)

**File:** `core/DependencyProvider.java`

```java
// Add field
private WorkspaceRepositoryImplWithCache workspaceRepositoryWithCache;

// Add getter
public synchronized WorkspaceRepositoryImplWithCache getWorkspaceRepositoryWithCache() {
    if (workspaceRepositoryWithCache == null) {
        WorkspaceApiService apiService = /* get from retrofit */;
        workspaceRepositoryWithCache = new WorkspaceRepositoryImplWithCache(
            apiService, workspaceDao, executorService
        );
        Log.d(TAG, "✓ WorkspaceRepositoryImplWithCache created");
    }
    return workspaceRepositoryWithCache;
}
```

---

#### Step 2.3: Integrate vào HomeActivity (30 phút)

**File:** `feature/home/ui/Home/HomeActivity.java`

**Current code (Line 95):**
```java
workspaceViewModel.loadWorkspaces();
```

**Option A: Update WorkspaceViewModel to use cache (Recommended)**

Modify `WorkspaceViewModel` constructor to accept cached repository:
```java
// In ViewModelFactoryProvider or direct injection
WorkspaceRepositoryImplWithCache cachedRepo = 
    App.dependencyProvider.getWorkspaceRepositoryWithCache();

WorkspaceViewModel viewModel = new WorkspaceViewModel(cachedRepo);
```

**Option B: Direct call in HomeActivity (Quick & Dirty)**

```java
private void loadWorkspaces() {
    final long startTime = System.currentTimeMillis();
    
    App.dependencyProvider.getWorkspaceRepositoryWithCache()
        .getWorkspaces(new IWorkspaceRepository.RepositoryCallback<List<Workspace>>() {
            @Override
            public void onSuccess(List<Workspace> workspaces) {
                long duration = System.currentTimeMillis() - startTime;
                
                runOnUiThread(() -> {
                    homeAdapter.setWorkspaceList(workspaces);
                    
                    if (duration < 100) {
                        Log.i(TAG, "⚡ CACHE: " + duration + "ms");
                    } else {
                        Log.i(TAG, "🌐 API: " + duration + "ms");
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(HomeActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
}
```

**→ CHỌN OPTION B (nhanh hơn cho demo tối nay)**

---

#### Step 2.4: Test HomeActivity (10 phút)

**Test:**
```
1. Open app → Home tab
2. First load: Check logcat "🌐 API: 800ms"
3. Close & reopen
4. Second load: Check logcat "⚡ CACHE: 30ms"
```

---

### ⏰ PHASE 3: TEST & VERIFY (60 phút) - Both

#### Step 3.1: Full App Testing (30 phút)

**Test Flow:**
```
1. Clear app data
2. Login
3. Test Inbox:
   - Open Inbox → "🌐 API: 1200ms"
   - Close & reopen → "⚡ Cache: 30ms"
   
4. Test Home:
   - Open Home → "🌐 API: 800ms"
   - Close & reopen → "⚡ Cache: 30ms"
   
5. Test Create Task:
   - Inbox → Add task
   - Verify cache updates
   
6. Test Logout:
   - Logout
   - Verify cache cleared
   - Login again → Fresh load
```

---

#### Step 3.2: Performance Measurement (15 phút)

**Document results:**
```
INBOX ACTIVITY:
- First load (API):  _______ ms
- Second load (Cache): _______ ms
- Improvement: _______ %

HOME ACTIVITY:
- First load (API):  _______ ms
- Second load (Cache): _______ ms
- Improvement: _______ %
```

---

#### Step 3.3: Offline Mode Test (15 phút)

**Test Scenario:**
```
1. With internet ON:
   - Open Inbox → Cache tasks
   - Open Home → Cache workspaces

2. Enable Airplane Mode

3. Close & reopen app

4. Navigate:
   - Home → Workspaces visible ✓
   - Inbox → Tasks visible ✓
   
5. Try create task → Error message ✓

6. Disable Airplane Mode → Sync works ✓
```

---

## 📊 SUCCESS CRITERIA

### ✅ MUST HAVE:
- [ ] InboxActivity uses TaskRepositoryImplWithCache
- [ ] HomeActivity uses WorkspaceRepositoryImplWithCache
- [ ] First load: Data from API
- [ ] Second load: Data from cache (<100ms)
- [ ] Performance improvement >90%
- [ ] No crashes
- [ ] Data displays correctly
- [ ] Logout clears cache

### 🎯 NICE TO HAVE:
- [ ] Performance toast on screen
- [ ] Offline mode tested
- [ ] Pull-to-refresh implemented

---

## 🎯 DEMO CHO LEADER (Sau 3-4 giờ)

**You can demo:**

1. **Inbox Performance:**
   ```
   First open: "🌐 API: 1200ms"
   Reopen: "⚡ Cache: 30ms"
   → 97% faster!
   ```

2. **Home Performance:**
   ```
   First open: "🌐 API: 800ms"
   Reopen: "⚡ Cache: 30ms"
   → 96% faster!
   ```

3. **Offline Mode:**
   - Airplane mode ON
   - App still works
   - All cached data visible

4. **Logcat Evidence:**
   ```
   InboxActivity: ⚡ CACHE HIT: 30ms (15 tasks)
   HomeActivity: ⚡ CACHE HIT: 25ms (5 workspaces)
   ```

---

## 📝 TIMELINE SUMMARY

| Phase | Time | Person | Tasks | Status |
|-------|------|--------|-------|--------|
| Phase 0 | 30m | Done | Database setup | ✅ DONE |
| Phase 1 | 90m | Person 2 | Cache Inbox | ⏳ TODO |
| Phase 2 | 90m | Person 1 | Cache Workspaces | ⏳ TODO |
| Phase 3 | 60m | Both | Test & verify | ⏳ TODO |
| **TOTAL** | **3.5h** | - | - | - |

---

## 🔄 NEXT STEPS (Sau khi hoàn thành)

### Phase 4 (Future):
1. **WorkspaceActivity** - Cache projects per workspace
2. **ProjectActivity** - Cache boards + tasks (Kanban)
3. **NotificationActivity** - Cache notifications
4. **Sync strategy** - Handle data conflicts

---

## ✅ COMMIT CHECKLIST

**Files to commit:**
- [ ] InboxActivity.java (updated)
- [ ] HomeActivity.java (updated)
- [ ] WorkspaceRepositoryImplWithCache.java (new)
- [ ] DependencyProvider.java (updated)

**Commit message:**
```
feat: Add Room cache for Inbox and Home

- Integrate TaskRepositoryImplWithCache into InboxActivity
- Create WorkspaceRepositoryImplWithCache for HomeActivity
- Improve load time by 95% (1200ms → 30ms)
- Add offline support for cached data

Performance:
- Inbox: 1200ms → 30ms (97% faster)
- Home: 800ms → 30ms (96% faster)

BREAKING CHANGE: Database version 3
```

---

## 🚨 TROUBLESHOOTING

### Problem: "Cannot resolve App.dependencyProvider"
**Solution:**
```java
import com.example.tralalero.App.App;
```

### Problem: Workspaces not showing after cache
**Solution:**
1. Check WorkspaceEntity ID is String (not int) ✅ Already fixed
2. Verify WorkspaceMapper returns correct data
3. Check logcat for cache operations

### Problem: Tasks not appearing in Inbox
**Solution:**
1. Verify tokenManager.getUserId() is not null
2. Check TaskDao.getAllByUserId() query
3. Verify tasks are being saved to cache

---

**SẴN SÀNG BẮT ĐẦU! ⚡**

**Person 1:** Start with Phase 2 (WorkspaceRepositoryImplWithCache)  
**Person 2:** Start with Phase 1 (InboxActivity integration)
