package com.example.tralalero.data.dto.event;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Response DTO for suggest meeting times
 */
public class SuggestEventTimeResponse {
    
    @SerializedName("suggestions")
    private List<TimeSlotSuggestion> suggestions;
    
    @SerializedName("totalParticipants")
    private int totalParticipants;
    
    public SuggestEventTimeResponse() {}
    
    // Getters and Setters
    public List<TimeSlotSuggestion> getSuggestions() {
        return suggestions;
    }
    
    public void setSuggestions(List<TimeSlotSuggestion> suggestions) {
        this.suggestions = suggestions;
    }
    
    public int getTotalParticipants() {
        return totalParticipants;
    }
    
    public void setTotalParticipants(int totalParticipants) {
        this.totalParticipants = totalParticipants;
    }
}
