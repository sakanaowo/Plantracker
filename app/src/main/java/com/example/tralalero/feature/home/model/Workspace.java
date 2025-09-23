package com.example.tralalero.feature.home.model;

import com.google.gson.annotations.SerializedName;

public class Workspace {
    @SerializedName("id")
    private String id;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("owner_id")
    private String ownerId;
    
    @SerializedName("type")
    private String type;
    
    @SerializedName("created_at")
    private String createdAt;
    
    @SerializedName("updated_at")
    private String updatedAt;

    // Default constructor
    public Workspace() {
    }

    // Constructor
    public Workspace(String id, String name, String ownerId, String type) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
        this.type = type;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getType() {
        return type;
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

    public void setName(String name) {
        this.name = name;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}