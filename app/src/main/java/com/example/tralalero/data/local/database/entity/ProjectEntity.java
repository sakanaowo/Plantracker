package com.example.tralalero.data.local.database.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import java.util.Date;

/**
 * Room Entity for Project
 * Matches backend schema with all required fields including issueSeq and timestamps
 */
@Entity(
    tableName = "projects",
    indices = {
        @Index(value = "workspaceId"),
        @Index(value = "key", unique = true)
    }
)
public class ProjectEntity {
    
    @PrimaryKey
    @NonNull
    private String id;
    
    @NonNull
    private String workspaceId;
    
    @NonNull
    private String name;
    
    private String description;
    
    @NonNull
    private String key;  // CRITICAL FIELD - Project unique key (e.g., "PROJ")
    
    @NonNull
    private String boardType;  // CRITICAL FIELD - BoardType: KANBAN, SCRUM
    
    // ADDED: issue_seq for generating issue keys (PROJ-1, PROJ-2, etc.)
    private int issueSeq;  // Default 0, increments for each new issue

    // ADDED: Timestamps to match backend schema
    private Date createdAt;
    private Date updatedAt;

    // Constructor with required fields (backward compatibility)
    public ProjectEntity(@NonNull String id, @NonNull String workspaceId,
                        @NonNull String name, @NonNull String key, 
                        @NonNull String boardType) {
        this.id = id;
        this.workspaceId = workspaceId;
        this.name = name;
        this.key = key;
        this.boardType = boardType;
        this.issueSeq = 0;  // Default value
    }
    
    // Getters and Setters
    @NonNull
    public String getId() {
        return id;
    }
    
    public void setId(@NonNull String id) {
        this.id = id;
    }
    
    @NonNull
    public String getWorkspaceId() {
        return workspaceId;
    }
    
    public void setWorkspaceId(@NonNull String workspaceId) {
        this.workspaceId = workspaceId;
    }
    
    @NonNull
    public String getName() {
        return name;
    }
    
    public void setName(@NonNull String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    @NonNull
    public String getKey() {
        return key;
    }
    
    public void setKey(@NonNull String key) {
        this.key = key;
    }
    
    @NonNull
    public String getBoardType() {
        return boardType;
    }
    
    public void setBoardType(@NonNull String boardType) {
        this.boardType = boardType;
    }

    // ADDED: Getter/setter for issueSeq
    public int getIssueSeq() {
        return issueSeq;
    }

    public void setIssueSeq(int issueSeq) {
        this.issueSeq = issueSeq;
    }

    // ADDED: Getter/setter for createdAt
    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    // ADDED: Getter/setter for updatedAt
    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
