package com.example.tralalero.data.dto.event;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Single time slot suggestion
 */
public class TimeSlotSuggestion {
    
    @SerializedName("start")
    private String startTime; // ISO 8601: "2025-12-09T10:00:00Z"
    
    @SerializedName("end")
    private String endTime; // ISO 8601: "2025-12-09T10:30:00Z"
    
    @SerializedName("score")
    private double score; // 0.0 - 1.0 (e.g., 0.95 = 95%)
    
    @SerializedName("scoreLabel")
    private String scoreLabel; // "Excellent", "Good", "Fair"
    
    @SerializedName("availableUsers")
    private List<String> availableUserIds;
    
    @SerializedName("unavailableUsers")
    private List<String> busyUserIds;
    
    public TimeSlotSuggestion() {}
    
    // Getters and Setters
    public String getStartTime() {
        return startTime;
    }
    
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
    
    public String getEndTime() {
        return endTime;
    }
    
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
    
    public double getScore() {
        return score;
    }
    
    public void setScore(double score) {
        this.score = score;
    }
    
    public String getScoreLabel() {
        return scoreLabel;
    }
    
    public void setScoreLabel(String scoreLabel) {
        this.scoreLabel = scoreLabel;
    }
    
    public List<String> getAvailableUserIds() {
        return availableUserIds;
    }
    
    public void setAvailableUserIds(List<String> availableUserIds) {
        this.availableUserIds = availableUserIds;
    }
    
    public List<String> getBusyUserIds() {
        return busyUserIds;
    }
    
    public void setBusyUserIds(List<String> busyUserIds) {
        this.busyUserIds = busyUserIds;
    }
    
    /**
     * Get score as percentage (0-100)
     */
    public int getScorePercent() {
        return (int) Math.round(score * 100);
    }
}
