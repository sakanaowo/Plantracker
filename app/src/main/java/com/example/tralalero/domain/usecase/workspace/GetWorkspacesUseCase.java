package com.example.tralalero.domain.usecase.workspace;

import com.example.tralalero.domain.model.Workspace;
import com.example.tralalero.domain.repository.IWorkspaceRepository;

import java.util.List;

/**
 * UseCase: Get all workspaces for the current user
 *
 * Input: None (uses current user session)
 * Output: List<Workspace>
 *
 * Business Logic:
 * - Retrieve all workspaces the user has access to
 * - No additional filtering or sorting needed (handled by repository)
 */
public class GetWorkspacesUseCase {

    private final IWorkspaceRepository repository;

    public GetWorkspacesUseCase(IWorkspaceRepository repository) {
        this.repository = repository;
    }

    /**
     * Execute the use case to get all workspaces
     *
     * @param callback Callback to receive the result
     */
    public void execute(Callback<List<Workspace>> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        repository.getWorkspaces(new IWorkspaceRepository.RepositoryCallback<List<Workspace>>() {
            @Override
            public void onSuccess(List<Workspace> result) {
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