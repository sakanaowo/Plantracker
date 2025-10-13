package com.example.tralalero.domain.usecase.task;

import com.example.tralalero.domain.model.Task;
import com.example.tralalero.domain.repository.ITaskRepository;

/**
 * UseCase: Create new task
 * 
 * Matches Backend: POST /api/tasks
 * Backend DTO: CreateTaskDto {
 *   project_id: string (UUID)
 *   board_id: string (UUID) 
 *   title: string
 *   description?: string
 *   assignee_id?: string (UUID)
 *   ... other optional fields
 * }
 * 
 * Input: Task task (with required: project_id, board_id, title)
 * Output: Task (created with generated ID)
 * 
 * Business Logic:
 * - Validate title not empty
 * - Validate required IDs (project_id, board_id)
 * - Backend sets defaults: status=TO_DO, priority=MEDIUM, position (auto)
 */
public class CreateTaskUseCase {
    
    private final ITaskRepository taskRepository;
    
    public CreateTaskUseCase(ITaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
    
    /**
     * Create a new task
     * 
     * @param task Task object to create (must have: projectId, boardId, title)
     * @param callback Callback to receive result
     */
    public void execute(Task task, Callback<Task> callback) {
        // Validate task object
        if (task == null) {
            callback.onError("Task cannot be null");
            return;
        }
        
        // Validate required field: title
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            callback.onError("Task title cannot be empty");
            return;
        }
        
        // Validate required field: project_id
        if (task.getProjectId() == null || task.getProjectId().trim().isEmpty()) {
            callback.onError("Project ID is required");
            return;
        }
        
        // Validate required field: board_id
        if (task.getBoardId() == null || task.getBoardId().trim().isEmpty()) {
            callback.onError("Board ID is required");
            return;
        }
        
        // Validate UUID formats
        if (!isValidUUID(task.getProjectId())) {
            callback.onError("Invalid project ID format");
            return;
        }
        
        if (!isValidUUID(task.getBoardId())) {
            callback.onError("Invalid board ID format");
            return;
        }
        
        // Validate title length (reasonable limit)
        if (task.getTitle().length() > 255) {
            callback.onError("Task title cannot exceed 255 characters");
            return;
        }
        
        // Call repository - maps to POST /api/tasks
        // Backend will handle: default status, priority, position generation
        taskRepository.createTask(task.getBoardId(), task, 
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