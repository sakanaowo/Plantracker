package com.example.tralalero.domain.repository;

import com.example.tralalero.domain.model.ActivityLog;
import java.util.List;

/**
 * Repository interface for Activity Logs
 */
public interface IActivityLogRepository {
    
    /**
     * Get activity logs for a specific project
     * @param projectId The project ID
     * @param limit Maximum number of logs to fetch
     * @param callback Callback with list of activity logs
     */
    void getProjectActivityLogs(String projectId, int limit, 
        RepositoryCallback<List<ActivityLog>> callback);
    
    /**
     * Get activity logs for a specific workspace
     * @param workspaceId The workspace ID
     * @param limit Maximum number of logs to fetch
     * @param callback Callback with list of activity logs
     */
    void getWorkspaceActivityLogs(String workspaceId, int limit,
        RepositoryCallback<List<ActivityLog>> callback);
    
    /**
     * Get activity logs for a specific task
     * @param taskId The task ID
     * @param limit Maximum number of logs to fetch
     * @param callback Callback with list of activity logs
     */
    void getTaskActivityLogs(String taskId, int limit,
        RepositoryCallback<List<ActivityLog>> callback);
    
    /**
     * Get personal activity feed for user
     * @param userId The user ID
     * @param limit Maximum number of logs to fetch
     * @param callback Callback with list of activity logs
     */
    void getUserActivityLogs(String userId, int limit,
        RepositoryCallback<List<ActivityLog>> callback);
    
    /**
     * Callback interface for repository operations
     */
    interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}
