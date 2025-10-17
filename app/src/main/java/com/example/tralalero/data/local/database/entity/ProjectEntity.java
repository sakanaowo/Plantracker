package com.example.tralalero.data.local.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "projects")
public class ProjectEntity {
    
    @PrimaryKey
    private int id;
    
    private String name;
    private String description;
    private String color;
    private int workspaceId;
    private String userId;
    private Date createdAt;
    private Date updatedAt;
    
    public ProjectEntity() {
    }
    
    public ProjectEntity(int id, String name, String description, String color,
                        int workspaceId, String userId, Date createdAt, Date updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.color = color;
        this.workspaceId = workspaceId;
        this.userId = userId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
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
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
    
    public int getWorkspaceId() {
        return workspaceId;
    }
    
    public void setWorkspaceId(int workspaceId) {
        this.workspaceId = workspaceId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public Date getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
