package com.example.tralalero.domain.usecase.workspace;

import com.example.tralalero.domain.model.Workspace;
import com.example.tralalero.domain.repository.IWorkspaceRepository;

/**
 * UseCase: Create a new workspace
 *
 * Input: Workspace workspace
 * Output: Workspace (created)
 *
 * Business Logic:
 * - Validate workspace name is not empty
 * - Validate name length does not exceed 100 characters
 * - Validate workspace type is PERSONAL or TEAM
 * - Trim whitespace from name
 */
public class CreateWorkspaceUseCase {

    private static final int MAX_NAME_LENGTH = 100;
    private static final String TYPE_PERSONAL = "PERSONAL";
    private static final String TYPE_TEAM = "TEAM";

    private final IWorkspaceRepository repository;

    public CreateWorkspaceUseCase(IWorkspaceRepository repository) {
        this.repository = repository;
    }

    /**
     * Execute the use case to create a new workspace
     *
     * @param workspace The workspace object to create
     * @param callback Callback to receive the result
     */
    public void execute(Workspace workspace, Callback<Workspace> callback) {
        // Validate callback
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        // Validate workspace object
        if (workspace == null) {
            callback.onError("Workspace cannot be null");
            return;
        }

        // Validate workspace name
        String name = workspace.getName();
        if (name == null || name.trim().isEmpty()) {
            callback.onError("Workspace name cannot be empty");
            return;
        }

        // Validate name length
        String trimmedName = name.trim();
        if (trimmedName.length() > MAX_NAME_LENGTH) {
            callback.onError("Workspace name cannot exceed " + MAX_NAME_LENGTH + " characters");
            return;
        }

        // Validate workspace type
        String type = workspace.getType();
        if (type == null || type.trim().isEmpty()) {
            callback.onError("Workspace type cannot be empty");
            return;
        }

        String upperType = type.trim().toUpperCase();
        if (!TYPE_PERSONAL.equals(upperType) && !TYPE_TEAM.equals(upperType)) {
            callback.onError("Workspace type must be PERSONAL or TEAM");
            return;
        }

        // Create workspace with validated values
        Workspace processedWorkspace = new Workspace(
                workspace.getId(),
                trimmedName,
                upperType,
                workspace.getOwnerId()
        );

        repository.createWorkspace(processedWorkspace, new IWorkspaceRepository.RepositoryCallback<Workspace>() {
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
     * Callback interface for use case result
     */
    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}