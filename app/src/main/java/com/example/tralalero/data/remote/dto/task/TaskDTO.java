package com.example.tralalero.data.remote.dto.task;

import com.google.gson.annotations.SerializedName;

public class TaskDTO {
    // Core fields
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

    @SerializedName("issue_key")
    private String issueKey;

    @SerializedName("type")
    private String type; // TASK, STORY, BUG, EPIC, SUBTASK

    @SerializedName("status")
    private String status; // TO_DO, IN_PROGRESS, IN_REVIEW, DONE

    @SerializedName("priority")
    private String priority; // LOW, MEDIUM, HIGH

    @SerializedName("position")
    private double position;

    // Assignment
    @SerializedName("assignee_id")
    private String assigneeId;

    @SerializedName("created_by")
    private String createdBy;

    // Relationships
    @SerializedName("sprint_id")
    private String sprintId;

    @SerializedName("epic_id")
    private String epicId;

    @SerializedName("parent_task_id")
    private String parentTaskId;

    // Time tracking
    @SerializedName("start_at")
    private String startAt;

    @SerializedName("due_at")
    private String dueAt;

    @SerializedName("story_points")
    private Integer storyPoints;

    @SerializedName("original_estimate_sec")
    private Integer originalEstimateSec;

    @SerializedName("remaining_estimate_sec")
    private Integer remainingEstimateSec;

    // Timestamps
    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    @SerializedName("deleted_at")
    private String deletedAt;

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

    public String getBoardId() {
        return boardId;
    }

    public void setBoardId(String boardId) {
        this.boardId = boardId;
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

    public String getIssueKey() {
        return issueKey;
    }

    public void setIssueKey(String issueKey) {
        this.issueKey = issueKey;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public double getPosition() {
        return position;
    }

    public void setPosition(double position) {
        this.position = position;
    }

    public String getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(String assigneeId) {
        this.assigneeId = assigneeId;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getSprintId() {
        return sprintId;
    }

    public void setSprintId(String sprintId) {
        this.sprintId = sprintId;
    }

    public String getEpicId() {
        return epicId;
    }

    public void setEpicId(String epicId) {
        this.epicId = epicId;
    }

    public String getParentTaskId() {
        return parentTaskId;
    }

    public void setParentTaskId(String parentTaskId) {
        this.parentTaskId = parentTaskId;
    }

    public String getStartAt() {
        return startAt;
    }

    public void setStartAt(String startAt) {
        this.startAt = startAt;
    }

    public String getDueAt() {
        return dueAt;
    }

    public void setDueAt(String dueAt) {
        this.dueAt = dueAt;
    }

    public Integer getStoryPoints() {
        return storyPoints;
    }

    public void setStoryPoints(Integer storyPoints) {
        this.storyPoints = storyPoints;
    }

    public Integer getOriginalEstimateSec() {
        return originalEstimateSec;
    }

    public void setOriginalEstimateSec(Integer originalEstimateSec) {
        this.originalEstimateSec = originalEstimateSec;
    }

    public Integer getRemainingEstimateSec() {
        return remainingEstimateSec;
    }

    public void setRemainingEstimateSec(Integer remainingEstimateSec) {
        this.remainingEstimateSec = remainingEstimateSec;
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

    public String getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(String deletedAt) {
        this.deletedAt = deletedAt;
    }
}
