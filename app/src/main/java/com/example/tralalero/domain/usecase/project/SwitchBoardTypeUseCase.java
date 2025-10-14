package com.example.tralalero.domain.usecase.project;

import com.example.tralalero.domain.model.Project;
import com.example.tralalero.domain.repository.IProjectRepository;

/**
 * UseCase: Switch project board type between KANBAN and SCRUM
 *
 * Input: String projectId, String boardType
 * Output: Project (updated)
 *
 * Business Logic:
 * - Validate projectId is not null or empty
 * - Validate boardType is either "KANBAN" or "SCRUM"
 * - Convert boardType to uppercase
 * - Update project board type
 */
public class SwitchBoardTypeUseCase {

    private static final String BOARD_TYPE_KANBAN = "KANBAN";
    private static final String BOARD_TYPE_SCRUM = "SCRUM";

    private final IProjectRepository repository;

    public SwitchBoardTypeUseCase(IProjectRepository repository) {
        this.repository = repository;
    }

    /**
     * Execute the use case to switch board type
     *
     * @param projectId The project ID to update
     * @param boardType The new board type (KANBAN or SCRUM)
     * @param callback Callback to receive the result
     */
    public void execute(String projectId, String boardType, Callback<Project> callback) {
        // Validate callback
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        // Validate project ID
        if (projectId == null || projectId.trim().isEmpty()) {
            callback.onError("Project ID cannot be empty");
            return;
        }

        // Validate board type
        if (boardType == null || boardType.trim().isEmpty()) {
            callback.onError("Board type cannot be empty");
            return;
        }

        String normalizedBoardType = boardType.trim().toUpperCase();

        // Validate board type value
        if (!BOARD_TYPE_KANBAN.equals(normalizedBoardType) &&
                !BOARD_TYPE_SCRUM.equals(normalizedBoardType)) {
            callback.onError("Board type must be either KANBAN or SCRUM");
            return;
        }

        repository.updateBoardType(projectId, normalizedBoardType,
                new IProjectRepository.RepositoryCallback<Project>() {
                    @Override
                    public void onSuccess(Project result) {
                        callback.onSuccess(result);
                    }

                    @Override
                    public void onError(String error) {
                        callback.onError(error);
                    }
                }
        );
    }

    /**
     * Callback interface for use case result
     */
    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}