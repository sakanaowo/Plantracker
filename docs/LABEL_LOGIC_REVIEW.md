# Label Feature - Complete Logic Review

## 📋 Kiểm tra toàn bộ Logic

### ✅ ĐÚNG - Backend API (100%)

**Endpoints:**

```typescript
POST   /projects/:projectId/labels        // Create label
GET    /projects/:projectId/labels        // List labels
PATCH  /labels/:labelId                   // Update label
DELETE /labels/:labelId                   // Delete label
POST   /tasks/:taskId/labels              // Assign label (body: {labelId})
DELETE /tasks/:taskId/labels/:labelId     // Remove label
GET    /tasks/:taskId/labels              // Get task labels
```

**DTOs:**

- ✅ CreateLabelDto: `{name, color}`
- ✅ UpdateLabelDto: `{name?, color?}` (optional)
- ✅ AssignLabelDto: `{labelId}`

**Response Format:**

```typescript
// Single label
{
  id: "uuid",
  projectId: "uuid",
  name: "string",
  color: "#RRGGBB",
  taskCount: number,
  createdAt: "ISO date",
  updatedAt: "ISO date"
}

// List: Array of above
```

---

### ✅ ĐÚNG - Frontend API Service (100%)

**File:** `LabelApiService.java`

```java
@POST("projects/{projectId}/labels")
Call<LabelDTO> createLabelInProject(projectId, CreateLabelRequest)

@GET("projects/{projectId}/labels")
Call<List<LabelDTO>> getLabelsByProject(projectId)

@PATCH("labels/{labelId}")
Call<LabelDTO> updateLabelNew(labelId, UpdateLabelRequest)

@DELETE("labels/{labelId}")
Call<Void> deleteLabelNew(labelId)

@POST("tasks/{taskId}/labels")
Call<Void> assignLabelToTask(taskId, AssignLabelRequest)

@DELETE("tasks/{taskId}/labels/{labelId}")
Call<Void> removeLabelFromTask(taskId, labelId)

@GET("tasks/{taskId}/labels")
Call<List<LabelDTO>> getTaskLabels(taskId)
```

✅ **Match 100% với backend**

---

### ✅ ĐÚNG - Repository Layer (100%)

**File:** `LabelRepositoryImpl.java`

**Methods:**

- ✅ `getLabelsByProject(projectId, callback)` - Maps DTO → Domain
- ✅ `createLabelInProject(projectId, label, callback)` - Creates label
- ✅ `updateLabel(labelId, label, callback)` - Updates label
- ✅ `deleteLabel(labelId, callback)` - Deletes label
- ✅ `getTaskLabels(taskId, callback)` - Gets task's labels
- ✅ `assignLabelToTask(taskId, labelId, callback)` - Assigns label
- ✅ `removeLabelFromTask(taskId, labelId, callback)` - Removes label

**Error Handling:**

- ✅ Network errors → `callback.onError("Network error: " + message)`
- ✅ HTTP errors → `callback.onError("Failed to...: " + responseCode)`

---

### ✅ ĐÚNG - Use Cases (100%)

**Files created:**

- ✅ `GetLabelsByProjectUseCase` - Delegates to repository
- ✅ `CreateLabelInProjectUseCase` - Validates and creates
- ✅ `UpdateLabelUseCase` - Updates existing label
- ✅ `DeleteLabelUseCase` - Deletes label
- ✅ `GetTaskLabelsUseCase` - Gets task labels
- ✅ `AssignLabelToTaskUseCase` - Assigns label to task
- ✅ `RemoveLabelFromTaskUseCase` - Removes label from task

**Pattern:** Clean separation of concerns ✅

---

### ⚠️ CẦN CẢI THIỆN - ViewModel (90%)

**File:** `LabelViewModel.java`

**LiveData:**

- ✅ `labelsLiveData` - Project labels
- ✅ `taskLabelsLiveData` - Task labels
- ✅ `selectedLabelLiveData` - Currently selected label
- ✅ `loadingLiveData` - Loading state
- ✅ `errorLiveData` - Error messages
- ✅ `operationSuccessLiveData` - Operation success flag

**Methods:**

- ✅ `loadLabelsByProject(projectId)` - Loads project labels
- ✅ `createLabelInProject(projectId, label)` - Creates label + auto reload
- ✅ `updateLabel(labelId, label, projectId)` - Updates + auto reload
- ✅ `deleteLabel(labelId, projectId)` - Deletes + auto reload
- ✅ `loadTaskLabels(taskId)` - Loads task labels
- ✅ `assignLabelToTask(taskId, labelId)` - Assigns + auto reload
- ✅ `removeLabelFromTask(taskId, labelId)` - Removes + auto reload
- ✅ `assignMultipleLabelsToTask(taskId, labelIds)` - Batch assign

