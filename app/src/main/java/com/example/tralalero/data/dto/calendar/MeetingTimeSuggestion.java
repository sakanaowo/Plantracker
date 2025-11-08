package com.example.tralalero.data.dto.calendar;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Response DTO for meeting time suggestions
 */
public class MeetingTimeSuggestion {
    
    @SerializedName("suggestions")
    private List<TimeSlot> suggestions;
    
    @SerializedName("requestedDuration")
    private int requestedDuration;
    
    public MeetingTimeSuggestion() {}
    
    public MeetingTimeSuggestion(List<TimeSlot> suggestions, int requestedDuration) {
        this.suggestions = suggestions;
        this.requestedDuration = requestedDuration;
    }
    
    public List<TimeSlot> getSuggestions() {
        return suggestions;
    }
    
    public void setSuggestions(List<TimeSlot> suggestions) {
        this.suggestions = suggestions;
    }
    
    public int getRequestedDuration() {
        return requestedDuration;
    }
    
    public void setRequestedDuration(int requestedDuration) {
        this.requestedDuration = requestedDuration;
    }
}
