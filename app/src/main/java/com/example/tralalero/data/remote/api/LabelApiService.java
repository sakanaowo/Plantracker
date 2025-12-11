package com.example.tralalero.data.remote.api;

import com.example.tralalero.data.remote.dto.label.AssignLabelRequest;
import com.example.tralalero.data.remote.dto.label.CreateLabelRequest;
import com.example.tralalero.data.remote.dto.label.LabelDTO;
import com.example.tralalero.data.remote.dto.label.UpdateLabelRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

/**
 * API service for label management
 * Backend endpoints: plantracker-backend/src/modules/labels/labels.controller.ts
 * 
 * Endpoints:
 * - POST /projects/:projectId/labels - Create label in project
 * - GET /projects/:projectId/labels - Get all labels in project  
 * - PATCH /labels/:labelId - Update label
 * - DELETE /labels/:labelId - Delete label
 * - POST /tasks/:taskId/labels - Assign label to task
 * - DELETE /tasks/:taskId/labels/:labelId - Remove label from task
 * - GET /tasks/:taskId/labels - Get task's labels
 */
public interface LabelApiService {

    // ========== Old Workspace Endpoints (Legacy, keep for backwards compatibility) ==========
    
    @GET("labels")
    Call<List<LabelDTO>> getLabelsByWorkspace(@Query("workspaceId") String workspaceId);

    @GET("labels/{id}")
    Call<LabelDTO> getLabelById(@Path("id") String labelId);

    @POST("labels")
    Call<LabelDTO> createLabel(@Body LabelDTO label);

    @PATCH("labels/{id}")
    Call<LabelDTO> updateLabel(
        @Path("id") String labelId,
        @Body LabelDTO label
    );

    @DELETE("labels/{id}")
    Call<Void> deleteLabel(@Path("id") String labelId);

    // ========== New Project-based Endpoints (Match current backend) ==========

    /**
     * Create a new label in a project
     * POST /projects/:projectId/labels
     */
    @POST("projects/{projectId}/labels")
    Call<LabelDTO> createLabelInProject(
            @Path("projectId") String projectId,
            @Body CreateLabelRequest request
    );

    /**
     * Get all labels in a project
     * GET /projects/:projectId/labels
     */
    @GET("projects/{projectId}/labels")
    Call<List<LabelDTO>> getLabelsByProject(@Path("projectId") String projectId);

    /**
     * Update a label
     * PATCH /labels/:labelId
     */
    @PATCH("labels/{labelId}")
    Call<LabelDTO> updateLabelNew(
            @Path("labelId") String labelId,
            @Body UpdateLabelRequest request
    );

    /**
     * Delete a label
     * DELETE /labels/:labelId
     */
    @DELETE("labels/{labelId}")
    Call<Void> deleteLabelNew(@Path("labelId") String labelId);

    // ========== Task Label Assignment Endpoints ==========

    /**
     * Assign a label to a task
     * POST /tasks/:taskId/labels
     */
    @POST("tasks/{taskId}/labels")
    Call<Void> assignLabelToTask(
            @Path("taskId") String taskId,
            @Body AssignLabelRequest request
    );

    /**
     * Remove a label from a task
     * DELETE /tasks/:taskId/labels/:labelId
     */
    @DELETE("tasks/{taskId}/labels/{labelId}")
    Call<Void> removeLabelFromTask(
            @Path("taskId") String taskId,
            @Path("labelId") String labelId
    );

    /**
     * Get all labels assigned to a task
     * GET /tasks/:taskId/labels
     */
    @GET("tasks/{taskId}/labels")
    Call<List<LabelDTO>> getTaskLabels(@Path("taskId") String taskId);
}


