package com.example.tralalero.data.repository;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.example.tralalero.auth.remote.AuthManager;
import com.example.tralalero.data.remote.api.ActivityLogApiService;
import com.example.tralalero.data.remote.dto.activity.ActivityLogDTO;
import com.example.tralalero.domain.model.ActivityLog;
import com.example.tralalero.domain.repository.IActivityLogRepository;
import com.example.tralalero.network.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository implementation for Activity Logs
 */
public class ActivityLogRepositoryImpl implements IActivityLogRepository {
    
    private static final String TAG = "ActivityLogRepository";
    private ActivityLogApiService apiService;

    public ActivityLogRepositoryImpl(Context context) {
        Application app = (Application) context.getApplicationContext();
        AuthManager authManager = new AuthManager(app);
        this.apiService = ApiClient.get(authManager).create(ActivityLogApiService.class);
    }

    @Override
    public void getProjectActivityLogs(String projectId, int limit,
                                       RepositoryCallback<List<ActivityLog>> callback) {
        Log.d(TAG, "Fetching activity logs for project: " + projectId + " (limit: " + limit + ")");
        
        apiService.getProjectActivityFeed(projectId, limit).enqueue(
            new Callback<List<ActivityLogDTO>>() {
                @Override
                public void onResponse(Call<List<ActivityLogDTO>> call,
                                     Response<List<ActivityLogDTO>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<ActivityLog> logs = mapDTOsToDomain(response.body());
                        Log.d(TAG, "Loaded " + logs.size() + " activity logs for project");
                        callback.onSuccess(logs);
                    } else {
                        String error = "Failed to load project activity logs: " + response.code();
                        Log.e(TAG, error);
                        callback.onError(error);
                    }
                }

                @Override
                public void onFailure(Call<List<ActivityLogDTO>> call, Throwable t) {
                    Log.e(TAG, "Network error fetching project activity logs: " + t.getMessage());
                    callback.onError(t.getMessage());
                }
            });
    }

    @Override
    public void getWorkspaceActivityLogs(String workspaceId, int limit,
                                        RepositoryCallback<List<ActivityLog>> callback) {
        Log.d(TAG, "Fetching activity logs for workspace: " + workspaceId);
        
        apiService.getWorkspaceActivityFeed(workspaceId, limit).enqueue(
            new Callback<List<ActivityLogDTO>>() {
                @Override
                public void onResponse(Call<List<ActivityLogDTO>> call,
                                     Response<List<ActivityLogDTO>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<ActivityLog> logs = mapDTOsToDomain(response.body());
                        Log.d(TAG, "Loaded " + logs.size() + " activity logs for workspace");
                        callback.onSuccess(logs);
                    } else {
                        String error = "Failed to load workspace activity logs: " + response.code();
                        Log.e(TAG, error);
                        callback.onError(error);
                    }
                }

                @Override
                public void onFailure(Call<List<ActivityLogDTO>> call, Throwable t) {
                    Log.e(TAG, "Network error fetching workspace activity logs: " + t.getMessage());
                    callback.onError(t.getMessage());
                }
            });
    }

    @Override
    public void getTaskActivityLogs(String taskId, int limit,
                                   RepositoryCallback<List<ActivityLog>> callback) {
        Log.d(TAG, "Fetching activity logs for task: " + taskId);
        
        apiService.getTaskActivityFeed(taskId, limit).enqueue(
            new Callback<List<ActivityLogDTO>>() {
                @Override
                public void onResponse(Call<List<ActivityLogDTO>> call,
                                     Response<List<ActivityLogDTO>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<ActivityLog> logs = mapDTOsToDomain(response.body());
                        Log.d(TAG, "Loaded " + logs.size() + " activity logs for task");
                        callback.onSuccess(logs);
                    } else {
                        String error = "Failed to load task activity logs: " + response.code();
                        Log.e(TAG, error);
                        callback.onError(error);
                    }
                }

                @Override
                public void onFailure(Call<List<ActivityLogDTO>> call, Throwable t) {
                    Log.e(TAG, "Network error fetching task activity logs: " + t.getMessage());
                    callback.onError(t.getMessage());
                }
            });
    }

    @Override
    public void getUserActivityLogs(String userId, int limit,
                                   RepositoryCallback<List<ActivityLog>> callback) {
        Log.d(TAG, "Fetching activity logs for user: " + userId);
        
        apiService.getUserActivityFeed(userId, limit).enqueue(
            new Callback<List<ActivityLogDTO>>() {
                @Override
                public void onResponse(Call<List<ActivityLogDTO>> call,
                                     Response<List<ActivityLogDTO>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<ActivityLog> logs = mapDTOsToDomain(response.body());
                        Log.d(TAG, "Loaded " + logs.size() + " activity logs for user");
                        callback.onSuccess(logs);
                    } else {
                        String error = "Failed to load user activity logs: " + response.code();
                        Log.e(TAG, error);
                        callback.onError(error);
                    }
                }

                @Override
                public void onFailure(Call<List<ActivityLogDTO>> call, Throwable t) {
                    Log.e(TAG, "Network error fetching user activity logs: " + t.getMessage());
                    callback.onError(t.getMessage());
                }
            });
    }

    /**
     * Map DTOs to domain models
     */
    private List<ActivityLog> mapDTOsToDomain(List<ActivityLogDTO> dtos) {
        List<ActivityLog> logs = new ArrayList<>();
        for (ActivityLogDTO dto : dtos) {
            logs.add(mapDTOToDomain(dto));
        }
        return logs;
    }

    /**
     * Map single DTO to domain model
     */
    private ActivityLog mapDTOToDomain(ActivityLogDTO dto) {
        String userName = dto.getUsers() != null ? dto.getUsers().getName() : "Unknown";
        String userAvatar = dto.getUsers() != null ? dto.getUsers().getAvatarUrl() : null;
        
        return new ActivityLog(
            dto.getId(),
            dto.getUserId(),
            dto.getAction(),
            dto.getEntityType(),
            dto.getEntityName(),
            dto.getCreatedAt(),
            userName,
            userAvatar
        );
    }
}
