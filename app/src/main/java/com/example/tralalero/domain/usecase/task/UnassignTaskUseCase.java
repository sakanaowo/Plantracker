package com.example.tralalero.domain.usecase.task;

import com.example.tralalero.domain.model.Task;
import com.example.tralalero.domain.repository.ITaskRepository;

/**
 * UseCase: Unassign task from current assignee
 * 
 * Matches Backend: PATCH /api/tasks/:id (with assignee_id = null)
 * 
 * Input: String taskId
 * Output: Task (with assignee_id = null)
 * 
 * Business Logic:
 * - Validate taskId
 * - Set assignee_id to null
 */
public class UnassignTaskUseCase {
    
    private final ITaskRepository taskRepository;
    
    public UnassignTaskUseCase(ITaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
    
    /**
     * Unassign task from current assignee
     * 
     * @param taskId Task ID to unassign (UUID format)
     * @param callback Callback to receive result
     */
    public void execute(String taskId, Callback<Task> callback) {
        // Validate task ID
        if (taskId == null || taskId.trim().isEmpty()) {
            callback.onError("Task ID cannot be empty");
            return;
        }
        
        if (!isValidUUID(taskId)) {
            callback.onError("Invalid task ID format");
            return;
        }
        
        // Call repository - sets assignee_id to null
        taskRepository.unassignTask(taskId, 
            new ITaskRepository.RepositoryCallback<Task>() {
                @Override
                public void onSuccess(Task result) {
                    // Verify task is unassigned
                    if (result.getAssigneeId() != null) {
                        callback.onError("Failed to unassign task");
                        return;
                    }
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