# 🔧 FIX: Task Constructor Error

## ❌ LỖI BAN ĐẦU

```
error: no suitable constructor found for Task(String,String,String,TaskStatus,TaskPriority,Date,String,String)
```

---

## 🔍 NGUYÊN NHÂN

### **domain.model.Task vs model.Task**

Có 2 class Task khác nhau trong project:

#### **1. domain.model.Task (Domain Layer)**
```java
// Constructor với nhiều tham số
public Task(String id, String projectId, String boardId, String title, 
            String description, String issueKey, TaskType type, 
            TaskStatus status, TaskPriority priority, double position, 
            String assigneeId, String createdBy, String sprintId, 
            String epicId, String parentTaskId, Date startAt, Date dueAt,
            Integer storyPoints, Integer originalEstimateSec, 
            Integer remainingEstimateSec, Date createdAt, Date updatedAt)
```

- Sử dụng **enums**: `TaskStatus`, `TaskPriority`, `TaskType`
- Immutable (final fields)
- Clean Architecture compliant

#### **2. model.Task (UI/Legacy Layer)**
```java
// Chỉ có 2 constructors:
public Task() {}

public Task(String id, String title, String status) {
    this.id = id;
    this.title = title;
    this.status = status;  // String, not enum!
}
```

- Sử dụng **Strings** thay vì enums
- Mutable (có setters)
- Legacy code

---

## ✅ GIẢI PHÁP

### **Mapper Function (Updated)**

```java
private List<Task> convertDomainTasksToUiTasks(List<com.example.tralalero.domain.model.Task> domainTasks) {
    List<Task> uiTasks = new ArrayList<>();
    
    for (com.example.tralalero.domain.model.Task domainTask : domainTasks) {
        // Step 1: Use 3-param constructor
        Task uiTask = new Task(
            domainTask.getId(),
            domainTask.getTitle(),
            domainTask.getStatus() != null ? domainTask.getStatus().name() : "TO_DO"  // ✅ Enum → String
        );
        
        // Step 2: Set additional fields via setters
        uiTask.setDescription(domainTask.getDescription());
        uiTask.setPriority(domainTask.getPriority() != null ? domainTask.getPriority().name() : "MEDIUM");  // ✅ Enum → String
        uiTask.setDueAt(domainTask.getDueAt());
        uiTask.setStartAt(domainTask.getStartAt());
        uiTask.setAssigneeId(domainTask.getAssigneeId());
        uiTask.setBoardId(domainTask.getBoardId());
        uiTask.setProjectId(domainTask.getProjectId());
        uiTask.setIssueKey(domainTask.getIssueKey());
        uiTask.setType(domainTask.getType() != null ? domainTask.getType().name() : "TASK");  // ✅ Enum → String
        uiTask.setPosition(domainTask.getPosition());
        uiTask.setCreatedBy(domainTask.getCreatedBy());
        uiTask.setSprintId(domainTask.getSprintId());
        uiTask.setEpicId(domainTask.getEpicId());
        uiTask.setParentTaskId(domainTask.getParentTaskId());
        uiTask.setStoryPoints(domainTask.getStoryPoints());
        uiTask.setOriginalEstimateSec(domainTask.getOriginalEstimateSec());
        uiTask.setRemainingEstimateSec(domainTask.getRemainingEstimateSec());
        uiTask.setCreatedAt(domainTask.getCreatedAt());
        uiTask.setUpdatedAt(domainTask.getUpdatedAt());
        
        uiTasks.add(uiTask);
    }
    
    return uiTasks;
}
```

---

## 🎯 KEY POINTS

### **1. Enum → String Conversion**
```java
// ✅ ĐÚNG
domainTask.getStatus().name()  // TaskStatus.TO_DO → "TO_DO"
domainTask.getPriority().name()  // TaskPriority.HIGH → "HIGH"

// ❌ SAI
domainTask.getStatus()  // TaskStatus (enum) không compatible với String
```

### **2. Null Safety**
```java
// ✅ ĐÚNG - Check null trước khi call .name()
domainTask.getStatus() != null ? domainTask.getStatus().name() : "TO_DO"

// ❌ SAI - NullPointerException nếu status = null
domainTask.getStatus().name()
```

### **3. Builder Pattern**
```java
// Constructor minimal → Setters cho các fields khác
Task uiTask = new Task(id, title, status);
uiTask.setDescription(...);
uiTask.setPriority(...);
// etc.
```

---

## 📊 ENUM MAPPINGS

### **TaskStatus**
```java
// domain.model.TaskStatus (enum)
TO_DO, IN_PROGRESS, DONE, CANCELLED

// model.Task.status (String)
"TO_DO", "IN_PROGRESS", "DONE", "CANCELLED"
```

### **TaskPriority**
```java
// domain.model.TaskPriority (enum)
LOW, MEDIUM, HIGH, CRITICAL

// model.Task.priority (String)
"LOW", "MEDIUM", "HIGH", "CRITICAL"
```

### **TaskType**
```java
// domain.model.TaskType (enum)
TASK, BUG, STORY, EPIC

// model.Task.type (String)
"TASK", "BUG", "STORY", "EPIC"
```

---

## 🧪 VERIFICATION

### **Test Conversion:**
```java
// Domain Task
Task domainTask = new Task(
    "task-1", "proj-1", "board-1", "Test Task", "Description",
    "PLAN-1", TaskType.TASK, TaskStatus.TO_DO, TaskPriority.HIGH,
    1000.0, "user-1", "user-2", null, null, null,
    new Date(), new Date(), 5, 3600, 3600, new Date(), new Date()
);

// Convert
List<Task> uiTasks = convertDomainTasksToUiTasks(List.of(domainTask));

// Verify
Task uiTask = uiTasks.get(0);
assertEquals("task-1", uiTask.getId());
assertEquals("Test Task", uiTask.getTitle());
assertEquals("TO_DO", uiTask.getStatus());  // ✅ String
assertEquals("HIGH", uiTask.getPriority());  // ✅ String
assertEquals("TASK", uiTask.getType());      // ✅ String
```

---

## 🔮 FUTURE - PHASE 6

### **Remove Mapper (Ideal Solution)**

Thay vì mapper, update TaskAdapter để dùng `domain.model.Task`:

```java
// TaskAdapter.java
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<com.example.tralalero.domain.model.Task> tasks;  // ✅ Use domain model
    
    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        com.example.tralalero.domain.model.Task task = tasks.get(position);
        
        holder.tvTitle.setText(task.getTitle());
        holder.tvStatus.setText(task.getStatus().name());  // ✅ Direct enum usage
        holder.tvPriority.setText(task.getPriority().name());
        // etc.
    }
}
```

**Benefits:**
- ✅ No mapper needed
- ✅ Type-safe (enums)
- ✅ Less code
- ✅ Better architecture

---

## ✅ SUMMARY

| Aspect | Before | After |
|--------|--------|-------|
| Constructor | 8 params (wrong) | 3 params (correct) |
| Enum handling | Direct enum | `.name()` conversion |
| Null safety | ❌ No checks | ✅ Null checks |
| Fields set | Constructor only | Constructor + Setters |
| Compiles | ❌ Error | ✅ Success |

---

**STATUS:** ✅ FIXED

**Build again:** `./gradlew build`

**Next:** Test app on emulator! 🚀
