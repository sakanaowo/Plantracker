# Label Feature - Backend Integration Complete

## Overview

Tôi đã tích hợp hoàn chỉnh Label feature với backend API. Backend đã có sẵn tất cả các endpoint cần thiết.

## Backend API Endpoints (Đã có sẵn)

### Label Management (Quản lý Label)

1. **POST /projects/:projectId/labels** - Tạo label mới
2. **GET /projects/:projectId/labels** - Lấy tất cả labels trong project
3. **PATCH /labels/:labelId** - Cập nhật label
4. **DELETE /labels/:labelId** - Xóa label

### Task Label Assignment (Gán label cho task)

5. **POST /tasks/:taskId/labels** - Gán label cho task
6. **DELETE /tasks/:taskId/labels/:labelId** - Xóa label khỏi task
7. **GET /tasks/:taskId/labels** - Lấy tất cả labels của task

## Files Created/Updated

### 1. DTOs (Request/Response Models)

- ✅ **CreateLabelRequest.java** - DTO cho tạo label

  - `name`: string (max 50 ký tự)
  - `color`: hex color (#RRGGBB)

- ✅ **UpdateLabelRequest.java** - DTO cho update label (tất cả fields optional)

  - `name`: string (optional)
  - `color`: hex color (optional)

- ✅ **AssignLabelRequest.java** - DTO cho gán label

  - `labelId`: UUID của label

- ✅ **LabelDTO.java** - DTO cho response
  - `id`, `projectId`, `name`, `color`, `taskCount`, `createdAt`, `updatedAt`

### 2. API Service

- ✅ **LabelApiService.java** - Updated với tất cả 7 endpoints
  - Giữ nguyên old endpoints (backwards compatibility)
  - Thêm new project-based endpoints
  - Thêm task label assignment endpoints

### 3. Repository Layer

- ✅ **ILabelRepository.java** - Updated interface

  - Thêm `getLabelsByProject(projectId, callback)`
  - Thêm `createLabelInProject(projectId, label, callback)`
  - Thêm `getTaskLabels(taskId, callback)`
  - Thêm `assignLabelToTask(taskId, labelId, callback)`
  - Thêm `removeLabelFromTask(taskId, labelId, callback)`

- ✅ **LabelRepositoryImpl.java** - Implementation hoàn chỉnh
  - Tất cả methods đã implement với error handling

### 4. Use Cases

- ✅ **GetLabelsByProjectUseCase.java** - Lấy labels của project
- ✅ **CreateLabelInProjectUseCase.java** - Tạo label trong project
- ✅ **GetTaskLabelsUseCase.java** - Lấy labels của task
- ✅ **AssignLabelToTaskUseCase.java** - Gán label cho task
- ✅ **RemoveLabelFromTaskUseCase.java** - Xóa label khỏi task

### 5. ViewModel

- ✅ **LabelViewModel.java** - Updated với tất cả methods mới
  - `loadLabelsByProject(projectId)` - Load labels của project
  - `createLabelInProject(projectId, label)` - Tạo label
  - `loadTaskLabels(taskId)` - Load labels của task
  - `assignLabelToTask(taskId, labelId)` - Gán label
  - `removeLabelFromTask(taskId, labelId)` - Xóa label
  - `assignMultipleLabelsToTask(taskId, labelIds)` - Gán nhiều labels cùng lúc

## How to Use in UI

### Step 1: Setup ViewModel trong Activity/Fragment

```java
// Get dependencies (Repository, UseCases)
ILabelRepository labelRepository = new LabelRepositoryImpl(apiService);

GetLabelsByProjectUseCase getLabelsByProjectUseCase = new GetLabelsByProjectUseCase(labelRepository);
CreateLabelInProjectUseCase createLabelInProjectUseCase = new CreateLabelInProjectUseCase(labelRepository);
GetTaskLabelsUseCase getTaskLabelsUseCase = new GetTaskLabelsUseCase(labelRepository);
AssignLabelToTaskUseCase assignLabelToTaskUseCase = new AssignLabelToTaskUseCase(labelRepository);
RemoveLabelFromTaskUseCase removeLabelFromTaskUseCase = new RemoveLabelFromTaskUseCase(labelRepository);

// Tạo ViewModel
labelViewModel = new LabelViewModel(
    getLabelsByWorkspaceUseCase,
    getLabelsByProjectUseCase,
    getLabelByIdUseCase,
    createLabelUseCase,
    createLabelInProjectUseCase,
    updateLabelUseCase,
    deleteLabelUseCase,
    getTaskLabelsUseCase,
    assignLabelToTaskUseCase,
    removeLabelFromTaskUseCase
);
```

### Step 2: Observe LiveData

```java
// Observe labels list (cho LabelSelectionBottomSheet)
labelViewModel.getLabels().observe(this, labels -> {
    if (labels != null) {
        // Update RecyclerView adapter
        adapter.updateLabels(labels);
    }
});

// Observe task labels (cho CardDetailActivity)
labelViewModel.getTaskLabels().observe(this, taskLabels -> {
    if (taskLabels != null) {
        selectedLabels = taskLabels;
        displaySelectedLabels();
    }
});

// Observe errors
labelViewModel.getError().observe(this, error -> {
    if (error != null) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }
});

// Observe loading
labelViewModel.isLoading().observe(this, isLoading -> {
    if (isLoading) {
        showProgressBar();
    } else {
        hideProgressBar();
    }
});

// Observe operation success
labelViewModel.getOperationSuccess().observe(this, success -> {
    if (success != null && success) {
        Toast.makeText(this, "Thành công!", Toast.LENGTH_SHORT).show();
        labelViewModel.resetOperationSuccess();
    }
});
```

### Step 3: Load Labels cho LabelSelectionBottomSheet

```java
// Trong LabelSelectionBottomSheet.loadLabels()
// REPLACE getMockLabels() với:

private void loadLabels() {
    // Get projectId from task
    String projectId = task.getProjectId(); // Hoặc lấy từ workspace/board

    // Load labels từ backend
    labelViewModel.loadLabelsByProject(projectId);
}
```

### Step 4: Create Label trong LabelFormBottomSheet

```java
// Trong LabelFormBottomSheet.saveLabel()
private void saveLabel() {
    String name = etLabelName.getText().toString().trim();

    if (name.isEmpty()) {
        Toast.makeText(getContext(), "Vui lòng nhập tên nhãn", Toast.LENGTH_SHORT).show();
        return;
    }

    Label newLabel = new Label();
    newLabel.setName(name);
    newLabel.setColor(selectedColor);

    if (isEditMode && currentLabel != null) {
        // Update existing label
        labelViewModel.updateLabel(currentLabel.getId(), newLabel, projectId);
    } else {
        // Create new label
        labelViewModel.createLabelInProject(projectId, newLabel);
    }
}
```

### Step 5: Load Task Labels trong CardDetailActivity

```java
// Trong CardDetailActivity.loadTaskLabels()
private void loadTaskLabels() {
    if (task != null && task.getId() != null) {
        labelViewModel.loadTaskLabels(task.getId());
    }
}
```

### Step 6: Save Selected Labels khi đóng LabelSelectionBottomSheet

```java
// Trong LabelSelectionBottomSheet.onClose()
private void saveSelectedLabels() {
    // Get currently assigned labels
    List<Label> currentLabels = taskLabelsLiveData.getValue();
    List<String> currentLabelIds = new ArrayList<>();
    if (currentLabels != null) {
        for (Label label : currentLabels) {
            currentLabelIds.add(label.getId());
        }
    }

    // Find labels to add (in selectedLabelIds but not in currentLabelIds)
    List<String> labelsToAdd = new ArrayList<>();
    for (String id : selectedLabelIds) {
        if (!currentLabelIds.contains(id)) {
            labelsToAdd.add(id);
        }
    }

    // Find labels to remove (in currentLabelIds but not in selectedLabelIds)
    List<String> labelsToRemove = new ArrayList<>();
    for (String id : currentLabelIds) {
        if (!selectedLabelIds.contains(id)) {
            labelsToRemove.add(id);
        }
    }

    // Assign new labels
    if (!labelsToAdd.isEmpty()) {
        labelViewModel.assignMultipleLabelsToTask(taskId, labelsToAdd);
    }

    // Remove unselected labels
    for (String labelId : labelsToRemove) {
        labelViewModel.removeLabelFromTask(taskId, labelId);
    }
}
```

## Next Steps - Implementation trong UI

### 1. Update LabelSelectionBottomSheet.java

- [ ] Inject LabelViewModel vào constructor
- [ ] Replace `getMockLabels()` với `labelViewModel.loadLabelsByProject(projectId)`
- [ ] Observe `labelViewModel.getLabels()` để update adapter
- [ ] Implement `saveSelectedLabels()` như trên

### 2. Update LabelFormBottomSheet.java

- [ ] Inject LabelViewModel vào constructor
- [ ] Update `saveLabel()` để dùng `labelViewModel.createLabelInProject()` hoặc `updateLabel()`
- [ ] Observe `operationSuccess` để close dialog khi thành công

### 3. Update CardDetailActivity.java

- [ ] Inject LabelViewModel trong onCreate()
- [ ] Setup observers cho `taskLabelsLiveData`, `errorLiveData`, `loadingLiveData`
- [ ] Update `loadTaskLabels()` để dùng `labelViewModel.loadTaskLabels(taskId)`
- [ ] Remove temp ID logic - backend sẽ trả về real IDs

### 4. Fix truyền projectId

- [ ] Đảm bảo `Task` model có field `projectId`
- [ ] Hoặc lấy projectId từ parent Board/Workspace
- [ ] Truyền projectId vào LabelSelectionBottomSheet và LabelFormBottomSheet

## Important Notes

### ⚠️ Project ID vs Workspace ID

- Backend hiện tại dùng **Project-based labels** (không phải board-based)
- Cần truyền đúng `projectId` khi call API
- Label thuộc về Project, không phải Workspace hay Board

### ⚠️ Color Format

- Backend yêu cầu color format: `#RRGGBB` (uppercase hex)
- ColorPickerAdapter hiện tại đã dùng format này (20 colors)
- Khi tạo label mới, đảm bảo color ở format đúng

### ⚠️ Error Handling

- Backend có validation:
  - Name max 50 characters
  - Color phải hex format
  - Duplicate name trong project sẽ bị reject
  - Max labels per task (check backend constant)
- UI cần handle các error này với Toast messages

### ⚠️ ApiResponse Wrapper

- Backend responses được wrap trong `ApiResponse<T>`
- Repository layer đã handle unwrapping: `response.body().getData()`
- ViewModel nhận domain models (Label), không phải DTOs

## Testing Checklist

- [ ] Load labels của project hiển thị trong RecyclerView
- [ ] Create label mới và label xuất hiện trong list
- [ ] Update label và thấy thay đổi
- [ ] Delete label và label biến mất
- [ ] Load labels của task hiển thị dưới dạng chips
- [ ] Assign labels và chips xuất hiện
- [ ] Remove labels và chips biến mất
- [ ] Error handling: network error, validation error
- [ ] Loading states hoạt động
- [ ] Navigate away và quay lại: labels vẫn được saved

## Debugging Tips

### Check API calls trong Logcat

```java
// Thêm interceptor để log requests
OkHttpClient client = new OkHttpClient.Builder()
    .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
    .build();
```

### Check backend logs

```bash
# Trong plantracker-backend
npm run start:dev

# Watch logs cho label operations
# Sẽ thấy: [LabelsController] createLabel, assignLabel, etc.
```

### Common Issues

1. **Labels không load**:

   - Check projectId có đúng không
   - Check authentication token
   - Check network connection

2. **Create label fail**:

   - Check name length (<= 50)
   - Check color format (#RRGGBB)
   - Check duplicate name

3. **Assign label fail**:

   - Check taskId và labelId có tồn tại
   - Check max labels per task limit

4. **Labels mất khi navigate away**:
   - Đảm bảo đã call API assign/remove
   - Đảm bảo observe `taskLabelsLiveData` trong onResume()
   - Check lifecycle của ViewModel

## Contact Backend

Backend URL: `http://your-backend-url/api`

Example:

- Development: `http://localhost:3000/api`
- Production: `https://api.plantracker.com/api`

Cập nhật base URL trong `ApiClient.java` hoặc config file.
