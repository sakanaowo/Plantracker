package com.example.tralalero.data.remote.api;

import com.example.tralalero.data.remote.dto.task.TimeEntryDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

public interface TimerApiService {


    @GET("time-entries")
    Call<List<TimeEntryDTO>> getTimeEntriesByTask(@Query("taskId") String taskId);


    @GET("time-entries")
    Call<List<TimeEntryDTO>> getTimeEntriesByUser(@Query("userId") String userId);

 
    @GET("time-entries/active")
    Call<TimeEntryDTO> getActiveTimeEntry(@Query("userId") String userId);

 
    @POST("time-entries/start")
    Call<TimeEntryDTO> startTimer(@Query("taskId") String taskId);

  
    @POST("time-entries/{id}/stop")
    Call<TimeEntryDTO> stopTimer(@Path("id") String timeEntryId);

 
    @POST("time-entries")
    Call<TimeEntryDTO> createTimeEntry(@Body TimeEntryDTO timeEntry);

 
    @PATCH("time-entries/{id}")
    Call<TimeEntryDTO> updateTimeEntry(
        @Path("id") String timeEntryId,
        @Body TimeEntryDTO timeEntry
    );

  
    @DELETE("time-entries/{id}")
    Call<Void> deleteTimeEntry(@Path("id") String timeEntryId);
}
