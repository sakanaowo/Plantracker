package com.example.tralalero.domain.model;

import java.util.List;

public class CreateEventRequest {
    private String projectId;
    private String title;
    private String description;
    private String date;
    private String time;
    private int duration;
    private String type;
    private String recurrence;
    private List<String> attendeeIds;
    private boolean createGoogleMeet;
    
    // Constructors
    public CreateEventRequest() {}
    
    public CreateEventRequest(String projectId, String title, String description,
                             String date, String time, int duration, String type,
                             String recurrence, List<String> attendeeIds,
                             boolean createGoogleMeet) {
        this.projectId = projectId;
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
    public String getProjectId() {
        return projectId;
    }
    
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
    
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
    
    public int getDuration() {
        return duration;
    }
    
    public void setDuration(int duration) {
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
    
    public boolean isCreateGoogleMeet() {
        return createGoogleMeet;
    }
    
    public void setCreateGoogleMeet(boolean createGoogleMeet) {
        this.createGoogleMeet = createGoogleMeet;
    }
}
