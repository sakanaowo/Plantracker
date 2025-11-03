package com.example.tralalero.presentation.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.tralalero.domain.usecase.task.AddAttachmentUseCase;
import com.example.tralalero.domain.usecase.task.AddChecklistUseCase;
import com.example.tralalero.domain.usecase.task.AddCommentUseCase;
import com.example.tralalero.domain.usecase.task.AssignTaskUseCase;
import com.example.tralalero.domain.usecase.task.CreateTaskUseCase;
import com.example.tralalero.domain.usecase.task.DeleteTaskUseCase;
import com.example.tralalero.domain.usecase.task.GetTaskAttachmentsUseCase;
import com.example.tralalero.domain.usecase.task.GetTaskByIdUseCase;
import com.example.tralalero.domain.usecase.task.GetTaskChecklistsUseCase;
import com.example.tralalero.domain.usecase.task.GetTaskCommentsUseCase;
import com.example.tralalero.domain.usecase.task.GetTasksByBoardUseCase;
import com.example.tralalero.domain.usecase.task.MoveTaskToBoardUseCase;
import com.example.tralalero.domain.usecase.task.UnassignTaskUseCase;
import com.example.tralalero.domain.usecase.task.UpdateTaskPositionUseCase;
import com.example.tralalero.domain.usecase.task.UpdateTaskUseCase;
import com.example.tralalero.domain.usecase.task.UpdateCommentUseCase;
import com.example.tralalero.domain.usecase.task.DeleteCommentUseCase;
import com.example.tralalero.domain.usecase.task.DeleteAttachmentUseCase;
import com.example.tralalero.domain.usecase.task.GetAttachmentViewUrlUseCase;
import com.example.tralalero.domain.repository.ITaskRepository;

public class TaskViewModelFactory implements ViewModelProvider.Factory {
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
    private final UpdateCommentUseCase updateCommentUseCase;
    private final DeleteCommentUseCase deleteCommentUseCase;
    private final DeleteAttachmentUseCase deleteAttachmentUseCase;
    private final GetAttachmentViewUrlUseCase getAttachmentViewUrlUseCase;
    private final ITaskRepository repository; // For checklist item operations

    public TaskViewModelFactory(
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
            UpdateCommentUseCase updateCommentUseCase,
            DeleteCommentUseCase deleteCommentUseCase,
            DeleteAttachmentUseCase deleteAttachmentUseCase,
            GetAttachmentViewUrlUseCase getAttachmentViewUrlUseCase,
            ITaskRepository repository
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
        this.updateCommentUseCase = updateCommentUseCase;
        this.deleteCommentUseCase = deleteCommentUseCase;
        this.deleteAttachmentUseCase = deleteAttachmentUseCase;
        this.getAttachmentViewUrlUseCase = getAttachmentViewUrlUseCase;
        this.repository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(TaskViewModel.class)) {
            return (T) new TaskViewModel(
                    getTaskByIdUseCase,
                    getTasksByBoardUseCase,
                    createTaskUseCase,
                    updateTaskUseCase,
                    deleteTaskUseCase,
                    assignTaskUseCase,
                    unassignTaskUseCase,
                    moveTaskToBoardUseCase,
                    updateTaskPositionUseCase,
                    addCommentUseCase,
                    getTaskCommentsUseCase,
                    addAttachmentUseCase,
                    getTaskAttachmentsUseCase,
                    addChecklistUseCase,
                    getTaskChecklistsUseCase,
                    updateCommentUseCase,
                    deleteCommentUseCase,
                    deleteAttachmentUseCase,
                    getAttachmentViewUrlUseCase,
                    repository
            );
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }

}
