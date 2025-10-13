package com.example.tralalero.domain.usecase.task;

import com.example.tralalero.domain.model.Task;
import com.example.tralalero.domain.repository.ITaskRepository;

/**
 * UseCase: Assign task to a user
 * 
 * Matches Backend: PATCH /api/tasks/:id (with assignee_id in body)
 * 
 * Input: String taskId, String userId
 * Output: Task (with updated assignee)
 * 
 * Business Logic:
 * - Validate taskId and userId
 * - Backend validates user exists in workspace
 * - Backend may create notification for assignee
 */
public class AssignTaskUseCase {
    
    private final ITaskRepository taskRepository;
    
    public AssignTaskUseCase(ITaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
    
    /**
     * Assign task to a user
     * 
     * @param taskId Task ID to assign (UUID format)
     * @param userId User ID to assign to (UUID format)
     * @param callback Callback to receive result
     */
    public void execute(String taskId, String userId, Callback<Task> callback) {
        // Validate task ID
        if (taskId == null || taskId.trim().isEmpty()) {
            callback.onError("Task ID cannot be empty");
            return;
        }
        
        if (!isValidUUID(taskId)) {
            callback.onError("Invalid task ID format");
            return;
        }
        
        // Validate user ID
        if (userId == null || userId.trim().isEmpty()) {
            callback.onError("User ID cannot be empty");
            return;
        }
        
        if (!isValidUUID(userId)) {
            callback.onError("Invalid user ID format");
            return;
        }
        
        // Call repository - backend will:
        // 1. Validate user exists in workspace
        // 2. Update task.assignee_id
        // 3. Create notification for assignee (optional)
        taskRepository.assignTask(taskId, userId, 
            new ITaskRepository.RepositoryCallback<Task>() {
                @Override
                public void onSuccess(Task result) {
                    callback.onSuccess(result);
                }
                
                @Override
                public void onError(String error) {
                    // Common errors:
                    // - "Task not found"
                    // - "User not found"
                    // - "User not in workspace"
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