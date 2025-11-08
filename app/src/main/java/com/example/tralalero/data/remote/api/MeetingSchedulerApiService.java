package com.example.tralalero.data.remote.api;

import com.example.tralalero.domain.model.CreateMeetingRequest;
import com.example.tralalero.domain.model.MeetingResponse;
import com.example.tralalero.domain.model.MeetingTimeSuggestion;
import com.example.tralalero.domain.model.SuggestMeetingTimeRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * API interface for Meeting Scheduler features
 * Handles meeting time suggestions and event creation with Google Meet integration
 */
public interface MeetingSchedulerApiService {

    /**
     * Suggest meeting times based on participants' Google Calendar availability
     * Uses Google Calendar Free/Busy API to find optimal time slots
     *
     * @param request Contains userIds, date range, duration, and max suggestions
     * @return List of suggested time slots sorted by availability score
     */
    @POST("calendar/meetings/suggest-times")
    Call<MeetingTimeSuggestion> suggestMeetingTimes(@Body SuggestMeetingTimeRequest request);

    /**
     * Create a meeting event with automatic Google Meet link generation
     * Event is created in organizer's calendar and invites sent to attendees
     *
     * @param request Contains attendeeIds, timeSlot, summary, and optional description
     * @return Meeting details including Google Meet link and calendar event link
     */
    @POST("calendar/meetings/create")
    Call<MeetingResponse> createMeeting(@Body CreateMeetingRequest request);
}
