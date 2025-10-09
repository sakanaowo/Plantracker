package com.example.tralalero.data.remote.dto.task;

import com.google.gson.annotations.SerializedName;

public class TaskCommentDTO {
    @SerializedName("id")
    private String id;

    @SerializedName("task_id")
    private String taskId;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("body")
    private String body;

    @SerializedName("created_at")
    private String createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
