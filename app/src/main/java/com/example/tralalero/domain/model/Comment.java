package com.example.tralalero.domain.model;

import java.util.Date;

public class Comment {
    private final String id;
    private final String taskId;
    private final String userId;
    private final String body;
    private final Date createdAt;
    private final User user;
    
    // Nested User class
    public static class User {
        private final String id;
        private final String name;
        private final String avatarUrl;
        
        public User(String id, String name, String avatarUrl) {
            this.id = id;
            this.name = name;
            this.avatarUrl = avatarUrl;
        }
        
        public String getId() { return id; }
        public String getName() { return name; }
        public String getAvatarUrl() { return avatarUrl; }
    }
    
    // Constructor
    public Comment(String id, String taskId, String userId, String body, 
                   Date createdAt, User user) {
        this.id = id;
        this.taskId = taskId;
        this.userId = userId;
        this.body = body;
        this.createdAt = createdAt;
        this.user = user;
    }
    
    // Getters
    public String getId() { return id; }
    public String getTaskId() { return taskId; }
    public String getUserId() { return userId; }
    public String getBody() { return body; }
    public Date getCreatedAt() { return createdAt; }
    public User getUser() { return user; }
    
    // Helper method: Format relative time
    public String getRelativeTime() {
        if (createdAt == null) return "";
        
        long diffMillis = System.currentTimeMillis() - createdAt.getTime();
        long diffSeconds = diffMillis / 1000;
        long diffMinutes = diffSeconds / 60;
        long diffHours = diffMinutes / 60;
        long diffDays = diffHours / 24;
        
        if (diffSeconds < 60) {
            return "Just now";
        } else if (diffMinutes < 60) {
            return diffMinutes + (diffMinutes == 1 ? " minute ago" : " minutes ago");
        } else if (diffHours < 24) {
            return diffHours + (diffHours == 1 ? " hour ago" : " hours ago");
        } else if (diffDays < 7) {
            return diffDays + (diffDays == 1 ? " day ago" : " days ago");
        } else if (diffDays < 30) {
            long weeks = diffDays / 7;
            return weeks + (weeks == 1 ? " week ago" : " weeks ago");
        } else if (diffDays < 365) {
            long months = diffDays / 30;
            return months + (months == 1 ? " month ago" : " months ago");
        } else {
            long years = diffDays / 365;
            return years + (years == 1 ? " year ago" : " years ago");
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment = (Comment) o;
        return id != null && id.equals(comment.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
