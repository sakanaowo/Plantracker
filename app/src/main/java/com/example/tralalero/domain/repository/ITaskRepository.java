package com.example.tralalero.domain.repository;

import com.example.tralalero.domain.model.Task;
import com.example.tralalero.domain.model.Attachment;
import com.example.tralalero.domain.model.TaskComment;
import com.example.tralalero.domain.model.Checklist;
import com.example.tralalero.domain.model.Label;

import java.util.List;

public interface ITaskRepository {
    void getTaskById(String taskId, RepositoryCallback<Task> callback);

    void getTasksByBoard(String boardId, RepositoryCallback<List<Task>> callback);

    void getTasksByProject(String projectId, RepositoryCallback<List<Task>> callback);

    /**
     * Get all quick tasks from user's default board (To Do board)
     * Backend finds user's personal workspace, default project, and "To Do" board
     * 
     * @param callback Callback to receive list of quick tasks or error
     */
    void getQuickTasks(RepositoryCallback<List<Task>> callback);

    void createTask(String boardId, Task task, RepositoryCallback<Task> callback);

    /**
     * Create a quick task - automatically assigns to default project/board
     * Backend finds user's personal workspace, default project, and "To Do" board
     * 
     * @param title Task title
     * @param description Optional description (can be null or empty)
     * @param callback Callback to receive created task or error
     */
    void createQuickTask(String title, String description, RepositoryCallback<Task> callback);

    void updateTask(String taskId, Task task, RepositoryCallback<Task> callback);

    void deleteTask(String taskId, RepositoryCallback<Void> callback);

    void moveTaskToBoard(String taskId, String targetBoardId, double position, RepositoryCallback<Task> callback);

    void updateTaskPosition(String taskId, double newPosition, RepositoryCallback<Task> callback);

    void assignTask(String taskId, String userId, RepositoryCallback<Task> callback);

    void unassignTask(String taskId, RepositoryCallback<Task> callback);

    void getAttachments(String taskId, RepositoryCallback<List<Attachment>> callback);

    void addAttachment(String taskId, Attachment attachment, RepositoryCallback<Attachment> callback);

    void deleteAttachment(String attachmentId, RepositoryCallback<Void> callback);

    void getComments(String taskId, RepositoryCallback<List<TaskComment>> callback);

    void addComment(String taskId, TaskComment comment, RepositoryCallback<TaskComment> callback);

    void updateComment(String commentId, String body, RepositoryCallback<TaskComment> callback);

    void deleteComment(String commentId, RepositoryCallback<Void> callback);

    void getChecklists(String taskId, RepositoryCallback<List<Checklist>> callback);

    void addChecklist(String taskId, Checklist checklist, RepositoryCallback<Checklist> callback);

    void updateChecklist(String checklistId, Checklist checklist, RepositoryCallback<Checklist> callback);

    void deleteChecklist(String checklistId, RepositoryCallback<Void> callback);

    void addLabel(String taskId, String labelId, RepositoryCallback<Void> callback);

    void removeLabel(String taskId, String labelId, RepositoryCallback<Void> callback);

    interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}
