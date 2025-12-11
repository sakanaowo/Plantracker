package com.example.tralalero.data.remote.api;

import com.example.tralalero.data.dto.event.CreateProjectEventRequest;
import com.example.tralalero.data.remote.dto.event.EventDTO;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

public interface EventApiService {


    @GET("events/projects/{projectId}")
    Call<List<EventDTO>> getEventsByProject(
        @Path("projectId") String projectId,
        @Query("status") String status  // ACTIVE, CANCELLED, ALL
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
    
    
    @POST("events/projects/{id}/send-reminder")
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
    
    /**
     * Suggest meeting time slots based on participants' availability
     * @param projectId Project ID
     * @param request Contains date range, duration, and participant IDs
     */
    @POST("events/projects/{projectId}/suggest-times")
    Call<com.example.tralalero.data.dto.event.SuggestEventTimeResponse> suggestEventTimes(
        @Path("projectId") String projectId,
        @Body com.example.tralalero.data.dto.event.SuggestEventTimeRequest request
    );
    
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
    
    // ==================== EVENT REMINDERS ====================
    
    /**
     * Send event reminder to users
     * @param request Contains eventId, recipientIds, and optional message
     */
    @POST("events/reminders")
    Call<CreateEventReminderResponse> sendEventReminder(@Body CreateEventReminderRequest request);
    
    /**
     * Get my event reminders
     */
    @GET("events/reminders/my")
    Call<List<EventReminderDTO>> getMyEventReminders();
    
    /**
     * Mark event reminder as read
     * @param reminderId Reminder ID
     */
    @PATCH("events/reminders/{id}/read")
    Call<Void> markReminderAsRead(@Path("id") String reminderId);
    
    /**
     * Dismiss event reminder
     * @param reminderId Reminder ID
     */
    @DELETE("events/reminders/{id}")
    Call<Void> dismissEventReminder(@Path("id") String reminderId);
    
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
    
    /**
     * Request DTO for creating event reminder
     */
    class CreateEventReminderRequest {
        @SerializedName("eventId")
        private String eventId;
        
        @SerializedName("recipientIds")
        private List<String> recipientIds;
        
        @SerializedName("message")
        private String message;
        
        public CreateEventReminderRequest(String eventId, List<String> recipientIds, String message) {
            this.eventId = eventId;
            this.recipientIds = recipientIds;
            this.message = message;
        }
        
        // Getters and Setters
        public String getEventId() { return eventId; }
        public void setEventId(String eventId) { this.eventId = eventId; }
        public List<String> getRecipientIds() { return recipientIds; }
        public void setRecipientIds(List<String> recipientIds) { this.recipientIds = recipientIds; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
    
    /**
     * Response DTO for event reminder creation
     */
    class CreateEventReminderResponse {
        @SerializedName("success")
        private boolean success;
        
        @SerializedName("created")
        private int created;
        
        public boolean isSuccess() { return success; }
        public int getCreated() { return created; }
    }
    
    /**
     * Event Reminder DTO
     */
    class EventReminderDTO {
        @SerializedName("id")
        private String id;
        
        @SerializedName("eventId")
        private String eventId;
        
        @SerializedName("eventTitle")
        private String eventTitle;
        
        @SerializedName("eventDate")
        private String eventDate;
        
        @SerializedName("eventTime")
        private String eventTime;
        
        @SerializedName("projectId")
        private String projectId;
        
        @SerializedName("projectName")
        private String projectName;
        
        @SerializedName("senderName")
        private String senderName;
        
        @SerializedName("message")
        private String message;
        
        @SerializedName("timestamp")
        private long timestamp;
        
        @SerializedName("isRead")
        private boolean isRead;
        
        // Getters
        public String getId() { return id; }
        public String getEventId() { return eventId; }
        public String getEventTitle() { return eventTitle; }
        public String getEventDate() { return eventDate; }
        public String getEventTime() { return eventTime; }
        public String getProjectId() { return projectId; }
        public String getProjectName() { return projectName; }
        public String getSenderName() { return senderName; }
        public String getMessage() { return message; }
        public long getTimestamp() { return timestamp; }
        public boolean isRead() { return isRead; }
    }
}
