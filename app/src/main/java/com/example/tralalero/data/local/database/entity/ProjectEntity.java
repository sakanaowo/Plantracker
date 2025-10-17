package com.example.tralalero.data.local.database.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

/**
 * Room Entity for Project
 * Matches domain model Project.java with all 6 fields
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
    
    // Constructor with required fields
    public ProjectEntity(@NonNull String id, @NonNull String workspaceId, 
                        @NonNull String name, @NonNull String key, 
                        @NonNull String boardType) {
        this.id = id;
        this.workspaceId = workspaceId;
        this.name = name;
        this.key = key;
        this.boardType = boardType;
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
}
