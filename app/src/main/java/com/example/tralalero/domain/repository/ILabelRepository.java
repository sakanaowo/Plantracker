package com.example.tralalero.domain.repository;

import com.example.tralalero.domain.model.Label;

import java.util.List;

public interface ILabelRepository {
    void getLabelsByWorkspace(String workspaceId, RepositoryCallback<List<Label>> callback);
    
    void getLabelsByProject(String projectId, RepositoryCallback<List<Label>> callback);

    void getLabelById(String labelId, RepositoryCallback<Label> callback);

    void createLabel(String workspaceId, Label label, RepositoryCallback<Label> callback);
    
    void createLabelInProject(String projectId, Label label, RepositoryCallback<Label> callback);

    void updateLabel(String labelId, Label label, RepositoryCallback<Label> callback);

    void deleteLabel(String labelId, RepositoryCallback<Void> callback);
    
    // Task label operations
    void getTaskLabels(String taskId, RepositoryCallback<List<Label>> callback);
    
    void assignLabelToTask(String taskId, String labelId, RepositoryCallback<Void> callback);
    
    void removeLabelFromTask(String taskId, String labelId, RepositoryCallback<Void> callback);

    interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}

