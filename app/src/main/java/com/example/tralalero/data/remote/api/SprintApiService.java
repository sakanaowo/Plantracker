package com.example.tralalero.data.remote.api;

import com.example.tralalero.data.remote.dto.sprint.SprintDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

public interface SprintApiService {

    @GET("sprints")
    Call<List<SprintDTO>> getSprintsByProject(@Query("projectId") String projectId);


    @GET("sprints/active")
    Call<SprintDTO> getActiveSprint(@Query("projectId") String projectId);

 
    @GET("sprints/{id}")
    Call<SprintDTO> getSprintById(@Path("id") String sprintId);

  
    @POST("sprints")
    Call<SprintDTO> createSprint(@Body SprintDTO sprint);

  
    @PATCH("sprints/{id}")
    Call<SprintDTO> updateSprint(
        @Path("id") String sprintId,
        @Body SprintDTO sprint
    );


    @DELETE("sprints/{id}")
    Call<Void> deleteSprint(@Path("id") String sprintId);


    @POST("sprints/{id}/start")
    Call<SprintDTO> startSprint(@Path("id") String sprintId);

 
    @POST("sprints/{id}/complete")
    Call<SprintDTO> completeSprint(@Path("id") String sprintId);
}
