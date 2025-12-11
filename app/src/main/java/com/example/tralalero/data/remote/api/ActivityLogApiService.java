package com.example.tralalero.data.remote.api;

import com.example.tralalero.data.remote.dto.activity.ActivityLogDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Activity Logs API Service
 */
public interface ActivityLogApiService {

    /**
     * GET /activity-logs/workspace/{workspaceId}
     * Get activity feed for workspace
     */
    @GET("activity-logs/workspace/{workspaceId}")
    Call<List<ActivityLogDTO>> getWorkspaceActivityFeed(
        @Path("workspaceId") String workspaceId,
        @Query("limit") Integer limit
    );

    /**
     * GET /activity-logs/project/{projectId}
     * Get activity feed for project
     */
    @GET("activity-logs/project/{projectId}")
    Call<List<ActivityLogDTO>> getProjectActivityFeed(
        @Path("projectId") String projectId,
        @Query("limit") Integer limit
    );

    /**
     * GET /activity-logs/task/{taskId}
     * Get activity feed for task
     */
    @GET("activity-logs/task/{taskId}")
    Call<List<ActivityLogDTO>> getTaskActivityFeed(
        @Path("taskId") String taskId,
        @Query("limit") Integer limit
    );

    /**
     * GET /activity-logs/user/{userId}
     * Get personal activity feed
     */
    @GET("activity-logs/user/{userId}")
    Call<List<ActivityLogDTO>> getUserActivityFeed(
        @Path("userId") String userId,
        @Query("limit") Integer limit
    );
}
