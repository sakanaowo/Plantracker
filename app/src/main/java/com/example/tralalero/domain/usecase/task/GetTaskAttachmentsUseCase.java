package com.example.tralalero.domain.usecase.task;

import com.example.tralalero.domain.model.Attachment;
import com.example.tralalero.domain.repository.ITaskRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * UseCase: Get all attachments of a task
 * 
 * Input: String taskId
 * Output: List<Attachment>
 * 
 * Business Logic:
 * - Sort by createdAt DESC (newest first)
 */
public class GetTaskAttachmentsUseCase {
    
    private final ITaskRepository taskRepository;
    
    public GetTaskAttachmentsUseCase(ITaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
    
    /**
     * Get all attachments for a task
     * 
     * @param taskId Task ID (UUID format)
     * @param callback Callback to receive result
     */
    public void execute(String taskId, Callback<List<Attachment>> callback) {
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
        taskRepository.getAttachments(taskId, 
            new ITaskRepository.RepositoryCallback<List<Attachment>>() {
                @Override
                public void onSuccess(List<Attachment> result) {
                    // Business logic: Sort by createdAt DESC
                    List<Attachment> sorted = sortByCreatedAt(result);
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
     * Sort attachments by createdAt DESC (newest first)
     */
    private List<Attachment> sortByCreatedAt(List<Attachment> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Attachment> sorted = new ArrayList<>(attachments);
        Collections.sort(sorted, new Comparator<Attachment>() {
            @Override
            public int compare(Attachment a1, Attachment a2) {
                if (a1.getCreatedAt() == null) return 1;
                if (a2.getCreatedAt() == null) return -1;
                return a2.getCreatedAt().compareTo(a1.getCreatedAt()); // DESC
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