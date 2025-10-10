package com.example.tralalero.domain.usecase.workspace;

import com.example.tralalero.domain.model.Project;
import com.example.tralalero.domain.repository.IWorkspaceRepository;

import java.util.List;

/**
 * UseCase: Get all projects in a workspace
 *
 * Input: String workspaceId
 * Output: List<Project>
 *
 * Business Logic:
 * - Validate workspaceId is not null or empty
 * - Retrieve all projects in the workspace
 * - Projects are returned as-is from repository
 */
public class GetWorkspaceProjectsUseCase {

    private final IWorkspaceRepository repository;

    public GetWorkspaceProjectsUseCase(IWorkspaceRepository repository) {
        this.repository = repository;
    }

    /**
     * Execute the use case to get all projects in workspace
     *
     * @param workspaceId The workspace ID
     * @param callback Callback to receive the result
     */
    public void execute(String workspaceId, Callback<List<Project>> callback) {
        // Validate callback
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        // Validate workspace ID
        if (workspaceId == null || workspaceId.trim().isEmpty()) {
            callback.onError("Workspace ID cannot be empty");
            return;
        }

        repository.getProjects(workspaceId, new IWorkspaceRepository.RepositoryCallback<List<Project>>() {
            @Override
            public void onSuccess(List<Project> result) {
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