package com.example.tralalero.data.remote.dto.workspace;

import com.google.gson.annotations.SerializedName;

public class MembershipDTO {
    @SerializedName("id")
    private String id;

    @SerializedName("workspace_id")
    private String workspaceId;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("role")
    private String role;

    @SerializedName("created_at")
    private String createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
