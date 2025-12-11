package com.example.tralalero.domain.model;

import java.util.Date;

/**
 * Domain model representing a project invitation
 */
public class Invitation {
    private final String id;
    private final String projectId;
    private final String projectName;
    private final String projectDescription;
    private final String userId;
    private final String role;
    private final String status;
    private final String invitedBy;
    private final String inviterName;
    private final String inviterEmail;
    private final String inviterAvatarUrl;
    private final Date expiresAt;
    private final Date createdAt;

    public Invitation(String id, String projectId, String projectName, String projectDescription,
                      String userId, String role, String status, String invitedBy,
                      String inviterName, String inviterEmail, String inviterAvatarUrl,
                      Date expiresAt, Date createdAt) {
        this.id = id;
        this.projectId = projectId;
        this.projectName = projectName;
        this.projectDescription = projectDescription;
        this.userId = userId;
        this.role = role;
        this.status = status;
        this.invitedBy = invitedBy;
        this.inviterName = inviterName;
        this.inviterEmail = inviterEmail;
        this.inviterAvatarUrl = inviterAvatarUrl;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectDescription() {
        return projectDescription;
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

    public String getInviterName() {
        return inviterName;
    }

    public String getInviterEmail() {
        return inviterEmail;
    }

    public String getInviterAvatarUrl() {
        return inviterAvatarUrl;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public boolean isExpired() {
        return expiresAt != null && expiresAt.before(new Date());
    }

    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(status) && !isExpired();
    }
}
