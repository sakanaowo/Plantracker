package com.example.tralalero.auth.remote.dto;

import com.google.gson.annotations.SerializedName;

public class UpdateProfileRequest {
    public String name;
    
    @SerializedName("avatar_url")
    public String avatarUrl;
    
    public String bio;
    
    @SerializedName("job_title")
    public String jobTitle;
    
    @SerializedName("phone_number")
    public String phoneNumber;
    
    public UpdateProfileRequest(String name, String avatarUrl) {
        this.name = name;
        this.avatarUrl = avatarUrl;
    }
    
    public UpdateProfileRequest(String name, String avatarUrl, String bio, String jobTitle, String phoneNumber) {
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.bio = bio;
        this.jobTitle = jobTitle;
        this.phoneNumber = phoneNumber;
    }
    
    public static UpdateProfileRequest withAvatar(String avatarUrl) {
        return new UpdateProfileRequest(null, avatarUrl);
    }
    
    public static UpdateProfileRequest withName(String name) {
        return new UpdateProfileRequest(name, null);
    }
}

