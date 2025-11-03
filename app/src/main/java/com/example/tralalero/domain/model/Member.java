package com.example.tralalero.domain.model;

public class Member {
    private String id;
    private String userId;
    private String role;
    private User user;
    private String addedBy;
    private String createdAt;

    // Nested User class
    public static class User {
        private String id;
        private String name;
        private String email;
        private String avatarUrl;

        // Constructors
        public User() {}

        public User(String id, String name, String email, String avatarUrl) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.avatarUrl = avatarUrl;
        }

        // Getters & Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    }

    // Constructors
    public Member() {}

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getAddedBy() { return addedBy; }
    public void setAddedBy(String addedBy) { this.addedBy = addedBy; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    // Helper methods
    public boolean isOwner() {
        return "OWNER".equalsIgnoreCase(role);
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public boolean canManageMembers() {
        return isOwner() || isAdmin();
    }
}
