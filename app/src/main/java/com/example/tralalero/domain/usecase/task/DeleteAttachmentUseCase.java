package com.example.tralalero.domain.usecase.task;

import com.example.tralalero.domain.repository.ITaskRepository;

import java.util.regex.Pattern;

/**
 * UseCase: Delete attachment
 * 
 * Input: String attachmentId
 * Output: Void (success/error)
 * 
 * Business Logic:
 * - Validate attachment ID format
 * - Backend checks permissions and file existence
 * - Backend removes attachment record and file from storage
 */
public class DeleteAttachmentUseCase {
    
    private final ITaskRepository taskRepository;
    
    public DeleteAttachmentUseCase(ITaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
    
    /**
     * Delete attachment by ID
     * 
     * @param attachmentId Attachment ID (UUID format)
     * @param callback Callback to receive result
     */
    public void execute(String attachmentId, Callback<Void> callback) {
        // Validate attachment ID
        if (attachmentId == null || attachmentId.trim().isEmpty()) {
            callback.onError("Attachment ID cannot be empty");
            return;
        }
        
        if (!isValidUUID(attachmentId)) {
            callback.onError("Invalid attachment ID format");
            return;
        }
        
        // Call repository to delete attachment
        taskRepository.deleteAttachment(attachmentId, new ITaskRepository.RepositoryCallback<Void>() {
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