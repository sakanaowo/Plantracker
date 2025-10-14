package com.example.tralalero.domain.usecase.task;

import com.example.tralalero.domain.model.Checklist;
import com.example.tralalero.domain.repository.ITaskRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * UseCase: Get all checklists of a task
 * 
 * Input: String taskId
 * Output: List<Checklist>
 * 
 * Business Logic:
 * - Sort by createdAt ASC (order of creation)
 */
public class GetTaskChecklistsUseCase {
    
    private final ITaskRepository taskRepository;
    
    public GetTaskChecklistsUseCase(ITaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
    
    /**
     * Get all checklists for a task
     * 
     * @param taskId Task ID (UUID format)
     * @param callback Callback to receive result
     */
    public void execute(String taskId, Callback<List<Checklist>> callback) {
        // Validate task ID
        if (taskId == null || taskId.trim().isEmpty()) {
            callback.onError("Task ID cannot be empty");
            return;
        }
        
        if (!isValidUUID(taskId)) {
            callback.onError("Invalid task ID format");
            return;
        }
        
        // Call repository
        taskRepository.getChecklists(taskId, 
            new ITaskRepository.RepositoryCallback<List<Checklist>>() {
                @Override
                public void onSuccess(List<Checklist> result) {
                    // Business logic: Sort by createdAt ASC
                    List<Checklist> sorted = sortByCreatedAt(result);
                    callback.onSuccess(sorted);
                }
                
                @Override
                public void onError(String error) {
                    callback.onError(error);
                }
            }
        );
    }
    
    /**
     * Sort checklists by createdAt ASC
     */
    private List<Checklist> sortByCreatedAt(List<Checklist> checklists) {
        if (checklists == null || checklists.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Checklist> sorted = new ArrayList<>(checklists);
        Collections.sort(sorted, new Comparator<Checklist>() {
            @Override
            public int compare(Checklist c1, Checklist c2) {
                if (c1.getCreatedAt() == null) return 1;
                if (c2.getCreatedAt() == null) return -1;
                return c1.getCreatedAt().compareTo(c2.getCreatedAt()); // ASC
            }
        });
        
        return sorted;
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