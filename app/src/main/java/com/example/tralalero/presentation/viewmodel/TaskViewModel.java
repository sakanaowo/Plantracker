package com.example.tralalero.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

// Import domain models
import com.example.tralalero.domain.model.Task;
import com.example.tralalero.domain.model.TaskComment;
import com.example.tralalero.domain.model.Attachment;
import com.example.tralalero.domain.model.Checklist;

// Import UseCases (15 UseCases)
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

import java.util.List;

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

    private final MutableLiveData<List<Task>> tasksLiveData = new MutableLiveData<>();
    private final MutableLiveData<Task> selectedTaskLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<TaskComment>> commentsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Attachment>> attachmentsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Checklist>> checklistsLiveData = new MutableLiveData<>();
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
            GetTaskChecklistsUseCase getTaskChecklistsUseCase
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
    }

    public LiveData<List<Task>> getTasks() {
        return tasksLiveData;
    }

    public LiveData<Task> getSelectedTask() {
        return selectedTaskLiveData;
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
                // Optionally reload board tasks
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

        deleteTaskUseCase.execute(taskId, new DeleteTaskUseCase.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadingLiveData.setValue(false);
                selectedTaskLiveData.setValue(null);
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
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        updateTaskPositionUseCase.execute(taskId, newPosition, new UpdateTaskPositionUseCase.Callback<Task>() {
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

    public void addComment(String taskId, TaskComment comment) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        addCommentUseCase.execute(taskId, comment, new AddCommentUseCase.Callback<TaskComment>() {
            @Override
            public void onSuccess(TaskComment result) {
                loadingLiveData.setValue(false);
                // Reload comments
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
                // Reload attachments
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
                // Reload checklists
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

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}