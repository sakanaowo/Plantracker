package com.example.tralalero.domain.model;

import java.util.List;

/**
 * Request to create a meeting event with Google Meet link
 */
public class CreateMeetingRequest {
    private List<String> attendeeIds;
    private TimeSlot timeSlot;
    private String summary;
    private String description;

    public CreateMeetingRequest() {
    }

    public CreateMeetingRequest(List<String> attendeeIds, TimeSlot timeSlot, String summary) {
        this.attendeeIds = attendeeIds;
        this.timeSlot = timeSlot;
        this.summary = summary;
    }

    public CreateMeetingRequest(List<String> attendeeIds, TimeSlot timeSlot, String summary, String description) {
        this.attendeeIds = attendeeIds;
        this.timeSlot = timeSlot;
        this.summary = summary;
        this.description = description;
    }

    // Getters and Setters
    public List<String> getAttendeeIds() {
        return attendeeIds;
    }

    public void setAttendeeIds(List<String> attendeeIds) {
        this.attendeeIds = attendeeIds;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
