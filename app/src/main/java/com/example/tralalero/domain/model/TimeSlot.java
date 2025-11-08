package com.example.tralalero.domain.model;

import java.util.List;

/**
 * Represents a suggested time slot for a meeting
 */
public class TimeSlot {
    private String start;  // ISO 8601 datetime string
    private String end;    // ISO 8601 datetime string
    private List<String> availableUsers; // User IDs who are free
    private int score;     // 0-100, percentage of users available

    public TimeSlot() {
    }

    public TimeSlot(String start, String end, List<String> availableUsers, int score) {
        this.start = start;
        this.end = end;
        this.availableUsers = availableUsers;
        this.score = score;
    }

    // Getters and Setters
    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public List<String> getAvailableUsers() {
        return availableUsers;
    }

    public void setAvailableUsers(List<String> availableUsers) {
        this.availableUsers = availableUsers;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    /**
     * Get formatted date string (e.g., "Nov 9, 2025")
     */
    public String getFormattedDate() {
        try {
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("MMM d, yyyy");
            java.util.Date date = inputFormat.parse(start);
            return outputFormat.format(date);
        } catch (Exception e) {
            return start;
        }
    }

    /**
     * Get formatted time range (e.g., "9:00 AM - 10:00 AM")
     */
    public String getFormattedTimeRange() {
        try {
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("h:mm a");
            java.util.Date startDate = inputFormat.parse(start);
            java.util.Date endDate = inputFormat.parse(end);
            return outputFormat.format(startDate) + " - " + outputFormat.format(endDate);
        } catch (Exception e) {
            return start + " - " + end;
        }
    }

    /**
     * Get score badge text (e.g., "100% available")
     */
    public String getScoreBadge() {
        return score + "% available";
    }
}
