package com.example.tralalero.data.local.database.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import java.util.Date;

@Entity(tableName = "workspaces")
public class WorkspaceEntity {
    
    @PrimaryKey
    @NonNull
    private String id;  // ✅ FIXED: Changed from int to String

    private String name;
    private String description;  // Keep for backward compatibility (optional field)

    // FIXED: Renamed from userId to ownerId to match backend schema
    private String ownerId;

    // ADDED: type field for PERSONAL/TEAM workspace type (matches backend)
    @NonNull
    private String type;  // "PERSONAL" or "TEAM"

    private Date createdAt;
    private Date updatedAt;
    
    public WorkspaceEntity() {
    }
    
    @Ignore
    public WorkspaceEntity(@NonNull String id, String name, String description, String ownerId,
                          @NonNull String type, Date createdAt, Date updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.ownerId = ownerId;
        this.type = type;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    @NonNull
    public String getId() {
        return id;
    }
    
    public void setId(@NonNull String id) {
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
    
    // FIXED: Renamed getter/setter from getUserId/setUserId to getOwnerId/setOwnerId
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    // ADDED: Getter/setter for type field
    @NonNull
    public String getType() {
        return type;
    }
    
    public void setType(@NonNull String type) {
        this.type = type;
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
