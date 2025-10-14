# 🚀 QUICK START - LISTPROJECT SỬ DỤNG

## ⚡ TÓM TẮT NHANH

**File đã sửa:**
1. ✅ `activity_list_frm.xml` - Thêm ProgressBar + EmptyView
2. ✅ `ListProject.java` - Tích hợp TaskViewModel

**Thời gian:** ~40 phút

---

## 📝 ĐÃ THÊM MỚI

### **1. UI Components**
```java
private ProgressBar progressBar;  // Hiển thị khi loading
private TextView emptyView;       // Hiển thị khi không có tasks
```

### **2. ViewModels**
```java
private TaskViewModel taskViewModel;           // Phase 5 - NEW
private ListProjectViewModel legacyViewModel;  // Backward compatibility
```

### **3. Methods**
```java
initViews(View view)                    // Khởi tạo UI components
setupViewModels()                       // Khởi tạo ViewModels
setupTaskViewModel()                    // Tạo TaskViewModel với 15 UseCases
setupRecyclerView()                     // Setup RecyclerView + Adapter
observeViewModel()                      // Observe LiveData
convertDomainTasksToUiTasks()          // Convert domain → UI model
loadTasks()                            // Load tasks từ API
setBoardId(String boardId)             // Set boardId và reload
refreshTasks()                         // Refresh tasks
```

---

## 🎯 CÁCH SỬ DỤNG

### **Option 1: Với BoardId (RECOMMENDED)**
```java
// Tạo fragment với boardId
ListProject fragment = ListProject.newInstance("TO_DO", projectId, boardId);

// Fragment sẽ tự động:
// 1. Setup TaskViewModel
// 2. Load tasks từ boardId
// 3. Hiển thị loading → tasks
```

### **Option 2: Legacy Mode (Backward Compatibility)**
```java
// Tạo fragment chỉ với type và projectId
ListProject fragment = ListProject.newInstance("TO_DO", projectId);

// Fragment sẽ:
// 1. Dùng legacyViewModel
// 2. Load tasks bằng projectId + status
// 3. Hiển thị warning message
```

---

## 🔄 UI STATE TRANSITIONS

```
┌─────────────────┐
│  INITIAL STATE  │
│  (All hidden)   │
└────────┬────────┘
         │
         ▼
    loadTasks()
         │
         ▼
┌─────────────────┐
│   LOADING       │
│  ProgressBar ✓  │
│  RecyclerView ✗ │
│  EmptyView ✗    │
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
┌────────┐ ┌──────────┐
│SUCCESS │ │  ERROR   │
└───┬────┘ └────┬─────┘
    │           │
    ▼           ▼
┌─────────┐ ┌──────────┐
│HAS DATA?│ │EmptyView │
└──┬───┬──┘ │+ Error   │
   │   │    └──────────┘
  YES  NO
   │   │
   ▼   ▼
┌────┐ ┌────┐
│RV ✓│ │EV ✓│
└────┘ └────┘
```

---

## 🧪 TEST SCENARIOS

### **Scenario 1: Normal Load**
```
Input: boardId = "board-123"
Expected:
  1. ProgressBar shows
  2. API called with boardId
  3. Tasks loaded
  4. RecyclerView shows tasks
  5. ProgressBar hides
```

### **Scenario 2: Empty Board**
```
Input: boardId = "empty-board"
Expected:
  1. ProgressBar shows
  2. API returns empty array
  3. EmptyView shows "No tasks in TO_DO"
  4. ProgressBar hides
```

### **Scenario 3: Error**
```
Input: boardId = "invalid-id"
Expected:
  1. ProgressBar shows
  2. API returns error
  3. Toast shows error message
  4. EmptyView shows "Failed to load tasks\n[error]"
  5. ProgressBar hides
```

### **Scenario 4: No BoardId**
```
Input: boardId = null
Expected:
  1. EmptyView shows "No board selected"
  2. No API call
```

---

## 🐛 COMMON ERRORS & FIXES

### **Error 1: NullPointerException in observeViewModel**
```
Cause: ViewModel không được khởi tạo
Fix: Check setupViewModels() được gọi trong onCreateView()
```

### **Error 2: Tasks không hiển thị**
```
Cause: 
  - Mapper không đúng
  - ObserveDomain tasks nhưng adapter nhận UI tasks
  
Fix: Check convertDomainTasksToUiTasks() được gọi
```

