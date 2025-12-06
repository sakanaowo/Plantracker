package com.example.tralalero.domain.model;

import java.util.Date;
import java.util.List;
import java.io.Serializable;

public class ProjectEvent implements Serializable {
    private String id;
    private String projectId;
    private String title;
    private String description;
    private Date date;
    private String time;
    private int duration; // in minutes
    private String type; // MEETING, MILESTONE, OTHER
    private String recurrence; // NONE, DAILY, WEEKLY, BIWEEKLY, MONTHLY
    private List<String> attendeeIds;
    private int attendeeCount;
    private String meetLink;
    private String meetingLink; // Alternative name for meetLink
    private String location; // Location for milestones/other events
    private String startAt; // ISO 8601 format with timezone
    private String endAt; // ISO 8601 format with timezone
    private boolean createGoogleMeet;
    private String calendarEventId;
    private String status; // ACTIVE, CANCELLED, COMPLETED
    private Date cancelledAt;
    private String cancelledBy;
    private String cancellationReason;
    private Date createdAt;
    private String createdBy;
    
    // Constructors
    public ProjectEvent() {}
    
    public ProjectEvent(String id, String projectId, String title, String description,
                       Date date, String time, int duration, String type, String recurrence,
                       List<String> attendeeIds, int attendeeCount, String meetLink,
                       boolean createGoogleMeet, String calendarEventId,
                       Date createdAt, String createdBy) {
        this.id = id;
        this.projectId = projectId;
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.duration = duration;
        this.type = type;
        this.recurrence = recurrence;
        this.attendeeIds = attendeeIds;
        this.attendeeCount = attendeeCount;
        this.meetLink = meetLink;
        this.createGoogleMeet = createGoogleMeet;
        this.calendarEventId = calendarEventId;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
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
    
    public Date getDate() {
        return date;
    }
    
    public void setDate(Date date) {
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
    
    public int getAttendeeCount() {
        return attendeeCount;
    }
    
    public void setAttendeeCount(int attendeeCount) {
        this.attendeeCount = attendeeCount;
    }
    
    public String getMeetLink() {
        return meetLink;
    }
    
    public void setMeetLink(String meetLink) {
        this.meetLink = meetLink;
    }
    
    public String getMeetingLink() {
        return meetingLink;
    }
    
    public void setMeetingLink(String meetingLink) {
        this.meetingLink = meetingLink;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
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
    
    public boolean isCreateGoogleMeet() {
        return createGoogleMeet;
    }
    
    public void setCreateGoogleMeet(boolean createGoogleMeet) {
        this.createGoogleMeet = createGoogleMeet;
    }
    
    public String getCalendarEventId() {
        return calendarEventId;
    }
    
    public void setCalendarEventId(String calendarEventId) {
        this.calendarEventId = calendarEventId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Date getCancelledAt() {
        return cancelledAt;
    }
    
    public void setCancelledAt(Date cancelledAt) {
        this.cancelledAt = cancelledAt;
    }
    
    public String getCancelledBy() {
        return cancelledBy;
    }
    
    public void setCancelledBy(String cancelledBy) {
        this.cancelledBy = cancelledBy;
    }
    
    public String getCancellationReason() {
        return cancellationReason;
    }
    
    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
