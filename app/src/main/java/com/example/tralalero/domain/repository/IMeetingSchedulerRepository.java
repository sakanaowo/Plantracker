package com.example.tralalero.domain.repository;

import com.example.tralalero.domain.model.CreateMeetingRequest;
import com.example.tralalero.domain.model.MeetingResponse;
import com.example.tralalero.domain.model.MeetingTimeSuggestion;
import com.example.tralalero.domain.model.SuggestMeetingTimeRequest;

/**
 * Repository interface for Meeting Scheduler functionality
 */
public interface IMeetingSchedulerRepository {
    
    /**
     * Suggest meeting times based on participants' availability
     * @param request Contains userIds, date range, duration
     * @param callback Callback with suggestions or error
     */
    void suggestMeetingTimes(
        SuggestMeetingTimeRequest request,
        RepositoryCallback<MeetingTimeSuggestion> callback
    );
    
    /**
     * Create a meeting event with Google Meet link
     * @param request Contains attendeeIds, timeSlot, summary, description
     * @param callback Callback with meeting details or error
     */
    void createMeeting(
        CreateMeetingRequest request,
        RepositoryCallback<MeetingResponse> callback
    );
    
    /**
     * Generic callback interface for repository operations
     */
    interface RepositoryCallback<T> {
        void onSuccess(T data);
        void onError(String error);
    }
}
