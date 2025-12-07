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
    
    // Project info
    private String projectName;
    
    // For ASSIGNED action - the person who received the task
    private String assigneeId;

    // Additional context from backend
    private Object oldValue;
    private Object newValue;
    private Object metadata;

    // For ADDED MEMBERSHIP action - the person who invited (when invitation accepted)
    private String invitedBy;

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
    
    public ActivityLog(String id, String userId, String action, String entityType, 
                      String entityName, String createdAt, String userName, String userAvatar, String assigneeId) {
        this(id, userId, action, entityType, entityName, createdAt, userName, userAvatar);
        this.assigneeId = assigneeId;
    }
    public ActivityLog(String id, String userId, String action, String entityType,
                      String entityName, String createdAt, String userName, String userAvatar,
                      String assigneeId, Object oldValue, Object newValue, Object metadata) {
        this(id, userId, action, entityType, entityName, createdAt, userName, userAvatar, assigneeId);
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.metadata = metadata;
    }

    public ActivityLog(String id, String userId, String action, String entityType,
                      String entityName, String createdAt, String userName, String userAvatar,
                      String assigneeId, String invitedBy) {
        this(id, userId, action, entityType, entityName, createdAt, userName, userAvatar, assigneeId);
        this.invitedBy = invitedBy;
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
    
    public String getAssigneeId() {
        return assigneeId;
    }
    
    public String getEntityId() {
        return entityId;
    }
    
    public String getProjectId() {
        return projectId;
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

    public String getInvitedBy() {
        return invitedBy;
    }

    // Setters for JSON parsing
    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public void setBoardId(String boardId) {
        this.boardId = boardId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectName() {
        return projectName;
    }

    /**
     * Format activity log message for display
     * E.g. "John Doe created event Meeting in project MyProject"
     */
    public String getFormattedMessage() {
        StringBuilder message = new StringBuilder(userName);
        message.append(" ").append(formatAction(action, entityType));
        
        if (entityName != null && !entityName.isEmpty()) {
            message.append(" ").append(entityName);
        }
        
        if (projectName != null && !projectName.isEmpty()) {
            message.append(" in project ").append(projectName);
        }
        
        return message.toString();
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
            case "COMPLETED":
                return "completed";
            case "REOPENED":
                return "reopened";
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
            case "EVENT":
                return "event";
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
