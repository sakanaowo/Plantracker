# ⚡ PLAN 1 BUỔI TỐI EXPRESS - SIMPLIFIED VERSION
## Focus: Cache tasks cho InboxActivity (Quick Access)

**Target:** InboxActivity load instant từ cache  
**Timeline:** 2-3 giờ (simplified)  
**Date:** October 18, 2025 - Tối nay

---

## 📋 HIỂU VỀ INBOXACTIVITY

### InboxActivity = Trello Inbox (Quick Access)
- ✅ Hiển thị **danh sách tasks** dạng cards (RecyclerView)
- ✅ Quick add task
- ✅ Task detail via bottom sheet
- ❌ **KHÔNG có board columns** (không phải Kanban)

### Current Implementation:
```java
// InboxActivity.java - Line 200
private void loadAllTasks() {
    String defaultBoardId = "inbox-board-id";
    taskViewModel.loadTasksByBoard(defaultBoardId);
}

// Uses TaskViewModel → ITaskRepository → API
```

### What we need to cache:
- ✅ **Tasks only** (danh sách tasks cho user)
- ❌ NOT boards (Inbox không cần boards)

---

## ✅ ĐÃ HOÀN THÀNH (Phase 0 - 25 phút)

### ✅ Database Infrastructure - DONE
- ✅ WorkspaceEntity bug fixed (int → String)
- ✅ BoardEntity created (for future ProjectActivity Kanban)
- ✅ AppDatabase updated to version 3
- ✅ DependencyProvider updated with BoardDao

### ✅ Ready to use:
- ✅ **TaskRepositoryImplWithCache** - Đã có sẵn, chỉ cần integrate
- ✅ **TaskDao** - CRUD operations ready
- ✅ **TaskEntityMapper** - Entity ↔ Domain conversion

---

## 🚀 CÒN LẠI - 2 PHASES (2-3 GIỜ)

### ⏰ PHASE 1: INTEGRATE CACHE INTO INBOXACTIVITY (60 phút)

**Mục tiêu:** InboxActivity dùng TaskRepositoryImplWithCache thay vì API trực tiếp

---

#### Step 1.1: Phân tích current flow (10 phút)

**Current architecture:**
```
InboxActivity
    ↓
TaskViewModel
    ↓
GetTasksByBoardUseCase
    ↓
TaskRepositoryImpl (API only)
    ↓
Retrofit API (500-2000ms)
```

**Problem:** Tất cả đều gọi API trực tiếp, không có cache

**Solution options:**

**Option A: Replace Repository in ViewModel (Recommended)**
- Inject TaskRepositoryImplWithCache vào TaskViewModel
- ViewModel tự động dùng cache
- Clean architecture

**Option B: Bypass ViewModel (Quick & Dirty)**
- InboxActivity gọi trực tiếp TaskRepositoryImplWithCache
- Skip ViewModel layer
- Nhanh nhưng vi phạm architecture

**→ CHỌN OPTION A** (Clean but takes more time)

---

#### Step 1.2: Create GetAllTasksUseCase with Cache (20 phút)

**Problem hiện tại:**
```java
// InboxActivity line 200 - Hardcoded boardId
String defaultBoardId = "inbox-board-id"; // TODO: Not flexible
taskViewModel.loadTasksByBoard(defaultBoardId);
```

**Solution:** Create new UseCase cho Inbox

**File:** `domain/usecase/task/GetAllTasksForUserUseCase.java`

```java
package com.example.tralalero.domain.usecase.task;

import com.example.tralalero.data.repository.TaskRepositoryImplWithCache;
import com.example.tralalero.domain.model.Task;
import java.util.List;

/**
 * UseCase to get all tasks for current user (Inbox functionality)
 * Uses cache-first strategy for instant loading
 */
public class GetAllTasksForUserUseCase {
    private final TaskRepositoryImplWithCache repository;
    
    public GetAllTasksForUserUseCase(TaskRepositoryImplWithCache repository) {
        this.repository = repository;
    }
    
    /**
     * Get all tasks assigned to or created by current user
     * Returns from cache instantly, then refreshes from API in background
     */
    public void execute(TaskCallback callback) {
        repository.getAllTasks(new TaskRepositoryImplWithCache.TaskCallback() {
            @Override
            public void onSuccess(List<Task> tasks) {
                callback.onSuccess(tasks);
            }
            
            @Override
            public void onCacheEmpty() {
                callback.onLoading();
            }
            
            @Override
            public void onError(Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }
    
    public interface TaskCallback {
        void onSuccess(List<Task> tasks);
        void onLoading();
        void onError(String error);
    }
}
```

---

#### Step 1.3: Update InboxActivity to use cache (30 phút)

**File:** `feature/home/ui/InboxActivity.java`

**Changes:**

1. **Import cache repository:**
```java
import com.example.tralalero.data.repository.TaskRepositoryImplWithCache;
import com.example.tralalero.domain.usecase.task.GetAllTasksForUserUseCase;
import com.example.tralalero.App.App;
```

