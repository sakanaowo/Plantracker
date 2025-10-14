package com.example.tralalero.domain.usecase.task;

import com.example.tralalero.domain.model.Checklist;
import com.example.tralalero.domain.repository.ITaskRepository;

/**
 * UseCase: Add checklist to task
 * 
 * Input: String taskId, Checklist checklist
 * Output: Checklist (created)
 * 
 * Business Logic:
 * - Validate title not empty
 * - Title max length 100 chars
 */
public class AddChecklistUseCase {
    
    private final ITaskRepository taskRepository;
    
    private static final int MAX_TITLE_LENGTH = 100;
    
    public AddChecklistUseCase(ITaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
    
    /**
     * Add checklist to task
     * 
     * @param taskId Task ID (UUID format)
     * @param checklist Checklist to add
     * @param callback Callback to receive result
     */
    public void execute(String taskId, Checklist checklist, Callback<Checklist> callback) {
        // Validate task ID
        if (taskId == null || taskId.trim().isEmpty()) {
            callback.onError("Task ID cannot be empty");
            return;
        }
        
        if (!isValidUUID(taskId)) {
            callback.onError("Invalid task ID format");
            return;
        }
        
        // Validate checklist
        if (checklist == null) {
            callback.onError("Checklist cannot be null");
            return;
        }
        
        // Validate title
        if (checklist.getTitle() == null || checklist.getTitle().trim().isEmpty()) {
            callback.onError("Checklist title cannot be empty");
            return;
        }
        
        // Validate title length
        if (checklist.getTitle().length() > MAX_TITLE_LENGTH) {
            callback.onError("Checklist title cannot exceed " + MAX_TITLE_LENGTH + " characters");
            return;
        }
        
        // Call repository
        taskRepository.addChecklist(taskId, checklist, 
            new ITaskRepository.RepositoryCallback<Checklist>() {
                @Override
                public void onSuccess(Checklist result) {
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