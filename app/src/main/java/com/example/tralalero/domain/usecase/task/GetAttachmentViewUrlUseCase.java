package com.example.tralalero.domain.usecase.task;

import com.example.tralalero.domain.repository.ITaskRepository;

import java.util.regex.Pattern;

/**
 * UseCase: Get attachment view URL
 * 
 * Input: String attachmentId
 * Output: String (view URL)
 * 
 * Business Logic:
 * - Validate attachment ID format
 * - Backend checks permissions and file existence
 * - Backend generates signed URL for viewing/downloading file
 */
public class GetAttachmentViewUrlUseCase {
    
    private final ITaskRepository taskRepository;
    
    public GetAttachmentViewUrlUseCase(ITaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
    
    /**
     * Get view URL for attachment
     * 
     * @param attachmentId Attachment ID (UUID format)
     * @param callback Callback to receive result
     */
    public void execute(String attachmentId, Callback<String> callback) {
        // Validate attachment ID
        if (attachmentId == null || attachmentId.trim().isEmpty()) {
            callback.onError("Attachment ID cannot be empty");
            return;
        }
        
        if (!isValidUUID(attachmentId)) {
            callback.onError("Invalid attachment ID format");
            return;
        }
        
        // Call repository to get attachment view URL
        taskRepository.getAttachmentViewUrl(attachmentId, new ITaskRepository.RepositoryCallback<String>() {
            @Override
            public void onSuccess(String viewUrl) {
                if (viewUrl == null || viewUrl.trim().isEmpty()) {
                    callback.onError("Received empty view URL from server");
                    return;
                }
                callback.onSuccess(viewUrl);
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