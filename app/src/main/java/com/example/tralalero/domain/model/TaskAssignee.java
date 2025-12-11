package com.example.tralalero.domain.model;

public class TaskAssignee {
    private final String taskId;
    private final String userId;
    private final String name;
    private final String email;
    private final String avatarUrl;
    private final String assignedAt;
    private final String assignedBy;

    public TaskAssignee(String taskId, String userId, String name, String email, 
                        String avatarUrl, String assignedAt, String assignedBy) {
        this.taskId = taskId;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.assignedAt = assignedAt;
        this.assignedBy = assignedBy;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getAssignedAt() {
        return assignedAt;
    }

    public String getAssignedBy() {
        return assignedBy;
    }

    public String getInitials() {
        if (name == null || name.trim().isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].charAt(0) + "" + parts[1].charAt(0)).toUpperCase();
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }
}
