package com.example.tralalero.domain.usecase.project;

import com.example.tralalero.domain.model.Project;
import com.example.tralalero.domain.repository.IProjectRepository;

/**
 * UseCase: Get project details by ID
 *
 * Input: String projectId
 * Output: Project
 *
 * Business Logic:
 * - Validate projectId is not null or empty
 * - Retrieve project details from repository
 */
public class GetProjectByIdUseCase {

    private final IProjectRepository repository;

    public GetProjectByIdUseCase(IProjectRepository repository) {
        this.repository = repository;
    }

    /**
     * Execute the use case to get project by ID
     *
     * @param projectId The project ID to retrieve
     * @param callback Callback to receive the result
     */
    public void execute(String projectId, Callback<Project> callback) {
        // Validate callback
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        // Validate project ID
        if (projectId == null || projectId.trim().isEmpty()) {
            callback.onError("Project ID cannot be empty");
            return;
        }

        repository.getProjectById(projectId, new IProjectRepository.RepositoryCallback<Project>() {
            @Override
            public void onSuccess(Project result) {
                if (result == null) {
                    callback.onError("Project not found");
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