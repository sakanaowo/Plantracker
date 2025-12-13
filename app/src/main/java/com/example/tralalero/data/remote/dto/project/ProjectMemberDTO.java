package com.example.tralalero.data.remote.dto.project;

import com.google.gson.annotations.SerializedName;

public class ProjectMemberDTO {
    @SerializedName("id")
    private String id;
    
    @SerializedName(value = "userId", alternate = {"user_id"})
    private String userId;
    
    @SerializedName(value = "projectId", alternate = {"project_id"})
    private String projectId;
    
    @SerializedName("role")
    private String role;
    
    @SerializedName("users")
    private UserInfo users;
    
    public static class UserInfo {
        @SerializedName("id")
        private String id;
        
        @SerializedName("name")
        private String name;
        
        @SerializedName("email")
        private String email;
        
        @SerializedName(value = "avatarUrl", alternate = {"avatar_url"})
        private String avatarUrl;
        
        @SerializedName("firebaseUid")
        private String firebaseUid;

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
        
        public String getFirebaseUid() {
            return firebaseUid;
        }
    }

    public String getId() {
        return id;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }

    public UserInfo getUsers() {
        return users;
    }
}
