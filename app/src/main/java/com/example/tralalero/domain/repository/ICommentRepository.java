package com.example.tralalero.domain.repository;

import com.example.tralalero.domain.model.TaskComment;

import java.util.List;

public interface ICommentRepository {
    void getComments(String taskId, ITaskRepository.RepositoryCallback<List<TaskComment>> callback);

    void addComment(String taskId, TaskComment comment, ITaskRepository.RepositoryCallback<TaskComment> callback);

    void updateComment(String commentId, String body, ITaskRepository.RepositoryCallback<TaskComment> callback);

    void deleteComment(String commentId, ITaskRepository.RepositoryCallback<Void> callback);
}
