package com.example.tralalero.data.remote.api;

import com.example.tralalero.data.remote.dto.event.EventDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

public interface EventApiService {

    /**
     * Get all events in a project
     * GET /events?projectId={projectId}
     */
    @GET("events")
    Call<List<EventDTO>> getEventsByProject(@Query("projectId") String projectId);

    /**
     * Get event by ID
     * GET /events/{id}
     */
    @GET("events/{id}")
    Call<EventDTO> getEventById(@Path("id") String eventId);

    /**
     * Create a new event
     * POST /events
     */
    @POST("events")
    Call<EventDTO> createEvent(@Body EventDTO event);

    /**
     * Update event
     * PATCH /events/{id}
     */
    @PATCH("events/{id}")
    Call<EventDTO> updateEvent(
        @Path("id") String eventId,
        @Body EventDTO event
    );

    /**
     * Delete event
     * DELETE /events/{id}
     */
    @DELETE("events/{id}")
    Call<Void> deleteEvent(@Path("id") String eventId);
}

