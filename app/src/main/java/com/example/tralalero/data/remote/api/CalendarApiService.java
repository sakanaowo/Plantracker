package com.example.tralalero.data.remote.api;

import com.example.tralalero.data.remote.dto.calendar.AuthUrlResponse;
import com.example.tralalero.data.remote.dto.calendar.CalendarConnectionStatusResponse;
import com.example.tralalero.data.remote.dto.calendar.CalendarEventDTO;
import com.example.tralalero.data.remote.dto.calendar.CalendarSyncRequest;
import com.example.tralalero.data.remote.dto.calendar.CalendarSyncResponse;
import com.example.tralalero.data.remote.dto.calendar.CallbackResponse;
import com.example.tralalero.data.remote.dto.calendar.SyncFromGoogleResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Retrofit API service for Google Calendar integration
 * Handles OAuth flow and calendar synchronization
 */
public interface CalendarApiService {

    /**
     * Get Google OAuth authorization URL
     * 
     * @return Auth URL for user to authorize in WebView
     */
    @GET("calendar/auth/url")
    Call<AuthUrlResponse> getAuthUrl();

    /**
     * Handle OAuth callback with authorization code
     * 
     * @param code Authorization code from Google OAuth
     * @return Success status and connection info
     */
    @GET("calendar/auth/callback")
    Call<CallbackResponse> handleCallback(@Query("code") String code);

    /**
     * Check if user has connected Google Calendar
     * 
     * @return Connection status and last sync time
     */
    @GET("calendar/connection/status")
    Call<CalendarConnectionStatusResponse> getConnectionStatus();

    /**
     * Sync internal events to Google Calendar
     * 
     * @param request List of event IDs to sync
     * @return Sync results with success/failure per event
     */
    @POST("calendar/sync/to-google")
    Call<CalendarSyncResponse> syncToGoogle(@Body CalendarSyncRequest request);

    /**
     * Import events from Google Calendar
     * ✅ FIXED: Changed return type to handle new response format {success, events, count, message}
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

    /**
     * Disconnect Google Calendar integration
     * Remove stored OAuth tokens and event mappings
     * 
     * @return Success status
     */
    @DELETE("calendar/connection")
    Call<Void> disconnectCalendar();
}
