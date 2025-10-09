package com.example.tralalero.domain.model;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Domain model for Time Entry
 * Tracks time spent working on a task
 */
public class TimeEntry {
    private final String id;
    private final String taskId;
    private final String userId;
    private final Date startAt;
    private final Date endAt;
    private final Integer durationSec; // Duration in seconds
    private final String note;
    private final Date createdAt;

    public TimeEntry(String id, String taskId, String userId, Date startAt,
                     Date endAt, Integer durationSec, String note, Date createdAt) {
        this.id = id;
        this.taskId = taskId;
        this.userId = userId;
        this.startAt = startAt;
        this.endAt = endAt;
        this.durationSec = durationSec;
        this.note = note;
        this.createdAt = createdAt;
    }

    // Getters
    public String getId() { return id; }
    public String getTaskId() { return taskId; }
    public String getUserId() { return userId; }
    public Date getStartAt() { return startAt; }
    public Date getEndAt() { return endAt; }
    public Integer getDurationSec() { return durationSec; }
    public String getNote() { return note; }
    public Date getCreatedAt() { return createdAt; }

    // Business logic
    public boolean isRunning() {
        return endAt == null;
    }

    public boolean isCompleted() {
        return endAt != null;
    }

    public int getDurationMinutes() {
        if (durationSec == null) return 0;
        return durationSec / 60;
    }

    public int getDurationHours() {
        if (durationSec == null) return 0;
        return durationSec / 3600;
    }

    public String getDurationFormatted() {
        if (durationSec == null || durationSec == 0) return "0m";

        int hours = durationSec / 3600;
        int minutes = (durationSec % 3600) / 60;

        if (hours > 0) {
            return hours + "h " + minutes + "m";
        } else {
            return minutes + "m";
        }
    }

    public long getCurrentDurationSec() {
        if (endAt != null && durationSec != null) {
            return durationSec;
        }

        if (startAt == null) return 0;

        long diff = new Date().getTime() - startAt.getTime();
        return TimeUnit.MILLISECONDS.toSeconds(diff);
    }

    public boolean hasNote() {
        return note != null && !note.trim().isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeEntry timeEntry = (TimeEntry) o;
        return id != null && id.equals(timeEntry.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
