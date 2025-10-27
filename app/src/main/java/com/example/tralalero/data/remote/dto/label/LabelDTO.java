package com.example.tralalero.data.remote.dto.label;

import com.google.gson.annotations.SerializedName;

public class LabelDTO {
    @SerializedName("id")
    private String id;

    @SerializedName("projectId")  // Changed from workspaceId to projectId
    private String projectId;

    @SerializedName("name")
    private String name;

    @SerializedName("color")
    private String color;

    @SerializedName("taskCount")
    private Integer taskCount;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectId() {  // Changed from getWorkspaceId to getProjectId
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    // Keep for backwards compatibility (deprecated)
    @Deprecated
    public String getWorkspaceId() {
        return projectId;
    }

    @Deprecated
    public void setWorkspaceId(String workspaceId) {
        this.projectId = workspaceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(Integer taskCount) {
        this.taskCount = taskCount;
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
}
