package com.example.tralalero.data.remote.dto.auth;

import com.google.gson.annotations.SerializedName;

public class UserDto {
    public String id;
    public String name;
    public String email;

    public String passwordHash;
    public String avatarUrl;
    public String bio;
    public String jobTitle;
    public String phoneNumber;
    public String firebaseUid;
    public String createdAt;
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

    public String getBio() {
        return bio;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getPhoneNumber() {
        return phoneNumber;
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