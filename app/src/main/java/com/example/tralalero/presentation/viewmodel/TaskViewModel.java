package com.example.tralalero.presentation.viewmodel;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.tralalero.domain.model.Task;
import com.example.tralalero.domain.model.TaskComment;
import com.example.tralalero.domain.model.Attachment;
import com.example.tralalero.domain.model.Checklist;
import com.example.tralalero.domain.usecase.task.GetTaskByIdUseCase;
import com.example.tralalero.domain.usecase.task.GetTasksByBoardUseCase;
import com.example.tralalero.domain.usecase.task.CreateTaskUseCase;
import com.example.tralalero.domain.usecase.task.CreateQuickTaskUseCase;
import com.example.tralalero.domain.usecase.task.UpdateTaskUseCase;
import com.example.tralalero.domain.usecase.task.DeleteTaskUseCase;
import com.example.tralalero.domain.usecase.task.AssignTaskUseCase;
import com.example.tralalero.domain.usecase.task.UnassignTaskUseCase;
import com.example.tralalero.domain.usecase.task.MoveTaskToBoardUseCase;
import com.example.tralalero.domain.usecase.task.UpdateTaskPositionUseCase;
import com.example.tralalero.domain.usecase.task.AddCommentUseCase;
import com.example.tralalero.domain.usecase.task.GetTaskCommentsUseCase;
import com.example.tralalero.domain.usecase.task.AddAttachmentUseCase;
import com.example.tralalero.domain.usecase.task.GetTaskAttachmentsUseCase;
import com.example.tralalero.domain.usecase.task.AddChecklistUseCase;
import com.example.tralalero.domain.usecase.task.GetTaskChecklistsUseCase;
import com.example.tralalero.domain.usecase.task.UpdateCommentUseCase;
import com.example.tralalero.domain.usecase.task.DeleteCommentUseCase;
import com.example.tralalero.domain.usecase.task.DeleteAttachmentUseCase;
import com.example.tralalero.domain.usecase.task.GetAttachmentViewUrlUseCase;
import com.example.tralalero.domain.repository.ITaskRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TaskViewModel - Enhanced for MVVM Architecture
 * 
 * DEV 2 WORK - DAY 3-5: Complete TaskViewModel with optimistic updates
 * 
 * Features:
 * - Inbox tasks management
 * - Optimistic updates for all CRUD operations
 * - Toggle task complete with instant feedback
 * - Move task to board with instant UI update
 * - Zero manual reload needed in Activities
 */
import java.util.function.Function;

public class TaskViewModel extends ViewModel {
    
    private static final String TAG = "TaskViewModel";

    private final GetTaskByIdUseCase getTaskByIdUseCase;
    private final GetTasksByBoardUseCase getTasksByBoardUseCase;
    private final CreateTaskUseCase createTaskUseCase;
    private final CreateQuickTaskUseCase createQuickTaskUseCase;
    private final UpdateTaskUseCase updateTaskUseCase;
    private final DeleteTaskUseCase deleteTaskUseCase;
    private final AssignTaskUseCase assignTaskUseCase;
    private final UnassignTaskUseCase unassignTaskUseCase;
    private final MoveTaskToBoardUseCase moveTaskToBoardUseCase;
    private final UpdateTaskPositionUseCase updateTaskPositionUseCase;
    private final AddCommentUseCase addCommentUseCase;
    private final GetTaskCommentsUseCase getTaskCommentsUseCase;
    private final AddAttachmentUseCase addAttachmentUseCase;
    private final GetTaskAttachmentsUseCase getTaskAttachmentsUseCase;
    private final AddChecklistUseCase addChecklistUseCase;
    private final GetTaskChecklistsUseCase getTaskChecklistsUseCase;
    private final UpdateCommentUseCase updateCommentUseCase;
    private final DeleteCommentUseCase deleteCommentUseCase;
    private final DeleteAttachmentUseCase deleteAttachmentUseCase;
    private final GetAttachmentViewUrlUseCase getAttachmentViewUrlUseCase;
    private final ITaskRepository repository; // For checklist item operations
    
    // ========== State Management ==========
    // Tasks per board for ProjectActivity
    private final Map<String, MutableLiveData<List<Task>>> tasksPerBoardMap = new HashMap<>();
    
    // ✅ NEW: Inbox tasks for InboxActivity
    private final MutableLiveData<List<Task>> inboxTasksLiveData = new MutableLiveData<>();
    
    private final MutableLiveData<List<Task>> tasksLiveData = new MutableLiveData<>();
    private final MutableLiveData<Task> selectedTaskLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<TaskComment>> commentsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Attachment>> attachmentsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Checklist>> checklistsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<com.example.tralalero.domain.model.ChecklistItem>> checklistItemsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    
    // ✅ Event for task moved successfully (contains source and target board IDs)
    public static class TaskMovedEvent {
        public final String taskId;
        public final String sourceBoardId;
        public final String targetBoardId;
        
        public TaskMovedEvent(String taskId, String sourceBoardId, String targetBoardId) {
            this.taskId = taskId;
            this.sourceBoardId = sourceBoardId;
            this.targetBoardId = targetBoardId;
        }
    }
    
    public static class TaskMoveFailedEvent {
        public final Task originalTask;
        public final String sourceBoardId;
        public final String targetBoardId;
        public final String error;
        
        public TaskMoveFailedEvent(Task originalTask, String sourceBoardId, String targetBoardId, String error) {
            this.originalTask = originalTask;
            this.sourceBoardId = sourceBoardId;
            this.targetBoardId = targetBoardId;
            this.error = error;
        }
    }
    
    private final MutableLiveData<TaskMovedEvent> taskMovedEventLiveData = new MutableLiveData<>();
    private final MutableLiveData<TaskMoveFailedEvent> taskMoveFailedEventLiveData = new MutableLiveData<>();

