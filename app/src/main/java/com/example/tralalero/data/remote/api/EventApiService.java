package com.example.tralalero.data.remote.api;

import com.example.tralalero.data.dto.event.CreateProjectEventRequest;
import com.example.tralalero.data.remote.dto.event.EventDTO;
import com.google.gson.annotations.SerializedName;

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
    
    // ==================== CANCEL/RESTORE EVENT ====================
    
    /**
     * Cancel an event (soft delete)
     * @param projectId Project ID
     * @param eventId Event ID
     * @param request Contains optional cancellation reason
     */
    @PATCH("events/projects/{projectId}/events/{eventId}/cancel")
    Call<EventDTO> cancelEvent(
        @Path("projectId") String projectId,
        @Path("eventId") String eventId,
        @Body CancelEventRequest request
    );
    
    /**
     * Restore a cancelled event
     * @param projectId Project ID
     * @param eventId Event ID
     */
    @PATCH("events/projects/{projectId}/events/{eventId}/restore")
    Call<EventDTO> restoreEvent(
        @Path("projectId") String projectId,
        @Path("eventId") String eventId
    );
    
    /**
     * Permanently delete an event
     * @param projectId Project ID
     * @param eventId Event ID
     */
    @DELETE("events/projects/{projectId}/events/{eventId}/hard-delete")
    Call<Void> hardDeleteEvent(
        @Path("projectId") String projectId,
        @Path("eventId") String eventId
    );
    
    /**
     * Request DTO for cancelling an event
     */
    class CancelEventRequest {
        @SerializedName("reason")
        private String reason;
        
        public CancelEventRequest(String reason) {
            this.reason = reason;
        }
        
        public String getReason() {
            return reason;
        }
        
        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
