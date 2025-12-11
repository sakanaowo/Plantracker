package com.example.tralalero.domain.model;

import java.util.List;

/**
 * Domain model for calendar event from Google Calendar
 */
public class CalendarEvent {
    private String id;
    private String googleEventId;
    private String title;
    private String description;
    private String startAt;
    private String endAt;
    private String location;
    private String meetLink;
    private List<String> attendees;
    private String status;
    private String createdAt;
    private String updatedAt;
    private String projectId; // ✅ Added for project filtering
    private String eventType; // ✅ MEETING, OTHER, TASK_REMINDER, etc.

    public CalendarEvent() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGoogleEventId() {
        return googleEventId;
    }

    public void setGoogleEventId(String googleEventId) {
        this.googleEventId = googleEventId;
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

    public String getStartAt() {
        return startAt;
    }

    public void setStartAt(String startAt) {
        this.startAt = startAt;
    }

    public String getEndAt() {
        return endAt;
    }

    public void setEndAt(String endAt) {
        this.endAt = endAt;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getMeetLink() {
        return meetLink;
    }

    public void setMeetLink(String meetLink) {
        this.meetLink = meetLink;
    }

    public List<String> getAttendees() {
        return attendees;
    }

    public void setAttendees(List<String> attendees) {
        this.attendees = attendees;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    /**
     * Check if this is a task (converted from task reminder)
     * Tasks are identified by:
     * 1. Title starting with "⏰ Nhắc nhở:"
     * 2. EventType containing "TASK" or "REMINDER"
     * 3. EventType is "OTHER" with reminder-like title
     */
    public boolean isTask() {
        // Check title pattern (backend adds "⏰ Nhắc nhở:" prefix for tasks)
        if (title != null && (title.startsWith("⏰ Nhắc nhở:") || title.startsWith("⏰"))) {
            return true;
        }
        
        // Check eventType
        if (eventType != null) {
            String type = eventType.toUpperCase();
            if (type.contains("TASK") || type.contains("REMINDER")) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check if this is a regular calendar event (meeting, deadline, etc.)
     */
    public boolean isEvent() {
        return !isTask();
    }
    
    /**
     * Get clean title without reminder prefix
     */
    public String getCleanTitle() {
        if (title != null && title.startsWith("⏰ Nhắc nhở: ")) {
            return title.substring("⏰ Nhắc nhở: ".length());
        }
        return title;
    }
}