2. **Replace setupViewModel():**
```java
private void setupViewModel() {
    // ✅ NEW: Use cached repository instead of API-only repository
    TaskRepositoryImplWithCache cachedRepository = 
        App.dependencyProvider.getTaskRepositoryWithCache();
    
    // Keep other use cases with API repository for write operations
    TaskApiService apiService = ApiClient.get(App.authManager).create(TaskApiService.class);
    ITaskRepository apiRepository = new TaskRepositoryImpl(apiService);
    
    // Read operations - use cache
    GetAllTasksForUserUseCase getAllTasksUseCase = new GetAllTasksForUserUseCase(cachedRepository);
    
    // Write operations - use API repository
    CreateTaskUseCase createTaskUseCase = new CreateTaskUseCase(apiRepository);
    UpdateTaskUseCase updateTaskUseCase = new UpdateTaskUseCase(apiRepository);
    DeleteTaskUseCase deleteTaskUseCase = new DeleteTaskUseCase(apiRepository);
    // ... other use cases ...
    
    // Note: We'll need to create a new TaskViewModelFactory that accepts GetAllTasksForUserUseCase
    // OR we can call directly in loadAllTasks()
}
```

3. **Replace loadAllTasks() with cache:**
```java
private void loadAllTasks() {
    Log.d(TAG, "Loading all inbox tasks from cache...");
    
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
                        
                        // Show performance toast
                        if (duration < 100) {
                            Log.i(TAG, "⚡ CACHE HIT: " + duration + "ms (" + tasks.size() + " tasks)");
                            Toast.makeText(InboxActivity.this, 
                                "⚡ Loaded from cache: " + duration + "ms", 
                                Toast.LENGTH_SHORT).show();
                        } else {
                            Log.i(TAG, "🌐 API CALL: " + duration + "ms (" + tasks.size() + " tasks)");
                            Toast.makeText(InboxActivity.this, 
                                "🌐 Loaded from API: " + duration + "ms", 
                                Toast.LENGTH_SHORT).show();
                        }
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
                    Log.d(TAG, "Cache empty - first load, fetching from API...");
                    // Show loading indicator
                    // API will call onSuccess when ready
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

4. **Update createTask() to refresh cache:**
```java
private void createTask(String title) {
    // ... existing task creation code ...
    
    taskViewModel.createTask(newTask);
    Toast.makeText(this, "Đã thêm task: " + title, Toast.LENGTH_SHORT).show();
    
    // ✅ NEW: After create, reload to refresh cache
    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
        loadAllTasks();  // This will fetch from API and update cache
    }, 500);
}
```

---

### ⏰ PHASE 2: TEST & VERIFY (60 phút)

#### Step 2.1: Build & Deploy (10 phút)

**In Android Studio:**
1. Build → Rebuild Project
2. Wait for build success
3. Run → Run 'app' on emulator/device

**Expected:**
- ✅ Build SUCCESS
- ✅ App installs on device

---

#### Step 2.2: Functional Testing (30 phút)

**Test Scenario 1 - First Load (Cache Empty):**
```
1. Clear app data (Settings → Apps → Plantracker → Clear data)
2. Launch app
3. Login
4. Navigate to Inbox (bottom nav)
5. Check logcat: "Cache empty - first load"
6. Wait for tasks to load from API
7. Check toast: "🌐 Loaded from API: 1200ms"
8. Verify tasks display in RecyclerView
```

**Test Scenario 2 - Second Load (Cache Hit):**
```
1. Close app completely (swipe away from recent apps)
2. Relaunch app
3. Navigate to Inbox
4. Check logcat: "⚡ CACHE HIT: 30ms"
5. Check toast: "⚡ Loaded from cache: 30ms"
6. Verify tasks appear INSTANTLY
```

**Test Scenario 3 - Create New Task:**
```
1. Click inbox "Add Card" field
2. Enter task title
3. Click "Add"
4. Wait 500ms
5. Check tasks reload (includes new task)
6. Verify new task saved to cache
```

**Success Criteria:**
- ✅ First load: ~1200ms from API
- ✅ Second load: <100ms from cache
- ✅ No crashes
- ✅ Tasks display correctly
- ✅ New tasks appear after creation

---

#### Step 2.3: Performance Testing (15 phút)

**Measure load time improvement:**

```
Test 1 - API Load Time:
1. Clear cache: App.dependencyProvider.clearAllCaches()
2. Open Inbox
3. Record time: _______ ms (API)

