package com.example.tralalero.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.tralalero.domain.model.CreateMeetingRequest;
import com.example.tralalero.domain.model.MeetingResponse;
import com.example.tralalero.domain.model.MeetingTimeSuggestion;
import com.example.tralalero.domain.model.SuggestMeetingTimeRequest;
import com.example.tralalero.data.remote.api.MeetingSchedulerApiService;
import com.example.tralalero.domain.repository.IMeetingSchedulerRepository;
import com.example.tralalero.network.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Implementation of Meeting Scheduler repository
 */
public class MeetingSchedulerRepositoryImpl implements IMeetingSchedulerRepository {
    
    private static final String TAG = "MeetingSchedulerRepo";
    private final MeetingSchedulerApiService apiService;
    
    public MeetingSchedulerRepositoryImpl(Context context) {
        this.apiService = ApiClient.get()
            .create(MeetingSchedulerApiService.class);
    }
    
    @Override
    public void suggestMeetingTimes(
        SuggestMeetingTimeRequest request,
        RepositoryCallback<MeetingTimeSuggestion> callback
    ) {
        Log.d(TAG, "Suggesting meeting times for " + request.getUserIds().size() + " users");
        
        apiService.suggestMeetingTimes(request).enqueue(new Callback<MeetingTimeSuggestion>() {
            @Override
            public void onResponse(@NonNull Call<MeetingTimeSuggestion> call, 
                                 @NonNull Response<MeetingTimeSuggestion> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Successfully got " + response.body().getSuggestions().size() + " suggestions");
                    callback.onSuccess(response.body());
                } else {
                    // Try to parse error body
                    String errorMsg = "Failed to suggest meeting times: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            if (!errorBody.isEmpty()) {
                                errorMsg = errorBody;
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to parse error body", e);
                    }
                    Log.e(TAG, errorMsg);
                    callback.onError(errorMsg);
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<MeetingTimeSuggestion> call, @NonNull Throwable t) {
                String error = "Network error: " + t.getMessage();
                Log.e(TAG, error, t);
                callback.onError(error);
            }
        });
    }
    
    @Override
    public void createMeeting(
        CreateMeetingRequest request,
        RepositoryCallback<MeetingResponse> callback
    ) {
        Log.d(TAG, "Creating meeting: " + request.getSummary());
        
        apiService.createMeeting(request).enqueue(new Callback<MeetingResponse>() {
            @Override
            public void onResponse(@NonNull Call<MeetingResponse> call, 
                                 @NonNull Response<MeetingResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Meeting created successfully with ID: " + response.body().getEventId());
                    callback.onSuccess(response.body());
                } else {
                    // Try to parse error body
                    String errorMsg = "Failed to create meeting: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            if (!errorBody.isEmpty()) {
                                errorMsg = errorBody;
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to parse error body", e);
                    }
                    Log.e(TAG, errorMsg);
                    callback.onError(errorMsg);
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<MeetingResponse> call, @NonNull Throwable t) {
                String error = "Network error: " + t.getMessage();
                Log.e(TAG, error, t);
                callback.onError(error);
            }
        });
    }
}
