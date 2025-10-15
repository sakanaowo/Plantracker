package com.example.tralalero.data.remote.dto.project;

import com.google.gson.annotations.SerializedName;

/**
 * ProjectDTO - Data Transfer Object for Project API
 *
 * SUPPORTS BOTH FORMATS:
 * - snake_case from backend (if TransformInterceptor not active)
 * - camelCase from backend (if TransformInterceptor active)
 */
public class ProjectDTO {
    @SerializedName("id")
    private String id;

    // Support both "workspaceId" (camelCase) and "workspace_id" (snake_case)
    @SerializedName(value = "workspaceId", alternate = {"workspace_id"})
    private String workspaceId;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("key")
    private String key;

    // Support both "issueSeq" (camelCase) and "issue_seq" (snake_case)
    @SerializedName(value = "issueSeq", alternate = {"issue_seq"})
    private int issueSeq;

    // Support both "boardType" (camelCase) and "board_type" (snake_case)
    @SerializedName(value = "boardType", alternate = {"board_type"})
    private String boardType;

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

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getIssueSeq() {
        return issueSeq;
    }

    public void setIssueSeq(int issueSeq) {
        this.issueSeq = issueSeq;
    }

    public String getBoardType() {
        return boardType;
    }

    public void setBoardType(String boardType) {
        this.boardType = boardType;
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