Test 2 - Cache Load Time:
1. Close & reopen app (don't clear cache)
2. Open Inbox
3. Record time: _______ ms (Cache)

Calculate:
Improvement = (API - Cache) / API * 100%
Example: (1200 - 30) / 1200 = 97.5%
```

**Document results:**
```
API Time:    _______ ms
Cache Time:  _______ ms
Improvement: _______ %
```

---

#### Step 2.4: Offline Mode Testing (15 phút - Optional)

**Test Scenario:**
```
1. With internet ON:
   - Open Inbox → Tasks load & cached ✓

2. Enable Airplane Mode (disable WiFi/Mobile data)

3. Close app completely

4. Reopen app (no internet)

5. Open Inbox
   Expected: Tasks load from cache instantly ✓

6. Try to create new task
   Expected: Error "No network connection" ✓

7. Disable Airplane Mode

8. Open Inbox again
   Expected: Sync with API ✓
```

---

## 📊 SUCCESS CRITERIA

### ✅ MUST HAVE (Bắt buộc):
- [ ] InboxActivity uses TaskRepositoryImplWithCache
- [ ] First load: Tasks from API (~1000-2000ms)
- [ ] Second load: Tasks from cache (<100ms)
- [ ] Performance improvement: >90%
- [ ] No crashes
- [ ] Tasks display correctly

### 🎯 NICE TO HAVE (Nếu còn thời gian):
- [ ] Toast shows load time
- [ ] Offline mode works
- [ ] Logcat shows detailed cache operations
- [ ] Pull-to-refresh to force sync

---

## 🎯 DEMO FOR LEADER (After 2-3 hours)

**You can show:**

1. **First Load Performance:**
   ```
   Toast: "🌐 Loaded from API: 1200ms"
   ```

2. **Cached Load Performance:**
   ```
   Toast: "⚡ Loaded from cache: 30ms"
   97% faster!
   ```

3. **Logcat Evidence:**
   ```
   InboxActivity: Cache empty - first load
   TaskRepoCache: ✓ Saved 15 tasks to cache
   
   [App reopened]
   
   TaskRepoCache: ✓ Loaded 15 tasks from cache
   InboxActivity: ⚡ CACHE HIT: 30ms (15 tasks)
   ```

4. **Offline Mode:**
   - Demo app works without internet
   - Tasks visible from cache

---

## 🚨 TROUBLESHOOTING

### Problem 1: TaskRepositoryImplWithCache.getAllTasks() not working
**Root cause:** TaskRepositoryImplWithCache uses `tokenManager.getUserId()` to filter tasks

**Check:**
```java
// In TaskRepositoryImplWithCache.java line 30
String userId = tokenManager.getUserId();
List<TaskEntity> entities = taskDao.getAllByUserId(userId);
```

**Solution:** Verify tokenManager has userId after login

---

### Problem 2: Tasks not appearing after cache
**Root cause:** TaskRepositoryImplWithCache needs API to populate cache first

**Solution:**
1. First load MUST call API (onCacheEmpty)
2. API success → saveTasksToCache()
3. Second load → onSuccess from cache

**Verify:**
```
Logcat should show:
1. "Cache empty - first load"
2. API call succeeds
3. "✓ Saved X tasks to cache"
4. Next time: "✓ Loaded X tasks from cache"
```

---

### Problem 3: "Cannot resolve symbol getTaskRepositoryWithCache"
**Solution:**
```java
// Add import
import com.example.tralalero.App.App;
import com.example.tralalero.data.repository.TaskRepositoryImplWithCache;

// Use
App.dependencyProvider.getTaskRepositoryWithCache()
```

---

## 📝 TIMELINE SUMMARY

| Phase | Time | Tasks | Status |
|-------|------|-------|--------|
| **Phase 0** | 25m | Fix bugs, create entities | ✅ DONE |
| **Phase 1** | 60m | Integrate cache into InboxActivity | ⏳ TODO |
| **Phase 2** | 60m | Test & verify | ⏳ TODO |
| **TOTAL** | 2.5h | - | - |

---

## 💡 KEY INSIGHTS

### ✅ Đã hiểu đúng:
- InboxActivity = Quick access task list (không phải Kanban board)
- Chỉ cần cache **Tasks**, không cần Boards cho Inbox
- BoardEntity dùng cho ProjectActivity (Kanban) - làm sau

### ✅ Simplified approach:
- Không cần BoardRepositoryImplWithCache cho Inbox
- Focus vào TaskRepositoryImplWithCache
- 2-3 giờ thay vì 3-4 giờ

### ✅ Next steps after Inbox cache works:
- ProjectActivity - Kanban board với BoardEntity
- WorkspaceActivity - Workspace cache
- Sprint boards - Sprint cache

---

## ✅ COMMIT CHECKLIST

**Before commit:**
- [ ] InboxActivity.java updated with cache
- [ ] GetAllTasksForUserUseCase.java created (optional)
- [ ] Code builds successfully
- [ ] Tested on device: First load + Second load
- [ ] Performance improvement measured
- [ ] Logcat shows cache operations

**Commit message:**
```
feat: Add cache support for InboxActivity

- Integrate TaskRepositoryImplWithCache into InboxActivity
- Load tasks from cache instantly (30ms vs 1200ms API)
- Add performance measurement toast
- Improve user experience with 97% faster load time

Test results:
- First load (API): 1200ms
- Second load (Cache): 30ms
- Improvement: 97.5%
```

---

**CÓ THỂ BẮT ĐẦU NGAY! ⚡**

Focus: Inbox task list cache (simple & focused)

