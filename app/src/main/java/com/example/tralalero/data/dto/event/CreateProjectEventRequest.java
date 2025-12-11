package com.example.tralalero.data.dto.event;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * DTO for creating project events with Google Meet
 */
public class CreateProjectEventRequest {
    
    @SerializedName("projectId")
    private String projectId;
    
    @SerializedName("title")
    private String title;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("date")
    private String date; // YYYY-MM-DD
    
    @SerializedName("time")
    private String time; // HH:mm
    
    @SerializedName("duration")
    private int duration; // minutes
    
    @SerializedName("type")
    private String type; // MEETING, MILESTONE, OTHER
    
    @SerializedName("recurrence")
    private String recurrence; // NONE, DAILY, WEEKLY, BIWEEKLY, MONTHLY
    
    @SerializedName("attendeeIds")
    private List<String> attendeeIds;
    
    @SerializedName("createGoogleMeet")
    private boolean createGoogleMeet;
    
    public CreateProjectEventRequest() {}
    
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
