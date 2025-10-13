package com.example.tralalero.domain.usecase.task;

import com.example.tralalero.domain.model.TaskComment;
import com.example.tralalero.domain.repository.ITaskRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * UseCase: Get all comments of a task
 * 
 * Input: String taskId
 * Output: List<TaskComment>
 * 
 * Business Logic:
 * - Sort by createdAt ASC (oldest first, like chat)
 */
public class GetTaskCommentsUseCase {
    
    private final ITaskRepository taskRepository;
    
    public GetTaskCommentsUseCase(ITaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
    
    /**
     * Get all comments for a task
     * 
     * @param taskId Task ID (UUID format)
     * @param callback Callback to receive result
     */
    public void execute(String taskId, Callback<List<TaskComment>> callback) {
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
        taskRepository.getComments(taskId, 
            new ITaskRepository.RepositoryCallback<List<TaskComment>>() {
                @Override
                public void onSuccess(List<TaskComment> result) {
                    // Business logic: Sort by createdAt ASC (chat style)
                    List<TaskComment> sorted = sortByCreatedAt(result);
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
     * Sort comments by createdAt ASC (oldest first, like chat)
     */
    private List<TaskComment> sortByCreatedAt(List<TaskComment> comments) {
        if (comments == null || comments.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<TaskComment> sorted = new ArrayList<>(comments);
        Collections.sort(sorted, new Comparator<TaskComment>() {
            @Override
            public int compare(TaskComment c1, TaskComment c2) {
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