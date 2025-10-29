package com.example.tralalero.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/**
 * DTO for checklist item responses from API
 * Used in GET /tasks/:taskId/checklists response
 */
public class ChecklistItemDTO {
    @SerializedName("id")
    private String id;

    @SerializedName("checklist_id")
    private String checklistId;

    @SerializedName("content")
    private String content;

    @SerializedName("is_done")
    private boolean isDone;

    @SerializedName("position")
    private double position;

    @SerializedName("created_at")
    private String createdAt;

    public ChecklistItemDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChecklistId() {
        return checklistId;
    }

    public void setChecklistId(String checklistId) {
        this.checklistId = checklistId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public double getPosition() {
        return position;
    }

    public void setPosition(double position) {
        this.position = position;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
