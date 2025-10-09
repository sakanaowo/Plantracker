package com.example.tralalero.data.remote.api;

import com.example.tralalero.data.remote.dto.project.ProjectDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

public interface ProjectApiService {

    /**
     * Get all projects in a workspace
     * GET /projects?workspaceId={workspaceId}
     */
    @GET("projects")
    Call<List<ProjectDTO>> getProjectsByWorkspace(@Query("workspaceId") String workspaceId);

    /**
     * Get project by ID
     * GET /projects/{id}
     */
    @GET("projects/{id}")
    Call<ProjectDTO> getProjectById(@Path("id") String projectId);

    /**
     * Create a new project
     * POST /projects
     */
    @POST("projects")
    Call<ProjectDTO> createProject(@Body ProjectDTO project);

    /**
     * Update project
     * PATCH /projects/{id}
     */
    @PATCH("projects/{id}")
    Call<ProjectDTO> updateProject(
        @Path("id") String projectId,
        @Body ProjectDTO project
    );

    /**
     * Delete project
     * DELETE /projects/{id}
     */
    @DELETE("projects/{id}")
    Call<Void> deleteProject(@Path("id") String projectId);
}

