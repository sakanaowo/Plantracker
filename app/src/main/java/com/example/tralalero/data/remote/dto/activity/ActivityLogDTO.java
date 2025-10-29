package com.example.tralalero.data.remote.dto.activity;

import com.google.gson.annotations.SerializedName;

public class ActivityLogDTO {
    @SerializedName("id")
    private String id;

    @SerializedName("workspace_id")
    private String workspaceId;

    @SerializedName("project_id")
    private String projectId;

    @SerializedName("board_id")
    private String boardId;

    @SerializedName("task_id")
    private String taskId;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("action")
    private String action;

    @SerializedName("entity_type")
    private String entityType;

    @SerializedName("entity_id")
    private String entityId;

    @SerializedName("entity_name")
    private String entityName;

    @SerializedName("old_value")
    private Object oldValue;

    @SerializedName("new_value")
    private Object newValue;

    @SerializedName("metadata")
    private Object metadata;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("users")
    private UserInfo users;

    public static class UserInfo {
        @SerializedName("id")
        private String id;

        @SerializedName("name")
        private String name;

        @SerializedName("avatar_url")
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
}
