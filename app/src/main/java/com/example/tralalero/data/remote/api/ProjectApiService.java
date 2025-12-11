package com.example.tralalero.data.remote.api;

import com.example.tralalero.data.dto.project.ProjectSummaryResponse;
import com.example.tralalero.data.remote.dto.project.ProjectDTO;
import com.example.tralalero.data.remote.dto.project.ProjectMemberDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

public interface ProjectApiService {


    @GET("projects")
    Call<List<ProjectDTO>> getProjectsByWorkspace(@Query("workspaceId") String workspaceId);

    /**
     * Get all projects accessible by the current user (across all workspaces)
     * @return List of all projects
     */
    @GET("projects")
    Call<List<ProjectDTO>> getAllUserProjects();


    @GET("projects/{id}")
    Call<ProjectDTO> getProjectById(@Path("id") String projectId);

    @GET("projects/{id}/members")
    Call<List<ProjectMemberDTO>> getProjectMembers(@Path("id") String projectId);


    @POST("projects")
    Call<ProjectDTO> createProject(@Body ProjectDTO project);

 
    @PATCH("projects/{id}")
    Call<ProjectDTO> updateProject(
        @Path("id") String projectId,
        @Body ProjectDTO project
    );


    @DELETE("projects/{id}")
    Call<Void> deleteProject(@Path("id") String projectId);
    
    /**
     * Leave project (for non-owners)
     * @param projectId Project ID
     * @return Response with success message
     */
    @POST("projects/{id}/leave")
    Call<Void> leaveProject(@Path("id") String projectId);
    
    /**
     * Get project summary statistics
     * @param projectId Project ID
     * @return Summary with done, updated, created, due counts and status overview
     */
    @GET("projects/{id}/summary")
    Call<ProjectSummaryResponse> getProjectSummary(@Path("id") String projectId);
    
    /**
     * Get calendar data for a specific month
     * @param projectId Project ID
     * @param month Month in YYYY-MM format (e.g., "2025-12")
     * @return Calendar data with tasks, events, and dates with items
     */
    @GET("projects/{id}/calendar-data")
    Call<com.example.tralalero.data.remote.dto.calendar.CalendarDataResponse> getCalendarData(
        @Path("id") String projectId,
        @Query("month") String month
    );
}
