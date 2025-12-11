package com.example.tralalero.data.remote.api;

import com.example.tralalero.data.remote.dto.calendar.CalendarSyncRequest;
import com.example.tralalero.data.remote.dto.calendar.CalendarSyncResponse;
import com.example.tralalero.data.remote.dto.calendar.SyncFromGoogleResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Retrofit API service for Google Calendar integration
 * 
 * NOTE: OAuth authentication flow is handled by GoogleAuthApiService (/auth/google/*)
 * This service only handles calendar data operations (sync, import)
 * 
 * @see com.example.tralalero.data.remote.api.GoogleAuthApiService for OAuth operations
 */
public interface CalendarApiService {

    /**
     * Sync internal events to Google Calendar
     * Pushes selected events to user's Google Calendar
     * 
     * @param request List of event IDs to sync
     * @return Sync results with success/failure per event
     */
    @POST("calendar/sync/to-google")
    Call<CalendarSyncResponse> syncToGoogle(@Body CalendarSyncRequest request);

    /**
     * Import events from Google Calendar
     * Fetches events from user's Google Calendar and creates them in the specified project
     * 
     * @param projectId Target project to import events into
     * @param timeMin Start time filter (ISO 8601 format)
     * @param timeMax End time filter (ISO 8601 format)
     * @return Response with success status, events list, and optional message
     */
    @GET("calendar/sync/from-google")
    Call<SyncFromGoogleResponse> syncFromGoogle(
        @Query("projectId") String projectId,
        @Query("timeMin") String timeMin,
        @Query("timeMax") String timeMax
    );
}
