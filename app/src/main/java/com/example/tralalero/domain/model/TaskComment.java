package com.example.tralalero.domain.model;

import java.util.Date;

public class TaskComment {
    private final String id;
    private final String taskId;
    private final String userId;
    private final String body; 
    private final Date createdAt;
    private final String userName;
    private final String userAvatarUrl;

    public TaskComment(String id, String taskId, String userId, String body, Date createdAt) {
        this.id = id;
        this.taskId = taskId;
        this.userId = userId;
        this.body = body;
        this.createdAt = createdAt;
        this.userName = null;
        this.userAvatarUrl = null;
    }
    
    public TaskComment(String id, String taskId, String userId, String body, Date createdAt, 
                      String userName, String userAvatarUrl) {
        this.id = id;
        this.taskId = taskId;
        this.userId = userId;
        this.body = body;
        this.createdAt = createdAt;
        this.userName = userName;
        this.userAvatarUrl = userAvatarUrl;
    }

    public String getId() { return id; }
    public String getTaskId() { return taskId; }
    public String getUserId() { return userId; }
    public String getBody() { return body; }
    public Date getCreatedAt() { return createdAt; }
    public String getUserName() { return userName; }
    public String getUserAvatarUrl() { return userAvatarUrl; }

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
