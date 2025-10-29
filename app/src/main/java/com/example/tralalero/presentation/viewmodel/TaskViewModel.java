package com.example.tralalero.presentation.viewmodel;

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
import com.example.tralalero.domain.repository.ITaskRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskViewModel extends ViewModel {

    private final GetTaskByIdUseCase getTaskByIdUseCase;
    private final GetTasksByBoardUseCase getTasksByBoardUseCase;
    private final CreateTaskUseCase createTaskUseCase;
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
    private final ITaskRepository repository; // For checklist item operations
    private final Map<String, MutableLiveData<List<Task>>> tasksPerBoardMap = new HashMap<>();
    private final MutableLiveData<List<Task>> tasksLiveData = new MutableLiveData<>();
    private final MutableLiveData<Task> selectedTaskLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<TaskComment>> commentsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Attachment>> attachmentsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Checklist>> checklistsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<com.example.tralalero.domain.model.ChecklistItem>> checklistItemsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public TaskViewModel(
            GetTaskByIdUseCase getTaskByIdUseCase,
            GetTasksByBoardUseCase getTasksByBoardUseCase,
            CreateTaskUseCase createTaskUseCase,
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
            ITaskRepository repository // Add repository for checklist item operations
    ) {
        this.getTaskByIdUseCase = getTaskByIdUseCase;
        this.getTasksByBoardUseCase = getTasksByBoardUseCase;
        this.createTaskUseCase = createTaskUseCase;
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

    public void createTask(Task task) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        createTaskUseCase.execute(task, new CreateTaskUseCase.Callback<Task>() {
            @Override
            public void onSuccess(Task result) {
                loadingLiveData.setValue(false);
                selectedTaskLiveData.setValue(result);
                if (result.getBoardId() != null && !result.getBoardId().isEmpty()) {
                    loadTasksByBoard(result.getBoardId());
                }
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void updateTask(String taskId, Task task) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        updateTaskUseCase.execute(taskId, task, new UpdateTaskUseCase.Callback<Task>() {
            @Override
            public void onSuccess(Task result) {
                loadingLiveData.setValue(false);
                selectedTaskLiveData.setValue(result);
                if (result.getBoardId() != null && !result.getBoardId().isEmpty()) {
                    loadTasksByBoard(result.getBoardId());
                }
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void deleteTask(String taskId) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);
        Task currentTask = selectedTaskLiveData.getValue();
        final String boardIdToReload = (currentTask != null) ? currentTask.getBoardId() : null;

        deleteTaskUseCase.execute(taskId, new DeleteTaskUseCase.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadingLiveData.setValue(false);
                selectedTaskLiveData.setValue(null);
                if (boardIdToReload != null && !boardIdToReload.isEmpty()) {
                    loadTasksByBoard(boardIdToReload);
                }
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
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

    public void moveTaskToBoard(String taskId, String targetBoardId, double position) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        moveTaskToBoardUseCase.execute(taskId, targetBoardId, position, new MoveTaskToBoardUseCase.Callback<Task>() {
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

    public void updateTaskPosition(String taskId, double newPosition) {
        errorLiveData.setValue(null);

        updateTaskPositionUseCase.execute(taskId, newPosition, new UpdateTaskPositionUseCase.Callback<Task>() {
            @Override
            public void onSuccess(Task result) {
                selectedTaskLiveData.setValue(result);
                if (result.getBoardId() != null && !result.getBoardId().isEmpty()) {
                    loadTasksByBoard(result.getBoardId());
                }
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