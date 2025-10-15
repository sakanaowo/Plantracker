package com.example.tralalero.data.remote.dto.board;

import com.google.gson.annotations.SerializedName;

/**
 * BoardDTO - Data Transfer Object for Board API
 *
 * SUPPORTS BOTH FORMATS:
 * - snake_case from backend (if TransformInterceptor not active)
 * - camelCase from backend (if TransformInterceptor active)
 *
 * @SerializedName supports multiple names via alternate attribute
 */
public class BoardDTO {
    @SerializedName("id")
    private String id;

    // Support both "projectId" (camelCase) and "project_id" (snake_case)
    @SerializedName(value = "projectId", alternate = {"project_id"})
    private String projectId;

    @SerializedName("name")
    private String name;

    @SerializedName("order")
    private int order;

    // Support both "createdAt" (camelCase) and "created_at" (snake_case)
    @SerializedName(value = "createdAt", alternate = {"created_at"})
    private String createdAt;

    // Support both "updatedAt" (camelCase) and "updated_at" (snake_case)
    @SerializedName(value = "updatedAt", alternate = {"updated_at"})
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
