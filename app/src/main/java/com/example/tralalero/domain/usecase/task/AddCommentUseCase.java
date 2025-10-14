package com.example.tralalero.domain.usecase.task;

import com.example.tralalero.domain.model.TaskComment;
import com.example.tralalero.domain.repository.ITaskRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * UseCase: Add comment to task
 * 
 * Input: String taskId, TaskComment comment
 * Output: TaskComment (created)
 * 
 * Business Logic:
 * - Validate body not empty
 * - Detect mentions (@username) 
 * - Backend creates notifications for mentioned users
 */
public class AddCommentUseCase {
    
    private final ITaskRepository taskRepository;
    
    // Regex pattern for mentions: @username
    private static final Pattern MENTION_PATTERN = Pattern.compile("@([a-zA-Z0-9_]+)");
    
    public AddCommentUseCase(ITaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
    
    /**
     * Add comment to task
     * 
     * @param taskId Task ID (UUID format)
     * @param comment Comment to add
     * @param callback Callback to receive result
     */
    public void execute(String taskId, TaskComment comment, Callback<TaskComment> callback) {
        // Validate task ID
        if (taskId == null || taskId.trim().isEmpty()) {
            callback.onError("Task ID cannot be empty");
            return;
        }
        
        if (!isValidUUID(taskId)) {
            callback.onError("Invalid task ID format");
            return;
        }
        
        // Validate comment
        if (comment == null) {
            callback.onError("Comment cannot be null");
            return;
        }
        
        // Validate body
        if (comment.getBody() == null || comment.getBody().trim().isEmpty()) {
            callback.onError("Comment body cannot be empty");
            return;
        }
        
        // Validate body length (reasonable limit)
        if (comment.getBody().length() > 5000) {
            callback.onError("Comment cannot exceed 5000 characters");
            return;
        }
        
        // Detect mentions (for notification purposes)
        List<String> mentions = detectMentions(comment.getBody());
        
        // Call repository
        // Backend will:
        // 1. Create comment
        // 2. Parse mentions from body
        // 3. Create notifications for mentioned users
        taskRepository.addComment(taskId, comment, 
            new ITaskRepository.RepositoryCallback<TaskComment>() {
                @Override
                public void onSuccess(TaskComment result) {
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
     * Detect mentions in comment body
     * 
     * @param body Comment body
     * @return List of mentioned usernames
     */
    private List<String> detectMentions(String body) {
        List<String> mentions = new ArrayList<>();
        
        if (body == null || body.isEmpty()) {
            return mentions;
        }
        
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
    private boolean isValidUUID(String uuid) {
        return uuid.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    }
    
    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}