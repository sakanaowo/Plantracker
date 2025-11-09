package com.example.tralalero.data.dto.calendar;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Request DTO for creating a meeting
 */
public class CreateMeetingRequest {
    
    @SerializedName("attendeeIds")
    private List<String> attendeeIds;
    
    @SerializedName("timeSlot")
    private TimeSlotSimple timeSlot;
    
    @SerializedName("summary")
    private String summary;
    
    @SerializedName("description")
    private String description;
    
    public CreateMeetingRequest() {}
    
    public CreateMeetingRequest(List<String> attendeeIds, TimeSlotSimple timeSlot, 
                               String summary, String description) {
        this.attendeeIds = attendeeIds;
        this.timeSlot = timeSlot;
        this.summary = summary;
        this.description = description;
    }
    
    // Getters and Setters
    public List<String> getAttendeeIds() {
        return attendeeIds;
    }
    
    public void setAttendeeIds(List<String> attendeeIds) {
        this.attendeeIds = attendeeIds;
    }
    
    public TimeSlotSimple getTimeSlot() {
        return timeSlot;
    }
    
    public void setTimeSlot(TimeSlotSimple timeSlot) {
        this.timeSlot = timeSlot;
    }
    
    public String getSummary() {
        return summary;
    }
    
    public void setSummary(String summary) {
        this.summary = summary;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Simplified TimeSlot for request body
     */
    public static class TimeSlotSimple {
        @SerializedName("start")
        private String start;
        
        @SerializedName("end")
        private String end;
        
        @SerializedName("availableUsers")
        private List<String> availableUsers;
        
        public TimeSlotSimple() {}
        
        public TimeSlotSimple(String start, String end, List<String> availableUsers) {
            this.start = start;
            this.end = end;
            this.availableUsers = availableUsers;
        }
        
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
    }
}
