package com.example.tralalero.data.remote.dto.activity;

import com.google.gson.annotations.SerializedName;

public class ActivityLogDTO {
    @SerializedName("id")
    private String id;

    @SerializedName("workspaceId")
    private String workspaceId;

    @SerializedName("projectId")
    private String projectId;

    @SerializedName("boardId")
    private String boardId;

    @SerializedName("taskId")
    private String taskId;

    @SerializedName("userId")
    private String userId;

    @SerializedName("action")
    private String action;

    @SerializedName("entityType")
    private String entityType;

    @SerializedName("entityId")
    private String entityId;

    @SerializedName("entityName")
    private String entityName;

    @SerializedName("oldValue")
    private Object oldValue;

    @SerializedName("newValue")
    private Object newValue;

    @SerializedName("metadata")
    private Object metadata;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("users")
    private UserInfo users;

    @SerializedName("projects")
    private ProjectInfo projects;

    public static class UserInfo {
        @SerializedName("id")
        private String id;

        @SerializedName("name")
        private String name;

        @SerializedName("avatarUrl")
        private String avatarUrl;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }
    }

    public static class ProjectInfo {
        @SerializedName("id")
        private String id;

        @SerializedName("name")
        private String name;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getBoardId() {
        return boardId;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getUserId() {
        return userId;
    }

    public String getAction() {
        return action;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getEntityName() {
        return entityName;
    }

    public Object getOldValue() {
        return oldValue;
    }

    public Object getNewValue() {
        return newValue;
    }

    public Object getMetadata() {
        return metadata;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public UserInfo getUsers() {
        return users;
    }

    public ProjectInfo getProjects() {
        return projects;
    }
}
