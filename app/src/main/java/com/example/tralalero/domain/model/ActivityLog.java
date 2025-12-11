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
    private String inviterName;

    // Default constructor for WebSocket parsing
    public ActivityLog() {
    }
    
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

    public String getInviterName() {
        return inviterName;
    }

    // Setters for JSON parsing
    public void setId(String id) {
        this.id = id;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }
    
    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }
    
    public void setTimestamp(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }
    
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
    
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

    public void setInviterName(String inviterName) {
        this.inviterName = inviterName;
    }

    public String getProjectName() {
        return projectName;
    }

    /**
     * Format activity log message for display
     * E.g. "John Doe created event Meeting in project MyProject"
     */
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
