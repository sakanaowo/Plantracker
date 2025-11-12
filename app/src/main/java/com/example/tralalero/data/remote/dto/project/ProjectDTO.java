package com.example.tralalero.data.remote.dto.project;

import com.google.gson.annotations.SerializedName;


public class ProjectDTO {
    @SerializedName("id")
    private String id;

    @SerializedName(value = "workspaceId", alternate = {"workspace_id"})
    private String workspaceId;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("key")
    private String key;

    @SerializedName(value = "issueSeq", alternate = {"issue_seq"})
    private int issueSeq;

    @SerializedName(value = "boardType", alternate = {"board_type"})
    private String boardType;

    @SerializedName(value = "createdAt", alternate = {"created_at"})
    private String createdAt;

    @SerializedName(value = "updatedAt", alternate = {"updated_at"})
    private String updatedAt;
    
    // Nested workspace object for workspaceName
    @SerializedName("workspaces")
    private WorkspaceInfo workspaces;
    
    // Inner class for workspace info
    public static class WorkspaceInfo {
        @SerializedName("id")
        private String id;
        
        @SerializedName("name")
        private String name;
        
        public String getId() {
            return id;
        }
        
        public String getName() {
            return name;
        }
    }

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
    
    public WorkspaceInfo getWorkspaces() {
        return workspaces;
    }
    
    public void setWorkspaces(WorkspaceInfo workspaces) {
        this.workspaces = workspaces;
    }
}
