package com.example.tralalero.data.remote.dto.task;



import com.google.gson.annotations.SerializedName;

public class CheckListDTO {
    @SerializedName("id")
    private String id;
    @SerializedName("task_id")
    private String taskId;
    @SerializedName("title")
    private String title;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
