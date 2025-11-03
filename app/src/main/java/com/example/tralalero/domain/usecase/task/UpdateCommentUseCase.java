package com.example.tralalero.domain.usecase.task;

import com.example.tralalero.domain.model.TaskComment;
import com.example.tralalero.domain.repository.ITaskRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * UseCase: Update comment content
 * 
 * Input: String commentId, String body
 * Output: TaskComment (updated)
 * 
 * Business Logic:
 * - Validate body not empty
 * - Detect mentions (@username) 
 * - Backend creates notifications for mentioned users
 */
public class UpdateCommentUseCase {
    
    private final ITaskRepository taskRepository;
    
    // Regex pattern for mentions: @username
    private static final Pattern MENTION_PATTERN = Pattern.compile("@([a-zA-Z0-9_]+)");
    
    public UpdateCommentUseCase(ITaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
    
    /**
     * Update comment content
     * 
     * @param commentId Comment ID (UUID format)
     * @param body New comment body
     * @param callback Callback to receive result
     */
    public void execute(String commentId, String body, Callback<TaskComment> callback) {
        // Validate comment ID
        if (commentId == null || commentId.trim().isEmpty()) {
            callback.onError("Comment ID cannot be empty");
            return;
        }
        
        if (!isValidUUID(commentId)) {
            callback.onError("Invalid comment ID format");
            return;
        }
        
        // Validate body
        if (body == null || body.trim().isEmpty()) {
            callback.onError("Comment body cannot be empty");
            return;
        }
        
        // Check body length (if there's a limit)
        if (body.length() > 10000) {
            callback.onError("Comment body too long (max 10,000 characters)");
            return;
        }
        
        // Extract mentions (optional business logic)
        List<String> mentions = extractMentions(body);
        
        // Call repository to update comment
        taskRepository.updateComment(commentId, body, new ITaskRepository.RepositoryCallback<TaskComment>() {
            @Override
            public void onSuccess(TaskComment result) {
                callback.onSuccess(result);
            }
            
            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }
    
    /**
     * Extract mentioned usernames from comment body
     */
    private List<String> extractMentions(String body) {
        List<String> mentions = new ArrayList<>();
        Matcher matcher = MENTION_PATTERN.matcher(body);
        while (matcher.find()) {
            String username = matcher.group(1);
            if (!mentions.contains(username)) {
                mentions.add(username);
            }
        }
        return mentions;
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