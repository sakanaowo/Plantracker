package com.example.tralalero.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * API response model for project invitation
 */
public class InvitationResponse {
    @SerializedName("id")
    private String id;

    @SerializedName("project_id")
    private String projectId;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("role")
    private String role;

    @SerializedName("status")
    private String status;

    @SerializedName("invited_by")
    private String invitedBy;

    @SerializedName("expires_at")
    private String expiresAt;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    @SerializedName("projects")
    private ProjectInfo projects;

    @SerializedName("inviter")
    private UserInfo inviter;

    public static class ProjectInfo {
        @SerializedName("id")
        private String id;

        @SerializedName("name")
        private String name;

        @SerializedName("description")
        private String description;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }

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

    // Getters
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

    public String getStatus() {
        return status;
    }

    public String getInvitedBy() {
        return invitedBy;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public ProjectInfo getProjects() {
        return projects;
    }

    public UserInfo getInviter() {
        return inviter;
    }
}
