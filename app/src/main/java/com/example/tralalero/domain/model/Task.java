package com.example.tralalero.domain.model;

import java.util.Date;

public class Task {
    private final String id;
    private final String projectId;
    private final String boardId;
    private final String title;
    private final String description;
    private final String issueKey; // e.g., "PLAN-123"

    // Type and status
    private final TaskType type;
    private final TaskStatus status;
    private final TaskPriority priority;

    // Position for ordering
    private final double position;

    // Relationships
    private final String assigneeId;
    private final String createdBy;
    private final String sprintId;
    private final String epicId;
    private final String parentTaskId;

    // Time tracking
    private final Date startAt;
    private final Date dueAt;
    private final Integer storyPoints;
    private final Integer originalEstimateSec;
    private final Integer remainingEstimateSec;

    // Timestamps
    private final Date createdAt;
    private final Date updatedAt;

    // Constructor
    public Task(String id, String projectId, String boardId, String title, String description,
                String issueKey, TaskType type, TaskStatus status, TaskPriority priority,
                double position, String assigneeId, String createdBy, String sprintId,
                String epicId, String parentTaskId, Date startAt, Date dueAt,
                Integer storyPoints, Integer originalEstimateSec, Integer remainingEstimateSec,
                Date createdAt, Date updatedAt) {
        this.id = id;
        this.projectId = projectId;
        this.boardId = boardId;
        this.title = title;
        this.description = description;
        this.issueKey = issueKey;
        this.type = type;
        this.status = status;
        this.priority = priority;
        this.position = position;
        this.assigneeId = assigneeId;
        this.createdBy = createdBy;
        this.sprintId = sprintId;
        this.epicId = epicId;
        this.parentTaskId = parentTaskId;
        this.startAt = startAt;
        this.dueAt = dueAt;
        this.storyPoints = storyPoints;
        this.originalEstimateSec = originalEstimateSec;
        this.remainingEstimateSec = remainingEstimateSec;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getBoardId() {
        return boardId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getIssueKey() {
        return issueKey;
    }

    public TaskType getType() {
        return type;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public double getPosition() {
        return position;
    }

    public String getAssigneeId() {
        return assigneeId;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getSprintId() {
        return sprintId;
    }

    public String getEpicId() {
        return epicId;
    }

    public String getParentTaskId() {
        return parentTaskId;
    }

    public Date getStartAt() {
        return startAt;
    }

    public Date getDueAt() {
        return dueAt;
    }

    public Integer getStoryPoints() {
        return storyPoints;
    }

    public Integer getOriginalEstimateSec() {
        return originalEstimateSec;
    }

    public Integer getRemainingEstimateSec() {
        return remainingEstimateSec;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    // Business logic methods
    public boolean isAssigned() {
        return assigneeId != null && !assigneeId.isEmpty();
    }

    public boolean hasDescription() {
        return description != null && !description.trim().isEmpty();
    }

    public boolean isOverdue() {
        if (dueAt == null) return false;
        return new Date().after(dueAt) && !isDone();
    }

    public boolean isDone() {
        return status == TaskStatus.DONE;
    }

    public boolean isInProgress() {
        return status == TaskStatus.IN_PROGRESS;
    }

    public boolean isSubtask() {
        return type == TaskType.SUBTASK || parentTaskId != null;
    }

    public boolean isEpic() {
        return type == TaskType.EPIC;
    }

    public boolean belongsToEpic() {
        return epicId != null && !epicId.isEmpty();
    }

    public boolean isInSprint() {
        return sprintId != null && !sprintId.isEmpty();
    }

    public boolean hasStoryPoints() {
        return storyPoints != null && storyPoints > 0;
    }

    public int getEstimatedHours() {
        if (originalEstimateSec == null) return 0;
        return originalEstimateSec / 3600;
    }

    public int getRemainingHours() {
        if (remainingEstimateSec == null) return 0;
        return remainingEstimateSec / 3600;
    }

    public int getProgressPercentage() {
        if (originalEstimateSec == null || originalEstimateSec == 0) return 0;
        if (remainingEstimateSec == null) return 0;

        int worked = originalEstimateSec - remainingEstimateSec;
        return (worked * 100) / originalEstimateSec;
    }

    @Override
    public String toString() {
        return "Task{issueKey='" + issueKey + "', title='" + title + "', status=" + status + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id != null && id.equals(task.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    // Enums
    public enum TaskType {
        TASK, STORY, BUG, EPIC, SUBTASK
    }

    public enum TaskStatus {
        TO_DO, IN_PROGRESS, IN_REVIEW, DONE
    }

    public enum TaskPriority {
        LOW, MEDIUM, HIGH
    }
}
