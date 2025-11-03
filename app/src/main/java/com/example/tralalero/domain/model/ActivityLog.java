package com.example.tralalero.domain.model;

public class ActivityLog {
    private String id;
    private String workspaceId;
    private String projectId;
    private String boardId;
    private String taskId;
    private String userId;
    private String action;
    private String entityType;
    private String entityId;
    private String entityName;
    private String createdAt;
    
    // User info
    private String userName;
    private String userAvatar;

    public ActivityLog(String id, String userId, String action, String entityType, 
                      String entityName, String createdAt, String userName, String userAvatar) {
        this.id = id;
        this.userId = userId;
        this.action = action;
        this.entityType = entityType;
        this.entityName = entityName;
        this.createdAt = createdAt;
        this.userName = userName;
        this.userAvatar = userAvatar;
    }

    // Getters
    public String getId() {
        return id;
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

    public String getEntityName() {
        return entityName;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    /**
     * Format activity log message for display
     * E.g. "John Doe added Checklist to Test card 2"
     */
    public String getFormattedMessage() {
        String actionText = formatAction(action, entityType);
        if (entityName != null && !entityName.isEmpty()) {
            return userName + " " + actionText + " " + entityName;
        }
        return userName + " " + actionText;
    }

    private String formatAction(String action, String entityType) {
        if (action == null) {
            return "performed an action";
        }
        
        switch (action) {
            case "CREATED":
                return "created " + formatEntityType(entityType);
            case "UPDATED":
                return "updated " + formatEntityType(entityType);
            case "DELETED":
                return "deleted " + formatEntityType(entityType);
            case "ADDED":
                // Special handling for MEMBERSHIP invitations
                if ("MEMBERSHIP".equals(entityType)) {
                    return "invited you to join";
                }
                return "added " + formatEntityType(entityType);
            case "COMMENTED":
                return "commented on";
            case "ASSIGNED":
                return "assigned";
            case "UNASSIGNED":
                return "unassigned";
            case "MOVED":
                return "moved";
            case "CHECKED":
                return "checked";
            case "UNCHECKED":
                return "unchecked";
            case "ADDED_MEMBER":
                return "added member to";
            case "REMOVED_MEMBER":
                return "removed member from";
            default:
                return action.toLowerCase();
        }
    }

    private String formatEntityType(String entityType) {
        if (entityType == null) {
            return "item";
        }
        
        switch (entityType) {
            case "TASK":
                return "task";
            case "CHECKLIST_ITEM":
                return "checklist item";
            case "COMMENT":
                return "comment";
            case "PROJECT":
                return "project";
            case "BOARD":
                return "board";
            case "LABEL":
                return "label";
            case "ATTACHMENT":
                return "attachment";
            case "MEMBERSHIP":
                return ""; // Empty string for "invited you to join [project name]"
            default:
                return entityType.toLowerCase();
        }
    }

    /**
     * Format timestamp for display
     * E.g. "Oct 2 at 10:52 AM"
     */
    public String getFormattedTime() {
        // TODO: Implement proper date formatting
        // For now, return the ISO string
        return createdAt;
    }

    /**
     * Get user initials for avatar
     * E.g. "John Doe" -> "JD"
     */
    public String getUserInitials() {
        if (userName == null || userName.isEmpty()) {
            return "??";
        }
        String[] parts = userName.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        }
        return userName.substring(0, Math.min(2, userName.length())).toUpperCase();
    }
}
