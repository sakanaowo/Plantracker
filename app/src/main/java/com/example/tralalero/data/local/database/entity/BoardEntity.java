package com.example.tralalero.data.local.database.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;
import java.util.Date;

@Entity(
    tableName = "boards",
    indices = {
        @Index(value = "projectId"),
        @Index(value = "order")
    }
)
public class BoardEntity {

    @PrimaryKey
    @NonNull
    private String id;

    @NonNull
    private String projectId;

    @NonNull
    private String name;

    private int order;

    private Date createdAt;
    private Date updatedAt;

    public BoardEntity() {
    }

    @Ignore
    public BoardEntity(@NonNull String id, @NonNull String projectId,
                       @NonNull String name, int order) {
        this.id = id;
        this.projectId = projectId;
        this.name = name;
        this.order = order;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @NonNull
    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(@NonNull String projectId) {
        this.projectId = projectId;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
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