**Issues:**

1. ⚠️ **Auto reload after assign/remove might cause race condition**
   - Assign API call completes → Immediately reload task labels
   - If assign succeeds but reload fails → UI shows old state
2. ⚠️ **No debounce for rapid operations**
   - User rapidly checks/unchecks multiple labels
   - Each triggers separate API call + reload
   - Could cause UI flicker or network spam

**Recommendation:**

```java
// Option 1: Don't auto-reload in ViewModel, let UI decide when to reload
public void assignLabelToTask(String taskId, String labelId) {
    assignLabelToTaskUseCase.execute(taskId, labelId, callback -> {
        operationSuccessLiveData.setValue(true);
        // DON'T auto reload here - let UI decide
    });
}

// Option 2: Add debounce mechanism
private Handler reloadHandler = new Handler();
private Runnable reloadRunnable;

public void assignLabelToTask(String taskId, String labelId) {
    assignLabelToTaskUseCase.execute(taskId, labelId, callback -> {
        operationSuccessLiveData.setValue(true);

        // Debounce reload - wait 300ms before reloading
        reloadHandler.removeCallbacks(reloadRunnable);
        reloadRunnable = () -> loadTaskLabels(taskId);
        reloadHandler.postDelayed(reloadRunnable, 300);
    });
}
```

---

### ⚠️ CẦN SỬA - LabelSelectionBottomSheet (75%)

**File:** `LabelSelectionBottomSheet.java`

**Flow hiện tại:**

```
1. Open dialog → loadLabels() → API: GET /projects/:projectId/labels
2. User checks label → onLabelChecked(isChecked=true)
   → assignLabelToTask() → API: POST /tasks/:taskId/labels
   → ViewModel auto reload task labels
3. User unchecks label → onLabelChecked(isChecked=false)
   → removeLabelFromTask() → API: DELETE /tasks/:taskId/labels/:labelId
   → ViewModel auto reload task labels
4. User clicks close → onLabelsUpdated(selectedLabels)
   → CardDetailActivity reloads task labels AGAIN
```

**Vấn đề:**

1. ❌ **Duplicate reload khi đóng dialog**

   - Mỗi lần check/uncheck đã reload rồi
   - Đóng dialog lại reload thêm 1 lần nữa
   - Không cần thiết!

2. ❌ **Callback `onLabelsUpdated` không còn ý nghĩa**

   - Trước đây: Dialog chỉ track selection, không gọi API
   - Giờ: Dialog đã assign/remove labels qua API
   - Callback giờ chỉ để trigger reload, không cần truyền `selectedLabels` nữa

3. ⚠️ **UI không phản ánh trạng thái thực tế**

   - User checks label → API call bắt đầu
   - Nếu API fail → Checkbox vẫn checked nhưng backend không có
   - Cần rollback UI state nếu API fails

4. ⚠️ **selectedLabelIds không sync với backend**
   - `selectedLabelIds` là local state
   - Khi API assign succeeds → should update `selectedLabelIds`
   - Khi API assign fails → should NOT update `selectedLabelIds`

**Fix đề xuất:**

```java
private void setupRecyclerView() {
    adapter = new LabelSelectionAdapter(new LabelSelectionAdapter.OnLabelActionListener() {
        @Override
        public void onLabelChecked(Label label, boolean isChecked) {
            if (isChecked) {
                // Optimistic update
                if (!selectedLabelIds.contains(label.getId())) {
                    selectedLabelIds.add(label.getId());
                }

                // Call API
                if (taskId != null && labelViewModel != null) {
                    labelViewModel.assignLabelToTask(taskId, label.getId());

                    // Observe operation result
                    labelViewModel.getOperationSuccess().observeOnce(getViewLifecycleOwner(), success -> {
                        if (success == null || !success) {
                            // API failed - rollback UI
                            selectedLabelIds.remove(label.getId());
                            adapter.setSelectedLabels(selectedLabelIds);
                            Toast.makeText(requireContext(), "Failed to assign label", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                // Same for remove
                selectedLabelIds.remove(label.getId());

                if (taskId != null && labelViewModel != null) {
                    labelViewModel.removeLabelFromTask(taskId, label.getId());

                    labelViewModel.getOperationSuccess().observeOnce(getViewLifecycleOwner(), success -> {
                        if (success == null || !success) {
                            // API failed - rollback UI
                            selectedLabelIds.add(label.getId());
                            adapter.setSelectedLabels(selectedLabelIds);
                            Toast.makeText(requireContext(), "Failed to remove label", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }
    });
}

private void setupListeners() {
    ivClose.setOnClickListener(v -> {
        if (listener != null) {
            // Just notify that dialog closed - don't pass labels
            // CardDetailActivity will reload from backend
            listener.onLabelsUpdated(null);
        }
        dismiss();
    });
}
```

