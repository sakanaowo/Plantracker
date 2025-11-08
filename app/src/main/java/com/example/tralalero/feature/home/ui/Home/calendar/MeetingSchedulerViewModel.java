package com.example.tralalero.feature.home.ui.Home.calendar;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tralalero.domain.model.CreateMeetingRequest;
import com.example.tralalero.domain.model.MeetingResponse;
import com.example.tralalero.domain.model.MeetingTimeSuggestion;
import com.example.tralalero.domain.model.SuggestMeetingTimeRequest;
import com.example.tralalero.domain.model.TimeSlot;
import com.example.tralalero.data.repository.MeetingSchedulerRepositoryImpl;
import com.example.tralalero.domain.repository.IMeetingSchedulerRepository;
import com.example.tralalero.util.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * ViewModel for Meeting Scheduler feature
 * Handles business logic for suggesting meeting times and creating meetings
 */
public class MeetingSchedulerViewModel extends AndroidViewModel {
    
    private static final String TAG = "MeetingSchedulerVM";
    
    private final IMeetingSchedulerRepository repository;
    
    // LiveData for selected members
    private final MutableLiveData<List<String>> selectedMemberIds = new MutableLiveData<>(new ArrayList<>());
    
    // LiveData for suggested time slots
    private final MutableLiveData<List<TimeSlot>> suggestedTimes = new MutableLiveData<>(new ArrayList<>());
    
    // LiveData for loading state
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    
    // LiveData for error messages
    private final MutableLiveData<String> error = new MutableLiveData<>();
    
    // LiveData for meeting creation success
    private final MutableLiveData<MeetingResponse> meetingCreated = new MutableLiveData<>();
    
    public MeetingSchedulerViewModel(@NonNull Application application) {
        super(application);
        this.repository = new MeetingSchedulerRepositoryImpl(application);
    }
    
    // Getters for LiveData
    public LiveData<List<String>> getSelectedMemberIds() {
        return selectedMemberIds;
    }
    
    public LiveData<List<TimeSlot>> getSuggestedTimes() {
        return suggestedTimes;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<String> getError() {
        return error;
    }
    
    public LiveData<MeetingResponse> getMeetingCreated() {
        return meetingCreated;
    }
    
    /**
     * Toggle member selection
     */
    public void toggleMemberSelection(String userId) {
        List<String> current = selectedMemberIds.getValue();
        if (current == null) current = new ArrayList<>();
        
        if (current.contains(userId)) {
            current.remove(userId);
        } else {
            current.add(userId);
        }
        
        selectedMemberIds.setValue(current);
        Log.d(TAG, "Selected members: " + current.size());
    }
    
    /**
     * Clear all selected members
     */
    public void clearSelectedMembers() {
        selectedMemberIds.setValue(new ArrayList<>());
    }
    
    /**
     * Suggest meeting times based on selected members and duration
     */
    public void suggestTimes(int durationMinutes, Calendar startDate, Calendar endDate) {
        // Validation
        List<String> userIds = selectedMemberIds.getValue();
        
        if (userIds == null || userIds.isEmpty()) {
            error.setValue("Please select at least one member");
            return;
        }
        
        if (durationMinutes < 15 || durationMinutes > 480) {
            error.setValue("Duration must be between 15 minutes and 8 hours");
            return;
        }
        
        if (startDate == null || endDate == null) {
            error.setValue("Please select valid date range");
            return;
        }
        
        if (!DateUtils.isValidDateRange(startDate, endDate)) {
            error.setValue("Start date must be before end date");
            return;
        }
        
        if (!DateUtils.isWithinMaxDays(startDate, endDate, 30)) {
            error.setValue("Date range cannot exceed 30 days");
            return;
        }
        
        isLoading.setValue(true);
        error.setValue(null);
        
        // Format dates to ISO 8601 using utility
        String startDateStr = DateUtils.formatToISO8601(startDate);
        String endDateStr = DateUtils.formatToISO8601(endDate);
        
        SuggestMeetingTimeRequest request = new SuggestMeetingTimeRequest(
            userIds,
            startDateStr,
            endDateStr,
            durationMinutes,
            5 // Max 5 suggestions
        );
        
        Log.d(TAG, "Requesting meeting suggestions for " + userIds.size() + " users, duration: " + durationMinutes);
        
        repository.suggestMeetingTimes(request, new IMeetingSchedulerRepository.RepositoryCallback<MeetingTimeSuggestion>() {
            @Override
            public void onSuccess(MeetingTimeSuggestion data) {
                isLoading.postValue(false);
                suggestedTimes.postValue(data.getSuggestions());
                Log.d(TAG, "Got " + data.getSuggestions().size() + " suggestions");
            }
            
            @Override
            public void onError(String errorMsg) {
                isLoading.postValue(false);
                error.postValue(errorMsg);
                suggestedTimes.postValue(new ArrayList<>());
            }
        });
    }
    
    /**
     * Create a meeting for the selected time slot
     */
    public void createMeeting(TimeSlot timeSlot, String title, String description) {
        List<String> attendeeIds = selectedMemberIds.getValue();
        
        if (attendeeIds == null || attendeeIds.isEmpty()) {
            error.setValue("No attendees selected");
            return;
        }
        
        if (title == null || title.trim().isEmpty()) {
            error.setValue("Meeting title is required");
            return;
        }
        
        isLoading.setValue(true);
        error.setValue(null);
        
        CreateMeetingRequest request = new CreateMeetingRequest(
            attendeeIds,
            timeSlot,
            title,
            description
        );
        
        Log.d(TAG, "Creating meeting: " + title);
        
        repository.createMeeting(request, new IMeetingSchedulerRepository.RepositoryCallback<MeetingResponse>() {
            @Override
            public void onSuccess(MeetingResponse data) {
                isLoading.postValue(false);
                meetingCreated.postValue(data);
                Log.d(TAG, "Meeting created with ID: " + data.getEventId());
            }
            
            @Override
            public void onError(String errorMsg) {
                isLoading.postValue(false);
                error.postValue(errorMsg);
            }
        });
    }
    
    /**
     * Clear error message
     */
    public void clearError() {
        error.setValue(null);
    }
}
