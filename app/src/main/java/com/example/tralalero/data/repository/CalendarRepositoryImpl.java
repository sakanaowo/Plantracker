package com.example.tralalero.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.tralalero.App.App;
import com.example.tralalero.auth.remote.AuthManager;
import com.example.tralalero.data.mapper.CalendarMapper;
import com.example.tralalero.data.remote.api.CalendarApiService;
import com.example.tralalero.data.remote.dto.calendar.CalendarSyncRequest;
import com.example.tralalero.data.remote.dto.calendar.CalendarSyncResponse;
import com.example.tralalero.domain.model.CalendarEvent;
import com.example.tralalero.domain.model.CalendarSyncResult;
import com.example.tralalero.domain.repository.ICalendarRepository;
import com.example.tralalero.network.ApiClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository implementation for Google Calendar integration
 * Handles all calendar-related API calls and data transformation
 */
public class CalendarRepositoryImpl implements ICalendarRepository {
    private static final String TAG = "CalendarRepository";
    private final CalendarApiService apiService;

    public CalendarRepositoryImpl(Context context) {
        AuthManager authManager = App.authManager;
        this.apiService = ApiClient.get(authManager).create(CalendarApiService.class);
    }

    @Override
    public void syncEventsToGoogle(String projectId, List<String> eventIds, RepositoryCallback<CalendarSyncResult> callback) {
        CalendarSyncRequest request = new CalendarSyncRequest(eventIds, projectId);

        apiService.syncToGoogle(request).enqueue(new Callback<CalendarSyncResponse>() {
            @Override
            public void onResponse(Call<CalendarSyncResponse> call, Response<CalendarSyncResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CalendarSyncResult result = CalendarMapper.toDomain(response.body());
                    Log.d(TAG, "Synced to Google: " + result.getSyncedCount() + " succeeded, " + result.getFailedCount() + " failed");
                    callback.onSuccess(result);
                } else {
                    String errorMsg = "Failed to sync events to Google: " + response.code();
                    Log.e(TAG, errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<CalendarSyncResponse> call, Throwable t) {
                String errorMsg = "Network error syncing to Google: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                callback.onError(errorMsg);
            }
        });
    }

    @Override
    public void syncEventsFromGoogle(String projectId, String timeMin, String timeMax, RepositoryCallback<List<CalendarEvent>> callback) {
        apiService.syncFromGoogle(projectId, timeMin, timeMax).enqueue(new Callback<com.example.tralalero.data.remote.dto.calendar.SyncFromGoogleResponse>() {
            @Override
            public void onResponse(Call<com.example.tralalero.data.remote.dto.calendar.SyncFromGoogleResponse> call, Response<com.example.tralalero.data.remote.dto.calendar.SyncFromGoogleResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.example.tralalero.data.remote.dto.calendar.SyncFromGoogleResponse syncResponse = response.body();
                    
                    // ✅ FIX: Check success flag and handle gracefully
                    if (syncResponse.isSuccess() && syncResponse.getEvents() != null) {
                        List<CalendarEvent> events = CalendarMapper.toDomainList(syncResponse.getEvents());
                        Log.d(TAG, "✅ Imported " + events.size() + " events from Google Calendar");
                        callback.onSuccess(events);
                    } else {
                        // Check if user hasn't connected Google Calendar
                        String message = syncResponse.getMessage();
                        if (message != null && message.contains("not connected")) {
                            Log.w(TAG, "⚠️ Google Calendar not connected: " + message);
                            // Use special error code to trigger UI prompt
                            callback.onError("CALENDAR_NOT_CONNECTED");
                        } else {
                            Log.e(TAG, "❌ Failed to sync: " + message);
                            callback.onError(message != null ? message : "Failed to sync events");
                        }
                    }
                } else {
                    String errorMsg = "Failed to sync events from Google: " + response.code();
                    Log.e(TAG, errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<com.example.tralalero.data.remote.dto.calendar.SyncFromGoogleResponse> call, Throwable t) {
                String errorMsg = "Network error syncing from Google: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                callback.onError(errorMsg);
            }
        });
    }

}
