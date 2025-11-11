package com.example.tralalero.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.tralalero.App.App;
import com.example.tralalero.auth.remote.AuthManager;
import com.example.tralalero.data.mapper.CalendarMapper;
import com.example.tralalero.data.remote.api.CalendarApiService;
import com.example.tralalero.data.remote.dto.calendar.AuthUrlResponse;
import com.example.tralalero.data.remote.dto.calendar.CalendarConnectionStatusResponse;
import com.example.tralalero.data.remote.dto.calendar.CalendarEventDTO;
import com.example.tralalero.data.remote.dto.calendar.CalendarSyncRequest;
import com.example.tralalero.data.remote.dto.calendar.CalendarSyncResponse;
import com.example.tralalero.data.remote.dto.calendar.CallbackResponse;
import com.example.tralalero.domain.model.CalendarConnection;
import com.example.tralalero.domain.model.CalendarEvent;
import com.example.tralalero.domain.model.CalendarSyncResult;
import com.example.tralalero.domain.repository.ICalendarRepository;
import com.example.tralalero.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

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
    public void getAuthUrl(RepositoryCallback<String> callback) {
        apiService.getAuthUrl().enqueue(new Callback<AuthUrlResponse>() {
            @Override
            public void onResponse(Call<AuthUrlResponse> call, Response<AuthUrlResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String authUrl = response.body().getAuthUrl();
                    Log.d(TAG, "Got auth URL successfully");
                    callback.onSuccess(authUrl);
                } else {
                    String errorMsg = "Failed to get auth URL: " + response.code();
                    Log.e(TAG, errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<AuthUrlResponse> call, Throwable t) {
                String errorMsg = "Network error getting auth URL: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                callback.onError(errorMsg);
            }
        });
    }

    @Override
    public void handleOAuthCallback(String code, RepositoryCallback<String> callback) {
        apiService.handleCallback(code).enqueue(new Callback<CallbackResponse>() {
            @Override
            public void onResponse(Call<CallbackResponse> call, Response<CallbackResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CallbackResponse callbackResponse = response.body();
                    if (callbackResponse.isSuccess()) {
                        String connectedAt = callbackResponse.getConnectedAt();
                        Log.d(TAG, "OAuth callback handled successfully: " + callbackResponse.getMessage());
                        callback.onSuccess(connectedAt);
                    } else {
                        String errorMsg = "OAuth callback failed: " + callbackResponse.getMessage();
                        Log.e(TAG, errorMsg);
                        callback.onError(errorMsg);
                    }
                } else {
                    String errorMsg = "Failed to handle OAuth callback: " + response.code();
                    Log.e(TAG, errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<CallbackResponse> call, Throwable t) {
                String errorMsg = "Network error handling OAuth callback: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                callback.onError(errorMsg);
            }
        });
    }

    @Override
    public void getConnectionStatus(RepositoryCallback<CalendarConnection> callback) {
        apiService.getConnectionStatus().enqueue(new Callback<CalendarConnectionStatusResponse>() {
            @Override
            public void onResponse(Call<CalendarConnectionStatusResponse> call, Response<CalendarConnectionStatusResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CalendarConnection connection = CalendarMapper.toDomain(response.body());
                    Log.d(TAG, "Got connection status: connected=" + connection.isConnected());
                    callback.onSuccess(connection);
                } else {
                    String errorMsg = "Failed to get connection status: " + response.code();
                    Log.e(TAG, errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<CalendarConnectionStatusResponse> call, Throwable t) {
                String errorMsg = "Network error getting connection status: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                callback.onError(errorMsg);
            }
        });
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
                        // Return empty list if user hasn't connected Google Calendar
                        String message = syncResponse.getMessage();
                        if (message != null && message.contains("not connected")) {
                            Log.w(TAG, "⚠️ Google Calendar not connected: " + message);
                            callback.onSuccess(new ArrayList<>());  // Return empty list, not error
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

    @Override
    public void disconnectCalendar(RepositoryCallback<Void> callback) {
        apiService.disconnectCalendar().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Calendar disconnected successfully");
                    callback.onSuccess(null);
                } else {
                    String errorMsg = "Failed to disconnect calendar: " + response.code();
                    Log.e(TAG, errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                String errorMsg = "Network error disconnecting calendar: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                callback.onError(errorMsg);
            }
        });
    }
}
