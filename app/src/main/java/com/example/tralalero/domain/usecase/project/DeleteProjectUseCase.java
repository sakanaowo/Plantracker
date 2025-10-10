package com.example.tralalero.domain.usecase.project;

import com.example.tralalero.domain.repository.IProjectRepository;

/**
 * UseCase: Delete a project
 *
 * Input: String projectId
 * Output: Void
 *
 * Business Logic:
 * - Validate projectId is not null or empty
 * - Delete the project from repository
 * - Note: Confirmation should be handled at UI layer
 */
public class DeleteProjectUseCase {

    private final IProjectRepository repository;

    public DeleteProjectUseCase(IProjectRepository repository) {
        this.repository = repository;
    }

    /**
     * Execute the use case to delete a project
     *
     * @param projectId The project ID to delete
     * @param callback Callback to receive the result
     */
    public void execute(String projectId, Callback<Void> callback) {
        // Validate callback
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        // Validate project ID
        if (projectId == null || projectId.trim().isEmpty()) {
            callback.onError("Project ID cannot be empty");
            return;
        }

        repository.deleteProject(projectId, new IProjectRepository.RepositoryCallback<Void>() {
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
     * Callback interface for use case result
     */
    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}