package com.example.tralalero.domain.repository;

import com.example.tralalero.domain.model.Task;
import com.example.tralalero.domain.model.Attachment;
import com.example.tralalero.domain.model.TaskComment;
import com.example.tralalero.domain.model.Checklist;
import com.example.tralalero.domain.model.Label;

import java.util.List;

public interface ITaskRepository {
    // CRUD operations for Task
    void getTaskById(String taskId, RepositoryCallback<Task> callback);

    void getTasksByBoard(String boardId, RepositoryCallback<List<Task>> callback);

    void getTasksByProject(String projectId, RepositoryCallback<List<Task>> callback);

    void createTask(String boardId, Task task, RepositoryCallback<Task> callback);

    void updateTask(String taskId, Task task, RepositoryCallback<Task> callback);

    void deleteTask(String taskId, RepositoryCallback<Void> callback);

    // Task movement and ordering
    void moveTaskToBoard(String taskId, String targetBoardId, double position, RepositoryCallback<Task> callback);

    void updateTaskPosition(String taskId, double newPosition, RepositoryCallback<Task> callback);

    // Task assignments
    void assignTask(String taskId, String userId, RepositoryCallback<Task> callback);

    void unassignTask(String taskId, RepositoryCallback<Task> callback);

    // Attachments
    void getAttachments(String taskId, RepositoryCallback<List<Attachment>> callback);

    void addAttachment(String taskId, Attachment attachment, RepositoryCallback<Attachment> callback);

    void deleteAttachment(String attachmentId, RepositoryCallback<Void> callback);

    // Comments
    void getComments(String taskId, RepositoryCallback<List<TaskComment>> callback);

    void addComment(String taskId, TaskComment comment, RepositoryCallback<TaskComment> callback);

    void updateComment(String commentId, String body, RepositoryCallback<TaskComment> callback);

    void deleteComment(String commentId, RepositoryCallback<Void> callback);

    // Checklists
    void getChecklists(String taskId, RepositoryCallback<List<Checklist>> callback);

    void addChecklist(String taskId, Checklist checklist, RepositoryCallback<Checklist> callback);

    void updateChecklist(String checklistId, Checklist checklist, RepositoryCallback<Checklist> callback);

    void deleteChecklist(String checklistId, RepositoryCallback<Void> callback);

    // Labels
    void addLabel(String taskId, String labelId, RepositoryCallback<Void> callback);

    void removeLabel(String taskId, String labelId, RepositoryCallback<Void> callback);

    interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}
