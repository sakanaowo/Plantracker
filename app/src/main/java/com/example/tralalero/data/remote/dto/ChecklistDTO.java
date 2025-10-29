package com.example.tralalero.data.remote.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * DTO for checklist with nested items
 * Used in GET /tasks/:taskId/checklists response
 */
public class ChecklistDTO {
    @SerializedName("id")
    private String id;

    @SerializedName("task_id")
    private String taskId;

    @SerializedName("title")
    private String title;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("checklist_items")
    private List<ChecklistItemDTO> checklistItems;

    public ChecklistDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public List<ChecklistItemDTO> getChecklistItems() {
        return checklistItems;
    }

    public void setChecklistItems(List<ChecklistItemDTO> checklistItems) {
        this.checklistItems = checklistItems;
    }
}
