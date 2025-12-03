package com.example.tralalero.data.remote.dto.member;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.Date;

public class MemberDTO implements Serializable {
    @SerializedName("id")
    private String id;

    @SerializedName("userId")
    private String userId;

    @SerializedName("role")
    private String role; // OWNER, ADMIN, MEMBER, VIEWER

    @SerializedName("users")  // Backend uses "users" (plural), not "user"
    private UserInfo user;

    @SerializedName("addedBy")
    private String addedBy;

    @SerializedName("createdAt")
    private String createdAt;

    // Nested class for user info
    public static class UserInfo implements Serializable {
        @SerializedName("id")
        private String id;

        @SerializedName("name")
        private String name;

        @SerializedName("email")
        private String email;

        @SerializedName("avatarUrl")
        private String avatarUrl;

        @SerializedName("bio")
        private String bio;

        @SerializedName("jobTitle")
        private String jobTitle;

        @SerializedName("phoneNumber")
        private String phoneNumber;

        // Getters & Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
        public String getBio() { return bio; }
        public void setBio(String bio) { this.bio = bio; }
        public String getJobTitle() { return jobTitle; }
        public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public UserInfo getUser() { return user; }
    public void setUser(UserInfo user) { this.user = user; }
    public String getAddedBy() { return addedBy; }
    public void setAddedBy(String addedBy) { this.addedBy = addedBy; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
