package com.example.tralalero.domain.model;

/**
 * Enum for User Role in Workspace
 * Represents the permission level of a user in a workspace
 */
public enum Role {
    OWNER,   // Full control over workspace, can delete workspace
    ADMIN,   // Can manage workspace and invite members
    MEMBER   // Regular member with basic permissions
}

