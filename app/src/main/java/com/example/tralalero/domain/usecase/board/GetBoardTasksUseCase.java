package com.example.tralalero.domain.usecase.board;

import com.example.tralalero.domain.model.Task;
import com.example.tralalero.domain.repository.ITaskRepository;

import java.util.List;

/**
 * UseCase: Get all tasks in a board
 *
 * Matches Backend: GET /api/boards/:boardId/tasks
 *
 * Input: String boardId
 * Output: List<Task>
 *
 * Business Logic:
 * - Retrieve all tasks in a specific board
 * - Sorted by position (for Kanban columns)
 * - Used for board view display
 */
public class GetBoardTasksUseCase {

    private final ITaskRepository taskRepository;

    public GetBoardTasksUseCase(ITaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Execute: Get board tasks
     *
     * @param boardId Board ID to get tasks from
     * @param callback Callback to receive result
     */
    public void execute(String boardId, Callback<List<Task>> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        // Validate board ID
        if (boardId == null || boardId.trim().isEmpty()) {
            callback.onError("Board ID cannot be empty");
            return;
        }

        if (!isValidUUID(boardId)) {
            callback.onError("Invalid board ID format");
            return;
        }

        taskRepository.getTasksByBoard(boardId, new ITaskRepository.RepositoryCallback<List<Task>>() {
            @Override
            public void onSuccess(List<Task> result) {
                callback.onSuccess(result);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    /**
     * Validate UUID format
     */
    private boolean isValidUUID(String uuid) {
        if (uuid == null) return false;
        String uuidPattern = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
        return uuid.matches(uuidPattern);
    }

    /**
     * Callback interface for use case result
     */
    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}

