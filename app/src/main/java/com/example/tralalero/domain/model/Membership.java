package com.example.tralalero.domain.model;

import java.util.Date;

/**
 * Domain model for Membership
 * Represents a user's membership in a workspace with a specific role
 */
public class Membership {
    private final String id;
    private final String userId;
    private final String workspaceId;
    private final Role role;
    private final Date createdAt;

    public Membership(String id, String userId, String workspaceId, Role role, Date createdAt) {
        this.id = id;
        this.userId = userId;
        this.workspaceId = workspaceId;
        this.role = role;
        this.createdAt = createdAt;
    }

    // Getters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getWorkspaceId() { return workspaceId; }
    public Role getRole() { return role; }
    public Date getCreatedAt() { return createdAt; }

    // Business logic
    public boolean isOwner() {
        return role == Role.OWNER;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isMember() {
        return role == Role.MEMBER;
    }

    public boolean canManageWorkspace() {
        return isOwner() || isAdmin();
    }

    public boolean canInviteMembers() {
        return isOwner() || isAdmin();
    }

    public boolean canDeleteWorkspace() {
        return isOwner();
    }

    public String getRoleDisplay() {
        switch (role) {
            case OWNER: return "Owner";
            case ADMIN: return "Admin";
            case MEMBER: return "Member";
            default: return "Unknown";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Membership that = (Membership) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public enum Role {
        OWNER, ADMIN, MEMBER
    }
}
