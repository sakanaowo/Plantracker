package com.example.tralalero.data.remote.api;

import com.example.tralalero.data.remote.dto.task.TimeEntryDTO;

import retrofit2.Call;
import retrofit2.http.*;

public interface TimerApiService {

    /**
     * Start a new timer for a task
     * POST /timers/start
     */
    @POST("timers/start")
    Call<TimeEntryDTO> startTimer(@Body TimeEntryDTO timeEntry);

    /**
     * Stop a running timer
     * PATCH /timers/{timerId}/stop
     */
    @PATCH("timers/{timerId}/stop")
    Call<TimeEntryDTO> stopTimer(
        @Path("timerId") String timerId,
        @Body TimeEntryDTO timeEntry
    );

    /**
     * Update timer note
     * PATCH /timers/{timerId}/note
     */
    @PATCH("timers/{timerId}/note")
    Call<TimeEntryDTO> updateTimerNote(
        @Path("timerId") String timerId,
        @Body TimeEntryDTO timeEntry
    );
}

