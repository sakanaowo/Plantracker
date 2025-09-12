package com.example.tralalero.auth.remote.dto;

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
}

