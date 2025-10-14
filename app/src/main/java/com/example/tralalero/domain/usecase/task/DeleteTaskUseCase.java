package com.example.tralalero.domain.usecase.task;

import com.example.tralalero.domain.repository.ITaskRepository;

/**
 * UseCase: Delete task (soft delete)
 * 
 * Matches Backend: DELETE /api/tasks/:id
 * Backend: Sets deletedAt timestamp (soft delete)
 * 
 * Input: String taskId
 * Output: Void (success/error)
 */
public class DeleteTaskUseCase {
    
    private final ITaskRepository taskRepository;
    
    public DeleteTaskUseCase(ITaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
    
    /**
     * Delete a task (soft delete)
     * 
     * @param taskId Task ID to delete (UUID format)
     * @param callback Callback to receive result
     */
    public void execute(String taskId, Callback<Void> callback) {
        // Validate input
        if (taskId == null || taskId.trim().isEmpty()) {
            callback.onError("Task ID cannot be empty");
            return;
        }
        
        // Validate UUID format
        if (!isValidUUID(taskId)) {
            callback.onError("Invalid task ID format");
            return;
        }
        
        // Call repository - maps to DELETE /api/tasks/{id}
        // Backend performs soft delete (sets deletedAt)
        taskRepository.deleteTask(taskId, new ITaskRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
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
        return uuid.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    }
    
    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}