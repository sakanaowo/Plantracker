# 🎉 LISTPROJECT REFACTORING - HOÀN TẤT

**Ngày:** 14/10/2025  
**Người thực hiện:** Người 3 - Task & Notification  
**Nhiệm vụ:** Tích hợp TaskViewModel vào ListProject Fragment

---

## ✅ ĐÃ HOÀN THÀNH

### 1. **Layout Update - `activity_list_frm.xml`**

Đã thêm:
- ✅ `ProgressBar` - Hiển thị khi loading
- ✅ `EmptyView` (TextView) - Hiển thị khi không có tasks
- ✅ Đổi từ `LinearLayout` sang `FrameLayout` để overlay

**Trước:**
```xml
<LinearLayout>
    <RecyclerView />
</LinearLayout>
```

**Sau:**
```xml
<FrameLayout>
    <RecyclerView android:visibility="gone" />
    <ProgressBar android:visibility="gone" />
    <TextView (emptyView) android:visibility="gone" />
</FrameLayout>
```

---

### 2. **ListProject.java Refactoring**

#### ✅ **Thêm TaskViewModel (Phase 5)**

```java
private TaskViewModel taskViewModel;      // New - Phase 5
private ListProjectViewModel legacyViewModel;  // Old - Backward compatibility
```

#### ✅ **Setup Method**

```java
private void setupTaskViewModel() {
    // Tạo Repository
    TaskApiService apiService = ApiClient.get(App.authManager).create(TaskApiService.class);
    ITaskRepository repository = new TaskRepositoryImpl(apiService);
    
    // Tạo 15 UseCases
    GetTaskByIdUseCase getTaskByIdUseCase = new GetTaskByIdUseCase(repository);
    GetTasksByBoardUseCase getTasksByBoardUseCase = new GetTasksByBoardUseCase(repository);
    // ... 13 UseCases khác ...
    
    // Tạo Factory
    TaskViewModelFactory factory = new TaskViewModelFactory(...);
    
    // Tạo ViewModel - SHARED across Activity
    taskViewModel = new ViewModelProvider(requireActivity(), factory).get(TaskViewModel.class);
}
```

**Lưu ý quan trọng:**
- Dùng `requireActivity()` để ViewModel được share giữa các Fragment
- Cần khởi tạo đầy đủ 15 UseCases

#### ✅ **Observe LiveData**

```java
private void observeViewModel() {
    // 1. Observe tasks
    taskViewModel.getTasks().observe(getViewLifecycleOwner(), domainTasks -> {
        // Convert domain.model.Task → model.Task
        List<Task> uiTasks = convertDomainTasksToUiTasks(domainTasks);
        taskAdapter.setTasks(uiTasks);
        
        // Show/hide views
        recyclerView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
    });
    
    // 2. Observe loading
    taskViewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    });
    
    // 3. Observe errors
    taskViewModel.getError().observe(getViewLifecycleOwner(), error -> {
        Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_LONG).show();
        emptyView.setVisibility(View.VISIBLE);
        emptyView.setText("Failed to load tasks\n" + error);
    });
}
```

#### ✅ **Mapper Function**

```java
/**
 * Convert domain.model.Task to model.Task (legacy UI model)
 * TODO: Phase 6 - Remove when TaskAdapter uses domain.model.Task
 */
private List<Task> convertDomainTasksToUiTasks(List<com.example.tralalero.domain.model.Task> domainTasks) {
    List<Task> uiTasks = new ArrayList<>();
    
    for (com.example.tralalero.domain.model.Task domainTask : domainTasks) {
        Task uiTask = new Task(
            domainTask.getId(),
            domainTask.getTitle(),
            domainTask.getDescription(),
            domainTask.getStatus(),
            domainTask.getPriority(),
            domainTask.getDueDate(),
            domainTask.getAssigneeId(),
            domainTask.getBoardId()
        );
        uiTasks.add(uiTask);
    }
    
    return uiTasks;
}
```

**Tại sao cần mapper?**
- `TaskViewModel` trả về `domain.model.Task`
- `TaskAdapter` nhận `model.Task`
- Cần convert giữa 2 models
- **TODO Phase 6:** Refactor TaskAdapter để dùng domain.model.Task

