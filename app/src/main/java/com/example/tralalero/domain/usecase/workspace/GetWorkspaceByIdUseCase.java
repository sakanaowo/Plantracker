package com.example.tralalero.domain.usecase.workspace;

import com.example.tralalero.domain.model.Workspace;
import com.example.tralalero.domain.repository.IWorkspaceRepository;

/**
 * UseCase: Get workspace details by ID
 *
 * Input: String workspaceId
 * Output: Workspace
 *
 * Business Logic:
 * - Validate workspaceId is not null or empty
 * - Retrieve workspace details from repository
 */
public class GetWorkspaceByIdUseCase {

    private final IWorkspaceRepository repository;

    public GetWorkspaceByIdUseCase(IWorkspaceRepository repository) {
        this.repository = repository;
    }

    /**
     * Execute the use case to get workspace by ID
     *
     * @param workspaceId The workspace ID to retrieve
     * @param callback Callback to receive the result
     */
    public void execute(String workspaceId, Callback<Workspace> callback) {
        // Validate callback
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        // Validate workspace ID
        if (workspaceId == null || workspaceId.trim().isEmpty()) {
            callback.onError("Workspace ID cannot be empty");
            return;
        }

        repository.getWorkspaceById(workspaceId, new IWorkspaceRepository.RepositoryCallback<Workspace>() {
            @Override
            public void onSuccess(Workspace result) {
                if (result == null) {
                    callback.onError("Workspace not found");
                } else {
                    callback.onSuccess(result);
                }
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