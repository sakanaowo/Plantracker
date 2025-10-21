package com.example.tralalero.data.remote.dto.auth;

import com.google.gson.annotations.SerializedName;

public class UserDto {
    public String id;
    public String name;
    public String email;

    @SerializedName("password_hash")
    public String passwordHash;

    @SerializedName("avatar_url")
    public String avatarUrl;

    @SerializedName("firebase_uid")
    public String firebaseUid;

    @SerializedName("created_at")
    public String createdAt;

    @SerializedName("updated_at")
    public String updatedAt;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getFirebaseUid() {
        return firebaseUid;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}