package com.example.tralalero.domain.usecase.workspace;

import com.example.tralalero.domain.model.Workspace;
import com.example.tralalero.domain.repository.IWorkspaceRepository;

/**
 * UseCase: Update an existing workspace
 *
 * Input: String workspaceId, Workspace updatedWorkspace
 * Output: Workspace (updated)
 *
 * Business Logic:
 * - Validate workspace ID is not empty
 * - Validate workspace name is not empty (if provided)
 * - Validate name length does not exceed 100 characters
 * - Validate workspace type is PERSONAL or TEAM (if provided)
 * - Trim whitespace from name
 */
public class UpdateWorkspaceUseCase {

    private static final int MAX_NAME_LENGTH = 100;
    private static final String TYPE_PERSONAL = "PERSONAL";
    private static final String TYPE_TEAM = "TEAM";

    private final IWorkspaceRepository repository;

    public UpdateWorkspaceUseCase(IWorkspaceRepository repository) {
        this.repository = repository;
    }

    /**
     * Execute the use case to update an existing workspace
     *
     * @param workspaceId The ID of the workspace to update
     * @param updatedWorkspace The workspace object with updated data
     * @param callback Callback to receive the result
     */
    public void execute(String workspaceId, Workspace updatedWorkspace, Callback<Workspace> callback) {
        // Validate callback
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        // Validate workspace ID
        if (workspaceId == null || workspaceId.trim().isEmpty()) {
            callback.onError("Workspace ID cannot be empty");
            return;
        }

        // Validate workspace object
        if (updatedWorkspace == null) {
            callback.onError("Updated workspace data cannot be null");
            return;
        }

        // Validate workspace name if provided
        String name = updatedWorkspace.getName();
        if (name != null) {
            String trimmedName = name.trim();
            if (trimmedName.isEmpty()) {
                callback.onError("Workspace name cannot be empty");
                return;
            }

            if (trimmedName.length() > MAX_NAME_LENGTH) {
                callback.onError("Workspace name cannot exceed " + MAX_NAME_LENGTH + " characters");
                return;
            }
        }

        // Validate workspace type if provided
        String type = updatedWorkspace.getType();
        if (type != null) {
            String upperType = type.toUpperCase();
            if (!TYPE_PERSONAL.equals(upperType) && !TYPE_TEAM.equals(upperType)) {
                callback.onError("Workspace type must be PERSONAL or TEAM");
                return;
            }
        }

        // Call repository to update workspace
        repository.updateWorkspace(workspaceId, updatedWorkspace, new IWorkspaceRepository.RepositoryCallback<Workspace>() {
            @Override
            public void onSuccess(Workspace result) {
                callback.onSuccess(result);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    /**
     * Callback interface for async result
     */
    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}

