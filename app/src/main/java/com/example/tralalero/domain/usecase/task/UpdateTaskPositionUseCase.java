package com.example.tralalero.domain.usecase.task;

import com.example.tralalero.domain.model.Task;
import com.example.tralalero.domain.repository.ITaskRepository;

/**
 * UseCase: Update task position within the same board
 * 
 * This is a specialized version of MoveTaskToBoardUseCase for same-board reordering
 * 
 * Input: String taskId, double newPosition
 * Output: Task (with updated position)
 * 
 * Business Logic:
 * - Validate taskId
 * - Validate position is non-negative
 * - Calculate position between adjacent tasks
 * - Used for drag & drop reordering in same board
 */
public class UpdateTaskPositionUseCase {
    
    private final ITaskRepository taskRepository;
    
    public UpdateTaskPositionUseCase(ITaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
    
    /**
     * Update task position in the same board
     * 
     * @param taskId Task ID (UUID format)
     * @param newPosition New position value (calculated from beforeId/afterId)
     * @param callback Callback to receive result
     */
    public void execute(String taskId, double newPosition, Callback<Task> callback) {
        // Validate task ID
        if (taskId == null || taskId.trim().isEmpty()) {
            callback.onError("Task ID cannot be empty");
            return;
        }
        
        if (!isValidUUID(taskId)) {
            callback.onError("Invalid task ID format");
            return;
        }
        
        // Validate position
        if (newPosition < 0) {
            callback.onError("Position cannot be negative");
            return;
        }
        
        // Call repository
        taskRepository.updateTaskPosition(taskId, newPosition, 
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
     * Calculate position between two tasks (helper method for UI)
     * 
     * @param beforePosition Position of task before (or 0 if at start)
     * @param afterPosition Position of task after (or MAX if at end)
     * @return Calculated middle position
     */
    public static double calculateMiddlePosition(double beforePosition, double afterPosition) {
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