package com.example.tralalero.data.remote.api;

import com.example.tralalero.data.remote.dto.label.LabelDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

public interface LabelApiService {


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
}