    public TaskViewModel(
            GetTaskByIdUseCase getTaskByIdUseCase,
            GetTasksByBoardUseCase getTasksByBoardUseCase,
            CreateTaskUseCase createTaskUseCase,
            CreateQuickTaskUseCase createQuickTaskUseCase,
            UpdateTaskUseCase updateTaskUseCase,
            DeleteTaskUseCase deleteTaskUseCase,
            AssignTaskUseCase assignTaskUseCase,
            UnassignTaskUseCase unassignTaskUseCase,
            MoveTaskToBoardUseCase moveTaskToBoardUseCase,
            UpdateTaskPositionUseCase updateTaskPositionUseCase,
            AddCommentUseCase addCommentUseCase,
            GetTaskCommentsUseCase getTaskCommentsUseCase,
            AddAttachmentUseCase addAttachmentUseCase,
            GetTaskAttachmentsUseCase getTaskAttachmentsUseCase,
            AddChecklistUseCase addChecklistUseCase,
            GetTaskChecklistsUseCase getTaskChecklistsUseCase,
            UpdateCommentUseCase updateCommentUseCase,
            DeleteCommentUseCase deleteCommentUseCase,
            DeleteAttachmentUseCase deleteAttachmentUseCase,
            GetAttachmentViewUrlUseCase getAttachmentViewUrlUseCase,
            ITaskRepository repository // Add repository for checklist item operations
    ) {
        this.getTaskByIdUseCase = getTaskByIdUseCase;
        this.getTasksByBoardUseCase = getTasksByBoardUseCase;
        this.createTaskUseCase = createTaskUseCase;
        this.createQuickTaskUseCase = createQuickTaskUseCase;
        this.updateTaskUseCase = updateTaskUseCase;
        this.deleteTaskUseCase = deleteTaskUseCase;
        this.assignTaskUseCase = assignTaskUseCase;
        this.unassignTaskUseCase = unassignTaskUseCase;
        this.moveTaskToBoardUseCase = moveTaskToBoardUseCase;
        this.updateTaskPositionUseCase = updateTaskPositionUseCase;
        this.addCommentUseCase = addCommentUseCase;
        this.getTaskCommentsUseCase = getTaskCommentsUseCase;
        this.addAttachmentUseCase = addAttachmentUseCase;
        this.getTaskAttachmentsUseCase = getTaskAttachmentsUseCase;
        this.addChecklistUseCase = addChecklistUseCase;
        this.getTaskChecklistsUseCase = getTaskChecklistsUseCase;
        this.updateCommentUseCase = updateCommentUseCase;
        this.deleteCommentUseCase = deleteCommentUseCase;
        this.deleteAttachmentUseCase = deleteAttachmentUseCase;
        this.getAttachmentViewUrlUseCase = getAttachmentViewUrlUseCase;
        this.repository = repository;
    }

    public LiveData<List<Task>> getTasksForBoard(String boardId) {
        if (boardId == null || boardId.isEmpty()) {
            return tasksLiveData; 
        }

        if (!tasksPerBoardMap.containsKey(boardId)) {
            tasksPerBoardMap.put(boardId, new MutableLiveData<>());
        }
        return tasksPerBoardMap.get(boardId);
    }

    @Deprecated
    public LiveData<List<Task>> getTasks() {
        return tasksLiveData;
    }
    
    /**
     * ✅ NEW: Get inbox tasks for InboxActivity
     * @return LiveData of all user's inbox tasks
     */
    public LiveData<List<Task>> getInboxTasks() {
        return inboxTasksLiveData;
    }

    public LiveData<Task> getSelectedTask() {
        return selectedTaskLiveData;
    }

    public void setSelectedTask(Task task) {
        selectedTaskLiveData.setValue(task);
    }

    public void clearSelectedTask() {
        selectedTaskLiveData.setValue(null);
    }

    public LiveData<List<TaskComment>> getComments() {
        return commentsLiveData;
    }

    public LiveData<List<Attachment>> getAttachments() {
        return attachmentsLiveData;
    }

    public LiveData<List<Checklist>> getChecklists() {
        return checklistsLiveData;
    }

    public LiveData<List<com.example.tralalero.domain.model.ChecklistItem>> getChecklistItems() {
        return checklistItemsLiveData;
    }

    public LiveData<Boolean> isLoading() {
        return loadingLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }
    
    public LiveData<TaskMovedEvent> getTaskMovedEvent() {
        return taskMovedEventLiveData;
    }
    
    public LiveData<TaskMoveFailedEvent> getTaskMoveFailedEvent() {
        return taskMoveFailedEventLiveData;
    }
    
