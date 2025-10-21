package com.example.tralalero.data.local.database.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;
import java.util.Date;

@Entity(
    tableName = "tasks",
    indices = {
        @Index(value = "boardId"),
        @Index(value = "projectId"),
        @Index(value = "position")
    }
)
public class TaskEntity {
    
    @PrimaryKey
    @NonNull
    private String id;
    
    @NonNull
    private String projectId;
    
    @NonNull
    private String boardId;
    
    @NonNull
    private String title;
    
    private String description;
    private String issueKey;
    private String type;
    private String status;
    private String priority;
    private double position;
    private String assigneeId;
    private String createdBy;
    private String sprintId;
    private String epicId;
    private String parentTaskId;
    private Date startAt;
    private Date dueAt;
    private Integer storyPoints;
    private Integer originalEstimateSec;
    private Integer remainingEstimateSec;
    private Date createdAt;
    private Date updatedAt;
    
    public TaskEntity(@NonNull String id, @NonNull String projectId, @NonNull String boardId, @NonNull String title) {
        this.id = id;
        this.projectId = projectId;
        this.boardId = boardId;
        this.title = title;
    }
    
    @NonNull public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }
    
    @NonNull public String getProjectId() { return projectId; }
    public void setProjectId(@NonNull String projectId) { this.projectId = projectId; }
    
    @NonNull public String getBoardId() { return boardId; }
    public void setBoardId(@NonNull String boardId) { this.boardId = boardId; }
    
    @NonNull public String getTitle() { return title; }
    public void setTitle(@NonNull String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getIssueKey() { return issueKey; }
    public void setIssueKey(String issueKey) { this.issueKey = issueKey; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    
    public double getPosition() { return position; }
    public void setPosition(double position) { this.position = position; }
    
    public String getAssigneeId() { return assigneeId; }
    public void setAssigneeId(String assigneeId) { this.assigneeId = assigneeId; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    public String getSprintId() { return sprintId; }
    public void setSprintId(String sprintId) { this.sprintId = sprintId; }
    
    public String getEpicId() { return epicId; }
    public void setEpicId(String epicId) { this.epicId = epicId; }
    
    public String getParentTaskId() { return parentTaskId; }
    public void setParentTaskId(String parentTaskId) { this.parentTaskId = parentTaskId; }
    
    public Date getStartAt() { return startAt; }
    public void setStartAt(Date startAt) { this.startAt = startAt; }
    
    public Date getDueAt() { return dueAt; }
    public void setDueAt(Date dueAt) { this.dueAt = dueAt; }
    
    public Integer getStoryPoints() { return storyPoints; }
    public void setStoryPoints(Integer storyPoints) { this.storyPoints = storyPoints; }
    
    public Integer getOriginalEstimateSec() { return originalEstimateSec; }
    public void setOriginalEstimateSec(Integer originalEstimateSec) { this.originalEstimateSec = originalEstimateSec; }
    
    public Integer getRemainingEstimateSec() { return remainingEstimateSec; }
    public void setRemainingEstimateSec(Integer remainingEstimateSec) { this.remainingEstimateSec = remainingEstimateSec; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
