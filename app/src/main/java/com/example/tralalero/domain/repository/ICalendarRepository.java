package com.example.tralalero.domain.repository;

import com.example.tralalero.domain.model.CalendarConnection;
import com.example.tralalero.domain.model.CalendarEvent;
import com.example.tralalero.domain.model.CalendarSyncResult;

import java.util.List;

/**
 * Repository interface for Google Calendar integration
 * Defines contract for calendar operations
 */
public interface ICalendarRepository {

    /**
     * Get Google OAuth authorization URL
     * User will open this URL in WebView to authorize the app
     * 
     * @param callback Success returns auth URL, Error returns error message
     */
    void getAuthUrl(RepositoryCallback<String> callback);

    /**
     * Handle OAuth callback with authorization code
     * Exchange code for access token and store it
     * 
     * @param code Authorization code from Google OAuth redirect
     * @param callback Success returns connection time, Error returns error message
     */
    void handleOAuthCallback(String code, RepositoryCallback<String> callback);

    /**
     * Check if user has connected Google Calendar
     * Returns connection status and last sync time
     * 
     * @param callback Success returns CalendarConnection, Error returns error message
     */
    void getConnectionStatus(RepositoryCallback<CalendarConnection> callback);

    /**
     * Sync internal events to Google Calendar
     * Pushes selected events to user's Google Calendar
     * 
     * @param projectId Project ID containing the events
     * @param eventIds List of event IDs to sync
     * @param callback Success returns sync result, Error returns error message
     */
    void syncEventsToGoogle(String projectId, List<String> eventIds, RepositoryCallback<CalendarSyncResult> callback);

    /**
     * Import events from Google Calendar
     * Pulls events from user's Google Calendar into project
     * 
     * @param projectId Target project to import events into
     * @param timeMin Start time filter (ISO 8601 format)
     * @param timeMax End time filter (ISO 8601 format)
     * @param callback Success returns list of imported events, Error returns error message
     */
    void syncEventsFromGoogle(String projectId, String timeMin, String timeMax, RepositoryCallback<List<CalendarEvent>> callback);

    /**
     * Disconnect Google Calendar integration
     * Removes OAuth tokens and event mappings
     * 
     * @param callback Success returns null, Error returns error message
     */
    void disconnectCalendar(RepositoryCallback<Void> callback);

    /**
     * Callback interface for asynchronous operations
     */
    interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}
