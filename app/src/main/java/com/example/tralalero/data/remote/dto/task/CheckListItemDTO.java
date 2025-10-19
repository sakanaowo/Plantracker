package com.example.tralalero.data.remote.dto.task;

import com.google.gson.annotations.SerializedName;

public class CheckListItemDTO {
    @SerializedName("id")
    private String id;

    @SerializedName("checklistId")
    private String checklistId;

    @SerializedName("content")
    private String content;

    @SerializedName("isDone")
    private boolean isDone;

    @SerializedName("position")
    private double position;

    @SerializedName("createdAt")
    private String createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChecklistId() {
        return checklistId;
    }

    public void setChecklistId(String checklistId) {
        this.checklistId = checklistId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public double getPosition() {
        return position;
    }

    public void setPosition(double position) {
        this.position = position;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
