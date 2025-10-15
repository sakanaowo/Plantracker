package com.example.tralalero.domain.usecase.label;

import com.example.tralalero.domain.model.Label;
import com.example.tralalero.domain.repository.ILabelRepository;

import java.util.List;


public class GetLabelsByWorkspaceUseCase {

    private final ILabelRepository repository;

    public GetLabelsByWorkspaceUseCase(ILabelRepository repository) {
        this.repository = repository;
    }

 
    public void execute(String workspaceId, Callback<List<Label>> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        if (workspaceId == null || workspaceId.trim().isEmpty()) {
            callback.onError("Workspace ID cannot be empty");
            return;
        }

        if (!isValidUUID(workspaceId)) {
            callback.onError("Invalid workspace ID format");
            return;
        }

        repository.getLabelsByWorkspace(workspaceId, new ILabelRepository.RepositoryCallback<List<Label>>() {
            @Override
            public void onSuccess(List<Label> result) {
                callback.onSuccess(result);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }


    private boolean isValidUUID(String uuid) {
        if (uuid == null) return false;
        String uuidPattern = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
        return uuid.matches(uuidPattern);
    }


    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}

