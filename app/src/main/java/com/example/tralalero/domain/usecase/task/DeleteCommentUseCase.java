package com.example.tralalero.domain.usecase.task;

import com.example.tralalero.domain.repository.ITaskRepository;

import java.util.regex.Pattern;

/**
 * UseCase: Delete comment
 * 
 * Input: String commentId
 * Output: Void (success/error)
 * 
 * Business Logic:
 * - Validate comment ID format
 * - Backend checks ownership permissions
 * - Backend removes comment from task
 */
public class DeleteCommentUseCase {
    
    private final ITaskRepository taskRepository;
    
    public DeleteCommentUseCase(ITaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
    
    /**
     * Delete comment by ID
     * 
     * @param commentId Comment ID (UUID format)
     * @param callback Callback to receive result
     */
    public void execute(String commentId, Callback<Void> callback) {
        // Validate comment ID
        if (commentId == null || commentId.trim().isEmpty()) {
            callback.onError("Comment ID cannot be empty");
            return;
        }
        
        if (!isValidUUID(commentId)) {
            callback.onError("Invalid comment ID format");
            return;
        }
        
        // Call repository to delete comment
        taskRepository.deleteComment(commentId, new ITaskRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                callback.onSuccess(null);
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
    private boolean isValidUUID(String id) {
        if (id == null) return false;
        
        // UUID pattern: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
        Pattern uuidPattern = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
        );
        return uuidPattern.matcher(id).matches();
    }
    
    /**
     * Callback interface for async operations
     */
    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}