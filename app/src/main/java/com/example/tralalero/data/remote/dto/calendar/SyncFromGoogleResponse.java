package com.example.tralalero.data.remote.dto.calendar;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Response from POST /calendar/sync/from-google endpoint
 * Includes success status, events list, count, and optional message
 */
public class SyncFromGoogleResponse {
    
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("events")
    private List<CalendarEventDTO> events;
    
    @SerializedName("count")
    private int count;
    
    @SerializedName("message")
    private String message;
    
    public SyncFromGoogleResponse() {}
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public List<CalendarEventDTO> getEvents() {
        return events;
    }
    
    public void setEvents(List<CalendarEventDTO> events) {
        this.events = events;
    }
    
    public int getCount() {
        return count;
    }
    
    public void setCount(int count) {
        this.count = count;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
