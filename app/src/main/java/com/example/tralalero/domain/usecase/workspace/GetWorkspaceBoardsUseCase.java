package com.example.tralalero.domain.usecase.workspace;

import com.example.tralalero.domain.model.Board;
import com.example.tralalero.domain.repository.IWorkspaceRepository;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * UseCase: Get all boards in a project
 *
 * Input: String projectId
 * Output: List<Board>
 *
 * Business Logic:
 * - Validate projectId is not null or empty
 * - Retrieve all boards in the project
 * - Sort boards by order ascending
 */
public class GetWorkspaceBoardsUseCase {

    private final IWorkspaceRepository repository;

    public GetWorkspaceBoardsUseCase(IWorkspaceRepository repository) {
        this.repository = repository;
    }

    /**
     * Execute the use case to get all boards in project
     *
     * @param projectId The project ID
     * @param callback Callback to receive the result
     */
    public void execute(String projectId, Callback<List<Board>> callback) {
        // Validate callback
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        // Validate project ID
        if (projectId == null || projectId.trim().isEmpty()) {
            callback.onError("Project ID cannot be empty");
            return;
        }

        repository.getBoards(projectId, new IWorkspaceRepository.RepositoryCallback<List<Board>>() {
            @Override
            public void onSuccess(List<Board> result) {
                // Sort by order ascending
                if (result != null && !result.isEmpty()) {
                    Collections.sort(result, new Comparator<Board>() {
                        @Override
                        public int compare(Board b1, Board b2) {
                            return Integer.compare(b1.getOrder(), b2.getOrder());
                        }
                    });
                }
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