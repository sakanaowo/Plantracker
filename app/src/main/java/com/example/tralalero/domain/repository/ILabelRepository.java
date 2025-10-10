package com.example.tralalero.domain.repository;

import com.example.tralalero.domain.model.Label;

import java.util.List;

public interface ILabelRepository {
    // Get labels
    void getLabelsByWorkspace(String workspaceId, RepositoryCallback<List<Label>> callback);

    void getLabelById(String labelId, RepositoryCallback<Label> callback);

    // CRUD operations
    void createLabel(String workspaceId, Label label, RepositoryCallback<Label> callback);

    void updateLabel(String labelId, Label label, RepositoryCallback<Label> callback);

    void deleteLabel(String labelId, RepositoryCallback<Void> callback);

    interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}

