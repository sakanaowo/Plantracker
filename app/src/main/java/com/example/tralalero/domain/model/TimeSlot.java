package com.example.tralalero.domain.model;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Represents a suggested time slot for a meeting
 */
public class TimeSlot {
    private static final String TAG = "TimeSlot";
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
        if (start == null || start.isEmpty()) {
            return "N/A";
        }
        
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            Date date = inputFormat.parse(start);
            return outputFormat.format(date);
        } catch (ParseException e) {
            Log.e(TAG, "Failed to parse date with milliseconds: " + start, e);
            // Try alternative ISO format without milliseconds
            try {
                SimpleDateFormat altFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                altFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
                Date date = altFormat.parse(start);
                return outputFormat.format(date);
            } catch (ParseException e2) {
                Log.e(TAG, "Failed to parse date with alternative format: " + start, e2);
                return "Invalid Date";
            }
        }
    }

    /**
     * Get formatted time range (e.g., "9:00 AM - 10:00 AM")
     */
    public String getFormattedTimeRange() {
        if (start == null || start.isEmpty() || end == null || end.isEmpty()) {
            return "N/A";
        }
        
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            SimpleDateFormat outputFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            Date startDate = inputFormat.parse(start);
            Date endDate = inputFormat.parse(end);
            return outputFormat.format(startDate) + " - " + outputFormat.format(endDate);
        } catch (ParseException e) {
            Log.e(TAG, "Failed to parse time range with milliseconds", e);
            // Try alternative ISO format without milliseconds
            try {
                SimpleDateFormat altFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                altFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                SimpleDateFormat outputFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
                Date startDate = altFormat.parse(start);
                Date endDate = altFormat.parse(end);
                return outputFormat.format(startDate) + " - " + outputFormat.format(endDate);
            } catch (ParseException e2) {
                Log.e(TAG, "Failed to parse time range with alternative format", e2);
                return "Invalid Time";
            }
        }
    }

    /**
     * Get score badge text (e.g., "100% available")
     */
    public String getScoreBadge() {
        return score + "% available";
    }
}
