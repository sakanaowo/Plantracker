package com.example.tralalero.domain.usecase.label;

import com.example.tralalero.domain.model.Label;
import com.example.tralalero.domain.repository.ILabelRepository;

import java.util.List;

/**
 * UseCase: Get all labels in a workspace
 *
 * Matches Backend: GET /api/workspaces/:workspaceId/labels
 *
 * Input: String workspaceId
 * Output: List<Label>
 *
 * Business Logic:
 * - Retrieve all labels for a workspace
 * - Used for label picker in task editor
 * - Sorted by name
 */
public class GetLabelsByWorkspaceUseCase {

    private final ILabelRepository repository;

    public GetLabelsByWorkspaceUseCase(ILabelRepository repository) {
        this.repository = repository;
    }

    /**
     * Execute: Get labels by workspace
     *
     * @param workspaceId Workspace ID to get labels from
     * @param callback Callback to receive result
     */
    public void execute(String workspaceId, Callback<List<Label>> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        // Validate workspace ID
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

    /**
     * Validate UUID format
     */
    private boolean isValidUUID(String uuid) {
        if (uuid == null) return false;
        String uuidPattern = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
        return uuid.matches(uuidPattern);
    }

    /**
     * Callback interface for use case result
     */
    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}