### **Error 3: ProgressBar không ẩn**
```
Cause: isLoading LiveData không emit false
Fix: Check TaskViewModel.loadTasksByBoard() có set loading = false
```

### **Error 4: Multiple ViewModel instances**
```
Cause: Dùng ViewModelProvider(this) thay vì requireActivity()
Fix: 
  // ✅ ĐÚNG
  new ViewModelProvider(requireActivity(), factory).get(TaskViewModel.class);
  
  // ❌ SAI
  new ViewModelProvider(this, factory).get(TaskViewModel.class);
```

### **Error 5: Memory leak khi rotate**
```
Cause: Observe với 'this' thay vì 'getViewLifecycleOwner()'
Fix:
  // ✅ ĐÚNG
  taskViewModel.getTasks().observe(getViewLifecycleOwner(), ...);
  
  // ❌ SAI
  taskViewModel.getTasks().observe(this, ...);
```

---

## 🎨 CUSTOMIZATION

### **Change Empty Message**
```java
private String getEmptyMessage() {
    switch (type) {
        case "TO_DO":
            return "No tasks to do yet! 🎯";
        case "IN_PROGRESS":
            return "Nothing in progress 🚀";
        case "DONE":
            return "No completed tasks ✅";
        default:
            return "No tasks";
    }
}
```

### **Add Loading Animation**
```xml
<!-- In activity_list_frm.xml -->
<ProgressBar
    android:id="@+id/progressBar"
    style="@style/Widget.AppCompat.ProgressBar.Horizontal"
    android:indeterminate="true"
    ... />
```

### **Custom Error Handling**
```java
taskViewModel.getError().observe(getViewLifecycleOwner(), error -> {
    if (error != null) {
        if (error.contains("Network")) {
            showNetworkError();
        } else if (error.contains("Unauthorized")) {
            showAuthError();
        } else {
            showGenericError(error);
        }
    }
});
```

---

## 📊 PERFORMANCE TIPS

### **1. Use DiffUtil in Adapter**
```java
// Instead of notifyDataSetChanged()
public void setTasks(List<Task> newTasks) {
    DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
        new TaskDiffCallback(this.tasks, newTasks)
    );
    this.tasks = newTasks;
    diffResult.dispatchUpdatesTo(this);
}
```

### **2. ViewHolder Pattern**
Already implemented in TaskAdapter ✓

### **3. Image Loading**
```java
// Use Glide/Picasso for task images
Glide.with(context)
    .load(task.getImageUrl())
    .placeholder(R.drawable.placeholder)
    .into(imageView);
```

---

## 🔗 RELATED FILES

```
ListProject.java                        ← Main file
  ├── TaskViewModel.java                ← ViewModel
  ├── TaskViewModelFactory.java         ← Factory
  ├── TaskRepositoryImpl.java           ← Repository
  ├── TaskApiService.java               ← API
  ├── TaskAdapter.java                  ← Adapter
  ├── activity_list_frm.xml             ← Layout
  └── item_task.xml                     ← Task item layout
```

---

## 📚 NEXT STEPS

1. **Test functionality** ✓
2. **Add TaskDetailBottomSheet** (Phase 6)
3. **Remove mapper** (when TaskAdapter uses domain model)
4. **Add pull-to-refresh**
5. **Add swipe-to-delete**

---

## 💡 TIPS & TRICKS

### **Debug Logging**
```java
// Enable verbose logging
private static final boolean DEBUG = true;

private void log(String message) {
    if (DEBUG) {
        Log.d(TAG, message);
    }
}
```

### **Check ViewModel State**
```java
// In onResume()
@Override
public void onResume() {
    super.onResume();
    Log.d(TAG, "Tasks count: " + (taskViewModel.getTasks().getValue() != null 
        ? taskViewModel.getTasks().getValue().size() 
        : 0));
}
```

### **Force Refresh**
```java
// Add in menu or FloatingActionButton
@Override
public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.action_refresh) {
        refreshTasks();
        return true;
    }
    return super.onOptionsItemSelected(item);
}
```

---

## ✅ VERIFICATION CHECKLIST

Before marking as complete:

- [ ] File compiles without errors
- [ ] ProgressBar shows when loading
- [ ] Tasks display correctly
- [ ] EmptyView shows when no tasks
- [ ] Error messages display correctly
- [ ] No memory leaks on rotation
- [ ] Logs are helpful for debugging
- [ ] Code is commented appropriately

---

**STATUS: ✅ READY TO USE**

**Happy Coding! 🚀**
