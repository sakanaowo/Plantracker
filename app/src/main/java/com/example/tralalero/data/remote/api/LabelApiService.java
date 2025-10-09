package com.example.tralalero.data.remote.api;

import com.example.tralalero.data.remote.dto.label.LabelDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

public interface LabelApiService {

    /**
     * Get all labels in a workspace
     * GET /labels?workspaceId={workspaceId}
     */
    @GET("labels")
    Call<List<LabelDTO>> getLabelsByWorkspace(@Query("workspaceId") String workspaceId);

    /**
     * Get label by ID
     * GET /labels/{id}
     */
    @GET("labels/{id}")
    Call<LabelDTO> getLabelById(@Path("id") String labelId);

    /**
     * Create a new label
     * POST /labels
     */
    @POST("labels")
    Call<LabelDTO> createLabel(@Body LabelDTO label);

    /**
     * Update label
     * PATCH /labels/{id}
     */
    @PATCH("labels/{id}")
    Call<LabelDTO> updateLabel(
        @Path("id") String labelId,
        @Body LabelDTO label
    );

    /**
     * Delete label
     * DELETE /labels/{id}
     */
    @DELETE("labels/{id}")
    Call<Void> deleteLabel(@Path("id") String labelId);
}

