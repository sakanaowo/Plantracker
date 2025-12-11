package com.example.tralalero.domain.usecase.task;

import com.example.tralalero.domain.model.Task;
import com.example.tralalero.domain.repository.ITaskRepository;

/**
 * UseCase: Create quick task (inbox task without project/board)
 * 
 * Matches Backend: POST /api/tasks/quick
 * Backend DTO: CreateQuickTaskDto {
 *   title: string
 *   description?: string
 * }
 * 
 * Input: title, description
 * Output: Task (created with auto-assigned project/board/status/priority)
 * 
 * Business Logic:
 * - Validate title not empty
 * - Backend auto-assigns to user's default project/inbox board
 * - Backend sets defaults: status=TO_DO, priority=MEDIUM, position (auto)
 */
public class CreateQuickTaskUseCase {
    
    private final ITaskRepository taskRepository;
    
    public CreateQuickTaskUseCase(ITaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
    
    /**
     * Create a quick task (inbox task)
     * 
     * @param title Task title (required)
     * @param description Task description (optional)
     * @param callback Callback to receive result
     */
    public void execute(String title, String description, Callback<Task> callback) {
        // Validate title
        if (title == null || title.trim().isEmpty()) {
            callback.onError("Task title cannot be empty");
            return;
        }

        // Validate title length
        if (title.length() > 255) {
            callback.onError("Task title cannot exceed 255 characters");
            return;
        }
        
        // Call repository - maps to POST /api/tasks/quick
        // Backend will: 
        // - Auto-assign to user's default project
        // - Create task in inbox board
        // - Set default status, priority, position
        taskRepository.createQuickTask(title, description != null ? description : "", 
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
    
    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}
