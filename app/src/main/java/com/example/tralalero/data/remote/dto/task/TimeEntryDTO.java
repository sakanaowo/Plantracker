package com.example.tralalero.data.remote.dto.task;

import com.google.gson.annotations.SerializedName;

public class TimeEntryDTO {
    @SerializedName("id")
    private String id;

    @SerializedName("taskId")  
    private String taskId;

    @SerializedName("userId")  
    private String userId;

    @SerializedName("startAt")  
    private String startAt;

    @SerializedName("endAt")  
    private String endAt;

    @SerializedName("durationSec") 
    private Integer durationSec;

    @SerializedName("note")
    private String note;

    @SerializedName("createdAt") 
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

    public String getStartAt() {
        return startAt;
    }

    public void setStartAt(String startAt) {
        this.startAt = startAt;
    }

    public String getEndAt() {
        return endAt;
    }

    public void setEndAt(String endAt) {
        this.endAt = endAt;
    }

    public Integer getDurationSec() {
        return durationSec;
    }

    public void setDurationSec(Integer durationSec) {
        this.durationSec = durationSec;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