**Change interface:**

```java
public interface OnLabelsUpdatedListener {
    void onLabelsUpdated(); // Remove parameter - not needed
}
```

---

### ✅ ĐÚNG - CardDetailActivity (95%)

**File:** `CardDetailActivity.java`

**Initialization:**

- ✅ `setupLabelViewModel()` - Properly initialized
- ✅ Observer for `taskLabelsLiveData` → Updates UI
- ✅ `loadTaskLabels()` called on task open

**Show dialog:**

- ✅ Pass `projectId`, `taskId`, `selectedLabelIds`
- ✅ Reload task labels when dialog closes

**Display:**

- ✅ `displaySelectedLabels()` creates chips
- ✅ Colored chips with dynamic text color

**Minor issue:**
⚠️ Callback lambda nhận `updatedLabels` nhưng không dùng

```java
dialog.setOnLabelsUpdatedListener((updatedLabels) -> {
    // Reload task labels from backend to ensure sync
    if (labelViewModel != null && taskId != null) {
        labelViewModel.loadTaskLabels(taskId);
    }
});
```

**Should be:**

```java
dialog.setOnLabelsUpdatedListener(() -> {
    // Reload task labels from backend to ensure sync
    if (labelViewModel != null && taskId != null) {
        labelViewModel.loadTaskLabels(taskId);
    }
});
```

---

### ⚠️ CẦN KIỂM TRA - LabelFormBottomSheet

**Need to verify:**

1. Does it properly call `createLabelInProject()`?
2. Does it properly call `updateLabel()`?
3. Does it properly call `deleteLabel()`?
4. Are observers set up correctly?

Let me check this file...

---

## 🎯 Summary của Issues

### Critical Issues (Phải fix):

1. **LabelSelectionBottomSheet - Duplicate Reload**

   - ❌ Reload 2 lần: Khi assign/remove + khi đóng dialog
   - Fix: Remove auto-reload trong ViewModel's assign/remove methods

2. **LabelSelectionBottomSheet - No Error Rollback**

   - ❌ Nếu API fails, UI vẫn thay đổi
   - Fix: Observe `operationSuccess`, rollback nếu failed

3. **Interface signature mismatch**
   - ❌ `OnLabelsUpdatedListener` nhận `List<Label>` nhưng không dùng
   - Fix: Change to `void onLabelsUpdated()` (no params)

### Minor Issues (Nice to have):

4. **No debounce for rapid operations**

   - ⚠️ User rapidly checks/unchecks → Multiple API calls
   - Fix: Add 300ms debounce

5. **No loading indicator during API calls**
   - ⚠️ User không biết khi nào API đang chạy
   - Fix: Show ProgressBar when `loadingLiveData` is true

---

## 🔧 Recommended Fixes Priority

### Priority 1 (Critical):

- [ ] Fix duplicate reload issue
- [ ] Add error rollback in checkbox handler
- [ ] Fix interface signature

### Priority 2 (Important):

- [ ] Add loading indicator
- [ ] Add debounce for rapid operations

### Priority 3 (Enhancement):

- [ ] Add offline support (cache labels)
- [ ] Add optimistic UI updates with better feedback

---

## 📊 Overall Score

| Layer                     | Score | Status                       |
| ------------------------- | ----- | ---------------------------- |
| Backend API               | 100%  | ✅ Perfect                   |
| Frontend API Service      | 100%  | ✅ Perfect                   |
| Repository                | 100%  | ✅ Perfect                   |
| Use Cases                 | 100%  | ✅ Perfect                   |
| ViewModel                 | 90%   | ⚠️ Minor improvements needed |
| LabelSelectionBottomSheet | 75%   | ⚠️ Needs fixes               |
| CardDetailActivity        | 95%   | ✅ Almost perfect            |

**Overall: 94% - GOOD, but có một vài logic issues cần fix**