    /**
     * ✅ NEW: Load inbox tasks for user
     * This replaces the manual load pattern in InboxActivity
     * @param userId User ID to load tasks for (can be empty for all tasks)
     * @param clearCache If true, clears cache before loading to force fresh data
     */
    public void loadInboxTasks(String userId, boolean clearCache) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);
        
        // Clear cache if requested (for pull-to-refresh)
        if (clearCache) {
            android.util.Log.d(TAG, "🔄 Clearing task cache for fresh data");
            repository.clearCache();
        }
        
        // Use repository to get all quick tasks (inbox tasks)
        // These are tasks without a specific board assignment
        repository.getQuickTasks(new ITaskRepository.RepositoryCallback<List<Task>>() {
            @Override
            public void onSuccess(List<Task> result) {
                loadingLiveData.setValue(false);
                inboxTasksLiveData.setValue(result != null ? result : new ArrayList<>());
                android.util.Log.d("TaskViewModel", "✅ Loaded " + 
                    (result != null ? result.size() : 0) + " inbox tasks");
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
                inboxTasksLiveData.setValue(new ArrayList<>());
                android.util.Log.e("TaskViewModel", "❌ Failed to load inbox tasks: " + error);
            }
        });
    }

    public void loadTaskById(String taskId) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        getTaskByIdUseCase.execute(taskId, new GetTaskByIdUseCase.Callback<Task>() {
            @Override
            public void onSuccess(Task result) {
                loadingLiveData.setValue(false);
                selectedTaskLiveData.setValue(result);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void loadTasksByBoard(String boardId) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        getTasksByBoardUseCase.execute(boardId, new GetTasksByBoardUseCase.Callback<List<Task>>() {
            @Override
            public void onSuccess(List<Task> result) {
                loadingLiveData.setValue(false);
                if (!tasksPerBoardMap.containsKey(boardId)) {
                    tasksPerBoardMap.put(boardId, new MutableLiveData<>());
                }
                MutableLiveData<List<Task>> boardLiveData = tasksPerBoardMap.get(boardId);
                if (boardLiveData != null) {
                    boardLiveData.setValue(result);
                }
                tasksLiveData.setValue(result);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    /**
     * ✅ ENHANCED: Create task with optimistic update
     * Instantly adds task to UI, then syncs with API
     */
    public void createTask(Task task) {
        errorLiveData.setValue(null);
        
        // ✅ Optimistic update: Add task to UI immediately
        String boardId = task.getBoardId();
        
        // Create temp task
        String tempId = "temp_" + System.currentTimeMillis();
        Task tempTask = task; // Use the task as-is, will get real ID from server
        
        // Add to appropriate LiveData
        if (boardId != null && !boardId.isEmpty()) {
            // Board task
            MutableLiveData<List<Task>> boardTasks = tasksPerBoardMap.get(boardId);
            if (boardTasks != null && boardTasks.getValue() != null) {
                List<Task> updated = new ArrayList<>(boardTasks.getValue());
                updated.add(0, tempTask); // Add at top
                boardTasks.setValue(updated);
            }
        } else {
            // Inbox task
            List<Task> currentInbox = inboxTasksLiveData.getValue();
            if (currentInbox != null) {
                List<Task> updated = new ArrayList<>(currentInbox);
                updated.add(0, tempTask);
                inboxTasksLiveData.setValue(updated);
            }
        }
        
        // Now call API
        loadingLiveData.setValue(true);
        createTaskUseCase.execute(task, new CreateTaskUseCase.Callback<Task>() {
            @Override
            public void onSuccess(Task result) {
                loadingLiveData.setValue(false);
                selectedTaskLiveData.setValue(result);
                
                // ✅ Replace temp with real task (NO RELOAD!)
                if (result.getBoardId() != null && !result.getBoardId().isEmpty()) {
                    // Update board tasks list directly
                    MutableLiveData<List<Task>> boardTasks = tasksPerBoardMap.get(result.getBoardId());
                    if (boardTasks != null && boardTasks.getValue() != null) {
                        List<Task> updated = new ArrayList<>();
                        for (Task t : boardTasks.getValue()) {
                            if (t == tempTask) {
                                updated.add(result); // Replace temp with real
                            } else {
                                updated.add(t);
                            }
                        }
                        boardTasks.setValue(updated);
                    }
                } else {
                    // Update inbox
                    List<Task> inbox = inboxTasksLiveData.getValue();
                    if (inbox != null) {
                        List<Task> updated = new ArrayList<>();
                        for (Task t : inbox) {
                            if (t == tempTask) {
                                updated.add(result); // Replace temp
                            } else {
                                updated.add(t);
                            }
                        }
                        inboxTasksLiveData.setValue(updated);
                    }
                }
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
                
                // ✅ Rollback: Remove temp task
                if (boardId != null && !boardId.isEmpty()) {
                    MutableLiveData<List<Task>> boardTasks = tasksPerBoardMap.get(boardId);
                    if (boardTasks != null && boardTasks.getValue() != null) {
                        List<Task> updated = new ArrayList<>();
                        for (Task t : boardTasks.getValue()) {
                            if (t != tempTask) {
                                updated.add(t);
                            }
                        }
                        boardTasks.setValue(updated);
                    }
                } else {
                    List<Task> inbox = inboxTasksLiveData.getValue();
                    if (inbox != null) {
                        List<Task> updated = new ArrayList<>();
                        for (Task t : inbox) {
                            if (t != tempTask) {
                                updated.add(t);
                            }
                        }
                        inboxTasksLiveData.setValue(updated);
                    }
                }
            }
        });
    }

    /**
     * ✅ NEW: Create quick task (inbox task without project/board ID)
     * Uses POST /api/tasks/quick endpoint
     * Backend auto-assigns to user's default project and inbox board
     */
    public void createQuickTask(String title, String description) {
        errorLiveData.setValue(null);
        
        // ✅ Optimistic update: Add temp task to inbox immediately
        String tempId = "temp_" + System.currentTimeMillis();
        java.util.Date now = new java.util.Date();
        
        // Create temp task with minimal data (projectId/boardId will be assigned by backend)
        Task tempTask = new Task(
            tempId,          // id (temp)
            null,            // projectId (backend assigns)
            null,            // boardId (backend assigns)
            title,           // title
            description != null ? description : "",  // description
            null,            // issueKey
            Task.TaskType.TASK,
            Task.TaskStatus.TO_DO,
            Task.TaskPriority.MEDIUM,
            0.0,             // position
            null,            // assigneeId
            null,            // createdBy
            null,            // sprintId
            null,            // epicId
            null,            // parentTaskId
            null,            // startAt
            null,            // dueAt
            null,            // storyPoints
            null,            // originalEstimateSec
            null,            // remainingEstimateSec
            now,             // createdAt
            now,             // updatedAt
            false, null, null, null, null  // calendar sync + labels
        );
        
        // Add to inbox immediately
        List<Task> currentInbox = inboxTasksLiveData.getValue();
        if (currentInbox != null) {
            List<Task> updated = new ArrayList<>(currentInbox);
            updated.add(0, tempTask);
            inboxTasksLiveData.setValue(updated);
        }
        
        // Now call API
        loadingLiveData.setValue(true);
        createQuickTaskUseCase.execute(title, description, new CreateQuickTaskUseCase.Callback<Task>() {
            @Override
            public void onSuccess(Task result) {
                loadingLiveData.setValue(false);
                selectedTaskLiveData.setValue(result);
                
                // ✅ Replace temp with real task
                List<Task> inbox = inboxTasksLiveData.getValue();
                if (inbox != null) {
                    List<Task> updated = new ArrayList<>();
                    for (Task t : inbox) {
                        if (t == tempTask) {
                            updated.add(result); // Replace temp with real
                        } else {
                            updated.add(t);
                        }
                    }
                    inboxTasksLiveData.setValue(updated);
                }
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
                
                // ✅ Rollback: Remove temp task
                List<Task> inbox = inboxTasksLiveData.getValue();
                if (inbox != null) {
                    List<Task> updated = new ArrayList<>();
                    for (Task t : inbox) {
                        if (t != tempTask) {
                            updated.add(t);
                        }
                    }
                    inboxTasksLiveData.setValue(updated);
                }
            }
        });
    }

    /**
     * ✅ ENHANCED: Update task with optimistic update
     * Used when editing task fields (title, description, dates, etc.)
     */
    public void updateTask(String taskId, Task task) {
        if (task == null || taskId == null) return;
        
        errorLiveData.setValue(null);
        
        // Find original task for rollback
        Task originalTask = null;
        
        // Find in board tasks
        String boardId = task.getBoardId();
        if (boardId != null && !boardId.isEmpty()) {
            MutableLiveData<List<Task>> boardTasks = tasksPerBoardMap.get(boardId);
            if (boardTasks != null && boardTasks.getValue() != null) {
                for (Task t : boardTasks.getValue()) {
                    if (t.getId().equals(taskId)) {
                        originalTask = t;
                        break;
                    }
                }
            }
        }
        
        // Find in inbox if not found
        if (originalTask == null) {
            List<Task> inbox = inboxTasksLiveData.getValue();
            if (inbox != null) {
                for (Task t : inbox) {
                    if (t.getId().equals(taskId)) {
                        originalTask = t;
                        break;
                    }
                }
            }
        }
        
        final Task taskToRollback = originalTask;
        
        // ✅ Optimistic update: Show updated task immediately
        updateTaskInAllLists(originalTask != null ? originalTask : task, t -> task);
        
        // Update selected task if it's the same
        if (selectedTaskLiveData.getValue() != null && 
            selectedTaskLiveData.getValue().getId().equals(taskId)) {
            selectedTaskLiveData.setValue(task);
        }
        
        // Now call API
        loadingLiveData.setValue(true);
        updateTaskUseCase.execute(taskId, task, new UpdateTaskUseCase.Callback<Task>() {
            @Override
            public void onSuccess(Task result) {
                loadingLiveData.setValue(false);
                
                // Update with server version
                updateTaskInAllLists(task, t -> result);
                selectedTaskLiveData.setValue(result);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
                
                // ✅ Rollback: Restore original task
                if (taskToRollback != null) {
                    updateTaskInAllLists(task, t -> taskToRollback);
                    if (selectedTaskLiveData.getValue() != null && 
                        selectedTaskLiveData.getValue().getId().equals(taskId)) {
                        selectedTaskLiveData.setValue(taskToRollback);
                    }
                }
            }
        });
    }

    /**
     * ✅ Update task status (for checkbox toggle)
     * Optimistic update with rollback on error
     */
    public void updateTaskStatus(String taskId, Task.TaskStatus newStatus) {
        if (taskId == null || newStatus == null) return;
        
        android.util.Log.d(TAG, "updateTaskStatus: taskId=" + taskId + ", newStatus=" + newStatus);
        
        errorLiveData.setValue(null);
        
        // Find task
        Task originalTask = findTaskById(taskId);
        if (originalTask == null) {
            android.util.Log.e(TAG, "Task not found: " + taskId);
            errorLiveData.setValue("Task not found");
            return;
        }
        
        android.util.Log.d(TAG, "Found task: " + originalTask.getTitle() + ", current status=" + originalTask.getStatus());
        
        // Create new task with updated status (Task is immutable)
        Task updatedTask = new Task(
            originalTask.getId(),
            originalTask.getProjectId(),
            originalTask.getBoardId(),
            originalTask.getTitle(),
            originalTask.getDescription(),
            originalTask.getIssueKey(),
            originalTask.getType(),
            newStatus,  // ✅ Updated status
            originalTask.getPriority(),
            originalTask.getPosition(),
            originalTask.getAssigneeId(),
            originalTask.getCreatedBy(),
            originalTask.getSprintId(),
            originalTask.getEpicId(),
            originalTask.getParentTaskId(),
            originalTask.getStartAt(),
            originalTask.getDueAt(),
            originalTask.getStoryPoints(),
            originalTask.getOriginalEstimateSec(),
            originalTask.getRemainingEstimateSec(),
            originalTask.getCreatedAt(),
            originalTask.getUpdatedAt(),
            originalTask.isCalendarSyncEnabled(),
            originalTask.getCalendarReminderMinutes(),
            originalTask.getCalendarEventId(),
            originalTask.getCalendarSyncedAt(),
            originalTask.getLabels()  // preserve labels
        );
        
        android.util.Log.d(TAG, "Calling updateTask API with new status=" + newStatus);
        
        // ✅ Optimistic update: Show change immediately
        updateTaskInAllLists(originalTask, t -> updatedTask);
        if (selectedTaskLiveData.getValue() != null && 
            selectedTaskLiveData.getValue().getId().equals(taskId)) {
            selectedTaskLiveData.setValue(updatedTask);
        }
        
        // Call API to persist
        loadingLiveData.setValue(true);
        updateTaskUseCase.execute(taskId, updatedTask, new UpdateTaskUseCase.Callback<Task>() {
            @Override
            public void onSuccess(Task result) {
                loadingLiveData.setValue(false);
                android.util.Log.d(TAG, "✅ Task status updated successfully: " + result.getTitle() + " -> " + result.getStatus());
                
                // Update with server version
                updateTaskInAllLists(updatedTask, t -> result);
                if (selectedTaskLiveData.getValue() != null && 
                    selectedTaskLiveData.getValue().getId().equals(taskId)) {
                    selectedTaskLiveData.setValue(result);
                }
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                android.util.Log.e(TAG, "❌ Failed to update task status: " + error);
                errorLiveData.setValue("Failed to update task: " + error);
                
                // ✅ Rollback: Restore original task
                updateTaskInAllLists(updatedTask, t -> originalTask);
                if (selectedTaskLiveData.getValue() != null && 
                    selectedTaskLiveData.getValue().getId().equals(taskId)) {
                    selectedTaskLiveData.setValue(originalTask);
                }
            }
        });
    }
    
    private Task findTaskById(String taskId) {
        // Search in board tasks
        for (MutableLiveData<List<Task>> boardTasks : tasksPerBoardMap.values()) {
            if (boardTasks.getValue() != null) {
                for (Task t : boardTasks.getValue()) {
                    if (t.getId().equals(taskId)) {
                        return t;
                    }
                }
            }
        }
        
        // Search in inbox
        List<Task> inbox = inboxTasksLiveData.getValue();
        if (inbox != null) {
            for (Task t : inbox) {
                if (t.getId().equals(taskId)) {
                    return t;
                }
            }
        }
        
        return null;
    }

    /**
     * ✅ NEW: Toggle task complete status with instant update
     * Perfect for checkbox in InboxActivity
     */
    public void toggleTaskComplete(Task task) {
        if (task == null) return;
        
        errorLiveData.setValue(null);
        
        // ✅ Optimistic update: Toggle isDone immediately
        final boolean newDoneStatus = !task.isDone();
        final Task.TaskStatus newStatus = newDoneStatus ? Task.TaskStatus.DONE : Task.TaskStatus.TO_DO;
        
        // Create updated task with new status (Task is immutable)
        final Task updatedTask = new Task(
            task.getId(),
            task.getProjectId(),
            task.getBoardId(),
            task.getTitle(),
            task.getDescription(),
            task.getIssueKey(),
            task.getType(),
            newStatus, // ✅ Changed status
            task.getPriority(),
            task.getPosition(),
            task.getAssigneeId(),
            task.getCreatedBy(),
            task.getSprintId(),
            task.getEpicId(),
            task.getParentTaskId(),
            task.getStartAt(),
            task.getDueAt(),
            task.getStoryPoints(),
            task.getOriginalEstimateSec(),
            task.getRemainingEstimateSec(),
            task.getCreatedAt(),
            task.getUpdatedAt(),
            task.isCalendarSyncEnabled(),
            task.getCalendarReminderMinutes(),
            task.getCalendarEventId(),
            task.getCalendarSyncedAt(),
            task.getLabels()  // preserve labels
        );
        
        // Update in all relevant LiveData
        updateTaskInAllLists(task, t -> updatedTask);
        
        // Update selected task if it's the same
        if (selectedTaskLiveData.getValue() != null && 
            selectedTaskLiveData.getValue().getId().equals(task.getId())) {
            selectedTaskLiveData.setValue(updatedTask);
        }
        
        // Now call API
        updateTaskUseCase.execute(task.getId(), updatedTask, new UpdateTaskUseCase.Callback<Task>() {
            @Override
            public void onSuccess(Task result) {
                // Already updated optimistically, just refresh with server version
                updateTaskInAllLists(task, t -> result);
                if (selectedTaskLiveData.getValue() != null && 
                    selectedTaskLiveData.getValue().getId().equals(result.getId())) {
                    selectedTaskLiveData.setValue(result);
                }
            }

            @Override
            public void onError(String error) {
                errorLiveData.setValue(error);
                
                // ✅ Rollback: Restore original task
                updateTaskInAllLists(task, t -> task);
                if (selectedTaskLiveData.getValue() != null && 
                    selectedTaskLiveData.getValue().getId().equals(task.getId())) {
                    selectedTaskLiveData.setValue(task);
                }
            }
        });
    }
    
    /**
     * Helper: Update task in all LiveData lists
     */
    private void updateTaskInAllLists(Task task, TaskUpdater updater) {
        String taskId = task.getId();
        
        // Update in board tasks
        String boardId = task.getBoardId();
        if (boardId != null && !boardId.isEmpty()) {
            MutableLiveData<List<Task>> boardTasks = tasksPerBoardMap.get(boardId);
            if (boardTasks != null && boardTasks.getValue() != null) {
                List<Task> updated = new ArrayList<>();
                for (Task t : boardTasks.getValue()) {
                    if (t.getId().equals(taskId)) {
                        updated.add(updater.update(t));
                    } else {
                        updated.add(t);
                    }
                }
                boardTasks.setValue(updated);
            }
        }
        
        // Update in inbox tasks
        List<Task> inbox = inboxTasksLiveData.getValue();
        if (inbox != null) {
            List<Task> updated = new ArrayList<>();
            for (Task t : inbox) {
                if (t.getId().equals(taskId)) {
                    updated.add(updater.update(t));
                } else {
                    updated.add(t);
                }
            }
            inboxTasksLiveData.setValue(updated);
        }
    }
    
    interface TaskUpdater {
        Task update(Task task);
    }

    /**
     * ✅ ENHANCED: Delete task with optimistic update
     * Instantly removes from UI, then syncs with API
     */
    public void deleteTask(String taskId) {
        errorLiveData.setValue(null);
        Task currentTask = selectedTaskLiveData.getValue();
        final String boardIdToReload = (currentTask != null) ? currentTask.getBoardId() : null;
        
        // ✅ Optimistic update: Remove from UI immediately
        Task deletedTask = null;
        
        // Remove from board tasks
        if (boardIdToReload != null && !boardIdToReload.isEmpty()) {
            MutableLiveData<List<Task>> boardTasks = tasksPerBoardMap.get(boardIdToReload);
            if (boardTasks != null && boardTasks.getValue() != null) {
                List<Task> updated = new ArrayList<>();
                for (Task t : boardTasks.getValue()) {
                    if (t.getId().equals(taskId)) {
                        deletedTask = t; // Save for rollback
                    } else {
                        updated.add(t);
                    }
                }
                boardTasks.setValue(updated);
            }
        }
        
        // Remove from inbox tasks
        List<Task> inbox = inboxTasksLiveData.getValue();
        if (inbox != null) {
            List<Task> updated = new ArrayList<>();
            for (Task t : inbox) {
                if (t.getId().equals(taskId)) {
                    if (deletedTask == null) deletedTask = t;
                } else {
                    updated.add(t);
                }
            }
            inboxTasksLiveData.setValue(updated);
        }
        
        final Task taskToRestore = deletedTask;
        
        // Now call API
        loadingLiveData.setValue(true);
        deleteTaskUseCase.execute(taskId, new DeleteTaskUseCase.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadingLiveData.setValue(false);
                selectedTaskLiveData.setValue(null);
                // Already removed from UI
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
                
                // ✅ Rollback: Restore task
                if (taskToRestore != null) {
                    if (boardIdToReload != null && !boardIdToReload.isEmpty()) {
                        MutableLiveData<List<Task>> boardTasks = tasksPerBoardMap.get(boardIdToReload);
                        if (boardTasks != null && boardTasks.getValue() != null) {
                            List<Task> updated = new ArrayList<>(boardTasks.getValue());
                            updated.add(taskToRestore);
                            boardTasks.setValue(updated);
                        }
                    }
                    
                    List<Task> inbox = inboxTasksLiveData.getValue();
                    if (inbox != null) {
                        List<Task> updated = new ArrayList<>(inbox);
                        updated.add(taskToRestore);
                        inboxTasksLiveData.setValue(updated);
                    }
                }
            }
        });
    }

    public void assignTask(String taskId, String userId) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        assignTaskUseCase.execute(taskId, userId, new AssignTaskUseCase.Callback<Task>() {
            @Override
            public void onSuccess(Task result) {
                loadingLiveData.setValue(false);
                selectedTaskLiveData.setValue(result);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void unassignTask(String taskId) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        unassignTaskUseCase.execute(taskId, new UnassignTaskUseCase.Callback<Task>() {
            @Override
            public void onSuccess(Task result) {
                loadingLiveData.setValue(false);
                selectedTaskLiveData.setValue(result);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    /**
     * ✅ SIMPLIFIED: Move task to board (for drag & drop in ProjectActivity)
     * NO optimistic update here - ProjectViewModel handles it
     * This only calls API and emits success/error events
     */
    public void moveTaskToBoard(Task task, String targetBoardId, double position) {
        if (task == null || targetBoardId == null) return;
        
        errorLiveData.setValue(null);
        
        final String sourceBoardId = task.getBoardId();
        final Task originalTask = task;
        
        // ✅ Call UseCase - ProjectViewModel already did optimistic update
        moveTaskToBoardUseCase.execute(
            task.getId(), targetBoardId, position,
            new MoveTaskToBoardUseCase.Callback<Task>() {
                @Override
                public void onSuccess(Task updatedTask) {
                    // Backend confirmed - emit success event
                    taskMovedEventLiveData.setValue(new TaskMovedEvent(
                        task.getId(), sourceBoardId, targetBoardId
                    ));
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "❌ moveTaskToBoard failed: " + error);
                    errorLiveData.setValue(error);
                    
                    // ✅ Emit rollback event with original task data
                    taskMoveFailedEventLiveData.setValue(new TaskMoveFailedEvent(
                        originalTask, sourceBoardId, targetBoardId, error
                    ));
                }
            }
        );
    }

    /**
     * ✅ ENHANCED: Move task to board with optimistic update
     * Instant move from Inbox → Board or Board → Board
     */
    public void moveTaskToBoard(String taskId, String targetBoardId, double position) {
        if (taskId == null || targetBoardId == null) return;
        
        errorLiveData.setValue(null);
        
        // Find task to move
        Task taskToMove = null;
        String sourceBoardId = null;
        boolean fromInbox = false;
        
        // Check all board tasks
        for (Map.Entry<String, MutableLiveData<List<Task>>> entry : tasksPerBoardMap.entrySet()) {
            if (entry.getValue() != null && entry.getValue().getValue() != null) {
                for (Task t : entry.getValue().getValue()) {
                    if (t.getId().equals(taskId)) {
                        taskToMove = t;
                        sourceBoardId = entry.getKey();
                        break;
                    }
                }
            }
            if (taskToMove != null) break;
        }
        
        // Check inbox if not found in boards
        if (taskToMove == null) {
            List<Task> inbox = inboxTasksLiveData.getValue();
            if (inbox != null) {
                for (Task t : inbox) {
                    if (t.getId().equals(taskId)) {
                        taskToMove = t;
                        fromInbox = true;
                        break;
                    }
                }
            }
        }
        
        if (taskToMove == null) {
            errorLiveData.setValue("Task not found");
            return;
        }
        
        final Task originalTask = taskToMove;
        final String originalBoardId = sourceBoardId;
        final boolean wasInInbox = fromInbox;
        
        // Create moved task with new boardId and position (Task is immutable)
        final Task movedTask = new Task(
            originalTask.getId(),
            originalTask.getProjectId(),
            targetBoardId, // ✅ New board
            originalTask.getTitle(),
            originalTask.getDescription(),
            originalTask.getIssueKey(),
            originalTask.getType(),
            originalTask.getStatus(),
            originalTask.getPriority(),
            position, // ✅ New position
            originalTask.getAssigneeId(),
            originalTask.getCreatedBy(),
            originalTask.getSprintId(),
            originalTask.getEpicId(),
            originalTask.getParentTaskId(),
            originalTask.getStartAt(),
            originalTask.getDueAt(),
            originalTask.getStoryPoints(),
            originalTask.getOriginalEstimateSec(),
            originalTask.getRemainingEstimateSec(),
            originalTask.getCreatedAt(),
            originalTask.getUpdatedAt(),
            originalTask.isCalendarSyncEnabled(),
            originalTask.getCalendarReminderMinutes(),
            originalTask.getCalendarEventId(),
            originalTask.getCalendarSyncedAt(),
            originalTask.getLabels()  // preserve labels
        );
        
        // ✅ Optimistic update: Remove from source, add to target immediately
        
        // Remove from source board
        if (originalBoardId != null && !originalBoardId.isEmpty()) {
            MutableLiveData<List<Task>> sourceTasks = tasksPerBoardMap.get(originalBoardId);
            if (sourceTasks != null && sourceTasks.getValue() != null) {
                List<Task> updated = new ArrayList<>();
                for (Task t : sourceTasks.getValue()) {
                    if (!t.getId().equals(taskId)) {
                        updated.add(t);
                    }
                }
                sourceTasks.setValue(updated);
            }
        }
        
        // Remove from inbox if source was inbox
        if (wasInInbox) {
            List<Task> inbox = inboxTasksLiveData.getValue();
            if (inbox != null) {
                List<Task> updated = new ArrayList<>();
                for (Task t : inbox) {
                    if (!t.getId().equals(taskId)) {
                        updated.add(t);
                    }
                }
                inboxTasksLiveData.setValue(updated);
            }
        }
        
        // Add to target board
        MutableLiveData<List<Task>> targetTasks = tasksPerBoardMap.get(targetBoardId);
        if (targetTasks != null && targetTasks.getValue() != null) {
            List<Task> updated = new ArrayList<>(targetTasks.getValue());
            updated.add(movedTask);
            targetTasks.setValue(updated);
        } else {
            // Target board not loaded yet, create new LiveData
            MutableLiveData<List<Task>> newLiveData = new MutableLiveData<>();
            List<Task> newList = new ArrayList<>();
            newList.add(movedTask);
            newLiveData.setValue(newList);
            tasksPerBoardMap.put(targetBoardId, newLiveData);
        }
        
        // Update selected task
        if (selectedTaskLiveData.getValue() != null && 
            selectedTaskLiveData.getValue().getId().equals(taskId)) {
            selectedTaskLiveData.setValue(movedTask);
        }
        
        // Now call API
        loadingLiveData.setValue(true);
        moveTaskToBoardUseCase.execute(taskId, targetBoardId, position, new MoveTaskToBoardUseCase.Callback<Task>() {
            @Override
            public void onSuccess(Task result) {
                loadingLiveData.setValue(false);
                
                // Update with server version in target board
                MutableLiveData<List<Task>> targetTasks = tasksPerBoardMap.get(targetBoardId);
                if (targetTasks != null && targetTasks.getValue() != null) {
                    List<Task> updated = new ArrayList<>();
                    for (Task t : targetTasks.getValue()) {
                        if (t.getId().equals(taskId)) {
                            updated.add(result); // Replace with server version
                        } else {
                            updated.add(t);
                        }
                    }
                    targetTasks.setValue(updated);
                }
                
                selectedTaskLiveData.setValue(result);
                
                // ✅ Emit event for UI to refresh boards if needed
                taskMovedEventLiveData.setValue(new TaskMovedEvent(taskId, originalBoardId, targetBoardId));
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
                
                // ✅ Rollback: Restore to original location
                
                // Remove from target board
                MutableLiveData<List<Task>> targetTasks = tasksPerBoardMap.get(targetBoardId);
                if (targetTasks != null && targetTasks.getValue() != null) {
                    List<Task> updated = new ArrayList<>();
                    for (Task t : targetTasks.getValue()) {
                        if (!t.getId().equals(taskId)) {
                            updated.add(t);
                        }
                    }
                    targetTasks.setValue(updated);
                }
                
                // Restore to source board
                if (originalBoardId != null && !originalBoardId.isEmpty()) {
                    MutableLiveData<List<Task>> sourceTasks = tasksPerBoardMap.get(originalBoardId);
                    if (sourceTasks != null && sourceTasks.getValue() != null) {
                        List<Task> updated = new ArrayList<>(sourceTasks.getValue());
                        updated.add(originalTask);
                        sourceTasks.setValue(updated);
                    }
                }
                
                // Restore to inbox if it was from inbox
                if (wasInInbox) {
                    List<Task> inbox = inboxTasksLiveData.getValue();
                    if (inbox != null) {
                        List<Task> updated = new ArrayList<>(inbox);
                        updated.add(originalTask);
                        inboxTasksLiveData.setValue(updated);
                    }
                }
                
                // Restore selected task
                if (selectedTaskLiveData.getValue() != null && 
                    selectedTaskLiveData.getValue().getId().equals(taskId)) {
                    selectedTaskLiveData.setValue(originalTask);
                }
            }
        });
    }

    public void updateTaskPosition(String taskId, double newPosition) {
        errorLiveData.setValue(null);

        updateTaskPositionUseCase.execute(taskId, newPosition, new UpdateTaskPositionUseCase.Callback<Task>() {
            @Override
            public void onSuccess(Task result) {
                selectedTaskLiveData.setValue(result);
                // Task position update should trigger board reload in Activity if needed
            }

            @Override
            public void onError(String error) {
                errorLiveData.setValue(error);
            }
        });
    }

    public void addComment(String taskId, TaskComment comment) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        addCommentUseCase.execute(taskId, comment, new AddCommentUseCase.Callback<TaskComment>() {
            @Override
            public void onSuccess(TaskComment result) {
                loadingLiveData.setValue(false);
                loadTaskComments(taskId);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void loadTaskComments(String taskId) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        getTaskCommentsUseCase.execute(taskId, new GetTaskCommentsUseCase.Callback<List<TaskComment>>() {
            @Override
            public void onSuccess(List<TaskComment> result) {
                loadingLiveData.setValue(false);
                commentsLiveData.setValue(result);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void addAttachment(String taskId, Attachment attachment) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        addAttachmentUseCase.execute(taskId, attachment, new AddAttachmentUseCase.Callback<Attachment>() {
            @Override
            public void onSuccess(Attachment result) {
                loadingLiveData.setValue(false);
                loadTaskAttachments(taskId);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void loadTaskAttachments(String taskId) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        getTaskAttachmentsUseCase.execute(taskId, new GetTaskAttachmentsUseCase.Callback<List<Attachment>>() {
            @Override
            public void onSuccess(List<Attachment> result) {
                loadingLiveData.setValue(false);
                attachmentsLiveData.setValue(result);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void updateComment(String commentId, String body) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        updateCommentUseCase.execute(commentId, body, new UpdateCommentUseCase.Callback<TaskComment>() {
            @Override
            public void onSuccess(TaskComment result) {
                loadingLiveData.setValue(false);
                // Refresh comments to show updated content
                if (result != null && result.getTaskId() != null) {
                    loadTaskComments(result.getTaskId());
                }
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void deleteComment(String commentId, String taskId) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        deleteCommentUseCase.execute(commentId, new DeleteCommentUseCase.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadingLiveData.setValue(false);
                // Refresh comments to remove deleted comment
                loadTaskComments(taskId);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void deleteAttachment(String attachmentId, String taskId) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        deleteAttachmentUseCase.execute(attachmentId, new DeleteAttachmentUseCase.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadingLiveData.setValue(false);
                // Refresh attachments to remove deleted attachment
                loadTaskAttachments(taskId);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void getAttachmentViewUrl(String attachmentId, AttachmentViewUrlCallback callback) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        getAttachmentViewUrlUseCase.execute(attachmentId, new GetAttachmentViewUrlUseCase.Callback<String>() {
            @Override
            public void onSuccess(String viewUrl) {
                loadingLiveData.setValue(false);
                callback.onSuccess(viewUrl);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
                callback.onError(error);
            }
        });
    }

    // Callback interface for attachment view URL
    public interface AttachmentViewUrlCallback {
        void onSuccess(String viewUrl);
        void onError(String error);
    }

    public void addChecklist(String taskId, Checklist checklist) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        addChecklistUseCase.execute(taskId, checklist, new AddChecklistUseCase.Callback<Checklist>() {
            @Override
            public void onSuccess(Checklist result) {
                loadingLiveData.setValue(false);
                loadTaskChecklists(taskId);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void loadTaskChecklists(String taskId) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        getTaskChecklistsUseCase.execute(taskId, new GetTaskChecklistsUseCase.Callback<List<Checklist>>() {
            @Override
            public void onSuccess(List<Checklist> result) {
                loadingLiveData.setValue(false);
                checklistsLiveData.setValue(result);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void clearError() {
        errorLiveData.setValue(null);
    }

    // ===== ChecklistItem Methods =====
    
    public void loadChecklistItems(String taskId) {
        loadingLiveData.setValue(true);
        
        android.util.Log.d("TaskViewModel", "🔍 Loading checklist items for task: " + taskId);
        
        // Load checklists and extract all items
        repository.getChecklists(taskId, new ITaskRepository.RepositoryCallback<List<com.example.tralalero.domain.model.Checklist>>() {
            @Override
            public void onSuccess(List<com.example.tralalero.domain.model.Checklist> checklists) {
                android.util.Log.d("TaskViewModel", "✅ Received " + (checklists != null ? checklists.size() : 0) + " checklists");
                
                java.util.List<com.example.tralalero.domain.model.ChecklistItem> allItems = new java.util.ArrayList<>();
                
                // Collect all items from all checklists
                if (checklists != null) {
                    for (com.example.tralalero.domain.model.Checklist checklist : checklists) {
                        android.util.Log.d("TaskViewModel", "📋 Checklist: " + checklist.getTitle() + 
                            " has " + (checklist.getItems() != null ? checklist.getItems().size() : 0) + " items");
                        
                        if (checklist.getItems() != null) {
                            allItems.addAll(checklist.getItems());
                        }
                    }
                }
                
                android.util.Log.d("TaskViewModel", "📊 Total items collected: " + allItems.size());
                
                checklistItemsLiveData.postValue(allItems);
                loadingLiveData.postValue(false);
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("TaskViewModel", "❌ Failed to load checklist items: " + error);
                loadingLiveData.postValue(false);
                errorLiveData.postValue("Failed to load checklist items: " + error);
            }
        });
    }
    
    public void addChecklistItem(String taskId, com.example.tralalero.domain.model.ChecklistItem item) {
        loadingLiveData.setValue(true);
        
        // Load checklists first to get the checklist ID
        repository.getChecklists(taskId, new ITaskRepository.RepositoryCallback<List<com.example.tralalero.domain.model.Checklist>>() {
            @Override
            public void onSuccess(List<com.example.tralalero.domain.model.Checklist> checklists) {
                if (checklists != null && !checklists.isEmpty()) {
                    // Use first checklist (or create logic to select correct one)
                    String checklistId = checklists.get(0).getId();
                    
                    // Now add item to this checklist
                    repository.addChecklistItem(checklistId, item, new ITaskRepository.RepositoryCallback<com.example.tralalero.domain.model.ChecklistItem>() {
                        @Override
                        public void onSuccess(com.example.tralalero.domain.model.ChecklistItem newItem) {
                            // Reload all checklist items to refresh UI
                            loadChecklistItems(taskId);
                            loadingLiveData.postValue(false);
                        }

                        @Override
                        public void onError(String error) {
                            loadingLiveData.postValue(false);
                            errorLiveData.postValue("Failed to add checklist item: " + error);
                        }
                    });
                } else {
                    // No checklist exists, need to create one first
                    loadingLiveData.postValue(false);
                    errorLiveData.postValue("No checklist found. Creating default checklist...");
                    
                    // Auto-create a default checklist
                    com.example.tralalero.domain.model.Checklist defaultChecklist = 
                        new com.example.tralalero.domain.model.Checklist(
                            null, taskId, "Checklist", null, null
                        );
                    
                    repository.addChecklist(taskId, defaultChecklist, new ITaskRepository.RepositoryCallback<com.example.tralalero.domain.model.Checklist>() {
                        @Override
                        public void onSuccess(com.example.tralalero.domain.model.Checklist checklist) {
                            // Now add the item to this new checklist
                            addChecklistItem(taskId, item);
                        }

                        @Override
                        public void onError(String error) {
                            loadingLiveData.postValue(false);
                            errorLiveData.postValue("Failed to create checklist: " + error);
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                loadingLiveData.postValue(false);
                errorLiveData.postValue("Failed to load checklists: " + error);
            }
        });
    }
    
    public void updateChecklistItem(String taskId, String itemId, boolean isDone) {
        loadingLiveData.setValue(true);
        
        // Call toggle API
        repository.toggleChecklistItem(itemId, new ITaskRepository.RepositoryCallback<com.example.tralalero.domain.model.ChecklistItem>() {
            @Override
            public void onSuccess(com.example.tralalero.domain.model.ChecklistItem updatedItem) {
                // Reload checklist items to reflect the change
                loadChecklistItems(taskId);
                loadingLiveData.postValue(false);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.postValue(false);
                errorLiveData.postValue("Failed to update checklist item: " + error);
            }
        });
    }
    
    public void deleteChecklistItem(String taskId, String itemId) {
        loadingLiveData.setValue(true);
        
        repository.deleteChecklistItem(itemId, new ITaskRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Reload checklist items to reflect the deletion
                loadChecklistItems(taskId);
                loadingLiveData.postValue(false);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.postValue(false);
                errorLiveData.postValue("Failed to delete checklist item: " + error);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}