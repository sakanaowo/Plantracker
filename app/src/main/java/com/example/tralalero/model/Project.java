package com.example.tralalero.model;

import com.google.gson.annotations.SerializedName;

public class Project {
    @SerializedName("id")
    private String id;

    @SerializedName("workspace_id")
    private String workspaceId;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("key")
    private String key;

    @SerializedName("issue_seq")
    private int issueSeq;

    @SerializedName("board_type")
    private String boardType;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    // Default constructor
    public Project() {
    }

    // Constructor with required fields
    public Project(String id, String name, String description, String type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.boardType = type;
    }

    // Full constructor
    public Project(String id, String workspaceId, String name, String description,
                   String key, int issueSeq, String boardType) {
        this.id = id;
        this.workspaceId = workspaceId;
        this.name = name;
        this.description = description;
        this.key = key;
        this.issueSeq = issueSeq;
        this.boardType = boardType;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getKey() {
        return key;
    }

    public int getIssueSeq() {
        return issueSeq;
    }

    public String getBoardType() {
        return boardType;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setIssueSeq(int issueSeq) {
        this.issueSeq = issueSeq;
    }

    public void setBoardType(String boardType) {
        this.boardType = boardType;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}

