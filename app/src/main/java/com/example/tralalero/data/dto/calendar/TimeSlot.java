package com.example.tralalero.data.dto.calendar;

import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Time slot suggestion from Meeting Scheduler API
 */
public class TimeSlot {
    
    @SerializedName("start")
    private String startTime;
    
    @SerializedName("end")
    private String endTime;
    
    @SerializedName("score")
    private double score;
    
    @SerializedName("availableUsers")
    private List<String> availableUsers;
    
    // Constructors
    public TimeSlot() {}
    
    public TimeSlot(String startTime, String endTime, double score, List<String> availableUsers) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.score = score;
        this.availableUsers = availableUsers;
    }
    
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
    
    public List<String> getAvailableUsers() {
        return availableUsers;
    }
    
    public void setAvailableUsers(List<String> availableUsers) {
        this.availableUsers = availableUsers;
    }
    
    // Helper methods
    public String getFormattedDate() {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault());
            Date date = inputFormat.parse(startTime);
            return date != null ? outputFormat.format(date) : startTime;
        } catch (ParseException e) {
            return startTime;
        }
    }
    
    public String getFormattedTimeRange() {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            
            Date startDate = inputFormat.parse(startTime);
            Date endDate = inputFormat.parse(endTime);
            
            if (startDate != null && endDate != null) {
                return outputFormat.format(startDate) + " - " + outputFormat.format(endDate);
            }
            return startTime + " - " + endTime;
        } catch (ParseException e) {
            return startTime + " - " + endTime;
        }
    }
    
    public String getScoreBadge() {
        if (score >= 80) {
            return "Excellent";
        } else if (score >= 60) {
            return "Good";
        } else if (score >= 40) {
            return "Fair";
        } else {
            return "Poor";
        }
    }
    
    public int getScoreColor() {
        if (score >= 80) {
            return android.graphics.Color.parseColor("#4CAF50"); // Green
        } else if (score >= 60) {
            return android.graphics.Color.parseColor("#8BC34A"); // Light Green
        } else if (score >= 40) {
            return android.graphics.Color.parseColor("#FFC107"); // Amber
        } else {
            return android.graphics.Color.parseColor("#F44336"); // Red
        }
    }
}
