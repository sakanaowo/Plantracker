package com.example.tralalero.domain.model;

import java.io.Serializable;

/**
 * Model for event reminders sent to users
 */
public class EventReminder implements Serializable {
    private String id;
    private String eventId;
    private String eventTitle;
    private String eventDate;
    private String eventTime;
    private String projectId;
    private String projectName;
    private String senderName;
    private String message;
    private long timestamp;
    private boolean isRead;
    
    public EventReminder() {}
    
    public EventReminder(String id, String eventId, String eventTitle, String eventDate, 
                        String eventTime, String projectId, String projectName, 
                        String senderName, String message, long timestamp, boolean isRead) {
        this.id = id;
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.projectId = projectId;
        this.projectName = projectName;
        this.senderName = senderName;
        this.message = message;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    
    public String getEventTitle() { return eventTitle; }
    public void setEventTitle(String eventTitle) { this.eventTitle = eventTitle; }
    
    public String getEventDate() { return eventDate; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }
    
    public String getEventTime() { return eventTime; }
    public void setEventTime(String eventTime) { this.eventTime = eventTime; }
    
    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }
    
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}
