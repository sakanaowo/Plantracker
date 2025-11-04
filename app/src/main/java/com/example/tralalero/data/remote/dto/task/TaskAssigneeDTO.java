package com.example.tralalero.data.remote.dto.task;

import com.google.gson.annotations.SerializedName;

public class TaskAssigneeDTO {
    @SerializedName("task_id")
    private String taskId;
    
    @SerializedName("user_id")
    private String userId;
    
    @SerializedName("assigned_at")
    private String assignedAt;
    
    @SerializedName("assigned_by")
    private String assignedBy;
    
    @SerializedName("users")
    private UserInfo users;
    
    public static class UserInfo {
        @SerializedName("id")
        private String id;
        
        @SerializedName("name")
        private String name;
        
        @SerializedName("email")
        private String email;
        
        @SerializedName("avatar_url")
        private String avatarUrl;

        public String getId() {
            return id;
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
    }

    public String getTaskId() {
        return taskId;
    }

    public String getUserId() {
        return userId;
    }

    public String getAssignedAt() {
        return assignedAt;
    }

    public String getAssignedBy() {
        return assignedBy;
    }

    public UserInfo getUsers() {
        return users;
    }
}
