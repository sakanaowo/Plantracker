package com.example.tralalero.domain.usecase.workspace;

import com.example.tralalero.domain.repository.IWorkspaceRepository;

/**
 * UseCase: Delete a workspace
 *
 * Input: String workspaceId
 * Output: Void (success/failure)
 *
 * Business Logic:
 * - Validate workspace ID is not empty
 * - Delete workspace and all associated data (projects, boards, tasks)
 * - Only workspace owner can delete
 */
public class DeleteWorkspaceUseCase {

    private final IWorkspaceRepository repository;

    public DeleteWorkspaceUseCase(IWorkspaceRepository repository) {
        this.repository = repository;
    }

    /**
     * Execute the use case to delete a workspace
     *
     * @param workspaceId The ID of the workspace to delete
     * @param callback Callback to receive the result
     */
    public void execute(String workspaceId, Callback<Void> callback) {
        // Validate callback
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        // Validate workspace ID
        if (workspaceId == null || workspaceId.trim().isEmpty()) {
            callback.onError("Workspace ID cannot be empty");
            return;
        }

        // Call repository to delete workspace
        repository.deleteWorkspace(workspaceId, new IWorkspaceRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
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

