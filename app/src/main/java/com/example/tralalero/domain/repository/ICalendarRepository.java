package com.example.tralalero.domain.repository;

import com.example.tralalero.domain.model.CalendarEvent;
import com.example.tralalero.domain.model.CalendarSyncResult;

import java.util.List;

/**
 * Repository interface for Google Calendar integration
 * Defines contract for calendar data operations
 * 
 * NOTE: OAuth authentication operations are handled separately by GoogleAuthApiService
 * This repository only handles calendar synchronization and event management
 */
public interface ICalendarRepository {

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
     * Callback interface for asynchronous operations
     */
    interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}
