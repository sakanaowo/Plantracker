package com.example.tralalero.data.remote.api;

import com.example.tralalero.data.remote.dto.sprint.SprintDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

public interface SprintApiService {

    /**
     * Get all sprints in a project
     * GET /sprints?projectId={projectId}
     */
    @GET("sprints")
    Call<List<SprintDTO>> getSprintsByProject(@Query("projectId") String projectId);

    /**
     * Get sprint by ID
     * GET /sprints/{id}
     */
    @GET("sprints/{id}")
    Call<SprintDTO> getSprintById(@Path("id") String sprintId);

    /**
     * Create a new sprint
     * POST /sprints
     */
    @POST("sprints")
    Call<SprintDTO> createSprint(@Body SprintDTO sprint);

    /**
     * Update sprint
     * PATCH /sprints/{id}
     */
    @PATCH("sprints/{id}")
    Call<SprintDTO> updateSprint(
        @Path("id") String sprintId,
        @Body SprintDTO sprint
    );

    /**
     * Delete sprint
     * DELETE /sprints/{id}
     */
    @DELETE("sprints/{id}")
    Call<Void> deleteSprint(@Path("id") String sprintId);
}

