package com.example.tralalero.model;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class Task {
    @SerializedName("id")
    private String id;

    @SerializedName("project_id")
    private String projectId;

    @SerializedName("board_id")
    private String boardId;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("assignee_id")
    private String assigneeId;

    @SerializedName("created_by")
    private String createdBy;

    @SerializedName("due_at")
    private Date dueAt;

    @SerializedName("start_at")
    private Date startAt;

    @SerializedName("priority")
    private String priority;

    @SerializedName("position")
    private double position;

    @SerializedName("issue_key")
    private String issueKey;

    @SerializedName("type")
    private String type;

    @SerializedName("status")
    private String status;

    @SerializedName("sprint_id")
    private String sprintId;

    @SerializedName("epic_id")
    private String epicId;

    @SerializedName("parent_task_id")
    private String parentTaskId;

    @SerializedName("story_points")
    private Integer storyPoints;

    @SerializedName("original_estimate_sec")
    private Integer originalEstimateSec;

    @SerializedName("remaining_estimate_sec")
    private Integer remainingEstimateSec;

    @SerializedName("created_at")
    private Date createdAt;

    @SerializedName("updated_at")
    private Date updatedAt;

    @SerializedName("deleted_at")
    private Date deletedAt;

    // Constructor
    public Task() {}

    public Task(String id, String title, String status) {
        this.id = id;
        this.title = title;
        this.status = status;
    }

    // Getters
    public String getId() { return id; }
    public String getProjectId() { return projectId; }
    public String getBoardId() { return boardId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getAssigneeId() { return assigneeId; }
    public String getCreatedBy() { return createdBy; }
    public Date getDueAt() { return dueAt; }
    public Date getStartAt() { return startAt; }
    public String getPriority() { return priority; }
    public double getPosition() { return position; }
    public String getIssueKey() { return issueKey; }
    public String getType() { return type; }
    public String getStatus() { return status; }
    public String getSprintId() { return sprintId; }
    public String getEpicId() { return epicId; }
    public String getParentTaskId() { return parentTaskId; }
    public Integer getStoryPoints() { return storyPoints; }
    public Integer getOriginalEstimateSec() { return originalEstimateSec; }
    public Integer getRemainingEstimateSec() { return remainingEstimateSec; }
    public Date getCreatedAt() { return createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public Date getDeletedAt() { return deletedAt; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setProjectId(String projectId) { this.projectId = projectId; }
    public void setBoardId(String boardId) { this.boardId = boardId; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setAssigneeId(String assigneeId) { this.assigneeId = assigneeId; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public void setDueAt(Date dueAt) { this.dueAt = dueAt; }
    public void setStartAt(Date startAt) { this.startAt = startAt; }
    public void setPriority(String priority) { this.priority = priority; }
    public void setPosition(double position) { this.position = position; }
    public void setIssueKey(String issueKey) { this.issueKey = issueKey; }
    public void setType(String type) { this.type = type; }
    public void setStatus(String status) { this.status = status; }
    public void setSprintId(String sprintId) { this.sprintId = sprintId; }
    public void setEpicId(String epicId) { this.epicId = epicId; }
    public void setParentTaskId(String parentTaskId) { this.parentTaskId = parentTaskId; }
    public void setStoryPoints(Integer storyPoints) { this.storyPoints = storyPoints; }
    public void setOriginalEstimateSec(Integer originalEstimateSec) { this.originalEstimateSec = originalEstimateSec; }
    public void setRemainingEstimateSec(Integer remainingEstimateSec) { this.remainingEstimateSec = remainingEstimateSec; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    public void setDeletedAt(Date deletedAt) { this.deletedAt = deletedAt; }
}