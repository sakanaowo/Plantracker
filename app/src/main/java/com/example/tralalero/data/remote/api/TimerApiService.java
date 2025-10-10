package com.example.tralalero.data.remote.api;

import com.example.tralalero.data.remote.dto.task.TimeEntryDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

public interface TimerApiService {

    /**
     * Get time entries by task
     * GET /time-entries?taskId={taskId}
     */
    @GET("time-entries")
    Call<List<TimeEntryDTO>> getTimeEntriesByTask(@Query("taskId") String taskId);

    /**
     * Get time entries by user
     * GET /time-entries?userId={userId}
     */
    @GET("time-entries")
    Call<List<TimeEntryDTO>> getTimeEntriesByUser(@Query("userId") String userId);

    /**
     * Get active time entry for user
     * GET /time-entries/active?userId={userId}
     */
    @GET("time-entries/active")
    Call<TimeEntryDTO> getActiveTimeEntry(@Query("userId") String userId);

    /**
     * Start a new timer for a task
     * POST /time-entries/start?taskId={taskId}
     */
    @POST("time-entries/start")
    Call<TimeEntryDTO> startTimer(@Query("taskId") String taskId);

    /**
     * Stop a running timer
     * POST /time-entries/{id}/stop
     */
    @POST("time-entries/{id}/stop")
    Call<TimeEntryDTO> stopTimer(@Path("id") String timeEntryId);

    /**
     * Create time entry
     * POST /time-entries
     */
    @POST("time-entries")
    Call<TimeEntryDTO> createTimeEntry(@Body TimeEntryDTO timeEntry);

    /**
     * Update time entry
     * PATCH /time-entries/{id}
     */
    @PATCH("time-entries/{id}")
    Call<TimeEntryDTO> updateTimeEntry(
        @Path("id") String timeEntryId,
        @Body TimeEntryDTO timeEntry
    );

    /**
     * Delete time entry
     * DELETE /time-entries/{id}
     */
    @DELETE("time-entries/{id}")
    Call<Void> deleteTimeEntry(@Path("id") String timeEntryId);
}
