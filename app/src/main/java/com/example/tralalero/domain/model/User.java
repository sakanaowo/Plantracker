package com.example.tralalero.domain.model;

public class User {
    public final String id;
    public final String name;
    public String email;
    public String avatarUrl;
    public String firebaseUid;

    public User(String id, String name, String email, String avatarUrl, String firebaseUid) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.firebaseUid = firebaseUid;
    }

    public String getId() {
        return id;
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

    public String getFirebaseUid() {
        return firebaseUid;
    }

    public boolean hasAvatar() {
        return avatarUrl != null && !avatarUrl.isEmpty();
    }

    public String getDisplayName() {
        if (name != null && !name.isEmpty()) {
            return name;
        }
        if (email != null && email.contains("@")) {
            return email.split("@")[0];
        }
        return "Unknown User";
    }

    public String getInitials() {
        String displayName = getDisplayName();
        if (displayName.isEmpty()) return "?";

        String[] parts = displayName.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].charAt(0) + "" + parts[1].charAt(0)).toUpperCase();
        }
        return displayName.substring(0, Math.min(2, displayName.length())).toUpperCase();
    }

    @Override
    public String toString() {
        return "User{id='" + id + "', name='" + name + "', email='" + email + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
