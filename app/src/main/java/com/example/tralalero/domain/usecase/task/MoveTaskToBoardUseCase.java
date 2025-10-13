package com.example.tralalero.domain.usecase.task;

import com.example.tralalero.domain.model.Task;
import com.example.tralalero.domain.repository.ITaskRepository;

/**
 * UseCase: Move task to another board
 *
 * Matches Backend: POST /api/tasks/:id/move
 *
 * Input: String taskId, String targetBoardId, double position
 * Output: Task (moved)
 *
 * Business Logic:
 * - Validate taskId and targetBoardId
 * - Position is calculated by caller (UI or another UseCase)
 */
public class MoveTaskToBoardUseCase {

    private final ITaskRepository taskRepository;

    public MoveTaskToBoardUseCase(ITaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Move task to another board with specified position
     *
     * @param taskId Task ID to move (UUID format)
     * @param targetBoardId Target board ID (UUID format)
     * @param position New position in target board
     * @param callback Callback to receive result
     */
    public void execute(String taskId, String targetBoardId, double position,
                        Callback<Task> callback) {
        // Validate task ID
        if (taskId == null || taskId.trim().isEmpty()) {
            callback.onError("Task ID cannot be empty");
            return;
        }

        if (!isValidUUID(taskId)) {
            callback.onError("Invalid task ID format");
            return;
        }

        // Validate target board ID
        if (targetBoardId == null || targetBoardId.trim().isEmpty()) {
            callback.onError("Target board ID cannot be empty");
            return;
        }

        if (!isValidUUID(targetBoardId)) {
            callback.onError("Invalid target board ID format");
            return;
        }

        // Validate position
        if (position < 0) {
            callback.onError("Position cannot be negative");
            return;
        }

        // Call repository - matches signature exactly
        taskRepository.moveTaskToBoard(taskId, targetBoardId, position,
                new ITaskRepository.RepositoryCallback<Task>() {
                    @Override
                    public void onSuccess(Task result) {
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
     * Helper: Calculate position between two tasks
     *
     * @param beforePosition Position of task before (or 0 if at start)
     * @param afterPosition Position of task after (or Double.MAX_VALUE if at end)
     * @return Calculated middle position
     */
    public static double calculatePosition(double beforePosition, double afterPosition) {
        return (beforePosition + afterPosition) / 2.0;
    }

    /**
     * Validate UUID format
     */
    private boolean isValidUUID(String uuid) {
        return uuid.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    }

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}