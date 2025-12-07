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
            return "đã thực hiện một hành động";
        }
        
        switch (action) {
            case "CREATED":
                return "đã tạo " + formatEntityType(entityType);
            case "UPDATED":
                return "đã cập nhật " + formatEntityType(entityType);
            case "DELETED":
                return "đã xóa " + formatEntityType(entityType);
            case "ADDED":
                // Special handling for MEMBERSHIP invitations
                if ("MEMBERSHIP".equals(entityType)) {
                    return "đã mời bạn tham gia";
                }
                return "đã thêm " + formatEntityType(entityType);
            case "COMMENTED":
                return "đã bình luận về";
            case "ASSIGNED":
                return "đã giao";
            case "UNASSIGNED":
                return "đã hủy giao";
            case "MOVED":
                return "đã chuyển";
            case "CHECKED":
                return "đã đánh dấu";
            case "UNCHECKED":
                return "đã bỏ đánh dấu";
            case "ADDED_MEMBER":
                return "đã thêm thành viên vào";
            case "REMOVED_MEMBER":
                return "đã xóa thành viên khỏi";
            case "COMPLETED":
                return "đã hoàn thành";
            case "REOPENED":
                return "đã mở lại";
            default:
                return action.toLowerCase();
        }
    }

    private String formatEntityType(String entityType) {
        if (entityType == null) {
            return "mục";
        }
        
        switch (entityType) {
            case "TASK":
                return "nhiệm vụ";
            case "CHECKLIST_ITEM":
                return "mục checklist";
            case "COMMENT":
                return "bình luận";
            case "PROJECT":
                return "dự án";
            case "BOARD":
                return "bảng";
            case "LABEL":
                return "nhãn";
            case "ATTACHMENT":
                return "tập tin đính kèm";
            case "MEMBERSHIP":
                return ""; // Empty string for "đã mời bạn tham gia [project name]"
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
