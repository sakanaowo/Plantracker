package com.example.tralalero.data.dto.calendar;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Request DTO for suggesting meeting times
 */
public class SuggestMeetingTimeRequest {
    
    @SerializedName("userIds")
    private List<String> userIds;
    
    @SerializedName("startDate")
    private String startDate;
    
    @SerializedName("endDate")
    private String endDate;
    
    @SerializedName("durationMinutes")
    private int durationMinutes;
    
    @SerializedName("maxSuggestions")
    private int maxSuggestions;
    
    public SuggestMeetingTimeRequest() {
        this.maxSuggestions = 5; // Default value
    }
    
    public SuggestMeetingTimeRequest(List<String> userIds, String startDate, String endDate, 
                                    int durationMinutes, int maxSuggestions) {
        this.userIds = userIds;
        this.startDate = startDate;
        this.endDate = endDate;
        this.durationMinutes = durationMinutes;
        this.maxSuggestions = maxSuggestions;
    }
    
    // Getters and Setters
    public List<String> getUserIds() {
        return userIds;
    }
    
    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }
    
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
    
    public int getMaxSuggestions() {
        return maxSuggestions;
    }
    
    public void setMaxSuggestions(int maxSuggestions) {
        this.maxSuggestions = maxSuggestions;
    }
}
