package com.example.tralalero.domain.model;

public class ProjectMember {
    private final String id;
    private final String userId;
    private final String name;
    private final String email;
    private final String avatarUrl;
    private final String role;
    private final String firebaseUid;

    public ProjectMember(String id, String userId, String name, String email, String avatarUrl, String role, String firebaseUid) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.role = role;
        this.firebaseUid = firebaseUid;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
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

    public String getRole() {
        return role;
    }
    
    public String getFirebaseUid() {
        return firebaseUid;
    }

    public String getInitials() {
        if (name == null || name.isEmpty()) {
            return "?";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].charAt(0) + "" + parts[1].charAt(0)).toUpperCase();
        } else {
            return name.substring(0, Math.min(2, name.length())).toUpperCase();
        }
    }
}
