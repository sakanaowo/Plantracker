package com.example.tralalero.data.remote.api;

import com.example.tralalero.data.dto.event.CreateProjectEventRequest;
import com.example.tralalero.data.remote.dto.event.EventDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

public interface EventApiService {


    @GET("events")
    Call<List<EventDTO>> getEventsByProject(
        @Query("projectId") String projectId,
        @Query("filter") String filter  // UPCOMING, PAST, RECURRING
    );


    @GET("events/{id}")
    Call<EventDTO> getEventById(@Path("id") String eventId);


    @POST("events")
    Call<EventDTO> createEvent(@Body EventDTO event);


    @PATCH("events/{id}")
    Call<EventDTO> updateEvent(
        @Path("id") String eventId,
        @Body EventDTO event
    );


    @DELETE("events/{id}")
    Call<Void> deleteEvent(@Path("id") String eventId);
    
    
    @POST("events/{id}/send-reminder")
    Call<Void> sendReminder(@Path("id") String eventId);
    
    // ==================== PROJECT EVENTS ENDPOINTS ====================
    
    /**
     * Get events for a specific project with filter
     * @param projectId Project ID
     * @param filter UPCOMING, PAST, or RECURRING
     */
    @GET("events/projects/{projectId}")
    Call<List<EventDTO>> getProjectEvents(
        @Path("projectId") String projectId,
        @Query("filter") String filter
    );
    
    /**
     * Create a project event with optional Google Meet link
     * @param request Contains event details and Google Meet flag
     */
    @POST("events/projects")
    Call<EventDTO> createProjectEvent(@Body CreateProjectEventRequest request);
    
    /**
     * Update a project event
     * @param eventId Event ID
     * @param request Updated event data
     */
    @PATCH("events/projects/{id}")
    Call<EventDTO> updateProjectEvent(
        @Path("id") String eventId,
        @Body CreateProjectEventRequest request
    );
    
    /**
     * Delete a project event
     * @param eventId Event ID
     */
    @DELETE("events/projects/{id}")
    Call<Void> deleteProjectEvent(@Path("id") String eventId);
}

