package com.example.tralalero.domain.usecase.task;

import com.example.tralalero.domain.model.Task;
import com.example.tralalero.domain.repository.ITaskRepository;

/**
 * UseCase: Get task detail by ID
 * 
 * Matches Backend: GET /api/tasks/:id
 * 
 * Input: String taskId
 * Output: Task
 */
public class GetTaskByIdUseCase {
    
    private final ITaskRepository taskRepository;
    
    public GetTaskByIdUseCase(ITaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
    
    /**
     * Execute: Get task by ID
     * 
     * @param taskId Task ID (UUID format)
     * @param callback Callback to receive result
     */
    public void execute(String taskId, Callback<Task> callback) {
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
        
        // Call repository - maps to GET /api/tasks/{id}
        taskRepository.getTaskById(taskId, new ITaskRepository.RepositoryCallback<Task>() {
            @Override
            public void onSuccess(Task result) {
                if (result == null) {
                    callback.onError("Task not found");
                } else {
                    callback.onSuccess(result);
                }
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