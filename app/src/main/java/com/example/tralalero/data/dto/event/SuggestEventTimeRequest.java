package com.example.tralalero.data.dto.event;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Request DTO for suggesting meeting time slots
 * POST /events/projects/:projectId/suggest-times
 */
public class SuggestEventTimeRequest {
    
    @SerializedName("startDate")
    private String startDate; // ISO 8601: "2025-12-09T00:00:00Z"
    
    @SerializedName("endDate")
    private String endDate; // ISO 8601: "2025-12-16T23:59:59Z"
    
    @SerializedName("durationMinutes")
    private int durationMinutes; // e.g., 30
    
    @SerializedName("participantIds")
    private List<String> participantIds;
    
    public SuggestEventTimeRequest() {}
    
    public SuggestEventTimeRequest(String startDate, String endDate, int durationMinutes, List<String> participantIds) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.durationMinutes = durationMinutes;
        this.participantIds = participantIds;
    }
    
    // Getters and Setters
    public String getStartDate() {
        return startDate;
    }
    
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
    
    public String getEndDate() {
        return endDate;
    }
    
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
    
    public int getDurationMinutes() {
        return durationMinutes;
    }
    
    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
    
    public List<String> getParticipantIds() {
        return participantIds;
    }
    
    public void setParticipantIds(List<String> participantIds) {
        this.participantIds = participantIds;
    }
}
