package com.example.tralalero.domain.model;

import java.util.Date;
import java.util.List;

public class Task {
    private final String id;
    private final String projectId;
    private final String boardId;
    private final String boardName; // Board name from boards relation
    private final String title;
    private final String description;
    private final String issueKey; 

    private final TaskType type;
    private final TaskStatus status;
    private final TaskPriority priority;

    private final double position;

    private final String assigneeId;
    private final String createdBy;
    private final String sprintId;
    private final String epicId;
    private final String parentTaskId;

    private final Date startAt;
    private final Date dueAt;
    private final Integer storyPoints;
    private final Integer originalEstimateSec;
    private final Integer remainingEstimateSec;

    private final Date createdAt;
    private final Date updatedAt;
    
    // Calendar Sync Fields
    private final boolean calendarSyncEnabled;
    private final List<Integer> calendarReminderMinutes;
    private final String calendarEventId;
    private final Date calendarSyncedAt;
    
    // Labels
    private final List<Label> labels;
    
    // Assignees
    private final List<AssigneeInfo> assignees;

    public Task(String id, String projectId, String boardId, String title, String description,
                String issueKey, TaskType type, TaskStatus status, TaskPriority priority,
                double position, String assigneeId, String createdBy, String sprintId,
                String epicId, String parentTaskId, Date startAt, Date dueAt,
                Integer storyPoints, Integer originalEstimateSec, Integer remainingEstimateSec,
                Date createdAt, Date updatedAt) {
        this(id, projectId, boardId, null, title, description, issueKey, type, status, priority,
             position, assigneeId, createdBy, sprintId, epicId, parentTaskId, startAt, dueAt,
             storyPoints, originalEstimateSec, remainingEstimateSec, createdAt, updatedAt,
             false, null, null, null, null, null);
    }
    
    // Constructor WITHOUT boardName (backward compatibility)
    public Task(String id, String projectId, String boardId, String title, String description,
                String issueKey, TaskType type, TaskStatus status, TaskPriority priority,
                double position, String assigneeId, String createdBy, String sprintId,
                String epicId, String parentTaskId, Date startAt, Date dueAt,
                Integer storyPoints, Integer originalEstimateSec, Integer remainingEstimateSec,
                Date createdAt, Date updatedAt,
                boolean calendarSyncEnabled, List<Integer> calendarReminderMinutes,
                String calendarEventId, Date calendarSyncedAt, List<Label> labels, 
                List<AssigneeInfo> assignees) {
        this(id, projectId, boardId, null, title, description, issueKey, type, status, priority,
             position, assigneeId, createdBy, sprintId, epicId, parentTaskId, startAt, dueAt,
             storyPoints, originalEstimateSec, remainingEstimateSec, createdAt, updatedAt,
             calendarSyncEnabled, calendarReminderMinutes, calendarEventId, calendarSyncedAt, 
             labels, assignees);
    }
    
    // Constructor WITH boardName (full)
    public Task(String id, String projectId, String boardId, String boardName, String title, String description,
                String issueKey, TaskType type, TaskStatus status, TaskPriority priority,
                double position, String assigneeId, String createdBy, String sprintId,
                String epicId, String parentTaskId, Date startAt, Date dueAt,
                Integer storyPoints, Integer originalEstimateSec, Integer remainingEstimateSec,
                Date createdAt, Date updatedAt,
                boolean calendarSyncEnabled, List<Integer> calendarReminderMinutes,
                String calendarEventId, Date calendarSyncedAt, List<Label> labels, 
                List<AssigneeInfo> assignees) {
        this.id = id;
        this.projectId = projectId;
        this.boardId = boardId;
        this.boardName = boardName;
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
        this.calendarSyncEnabled = calendarSyncEnabled;
        this.calendarReminderMinutes = calendarReminderMinutes;
        this.calendarEventId = calendarEventId;
        this.calendarSyncedAt = calendarSyncedAt;
        this.labels = labels;
        this.assignees = assignees;
    }

    public String getId() {
        return id;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getBoardId() {
        return boardId;
    }
    
    public String getBoardName() {
        return boardName;
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
    
    // Calendar Sync Getters
    public boolean isCalendarSyncEnabled() {
        return calendarSyncEnabled;
    }
    
    public List<Integer> getCalendarReminderMinutes() {
        return calendarReminderMinutes;
    }
    
    public String getCalendarEventId() {
        return calendarEventId;
    }
    
    public Date getCalendarSyncedAt() {
        return calendarSyncedAt;
    }
    
    public boolean hasCalendarEvent() {
        return calendarEventId != null && !calendarEventId.isEmpty();
    }
    
    public List<Label> getLabels() {
        return labels;
    }
    
    public boolean hasLabels() {
        return labels != null && !labels.isEmpty();
    }
    
    public List<AssigneeInfo> getAssignees() {
        return assignees;
    }
    
    public boolean hasAssignees() {
        return assignees != null && !assignees.isEmpty();
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
