package com.example.tralalero.data.remote.dto.task;

import com.google.gson.annotations.SerializedName;

public class TimeEntryDTO {
    @SerializedName("id")
    private String id;

    @SerializedName("task_id")
    private String taskId;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("start_at")
    private String startAt;

    @SerializedName("end_at")
    private String endAt;

    @SerializedName("duration_sec")
    private Integer durationSec;

    @SerializedName("note")
    private String note;

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
