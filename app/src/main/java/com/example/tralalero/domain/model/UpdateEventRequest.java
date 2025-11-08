package com.example.tralalero.domain.model;

import java.util.List;

/**
 * Request model for updating an event
 */
public class UpdateEventRequest {
    private String title;
    private String description;
    private String date;
    private String time;
    private Integer duration;
    private String type;
    private String recurrence;
    private List<String> attendeeIds;
    private Boolean createGoogleMeet;
    
    // Constructors
    public UpdateEventRequest() {}
    
    public UpdateEventRequest(String title, String description, String date, String time,
                             Integer duration, String type, String recurrence,
                             List<String> attendeeIds, Boolean createGoogleMeet) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.duration = duration;
        this.type = type;
        this.recurrence = recurrence;
        this.attendeeIds = attendeeIds;
        this.createGoogleMeet = createGoogleMeet;
    }
    
    // Getters and Setters
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    public String getTime() {
        return time;
    }
    
    public void setTime(String time) {
        this.time = time;
    }
    
    public Integer getDuration() {
        return duration;
    }
    
    public void setDuration(Integer duration) {
        this.duration = duration;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getRecurrence() {
        return recurrence;
    }
    
    public void setRecurrence(String recurrence) {
        this.recurrence = recurrence;
    }
    
    public List<String> getAttendeeIds() {
        return attendeeIds;
    }
    
    public void setAttendeeIds(List<String> attendeeIds) {
        this.attendeeIds = attendeeIds;
    }
    
    public Boolean getCreateGoogleMeet() {
        return createGoogleMeet;
    }
    
    public void setCreateGoogleMeet(Boolean createGoogleMeet) {
        this.createGoogleMeet = createGoogleMeet;
    }
}
