package com.example.tralalero.domain.model;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Domain model for Sprint
 * Time-boxed iteration in Scrum methodology
 */
public class Sprint {
    private final String id;
    private final String projectId;
    private final String name;
    private final String goal;
    private final Date startAt;
    private final Date endAt;
    private final SprintState state;
    private final Date createdAt;

    public Sprint(String id, String projectId, String name, String goal,
                  Date startAt, Date endAt, SprintState state, Date createdAt) {
        this.id = id;
        this.projectId = projectId;
        this.name = name;
        this.goal = goal;
        this.startAt = startAt;
        this.endAt = endAt;
        this.state = state;
        this.createdAt = createdAt;
    }

    // Getters
    public String getId() { return id; }
    public String getProjectId() { return projectId; }
    public String getName() { return name; }
    public String getGoal() { return goal; }
    public Date getStartAt() { return startAt; }
    public Date getEndAt() { return endAt; }
    public SprintState getState() { return state; }
    public Date getCreatedAt() { return createdAt; }

    // Business logic
    public boolean isPlanned() {
        return state == SprintState.PLANNED;
    }

    public boolean isActive() {
        return state == SprintState.ACTIVE;
    }

    public boolean isCompleted() {
        return state == SprintState.COMPLETED;
    }

    public boolean hasGoal() {
        return goal != null && !goal.trim().isEmpty();
    }

    public boolean hasStarted() {
        if (startAt == null) return false;
        return new Date().after(startAt) || new Date().equals(startAt);
    }

    public boolean hasEnded() {
        if (endAt == null) return false;
        return new Date().after(endAt);
    }

    public int getDurationDays() {
        if (startAt == null || endAt == null) return 0;
        long diff = endAt.getTime() - startAt.getTime();
        return (int) TimeUnit.MILLISECONDS.toDays(diff);
    }

    public int getRemainingDays() {
        if (endAt == null) return 0;
        if (hasEnded()) return 0;

        long diff = endAt.getTime() - new Date().getTime();
        return (int) TimeUnit.MILLISECONDS.toDays(diff);
    }

    public int getProgressPercentage() {
        if (startAt == null || endAt == null) return 0;

        long total = endAt.getTime() - startAt.getTime();
        long elapsed = new Date().getTime() - startAt.getTime();

        if (elapsed <= 0) return 0;
        if (elapsed >= total) return 100;

        return (int) ((elapsed * 100) / total);
    }

    public String getStateDisplay() {
        switch (state) {
            case PLANNED: return "Planned";
            case ACTIVE: return "Active";
            case COMPLETED: return "Completed";
            default: return "Unknown";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sprint sprint = (Sprint) o;
        return id != null && id.equals(sprint.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public enum SprintState {
        PLANNED, ACTIVE, COMPLETED
    }
}