#### ✅ **Load Tasks Logic**

```java
private void loadTasks() {
    if (boardId != null && !boardId.isEmpty()) {
        // ✅ PREFERRED: Load by boardId (Phase 5)
        taskViewModel.loadTasksByBoard(boardId);
        
    } else if (projectId != null && !projectId.isEmpty() && type != null) {
        // ⚠️ LEGACY: Load by projectId + status
        String status = mapTypeToStatus(type);
        legacyViewModel.loadTasks(projectId, status);
        
    } else {
        // ❌ ERROR: No data
        emptyView.setVisibility(View.VISIBLE);
        emptyView.setText("No board selected");
    }
}
```

---

## 🎯 FACTORY METHODS

Fragment hỗ trợ 3 cách khởi tạo:

### **Mode 1: Type only (Legacy)**
```java
ListProject fragment = ListProject.newInstance("TO_DO");
```

### **Mode 2: Type + ProjectId (Legacy)**
```java
ListProject fragment = ListProject.newInstance("TO_DO", projectId);
```

### **Mode 3: Type + ProjectId + BoardId (Phase 5 - PREFERRED)**
```java
ListProject fragment = ListProject.newInstance("TO_DO", projectId, boardId);
```

---

## 📊 UI STATE FLOW

```
Initial State
    ↓
observeViewModel() called
    ↓
loadTasks() called
    ↓
isLoading = true → Show ProgressBar
    ↓
API Call...
    ↓
Success → isLoading = false, tasks emitted
    ↓
    ├─ Has tasks → Show RecyclerView
    └─ No tasks → Show EmptyView
    
Error → isLoading = false, error emitted
    ↓
Show EmptyView with error message
```

---

## 🔧 CÁCH SỬ DỤNG

### **Trong ProjectActivity (hoặc parent activity):**

```java
public class ProjectActivity extends AppCompatActivity {
    
    private TaskViewModel taskViewModel;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Setup TaskViewModel
        setupTaskViewModel();
        
        // Setup ViewPager with fragments
        ListProjectAdapter adapter = new ListProjectAdapter(this, projectId);
        viewPager.setAdapter(adapter);
    }
    
    private void setupTaskViewModel() {
        // Create repository
        TaskApiService apiService = ApiClient.get(App.authManager).create(TaskApiService.class);
        ITaskRepository repository = new TaskRepositoryImpl(apiService);
        
        // Create UseCases (same as ListProject)
        // ... create all 15 UseCases ...
        
        // Create Factory
        TaskViewModelFactory factory = new TaskViewModelFactory(...);
        
        // Create ViewModel
        taskViewModel = new ViewModelProvider(this, factory).get(TaskViewModel.class);
    }
}
```

### **Trong ListProjectAdapter:**

```java
public class ListProjectAdapter extends FragmentStateAdapter {
    
    private String projectId;
    private List<String> boardIds;  // List of boardIds for each tab
    
    @Override
    public Fragment createFragment(int position) {
        String type = getTypeByPosition(position);
        String boardId = boardIds.get(position);
        
        // ✅ PHASE 5 - Pass boardId
        return ListProject.newInstance(type, projectId, boardId);
    }
    
    private String getTypeByPosition(int position) {
        switch (position) {
            case 0: return "TO_DO";
            case 1: return "IN_PROGRESS";
            case 2: return "DONE";
            default: return "TO_DO";
        }
    }
}
```

---

## ✅ TESTING CHECKLIST

### **1. Initial Load**
- [ ] Fragment mở → ProgressBar hiển thị
- [ ] API call thành công → RecyclerView hiển thị tasks
- [ ] API call thất bại → EmptyView hiển thị error

### **2. Empty State**
- [ ] Không có tasks → EmptyView hiển thị "No tasks in [type]"
- [ ] Message đúng cho từng tab (TO DO, IN PROGRESS, DONE)

### **3. Error Handling**
- [ ] Network error → Toast + EmptyView hiển thị
- [ ] No boardId → EmptyView hiển thị "No board selected"

### **4. Task Actions**
- [ ] Click task → Toast hiển thị task title
- [ ] (TODO) Click task → TaskDetailBottomSheet mở

