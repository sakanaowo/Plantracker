package com.example.tralalero.domain.model;

import java.util.Date;

/**
 * Domain model for Task Comment
 * Represents a comment/discussion on a task
 */
public class TaskComment {
    private final String id;
    private final String taskId;
    private final String userId;
    private final String body; // Comment content
    private final Date createdAt;

    public TaskComment(String id, String taskId, String userId, String body, Date createdAt) {
        this.id = id;
        this.taskId = taskId;
        this.userId = userId;
        this.body = body;
        this.createdAt = createdAt;
    }

    // Getters
    public String getId() { return id; }
    public String getTaskId() { return taskId; }
    public String getUserId() { return userId; }
    public String getBody() { return body; }
    public Date getCreatedAt() { return createdAt; }

    // Business logic
    public boolean isEmpty() {
        return body == null || body.trim().isEmpty();
    }

    public int getWordCount() {
        if (isEmpty()) return 0;
        return body.trim().split("\\s+").length;
    }

    public boolean isMention(String userId) {
        if (isEmpty() || userId == null) return false;
        return body.contains("@" + userId);
    }

    public String getPreview(int maxLength) {
        if (isEmpty()) return "";
        if (body.length() <= maxLength) return body;
        return body.substring(0, maxLength) + "...";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskComment that = (TaskComment) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
