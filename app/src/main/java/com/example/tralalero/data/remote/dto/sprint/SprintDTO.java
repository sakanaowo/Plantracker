package com.example.tralalero.data.remote.dto.sprint;

import com.google.gson.annotations.SerializedName;

public class SprintDTO {
    @SerializedName("id")
    private String id;

    @SerializedName("projectId")  // ✅ FIXED: camelCase
    private String projectId;

    @SerializedName("name")
    private String name;

    @SerializedName("goal")
    private String goal;

    @SerializedName("startAt")  // ✅ FIXED: camelCase
    private String startAt;

    @SerializedName("endAt")  // ✅ FIXED: camelCase
    private String endAt;

    @SerializedName("state")
    private String state;

    @SerializedName("createdAt")  // ✅ FIXED: camelCase
    private String createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
