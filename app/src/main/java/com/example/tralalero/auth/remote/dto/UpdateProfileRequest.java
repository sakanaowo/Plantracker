package com.example.tralalero.auth.remote.dto;

import com.google.gson.annotations.SerializedName;

public class UpdateProfileRequest {
    public String name;
    
    @SerializedName("avatar_url")
    public String avatarUrl;
    
    public UpdateProfileRequest(String name, String avatarUrl) {
        this.name = name;
        this.avatarUrl = avatarUrl;
    }
    
    // Constructor for only avatar update
    public static UpdateProfileRequest withAvatar(String avatarUrl) {
        return new UpdateProfileRequest(null, avatarUrl);
    }
    
    // Constructor for only name update
    public static UpdateProfileRequest withName(String name) {
        return new UpdateProfileRequest(name, null);
    }
}

