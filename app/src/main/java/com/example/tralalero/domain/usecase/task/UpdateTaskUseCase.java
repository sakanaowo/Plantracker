package com.example.tralalero.domain.usecase.task;

import com.example.tralalero.domain.model.Task;
import com.example.tralalero.domain.repository.ITaskRepository;

/**
 * UseCase: Update task
 * 
 * Matches Backend: PATCH /api/tasks/:id
 * Backend DTO: UpdateTaskDto {
 *   title?: string
 *   description?: string
 *   assignee_id?: string
 *   due_at?: Date
 *   priority?: string
 *   status?: string
 *   ... other optional fields
 * }
 * 
 * Input: String taskId, Task task (partial update)
 * Output: Task (updated)
 * 
 * Business Logic:
 * - Validate title if provided (not empty)
 * - All fields are optional except taskId
 */
public class UpdateTaskUseCase {
    
    private final ITaskRepository taskRepository;
    
    public UpdateTaskUseCase(ITaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
    
    /**
     * Update an existing task
     * 
     * @param taskId Task ID to update (UUID format)
     * @param task Task object with fields to update (partial)
     * @param callback Callback to receive result
     */
    public void execute(String taskId, Task task, Callback<Task> callback) {
        // Validate task ID
        if (taskId == null || taskId.trim().isEmpty()) {
            callback.onError("Task ID cannot be empty");
            return;
        }
        
        if (!isValidUUID(taskId)) {
            callback.onError("Invalid task ID format");
            return;
        }
        
        // Validate task object
        if (task == null) {
            callback.onError("Task cannot be null");
            return;
        }
        
        // Validate title if provided
        if (task.getTitle() != null && task.getTitle().trim().isEmpty()) {
            callback.onError("Task title cannot be empty");
            return;
        }
        
        // Validate title length if provided
        if (task.getTitle() != null && task.getTitle().length() > 255) {
            callback.onError("Task title cannot exceed 255 characters");
            return;
        }
        
        // Call repository - maps to PATCH /api/tasks/{id}
        taskRepository.updateTask(taskId, task, 
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