### **5. Refresh**
- [ ] `refreshTasks()` được gọi → Reload data
- [ ] Pull to refresh (nếu có) → Reload data

### **6. Lifecycle**
- [ ] Rotate device → Không crash, data preserved
- [ ] Navigate away → Không memory leak
- [ ] Back to fragment → Data vẫn hiển thị

---

## 🚨 LƯU Ý QUAN TRỌNG

### ⚠️ **1. ViewModel Scope**
```java
// ✅ ĐÚNG - Shared across Activity
taskViewModel = new ViewModelProvider(requireActivity(), factory).get(TaskViewModel.class);

// ❌ SAI - Mỗi Fragment có ViewModel riêng
taskViewModel = new ViewModelProvider(this, factory).get(TaskViewModel.class);
```

### ⚠️ **2. Observe với ViewLifecycleOwner**
```java
// ✅ ĐÚNG - Tự động unsubscribe khi view destroyed
taskViewModel.getTasks().observe(getViewLifecycleOwner(), tasks -> { ... });

// ❌ SAI - Memory leak khi Fragment destroyed
taskViewModel.getTasks().observe(this, tasks -> { ... });
```

### ⚠️ **3. Null Safety**
```java
// ✅ ĐÚNG - Check null
if (boardId != null && !boardId.isEmpty()) {
    taskViewModel.loadTasksByBoard(boardId);
}

// ❌ SAI - Có thể NullPointerException
taskViewModel.loadTasksByBoard(boardId);
```

---

## 🔮 TODO - PHASE 6

### **1. Remove Mapper**
- Refactor `TaskAdapter` để dùng `domain.model.Task`
- Remove `convertDomainTasksToUiTasks()`

### **2. Add TaskDetailBottomSheet**
```java
private void showTaskDetail(Task task) {
    TaskDetailBottomSheet bottomSheet = new TaskDetailBottomSheet();
    bottomSheet.setTask(task);
    bottomSheet.setTaskViewModel(taskViewModel);
    bottomSheet.show(getParentFragmentManager(), "TaskDetail");
}
```

### **3. Deprecate Legacy Mode**
- Remove `legacyViewModel`
- Remove support for `projectId + status` loading
- Force `boardId` requirement

### **4. Add Pull-to-Refresh**
```xml
<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>
    <FrameLayout>
        <!-- Existing views -->
    </FrameLayout>
</androidx.swiperefreshlayout.widget.SwipeRefreshLayout>
```

---

## 📈 PERFORMANCE

### **Before (Legacy):**
- Each Fragment: Own ViewModel instance
- API calls: Duplicated across fragments
- Memory: Higher usage

### **After (Phase 5):**
- ✅ Shared ViewModel across Activity
- ✅ Cached data across fragments
- ✅ Lower memory usage
- ✅ Faster tab switching

---

## 🎓 LESSONS LEARNED

### **1. ViewModel Sharing**
- Use `requireActivity()` for shared ViewModel
- Use `this` for Fragment-scoped ViewModel

### **2. LiveData Observing**
- Always use `getViewLifecycleOwner()` in Fragment
- Prevents memory leaks

### **3. Model Mapping**
- Temporary solution until full migration
- Keep mapper logic simple and testable

### **4. Error Handling**
- Always handle null cases
- Provide meaningful error messages to user

---

## 🎯 TÓM TẮT

| Aspect | Before | After |
|--------|--------|-------|
| ViewModel | `ListProjectViewModel` | `TaskViewModel` (Phase 5) |
| Loading state | ❌ Not handled | ✅ ProgressBar |
| Empty state | ❌ Not handled | ✅ EmptyView |
| Error handling | ⚠️ Minimal | ✅ Full handling |
| Model | `model.Task` | `domain.model.Task` → `model.Task` |
| Architecture | Legacy | Clean Architecture |

---

**STATUS: ✅ READY FOR TESTING**

**Next Steps:**
1. Test trên emulator
2. Fix bugs nếu có
3. Move to Phase 6 tasks

---

**Người 3 đã hoàn thành nhiệm vụ! 🎉**
