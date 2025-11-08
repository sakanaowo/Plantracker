package com.example.tralalero.domain.model;

import java.util.List;

/**
 * Request to suggest meeting times based on participants' availability
 */
public class SuggestMeetingTimeRequest {
    private List<String> userIds;
    private String startDate; // ISO 8601 format
    private String endDate;   // ISO 8601 format
    private Integer durationMinutes;
    private Integer maxSuggestions;

    public SuggestMeetingTimeRequest(List<String> userIds, String startDate, String endDate) {
        this.userIds = userIds;
        this.startDate = startDate;
        this.endDate = endDate;
        this.durationMinutes = 60; // Default 1 hour
        this.maxSuggestions = 5;   // Default 5 suggestions
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

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Integer getMaxSuggestions() {
        return maxSuggestions;
    }

    public void setMaxSuggestions(Integer maxSuggestions) {
        this.maxSuggestions = maxSuggestions;
    }
}
