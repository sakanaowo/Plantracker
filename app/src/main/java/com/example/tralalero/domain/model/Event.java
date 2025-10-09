package com.example.tralalero.domain.model;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Domain model for Event
 * Represents a calendar event or meeting
 */
public class Event {
    private final String id;
    private final String projectId;
    private final String title;
    private final Date startAt;
    private final Date endAt;
    private final String location;
    private final String meetLink;
    private final String createdBy;
    private final Date createdAt;
    private final Date updatedAt;

    public Event(String id, String projectId, String title, Date startAt, Date endAt,
                 String location, String meetLink, String createdBy,
                 Date createdAt, Date updatedAt) {
        this.id = id;
        this.projectId = projectId;
        this.title = title;
        this.startAt = startAt;
        this.endAt = endAt;
        this.location = location;
        this.meetLink = meetLink;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public String getId() { return id; }
    public String getProjectId() { return projectId; }
    public String getTitle() { return title; }
    public Date getStartAt() { return startAt; }
    public Date getEndAt() { return endAt; }
    public String getLocation() { return location; }
    public String getMeetLink() { return meetLink; }
    public String getCreatedBy() { return createdBy; }
    public Date getCreatedAt() { return createdAt; }
    public Date getUpdatedAt() { return updatedAt; }

    // Business logic
    public boolean hasLocation() {
        return location != null && !location.trim().isEmpty();
    }

    public boolean hasMeetLink() {
        return meetLink != null && !meetLink.trim().isEmpty();
    }

    public boolean isOnline() {
        return hasMeetLink();
    }

    public boolean isInPerson() {
        return hasLocation() && !hasMeetLink();
    }

    public boolean isHybrid() {
        return hasLocation() && hasMeetLink();
    }

    public boolean isUpcoming() {
        if (startAt == null) return false;
        return new Date().before(startAt);
    }

    public boolean isOngoing() {
        if (startAt == null || endAt == null) return false;
        Date now = new Date();
        return now.after(startAt) && now.before(endAt);
    }

    public boolean isPast() {
        if (endAt == null) return false;
        return new Date().after(endAt);
    }

    public long getDurationMinutes() {
        if (startAt == null || endAt == null) return 0;
        long diff = endAt.getTime() - startAt.getTime();
        return TimeUnit.MILLISECONDS.toMinutes(diff);
    }

    public String getDurationFormatted() {
        long minutes = getDurationMinutes();
        if (minutes < 60) {
            return minutes + " min";
        } else {
            long hours = minutes / 60;
            long mins = minutes % 60;
            if (mins == 0) {
                return hours + " hr";
            } else {
                return hours + " hr " + mins + " min";
            }
        }
    }

    public long getTimeUntilStartMinutes() {
        if (startAt == null) return 0;
        long diff = startAt.getTime() - new Date().getTime();
        return TimeUnit.MILLISECONDS.toMinutes(diff);
    }

    public boolean isStartingSoon(int thresholdMinutes) {
        long minutes = getTimeUntilStartMinutes();
        return minutes > 0 && minutes <= thresholdMinutes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return id != null && id.equals(event.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
