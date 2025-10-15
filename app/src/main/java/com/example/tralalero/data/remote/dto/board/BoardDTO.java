package com.example.tralalero.data.remote.dto.board;

import com.google.gson.annotations.SerializedName;

public class BoardDTO {
    @SerializedName("id")
    private String id;

    @SerializedName("project_id")  // ✅ FIXED: Match backend snake_case
    private String projectId;

    @SerializedName("name")
    private String name;

    @SerializedName("order")
    private int order;

    @SerializedName("created_at")  // ✅ FIXED: Match backend snake_case
    private String createdAt;

    @SerializedName("updated_at")  // ✅ FIXED: Match backend snake_case
    private String updatedAt;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
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
